package com.example.chatdesktop.controller;

import com.example.chatdesktop.model.ChatMessage;
import com.example.chatdesktop.service.GroqService;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ChatController {

    // ============================================================
    // COMPONENTES DO FXML
    // ============================================================

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


    // ============================================================
    // HISTÓRICO DA CONVERSA ATUAL
    // ============================================================

    private List<ChatMessage> historico;


    // ============================================================
    // LISTA DE CONVERSAS
    // ============================================================

    private List<String> conversas;


    // ============================================================
    // TÍTULO DA CONVERSA ATUAL
    // ============================================================

    private String tituloConversaAtual = null;


    // ============================================================
    // CONTROLE DO TEMA
    // ============================================================

    private boolean temaEscuro = false;


    // ============================================================
    // INICIALIZAR
    // ============================================================

    @FXML
    public void initialize() {

        groqService = new GroqService();

        historico = new ArrayList<>();

        conversas = new ArrayList<>();

        iniciarHistorico();

        adicionarMensagem(
                "Olá! 🌸\n\n" +
                        "Como posso ajudar você?",
                false
        );

        campoMensagem.requestFocus();
    }


    // ============================================================
    // HISTÓRICO INICIAL DA IA
    // ============================================================

    private void iniciarHistorico() {

        historico.clear();

        historico.add(
                new ChatMessage(
                        "system",
                        "Você é um assistente útil, educado e objetivo. " +
                                "Responda sempre em português do Brasil."
                )
        );
    }


    // ============================================================
    // NOVA CONVERSA
    // ============================================================

    @FXML
    private void novaConversa() {

        // ========================================================
        // LIMPAR CONVERSA ATUAL
        // ========================================================

        messagesBox
                .getChildren()
                .clear();

        iniciarHistorico();


        // ========================================================
        // RESETAR TÍTULO
        // ========================================================

        tituloConversaAtual = null;


        // ========================================================
        // MENSAGEM INICIAL
        // ========================================================

        adicionarMensagem(
                "Olá! 🌸\n\n" +
                        "Nova conversa iniciada.\n" +
                        "Como posso ajudar você?",
                false
        );


        // ========================================================
        // LIMPAR CAMPO
        // ========================================================

        campoMensagem.clear();

        campoMensagem.setDisable(false);

        botaoEnviar.setDisable(false);

        botaoNovaConversa.setDisable(false);

        campoMensagem.requestFocus();


        // ========================================================
        // DESMARCAR HISTÓRICO
        // ========================================================

        listaConversas
                .getSelectionModel()
                .clearSelection();


        Platform.runLater(() ->
                scrollChat.setVvalue(0)
        );
    }


    // ============================================================
    // ALTERNAR TEMA
    // ============================================================

    @FXML
    private void alternarTema() {

        Scene scene =
                botaoTema.getScene();

        if (scene == null) {
            return;
        }


        // ========================================================
        // TEMA ESCURO
        // ========================================================

        if (!temaEscuro) {

            URL cssEscuro =
                    getClass().getResource(
                            "/com/example/chatdesktop/css/chat-dark.css"
                    );

            if (cssEscuro == null) {

                System.err.println(
                        "Erro: chat-dark.css não foi encontrado."
                );

                return;
            }

            scene.getStylesheets().clear();

            scene.getStylesheets().add(
                    cssEscuro.toExternalForm()
            );

            temaEscuro = true;

            botaoTema.setText("☀");


        } else {

            // ====================================================
            // TEMA CLARO
            // ====================================================

            URL cssClaro =
                    getClass().getResource(
                            "/com/example/chatdesktop/css/chat.css"
                    );

            if (cssClaro == null) {

                System.err.println(
                        "Erro: chat.css não foi encontrado."
                );

                return;
            }

            scene.getStylesheets().clear();

            scene.getStylesheets().add(
                    cssClaro.toExternalForm()
            );

            temaEscuro = false;

            botaoTema.setText("🌙");
        }
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


        // ========================================================
        // GERAR TÍTULO AUTOMATICAMENTE
        // ========================================================

        if (tituloConversaAtual == null) {

            tituloConversaAtual =
                    gerarTituloConversa(
                            mensagem
                    );

            adicionarConversaAoHistorico(
                    tituloConversaAtual
            );
        }


        // ========================================================
        // LIMPAR CAMPO
        // ========================================================

        campoMensagem.clear();


        // ========================================================
        // MOSTRAR MENSAGEM DO USUÁRIO
        // ========================================================

        adicionarMensagem(
                mensagem,
                true
        );


        // ========================================================
        // ADICIONAR AO HISTÓRICO DA IA
        // ========================================================

        historico.add(
                new ChatMessage(
                        "user",
                        mensagem
                )
        );


        // ========================================================
        // BLOQUEAR INTERFACE
        // ========================================================

        bloquearInterface();


        // ========================================================
        // ENVIAR PARA GROQ
        // ========================================================

        groqService
                .enviarMensagem(historico)
                .thenAccept(this::receberResposta)
                .exceptionally(this::tratarErro);
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


            String mensagemErro =
                    causa.getMessage();


            if (mensagemErro == null ||
                    mensagemErro.isBlank()) {

                mensagemErro =
                        "Ocorreu um erro de comunicação com a IA.";
            }


            adicionarMensagem(
                    "Não foi possível obter uma resposta.\n\n"
                            + mensagemErro,
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

        // ========================================================
        // MENSAGEM DO USUÁRIO
        // ========================================================

        if (usuario) {

            Label label =
                    new Label(mensagem);

            label.setWrapText(true);

            label.setMaxWidth(500);

            label.getStyleClass()
                    .add("mensagem-usuario");


            HBox container =
                    new HBox(label);

            container.getStyleClass()
                    .add("container-mensagem");

            container.getStyleClass()
                    .add("container-usuario");

            container.setAlignment(
                    Pos.CENTER_RIGHT
            );


            messagesBox
                    .getChildren()
                    .add(container);
        }


        // ========================================================
        // MENSAGEM DA IA
        // ========================================================

        else {

            Label label =
                    new Label(mensagem);

            label.setWrapText(true);

            label.setMaxWidth(500);

            label.getStyleClass()
                    .add("mensagem-ia");


            // ====================================================
            // BOTÃO COPIAR
            // ====================================================

            Button botaoCopiar =
                    new Button("📋 Copiar");

            botaoCopiar.getStyleClass()
                    .add("botao-copiar");


            botaoCopiar.setOnAction(event -> {

                Clipboard clipboard =
                        Clipboard.getSystemClipboard();

                ClipboardContent content =
                        new ClipboardContent();

                content.putString(mensagem);

                clipboard.setContent(content);

                botaoCopiar.setText(
                        "✓ Copiado!"
                );


                PauseTransition pausa =
                        new PauseTransition(
                                Duration.seconds(1.5)
                        );


                pausa.setOnFinished(e ->
                        botaoCopiar.setText(
                                "📋 Copiar"
                        )
                );


                pausa.play();
            });


            // ====================================================
            // CAIXA DA RESPOSTA
            // ====================================================

            VBox caixaResposta =
                    new VBox(8);

            caixaResposta
                    .getStyleClass()
                    .add("caixa-resposta-ia");


            caixaResposta
                    .getChildren()
                    .addAll(
                            label,
                            botaoCopiar
                    );


            caixaResposta.setMaxWidth(520);


            // ====================================================
            // CONTAINER
            // ====================================================

            HBox container =
                    new HBox(caixaResposta);

            container.getStyleClass()
                    .add("container-mensagem");

            container.getStyleClass()
                    .add("container-ia");

            container.setAlignment(
                    Pos.CENTER_LEFT
            );


            messagesBox
                    .getChildren()
                    .add(container);
        }


        // ========================================================
        // ROLAR PARA BAIXO
        // ========================================================

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