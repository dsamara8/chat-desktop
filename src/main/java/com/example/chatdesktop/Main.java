package com.example.chatdesktop;

import com.example.chatdesktop.service.TemaService;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        URL fxml = Main.class.getResource(
                "/com/example/chatdesktop/view/login-view.fxml"
        );

        if (fxml == null) {
            throw new RuntimeException(
                    "ERRO: login-view.fxml não foi encontrado!"
            );
        }

        FXMLLoader loader = new FXMLLoader(fxml);

        Scene scene = new Scene(
                loader.load(),
                500,
                500
        );

        // Carrega o tema que foi salvo anteriormente
        TemaService temaService = new TemaService();

        boolean temaEscuro = temaService.carregarTema();

        URL css;

        if (temaEscuro) {
            css = Main.class.getResource(
                    "/com/example/chatdesktop/css/chat-dark.css"
            );
        } else {
            css = Main.class.getResource(
                    "/com/example/chatdesktop/css/chat.css"
            );
        }

        if (css != null) {
            scene.getStylesheets().add(
                    css.toExternalForm()
            );
        }

        stage.setTitle(
                "Login - Chat JavaFX + Groq"
        );

        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}