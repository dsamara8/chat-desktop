package com.example.chatdesktop.controller;

import com.example.chatdesktop.model.Usuario;
import com.example.chatdesktop.service.TemaService;
import com.example.chatdesktop.service.UsuarioService;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class CadastroController {

    @FXML
    private TextField campoNome;

    @FXML
    private TextField campoEmail;

    @FXML
    private PasswordField campoSenha;

    @FXML
    private PasswordField campoConfirmarSenha;


    private final UsuarioService usuarioService =
            new UsuarioService();


    private final TemaService temaService =
            new TemaService();


    // ============================================================
    // CADASTRAR
    // ============================================================

    @FXML
    private void cadastrar() {

        String nome =
                campoNome.getText().trim();

        String email =
                campoEmail.getText().trim();

        String senha =
                campoSenha.getText();

        String confirmarSenha =
                campoConfirmarSenha.getText();


        // ========================================================
        // VERIFICAR CAMPOS
        // ========================================================

        if (
                nome.isEmpty()
                        || email.isEmpty()
                        || senha.isEmpty()
                        || confirmarSenha.isEmpty()
        ) {

            mostrarAlerta(
                    "Atenção",
                    "Preencha todos os campos."
            );

            return;
        }


        // ========================================================
        // VERIFICAR SENHAS
        // ========================================================

        if (!senha.equals(confirmarSenha)) {

            mostrarAlerta(
                    "Atenção",
                    "As senhas não são iguais."
            );

            return;
        }


        // ========================================================
        // CRIAR USUÁRIO
        // ========================================================

        Usuario usuario =
                new Usuario(
                        nome,
                        email,
                        senha
                );


        boolean cadastrado =
                usuarioService.cadastrarUsuario(
                        usuario
                );


        // ========================================================
        // RESULTADO
        // ========================================================

        if (cadastrado) {

            mostrarAlerta(
                    "Sucesso",
                    "Conta criada com sucesso!"
            );

            voltarLogin();

        } else {

            mostrarAlerta(
                    "Erro",
                    "Não foi possível criar a conta.\n"
                            + "Verifique se o e-mail já está cadastrado."
            );
        }
    }


    // ============================================================
    // VOLTAR PARA LOGIN
    // ============================================================

    @FXML
    private void voltarLogin() {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/com/example/chatdesktop/view/login-view.fxml"
                            )
                    );


            Parent root =
                    loader.load();


            Scene scene =
                    new Scene(
                            root,
                            500,
                            500
                    );


            // Aplica o tema salvo
            aplicarTema(scene);


            Stage stage =
                    (Stage) campoNome
                            .getScene()
                            .getWindow();


            stage.setTitle(
                    "Login - Chat JavaFX + Groq"
            );


            stage.setScene(scene);

            stage.show();

        } catch (IOException e) {

            e.printStackTrace();

            mostrarAlerta(
                    "Erro",
                    "Não foi possível voltar para o login."
            );
        }
    }


    // ============================================================
    // APLICAR TEMA
    // ============================================================

    private void aplicarTema(
            Scene scene
    ) {

        boolean temaEscuro =
                temaService.carregarTema();


        URL css;


        if (temaEscuro) {

            css =
                    getClass().getResource(
                            "/com/example/chatdesktop/css/chat-dark.css"
                    );

        } else {

            css =
                    getClass().getResource(
                            "/com/example/chatdesktop/css/chat.css"
                    );
        }


        if (css != null) {

            scene.getStylesheets().clear();

            scene.getStylesheets().add(
                    css.toExternalForm()
            );
        }
    }


    // ============================================================
    // ALERTA
    // ============================================================

    private void mostrarAlerta(
            String titulo,
            String mensagem
    ) {

        Alert alerta =
                new Alert(
                        Alert.AlertType.INFORMATION
                );


        alerta.setTitle(titulo);

        alerta.setHeaderText(null);

        alerta.setContentText(mensagem);

        alerta.showAndWait();
    }
}