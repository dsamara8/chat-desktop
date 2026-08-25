package com.example.chatdesktop.controller;

import com.example.chatdesktop.model.ChatMessage;
import com.example.chatdesktop.service.GroqService;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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

    @FXML
    private Button botaoTema;


    // ============================================================
    // SERVIÇO
    // ============================================================

    private GroqService groqService;


    // ============================================================
    // HISTÓRICO
    // ============================================================

    private List<ChatMessage> historico;


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

        iniciarHistorico();

        adicionarMensagem(
                "Olá! 🌸\n\n" +
                        "Como posso ajudar você?",
                false
        );

        // Tema inicial: claro
        botaoTema.setText("🌙");

        // ========================================================
        // ATALHOS DE TECLADO
        // ========================================================

        configurarAtalhos();

        campoMensagem.requestFocus();
    }


    // ============================================================
    // CONFIGURAR ATALHOS
    // ============================================================

    private void configurarAtalhos() {

        // ========================================================
        // ENTER → ENVIAR MENSAGEM
        // ========================================================

        campoMensagem.setOnKeyPressed(event -> {

            if (event.getCode() == KeyCode.ENTER) {

                enviarMensagem();

                event.consume();
            }
        });


        // ========================================================
        // CTRL + N → NOVA CONVERSA
        // CTRL + L → LIMPAR CAMPO
        // ========================================================

        campoMensagem.sceneProperty().addListener(
                (observable, cenaAnterior, novaCena) -> {

                    if (novaCena == null) {
                        return;
                    }

                    novaCena.addEventFilter(
                            KeyEvent.KEY_PRESSED,
                            event -> {

                                // =================================
                                // CTRL + N
                                // NOVA CONVERSA
                                // =================================

                                if (event.isControlDown()
                                        && event.getCode() == KeyCode.N) {

                                    novaConversa();

                                    event.consume();

                                    return;
                                }


                                // =================================
                                // CTRL + L
                                // LIMPAR CAMPO
                                // =================================

                                if (event.isControlDown()
                                        && event.getCode() == KeyCode.L) {

                                    campoMensagem.clear();

                                    campoMensagem.requestFocus();

                                    event.consume();
                                }
                            }
                    );
                }
        );
    }


    // ============================================================
    // HISTÓRICO INICIAL
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

        messagesBox.getChildren().clear();

        iniciarHistorico();

        adicionarMensagem(
                "Olá! 🌸\n\n" +
                        "Nova conversa iniciada.\n" +
                        "Como posso ajudar você?",
                false
        );

        campoMensagem.clear();

        campoMensagem.setDisable(false);

        botaoEnviar.setDisable(false);

        botaoNovaConversa.setDisable(false);

        campoMensagem.requestFocus();

        Platform.runLater(() ->
                scrollChat.setVvalue(0)
        );
    }


    // ============================================================
    // ALTERNAR TEMA
    // ============================================================

    @FXML
    private void alternarTema() {

        Scene scene = botaoTema.getScene();

        if (scene == null) {
            return;
        }

        if (!temaEscuro) {

            // ====================================================
            // MUDAR PARA TEMA ESCURO
            // ====================================================

            URL cssEscuro = getClass().getResource(
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

            // No tema escuro mostramos SOL
            botaoTema.setText("☀");


        } else {

            // ====================================================
            // VOLTAR PARA TEMA CLARO
            // ====================================================

            URL cssClaro = getClass().getResource(
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

            // No tema claro mostramos LUA
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
                .thenAccept(this::receberResposta)
                .exceptionally(this::tratarErro);
    }


    // ============================================================
    // RECEBER RESPOSTA DA IA
    // ============================================================

    private void receberResposta(String resposta) {

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

    private Void tratarErro(Throwable erro) {

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


            // ====================================================
            // COPIAR RESPOSTA
            // ====================================================

            botaoCopiar.setOnAction(event -> {

                Clipboard clipboard =
                        Clipboard.getSystemClipboard();

                ClipboardContent content =
                        new ClipboardContent();

                content.putString(mensagem);

                clipboard.setContent(content);

                botaoCopiar.setText("✓ Copiado!");


                PauseTransition pausa =
                        new PauseTransition(
                                Duration.seconds(1.5)
                        );

                pausa.setOnFinished(e ->
                        botaoCopiar.setText("📋 Copiar")
                );

                pausa.play();
            });


            // ====================================================
            // CAIXA DA RESPOSTA DA IA
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


