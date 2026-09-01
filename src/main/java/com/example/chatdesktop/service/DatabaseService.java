package com.example.chatdesktop.service;

import com.example.chatdesktop.model.ChatMessage;
import com.example.chatdesktop.model.Conversa;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.List;

public class DatabaseService {

    private static final String URL =
            "jdbc:sqlite:chat.db";


    // ============================================================
    // CONSTRUTOR
    // ============================================================

    public DatabaseService() {

        criarTabelas();
    }


    // ============================================================
    // CONECTAR AO BANCO
    // ============================================================

    public Connection conectar()
            throws SQLException {

        return DriverManager.getConnection(URL);
    }


    // ============================================================
    // CRIAR TABELAS
    // ============================================================

    private void criarTabelas() {

        String sqlConversas =
                """
                CREATE TABLE IF NOT EXISTS conversas (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    titulo TEXT NOT NULL
                );
                """;


        String sqlMensagens =
                """
                CREATE TABLE IF NOT EXISTS mensagens (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    conversa_id INTEGER NOT NULL,
                    role TEXT NOT NULL,
                    content TEXT NOT NULL,

                    FOREIGN KEY (conversa_id)
                    REFERENCES conversas(id)
                    ON DELETE CASCADE
                );
                """;


        try (
                Connection conexao = conectar();

                Statement statement =
                        conexao.createStatement()
        ) {

            statement.execute(
                    sqlConversas
            );

            statement.execute(
                    sqlMensagens
            );

            System.out.println(
                    "Banco de dados inicializado com sucesso!"
            );

        } catch (SQLException e) {

            System.err.println(
                    "Erro ao criar banco de dados: "
                            + e.getMessage()
            );
        }
    }


    // ============================================================
    // SALVAR CONVERSA
    // ============================================================

    public int salvarConversa(
            String titulo
    ) {

        String sql =
                "INSERT INTO conversas (titulo) VALUES (?)";


        try (
                Connection conexao = conectar();

                PreparedStatement statement =
                        conexao.prepareStatement(
                                sql,
                                Statement.RETURN_GENERATED_KEYS
                        )
        ) {

            statement.setString(
                    1,
                    titulo
            );


            statement.executeUpdate();


            try (
                    ResultSet resultado =
                            statement.getGeneratedKeys()
            ) {

                if (resultado.next()) {

                    return resultado.getInt(
                            1
                    );
                }
            }

        } catch (SQLException e) {

            System.err.println(
                    "Erro ao salvar conversa: "
                            + e.getMessage()
            );
        }


        return -1;
    }


    // ============================================================
    // SALVAR MENSAGEM
    // ============================================================

    public void salvarMensagem(
            int conversaId,
            ChatMessage mensagem
    ) {

        String sql =
                """
                INSERT INTO mensagens
                (conversa_id, role, content)
                VALUES (?, ?, ?)
                """;


        try (
                Connection conexao = conectar();

                PreparedStatement statement =
                        conexao.prepareStatement(
                                sql
                        )
        ) {

            statement.setInt(
                    1,
                    conversaId
            );

            statement.setString(
                    2,
                    mensagem.getRole()
            );

            statement.setString(
                    3,
                    mensagem.getContent()
            );

            statement.executeUpdate();

        } catch (SQLException e) {

            System.err.println(
                    "Erro ao salvar mensagem: "
                            + e.getMessage()
            );
        }
    }


    // ============================================================
    // CARREGAR TODAS AS CONVERSAS
    // ============================================================

    public List<Conversa> carregarConversas() {

        List<Conversa> conversas =
                new ArrayList<>();


        String sql =
                """
                SELECT id, titulo
                FROM conversas
                ORDER BY id DESC
                """;


        try (
                Connection conexao = conectar();

                PreparedStatement statement =
                        conexao.prepareStatement(
                                sql
                        );

                ResultSet resultado =
                        statement.executeQuery()
        ) {

            while (resultado.next()) {

                int id =
                        resultado.getInt(
                                "id"
                        );


                String titulo =
                        resultado.getString(
                                "titulo"
                        );


                List<ChatMessage> mensagens =
                        carregarMensagens(
                                id
                        );


                Conversa conversa =
                        new Conversa(
                                id,
                                titulo,
                                mensagens
                        );


                conversas.add(
                        conversa
                );
            }

        } catch (SQLException e) {

            System.err.println(
                    "Erro ao carregar conversas: "
                            + e.getMessage()
            );
        }


        return conversas;
    }


    // ============================================================
    // CARREGAR MENSAGENS DE UMA CONVERSA
    // ============================================================

    public List<ChatMessage> carregarMensagens(
            int conversaId
    ) {

        List<ChatMessage> mensagens =
                new ArrayList<>();


        String sql =
                """
                SELECT role, content
                FROM mensagens
                WHERE conversa_id = ?
                ORDER BY id ASC
                """;


        try (
                Connection conexao = conectar();

                PreparedStatement statement =
                        conexao.prepareStatement(
                                sql
                        )
        ) {

            statement.setInt(
                    1,
                    conversaId
            );


            try (
                    ResultSet resultado =
                            statement.executeQuery()
            ) {

                while (resultado.next()) {

                    String role =
                            resultado.getString(
                                    "role"
                            );


                    String content =
                            resultado.getString(
                                    "content"
                            );


                    mensagens.add(
                            new ChatMessage(
                                    role,
                                    content
                            )
                    );
                }
            }

        } catch (SQLException e) {

            System.err.println(
                    "Erro ao carregar mensagens: "
                            + e.getMessage()
            );
        }


        return mensagens;
    }
    // ============================================================
// ATUALIZAR TÍTULO DA CONVERSA
// ============================================================

    public void atualizarTituloConversa(
            int conversaId,
            String novoTitulo
    ) {

        String sql =
                "UPDATE conversas SET titulo = ? WHERE id = ?";


        try (
                Connection conexao = conectar();

                PreparedStatement statement =
                        conexao.prepareStatement(sql)
        ) {

            statement.setString(
                    1,
                    novoTitulo
            );

            statement.setInt(
                    2,
                    conversaId
            );

            statement.executeUpdate();

        } catch (SQLException e) {

            System.err.println(
                    "Erro ao atualizar título da conversa: "
                            + e.getMessage()
            );
        }
    }
    // ============================================================
// EXCLUIR CONVERSA
// ============================================================

    public void excluirConversa(
            int conversaId
    ) {

        String sql =
                "DELETE FROM conversas WHERE id = ?";


        try (
                Connection conexao = conectar();

                PreparedStatement statement =
                        conexao.prepareStatement(sql)
        ) {

            statement.setInt(
                    1,
                    conversaId
            );

            statement.executeUpdate();

        } catch (SQLException e) {

            System.err.println(
                    "Erro ao excluir conversa: "
                            + e.getMessage()
            );
        }
    }
    // ============================================================
// EXCLUIR ÚLTIMA RESPOSTA DA IA
// ============================================================

    public void excluirUltimaRespostaIA(
            int conversaId
    ) {

        String sql =
                """
                DELETE FROM mensagens
                WHERE id = (
                    SELECT id
                    FROM mensagens
                    WHERE conversa_id = ?
                    AND role = 'assistant'
                    ORDER BY id DESC
                    LIMIT 1
                )
                """;


        try (
                Connection conexao = conectar();

                PreparedStatement statement =
                        conexao.prepareStatement(sql)
        ) {

            statement.setInt(
                    1,
                    conversaId
            );


            statement.executeUpdate();

        } catch (SQLException e) {

            System.err.println(
                    "Erro ao excluir resposta da IA: "
                            + e.getMessage()
            );
        }
    }
}