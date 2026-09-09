package com.example.chatdesktop.service;

import com.example.chatdesktop.model.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UsuarioService {

    private final DatabaseService databaseService;

    public UsuarioService() {
        databaseService = new DatabaseService();
    }


    // ============================================================
    // CADASTRAR USUÁRIO
    // ============================================================

    public boolean cadastrarUsuario(Usuario usuario) {

        String sql =
                """
                INSERT INTO usuarios (nome, email, senha)
                VALUES (?, ?, ?)
                """;

        try (
                Connection conexao =
                        databaseService.conectar();

                PreparedStatement statement =
                        conexao.prepareStatement(sql)
        ) {

            statement.setString(
                    1,
                    usuario.getNome()
            );

            statement.setString(
                    2,
                    usuario.getEmail()
            );

            statement.setString(
                    3,
                    usuario.getSenha()
            );

            statement.executeUpdate();

            return true;

        } catch (SQLException e) {

            System.err.println(
                    "Erro ao cadastrar usuário: "
                            + e.getMessage()
            );

            return false;
        }
    }


    // ============================================================
    // VERIFICAR LOGIN
    // ============================================================

    public Usuario fazerLogin(
            String email,
            String senha
    ) {

        String sql =
                """
                SELECT id, nome, email, senha
                FROM usuarios
                WHERE email = ?
                AND senha = ?
                """;

        try (
                Connection conexao =
                        databaseService.conectar();

                PreparedStatement statement =
                        conexao.prepareStatement(sql)
        ) {

            statement.setString(
                    1,
                    email
            );

            statement.setString(
                    2,
                    senha
            );


            try (
                    ResultSet resultado =
                            statement.executeQuery()
            ) {

                if (resultado.next()) {

                    return new Usuario(
                            resultado.getInt("id"),
                            resultado.getString("nome"),
                            resultado.getString("email"),
                            resultado.getString("senha")
                    );
                }
            }

        } catch (SQLException e) {

            System.err.println(
                    "Erro ao fazer login: "
                            + e.getMessage()
            );
        }

        return null;
    }
}