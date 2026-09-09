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

public class LoginController {

    @FXML
    private TextField campoEmail;

    @FXML
    private PasswordField campoSenha;

    private final UsuarioService usuarioService =
            new UsuarioService();

    private final TemaService temaService =
            new TemaService();


    // ============================================================
    // ENTRAR
    // ============================================================

    @FXML
    private void entrar() {

        String email =
                campoEmail.getText().trim();

        String senha =
                campoSenha.getText();


        if (email.isEmpty()
                || senha.isEmpty()) {

            mostrarAlerta(
                    "Atenção",
                    "Preencha o e-mail e a senha."
            );

            return;
        }


        Usuario usuario =
                usuarioService.fazerLogin(
                        email,
                        senha
                );


        if (usuario != null) {

            abrirChat(usuario);

        } else {

            mostrarAlerta(
                    "Login inválido",
                    "E-mail ou senha incorretos."
            );
        }
    }


    // ============================================================
    // ABRIR CADASTRO
    // ============================================================

    @FXML
    private void abrirCadastro() {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/com/example/chatdesktop/view/cadastro-view.fxml"
                            )
                    );


            Parent root =
                    loader.load();


            Scene scene =
                    new Scene(
                            root,
                            500,
                            550
                    );


            // Aplica o tema salvo
            aplicarTema(scene);


            Stage stage =
                    (Stage) campoEmail
                            .getScene()
                            .getWindow();


            stage.setTitle(
                    "Criar conta - Chat JavaFX + Groq"
            );


            stage.setScene(scene);

            stage.show();

        } catch (IOException e) {

            e.printStackTrace();

            mostrarAlerta(
                    "Erro",
                    "Não foi possível abrir a tela de cadastro."
            );
        }
    }


    // ============================================================
    // ABRIR CHAT
    // ============================================================

    private void abrirChat(
            Usuario usuario
    ) {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/com/example/chatdesktop/view/chat-view.fxml"
                            )
                    );


            Parent root =
                    loader.load();


            // Pega o controlador que acabou de ser criado
            ChatController controller =
                    loader.getController();


            // Entrega o usuário logado para o ChatController
            controller.setUsuario(usuario);


            Scene scene =
                    new Scene(
                            root,
                            700,
                            600
                    );


            // Aplica o tema salvo
            aplicarTema(scene);


            Stage stage =
                    (Stage) campoEmail
                            .getScene()
                            .getWindow();


            stage.setTitle(
                    "Chat JavaFX + Groq"
            );


            stage.setScene(scene);

            stage.show();

        } catch (IOException e) {

            e.printStackTrace();

            mostrarAlerta(
                    "Erro",
                    "Não foi possível abrir o chat."
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