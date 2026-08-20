package com.example.chatdesktop.controller;

import com.example.chatdesktop.model.ChatMessage;
import com.example.chatdesktop.service.GroqService;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

public class ChatController {

    @FXML
    private VBox messagesBox;

    @FXML
    private ScrollPane scrollChat;

    @FXML
    private TextField campoMensagem;

    @FXML
    private Button botaoEnviar;

    @FXML
    private Button botaoNovaConversa;

    private GroqService groqService;

    private List<ChatMessage> historico;


    @FXML
    public void initialize() {

        groqService = new GroqService();

        historico = new ArrayList<>();

        iniciarHistorico();

        adicionarMensagem(
                "Olá! 🌸\n\n"
                        + "Como posso ajudar você?",
                false
        );

        campoMensagem.requestFocus();
    }


    // ============================================================
    // HISTÓRICO INICIAL
    // ============================================================

    private void iniciarHistorico() {

        historico.clear();

        historico.add(
                new ChatMessage(
                        "system",
                        "Você é um assistente útil, educado e objetivo. "
                                + "Responda sempre em português do Brasil."
                )
        );
    }


    // ============================================================
    // NOVA CONVERSA
    // ============================================================

    @FXML
    private void novaConversa() {

        // Limpa as mensagens da tela
        messagesBox.getChildren().clear();

        // Limpa o histórico e coloca novamente
        // a instrução inicial da IA
        iniciarHistorico();

        // Mensagem inicial
        adicionarMensagem(
                "Olá! 🌸\n\n"
                        + "Nova conversa iniciada.\n"
                        + "Como posso ajudar você?",
                false
        );

        // Limpa o campo de mensagem
        campoMensagem.clear();

        // Garante que os controles estejam liberados
        campoMensagem.setDisable(false);
        botaoEnviar.setDisable(false);
        botaoNovaConversa.setDisable(false);

        campoMensagem.requestFocus();

        // Volta a conversa para o topo
        Platform.runLater(() ->
                scrollChat.setVvalue(0)
        );
    }


    // ============================================================
    // ENVIAR MENSAGEM
    // ============================================================

    @FXML
    private void enviarMensagem() {

        String mensagem =
                campoMensagem
                        .getText()
                        .trim();

        if (mensagem.isEmpty()) {
            return;
        }

        campoMensagem.clear();

        adicionarMensagem(
                mensagem,
                true
        );

        historico.add(
                new ChatMessage(
                        "user",
                        mensagem
                )
        );

        bloquearInterface();

        groqService
                .enviarMensagem(historico)
                .thenAccept(
                        this::receberResposta
                )
                .exceptionally(
                        this::tratarErro
                );
    }


    // ============================================================
    // RECEBER RESPOSTA
    // ============================================================

    private void receberResposta(
            String resposta
    ) {

        Platform.runLater(() -> {

            adicionarMensagem(
                    resposta,
                    false
            );

            historico.add(
                    new ChatMessage(
                            "assistant",
                            resposta
                    )
            );

            liberarInterface();
        });
    }


    // ============================================================
    // TRATAR ERRO
    // ============================================================

    private Void tratarErro(
            Throwable erro
    ) {

        Platform.runLater(() -> {

            Throwable causa =
                    erro.getCause() != null
                            ? erro.getCause()
                            : erro;

            adicionarMensagem(
                    "Não foi possível obter uma resposta.\n\n"
                            + causa.getMessage(),
                    false
            );

            liberarInterface();
        });

        return null;
    }


    // ============================================================
    // ADICIONAR MENSAGEM
    // ============================================================

    private void adicionarMensagem(
            String mensagem,
            boolean usuario
    ) {

        Label label =
                new Label(mensagem);

        label.setWrapText(true);

        label.setMaxWidth(500);

        HBox container =
                new HBox(label);


        if (usuario) {

            label.getStyleClass()
                    .add("mensagem-usuario");

            container.setAlignment(
                    Pos.CENTER_RIGHT
            );

        } else {

            label.getStyleClass()
                    .add("mensagem-ia");

            container.setAlignment(
                    Pos.CENTER_LEFT
            );
        }


        messagesBox
                .getChildren()
                .add(container);


        Platform.runLater(() ->
                scrollChat.setVvalue(1.0)
        );
    }


    // ============================================================
    // BLOQUEAR INTERFACE
    // ============================================================

    private void bloquearInterface() {

        campoMensagem.setDisable(true);

        botaoEnviar.setDisable(true);
    }


    // ============================================================
    // LIBERAR INTERFACE
    // ============================================================

    private void liberarInterface() {

        campoMensagem.setDisable(false);

        botaoEnviar.setDisable(false);

        campoMensagem.requestFocus();
    }
}