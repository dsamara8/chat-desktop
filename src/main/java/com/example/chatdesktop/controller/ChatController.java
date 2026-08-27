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

    @FXML
    private Button botaoTema;

    @FXML
    private ListView<String> listaConversas;


    // ============================================================
    // SERVIÇO
    // ============================================================

    private GroqService groqService;


    // ============================================================
    // HISTÓRICO DA CONVERSA ATUAL
    // ============================================================

    private List<ChatMessage> historico;


    // ============================================================
    // CONVERSAS
    // ============================================================

    private List<Conversa> conversas;

    private Conversa conversaAtual;


    // ============================================================
    // CONTROLE DO TEMA
    // ============================================================

    private boolean temaEscuro = false;


    // ============================================================
    // INICIALIZAR
    // ============================================================

    @FXML
    public void initialize() {

        groqService =
                new GroqService();

        historico =
                new ArrayList<>();

        conversas =
                new ArrayList<>();


        iniciarNovaConversaInterna();


        adicionarMensagem(
                "Olá! 🌸\n\n" +
                        "Como posso ajudar você?",
                false,
                null
        );


        // ========================================================
        // TEMA INICIAL
        // ========================================================

        botaoTema.setText("🌙");


        // ========================================================
        // CONFIGURAR LISTA DE CONVERSAS
        // ========================================================

        configurarListaConversas();


        // ========================================================
        // ATALHOS
        // ========================================================

        configurarAtalhos();

        campoMensagem.requestFocus();
    }


    // ============================================================
    // CONFIGURAR LISTA DE CONVERSAS
    // ============================================================

    private void configurarListaConversas() {

        listaConversas.setItems(
                FXCollections.observableArrayList()
        );


        listaConversas
                .getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (observable,
                         conversaAnterior,
                         conversaSelecionada) -> {

                            if (conversaSelecionada == null) {
                                return;
                            }

                            abrirConversa(
                                    conversaSelecionada
                            );
                        }
                );
    }


    // ============================================================
    // ATUALIZAR LISTA DE CONVERSAS
    // ============================================================

    private void atualizarListaConversas() {

        List<String> titulos =
                new ArrayList<>();


        for (Conversa conversa : conversas) {

            titulos.add(
                    conversa.getTitulo()
            );
        }


        listaConversas
                .getItems()
                .setAll(titulos);
    }


    // ============================================================
    // ABRIR CONVERSA
    // ============================================================

    private void abrirConversa(
            String titulo
    ) {

        for (Conversa conversa : conversas) {

            if (conversa.getTitulo().equals(titulo)) {

                conversaAtual =
                        conversa;

                historico =
                        conversa.getHistorico();

                messagesBox
                        .getChildren()
                        .clear();


                boolean encontrouMensagem =
                        false;


                for (ChatMessage mensagem : historico) {

                    if ("user".equals(
                            mensagem.getRole()
                    )) {

                        adicionarMensagem(
                                mensagem.getContent(),
                                true,
                                null
                        );

                        encontrouMensagem = true;

                    } else if ("assistant".equals(
                            mensagem.getRole()
                    )) {

                        adicionarMensagem(
                                mensagem.getContent(),
                                false,
                                null
                        );

                        encontrouMensagem = true;
                    }
                }


                if (!encontrouMensagem) {

                    adicionarMensagem(
                            "Olá! 🌸\n\n" +
                                    "Como posso ajudar você?",
                            false,
                            null
                    );
                }


                Platform.runLater(() ->
                        scrollChat.setVvalue(1.0)
                );

                break;
            }
        }
    }


    // ============================================================
    // CRIAR NOVA CONVERSA INTERNA
    // ============================================================

    private void iniciarNovaConversaInterna() {

        historico =
                new ArrayList<>();


        historico.add(
                new ChatMessage(
                        "system",
                        "Você é um assistente útil, educado e objetivo. " +
                                "Responda sempre em português do Brasil."
                )
        );


        conversaAtual =
                new Conversa(
                        "Nova conversa",
                        historico
                );
    }


    // ============================================================
    // DEFINIR TÍTULO DA CONVERSA
    // ============================================================

    private void definirTituloDaConversa(
            String primeiraPergunta
    ) {

        if (conversaAtual == null) {
            return;
        }


        String titulo =
                primeiraPergunta
                        .trim();


        // ========================================================
        // LIMITAR TAMANHO DO TÍTULO
        // ========================================================

        if (titulo.length() > 45) {

            titulo =
                    titulo.substring(
                            0,
                            45
                    ).trim();

            titulo += "...";
        }


        conversaAtual.setTitulo(
                titulo
        );


        atualizarListaConversas();
    }


    // ============================================================
    // SALVAR CONVERSA ATUAL
    // ============================================================

    private void salvarConversaAtual() {

        if (conversaAtual == null) {
            return;
        }


        if (historico.size() <= 1) {
            return;
        }


        if (!conversas.contains(
                conversaAtual
        )) {

            conversas.add(
                    conversaAtual
            );
        }


        atualizarListaConversas();
    }


    // ============================================================
    // CONFIGURAR ATALHOS
    // ============================================================

    private void configurarAtalhos() {

        // ========================================================
        // ENTER → ENVIAR
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
                (observable,
                 cenaAnterior,
                 novaCena) -> {

                    if (novaCena == null) {
                        return;
                    }


                    novaCena.addEventFilter(
                            KeyEvent.KEY_PRESSED,
                            event -> {

                                // =================================
                                // CTRL + N
                                // =================================

                                if (event.isControlDown()
                                        && event.getCode() == KeyCode.N) {

                                    novaConversa();

                                    event.consume();

                                    return;
                                }


                                // =================================
                                // CTRL + L
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
    // NOVA CONVERSA
    // ============================================================

    @FXML
    private void novaConversa() {

        // ========================================================
        // SALVAR CONVERSA ANTERIOR
        // ========================================================

        salvarConversaAtual();


        // ========================================================
        // LIMPAR INTERFACE
        // ========================================================

        messagesBox
                .getChildren()
                .clear();


        // ========================================================
        // CRIAR NOVA CONVERSA
        // ========================================================

        iniciarNovaConversaInterna();


        adicionarMensagem(
                "Olá! 🌸\n\n" +
                        "Nova conversa iniciada.\n" +
                        "Como posso ajudar você?",
                false,
                null
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

        Scene scene =
                botaoTema.getScene();

        if (scene == null) {
            return;
        }


        if (!temaEscuro) {

            // ====================================================
            // TEMA ESCURO
            // ====================================================

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
        // PRIMEIRA PERGUNTA → DEFINIR TÍTULO
        // ========================================================

        boolean primeiraPergunta =
                historico.size() == 1;


        if (primeiraPergunta) {

            definirTituloDaConversa(
                    mensagem
            );


            if (!conversas.contains(
                    conversaAtual
            )) {

                conversas.add(
                        conversaAtual
                );
            }

            atualizarListaConversas();
        }


        // ========================================================
        // LIMPAR CAMPO
        // ========================================================

        campoMensagem.clear();


        // ========================================================
        // MOSTRAR MENSAGEM
        // ========================================================

        adicionarMensagem(
                mensagem,
                true,
                null
        );


        // ========================================================
        // ADICIONAR AO HISTÓRICO
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
                .enviarMensagemComOrigem(
                        historico
                )
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
            GroqService.ResultadoResposta resultado
    ) {

        Platform.runLater(() -> {

            adicionarMensagem(
                    resultado.getResposta(),
                    false,
                    resultado.getOrigem()
            );


            historico.add(
                    new ChatMessage(
                            "assistant",
                            resultado.getResposta()
                    )
            );


            if (conversaAtual != null) {

                conversaAtual.setHistorico(
                        historico
                );
            }


            atualizarListaConversas();


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
                    false,
                    null
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
            boolean usuario,
            GroqService.OrigemResposta origemResposta
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


                content.putString(
                        mensagem
                );


                clipboard.setContent(
                        content
                );


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
                    .add(label);


            // ====================================================
            // INDICADOR DE ORIGEM
            // ====================================================

            if (origemResposta != null) {

                Label origem =
                        new Label(
                                obterTextoOrigem(
                                        origemResposta
                                )
                        );


                origem.getStyleClass()
                        .add("origem-resposta");


                caixaResposta
                        .getChildren()
                        .add(origem);
            }


            // ====================================================
            // BOTÃO COPIAR
            // ====================================================

            caixaResposta
                    .getChildren()
                    .add(botaoCopiar);


            caixaResposta.setMaxWidth(520);


            // ====================================================
            // CONTAINER
            // ====================================================

            HBox container =
                    new HBox(
                            caixaResposta
                    );


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
    // OBTER TEXTO DA ORIGEM
    // ============================================================

    private String obterTextoOrigem(
            GroqService.OrigemResposta origem
    ) {

        return switch (origem) {

            case RAG ->
                    "📚 Origem: RAG";

            case INTERNET ->
                    "🌐 Origem: Internet";

            case FALLBACK_LOCAL ->
                    "💻 Origem: Fallback local";
        };
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


    // ============================================================
    // CLASSE CONVERSA
    // ============================================================

    private static class Conversa {

        private String titulo;

        private List<ChatMessage> historico;


        public Conversa(
                String titulo,
                List<ChatMessage> historico
        ) {

            this.titulo =
                    titulo;

            this.historico =
                    historico;
        }


        public String getTitulo() {

            return titulo;
        }


        public void setTitulo(
                String titulo
        ) {

            this.titulo =
                    titulo;
        }


        public List<ChatMessage> getHistorico() {

            return historico;
        }


        public void setHistorico(
                List<ChatMessage> historico
        ) {

            this.historico =
                    historico;
        }
    }
}