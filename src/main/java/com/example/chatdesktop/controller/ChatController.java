package com.example.chatdesktop.controller;

import com.example.chatdesktop.model.ChatMessage;
import com.example.chatdesktop.service.GroqService;
import com.example.chatdesktop.service.TemaService;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    private TemaService temaService;


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
    // CONTROLE DA REGENERAÇÃO
    // ============================================================

    private Button botaoRegenerarResposta;

    private HBox ultimoContainerIA;


    // ============================================================
    // INICIALIZAR
    // ============================================================

    @FXML
    public void initialize() {

        groqService =
                new GroqService();

        temaService =
                new TemaService();

        temaEscuro =
                temaService.carregarTema();

        historico =
                new ArrayList<>();

        conversas =
                new ArrayList<>();


        iniciarNovaConversaInterna();


        adicionarMensagem(
                "Olá! 🌸\n\n" +
                        "Como posso ajudar você?",
                false,
                null,
                null
        );


        Platform.runLater(this::aplicarTema);


        configurarListaConversas();

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


        // ========================================================
        // ABRIR CONVERSA AO CLICAR
        // ========================================================

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


        // ========================================================
        // MENU DE CONTEXTO
        // ========================================================

        ContextMenu menuContexto =
                new ContextMenu();


        // ========================================================
        // OPÇÃO RENOMEAR
        // ========================================================

        MenuItem itemRenomear =
                new MenuItem(
                        "✏️ Renomear conversa"
                );


        // ========================================================
        // OPÇÃO EXCLUIR
        // ========================================================

        MenuItem itemExcluir =
                new MenuItem(
                        "🗑 Excluir conversa"
                );


        menuContexto
                .getItems()
                .addAll(
                        itemRenomear,
                        itemExcluir
                );


        // ========================================================
        // RENOMEAR CONVERSA
        // ========================================================

        itemRenomear.setOnAction(event -> {

            String tituloSelecionado =
                    listaConversas
                            .getSelectionModel()
                            .getSelectedItem();


            if (tituloSelecionado == null) {
                return;
            }


            renomearConversa(
                    tituloSelecionado
            );
        });


        // ========================================================
        // EXCLUIR CONVERSA
        // ========================================================

        itemExcluir.setOnAction(event -> {

            String tituloSelecionado =
                    listaConversas
                            .getSelectionModel()
                            .getSelectedItem();


            if (tituloSelecionado == null) {
                return;
            }


            excluirConversa(
                    tituloSelecionado
            );
        });


        // ========================================================
        // MOSTRAR MENU COM BOTÃO DIREITO
        // ========================================================

        listaConversas.setOnContextMenuRequested(event -> {

            String tituloSelecionado =
                    listaConversas
                            .getSelectionModel()
                            .getSelectedItem();


            if (tituloSelecionado == null) {
                return;
            }


            menuContexto.show(
                    listaConversas,
                    event.getScreenX(),
                    event.getScreenY()
            );
        });
    }


    // ============================================================
    // APLICAR TEMA NA DIALOG (VISUAL PREMIUM, SEM MOLDURA NATIVA)
    // ============================================================

    private void aplicarTemaDialog(
            DialogPane dialogPane,
            String emoji
    ) {

        String caminhoCss =
                temaEscuro
                        ? "/com/example/chatdesktop/css/chat-dark.css"
                        : "/com/example/chatdesktop/css/chat.css";


        URL css =
                getClass().getResource(
                        caminhoCss
                );


        if (css == null) {

            System.err.println(
                    "Erro: CSS da dialog não foi encontrado: " + caminhoCss
            );

            return;
        }


        dialogPane.getStylesheets().add(
                css.toExternalForm()
        );

        dialogPane.getStyleClass().add(
                "dialog-pane-custom"
        );


        // ========================================================
        // ÍCONE CUSTOMIZADO NO LUGAR DO "?" PADRÃO
        // ========================================================

        Label icone =
                new Label(emoji);

        icone.getStyleClass().add(
                "dialog-icone"
        );

        dialogPane.setGraphic(
                icone
        );


        // ========================================================
        // JANELA SEM MOLDURA NATIVA + SOMBRA
        // ========================================================

        Stage stage =
                (Stage) dialogPane
                        .getScene()
                        .getWindow();

        stage.initStyle(
                StageStyle.TRANSPARENT
        );

        dialogPane
                .getScene()
                .setFill(
                        Color.TRANSPARENT
                );

        dialogPane.setEffect(
                new DropShadow(
                        24,
                        Color.rgb(120, 70, 90, 0.35)
                )
        );
    }


    // ============================================================
    // RENOMEAR CONVERSA
    // ============================================================

    private void renomearConversa(
            String tituloAtual
    ) {

        Conversa conversaParaRenomear = null;


        // ========================================================
        // PROCURAR CONVERSA
        // ========================================================

        for (Conversa conversa : conversas) {

            if (conversa.getTitulo().equals(
                    tituloAtual
            )) {

                conversaParaRenomear =
                        conversa;

                break;
            }
        }


        if (conversaParaRenomear == null) {
            return;
        }


        // ========================================================
        // CAIXA DE DIÁLOGO
        // ========================================================

        TextInputDialog dialog =
                new TextInputDialog(
                        conversaParaRenomear.getTitulo()
                );


        dialog.setTitle(
                "Renomear conversa"
        );


        dialog.setHeaderText(
                "Digite o novo nome da conversa:"
        );


        dialog.setContentText(
                "Novo nome:"
        );


        aplicarTemaDialog(
                dialog.getDialogPane(),
                "✏️"
        );


        Optional<String> resultado =
                dialog.showAndWait();


        // ========================================================
        // VERIFICAR RESULTADO
        // ========================================================

        if (resultado.isEmpty()) {
            return;
        }


        String novoTitulo =
                resultado
                        .get()
                        .trim();


        // ========================================================
        // NÃO ACEITAR NOME VAZIO
        // ========================================================

        if (novoTitulo.isEmpty()) {
            return;
        }


        // ========================================================
        // LIMITAR TAMANHO
        // ========================================================

        if (novoTitulo.length() > 45) {

            novoTitulo =
                    novoTitulo
                            .substring(
                                    0,
                                    45
                            )
                            .trim();

            novoTitulo += "...";
        }


        // ========================================================
        // ALTERAR TÍTULO
        // ========================================================

        conversaParaRenomear.setTitulo(
                novoTitulo
        );


        // ========================================================
        // ATUALIZAR LISTA
        // ========================================================

        atualizarListaConversas();


        // ========================================================
        // SELECIONAR NOVAMENTE A CONVERSA
        // ========================================================

        int indice =
                conversas.indexOf(
                        conversaParaRenomear
                );


        if (indice >= 0 &&
                indice < listaConversas
                        .getItems()
                        .size()) {

            listaConversas
                    .getSelectionModel()
                    .select(indice);
        }
    }


    // ============================================================
    // EXCLUIR CONVERSA
    // ============================================================

    private void excluirConversa(
            String titulo
    ) {

        Conversa conversaParaExcluir = null;


        // ========================================================
        // PROCURAR CONVERSA
        // ========================================================

        for (Conversa conversa : conversas) {

            if (conversa.getTitulo().equals(
                    titulo
            )) {

                conversaParaExcluir =
                        conversa;

                break;
            }
        }


        if (conversaParaExcluir == null) {
            return;
        }


        // ========================================================
        // CONFIRMAÇÃO
        // ========================================================

        Alert alerta =
                new Alert(
                        Alert.AlertType.CONFIRMATION
                );


        alerta.setTitle(
                "Excluir conversa"
        );


        alerta.setHeaderText(
                "Excluir esta conversa?"
        );


        alerta.setContentText(
                "Tem certeza que deseja excluir a conversa \""
                        + conversaParaExcluir.getTitulo()
                        + "\"?\n\n"
                        + "Essa ação não pode ser desfeita."
        );


        ButtonType botaoExcluir =
                new ButtonType(
                        "Excluir"
                );


        ButtonType botaoCancelar =
                new ButtonType(
                        "Cancelar"
                );


        alerta.getButtonTypes().setAll(
                botaoExcluir,
                botaoCancelar
        );


        aplicarTemaDialog(
                alerta.getDialogPane(),
                "🗑️"
        );


        Optional<ButtonType> resultado =
                alerta.showAndWait();


        // ========================================================
        // CANCELAR
        // ========================================================

        if (resultado.isEmpty() ||
                resultado.get() != botaoExcluir) {

            return;
        }


        // ========================================================
        // VERIFICAR SE É A CONVERSA ATUAL
        // ========================================================

        boolean eraConversaAtual =
                conversaParaExcluir ==
                        conversaAtual;


        // ========================================================
        // REMOVER CONVERSA
        // ========================================================

        conversas.remove(
                conversaParaExcluir
        );


        // ========================================================
        // ATUALIZAR BARRA LATERAL
        // ========================================================

        atualizarListaConversas();


        // ========================================================
        // SE ERA A CONVERSA ABERTA
        // ========================================================

        if (eraConversaAtual) {

            messagesBox
                    .getChildren()
                    .clear();


            iniciarNovaConversaInterna();


            adicionarMensagem(
                    "Olá! 🌸\n\n" +
                            "Nova conversa iniciada.\n" +
                            "Como posso ajudar você?",
                    false,
                    null,
                    null
            );


            campoMensagem.clear();

            campoMensagem.setDisable(false);

            botaoEnviar.setDisable(false);

            botaoNovaConversa.setDisable(false);


            campoMensagem.requestFocus();


            listaConversas
                    .getSelectionModel()
                    .clearSelection();


            Platform.runLater(() ->
                    scrollChat.setVvalue(0)
            );
        }
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

            if (conversa.getTitulo().equals(
                    titulo
            )) {

                conversaAtual =
                        conversa;

                historico =
                        conversa.getHistorico();

                messagesBox
                        .getChildren()
                        .clear();

                ultimoContainerIA = null;

                botaoRegenerarResposta = null;


                boolean encontrouMensagem =
                        false;


                for (ChatMessage mensagem : historico) {

                    if ("user".equals(
                            mensagem.getRole()
                    )) {

                        adicionarMensagem(
                                mensagem.getContent(),
                                true,
                                null,
                                null
                        );

                        encontrouMensagem = true;

                    } else if ("assistant".equals(
                            mensagem.getRole()
                    )) {

                        adicionarMensagem(
                                mensagem.getContent(),
                                false,
                                null,
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
                            null,
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


        ultimoContainerIA = null;

        botaoRegenerarResposta = null;
    }


    // ============================================================
    // DEFINIR TÍTULO
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

        campoMensagem.setOnKeyPressed(event -> {

            if (event.getCode() == KeyCode.ENTER) {

                enviarMensagem();

                event.consume();
            }
        });


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

                                if (event.isControlDown()
                                        && event.getCode() == KeyCode.N) {

                                    novaConversa();

                                    event.consume();

                                    return;
                                }


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

        salvarConversaAtual();


        messagesBox
                .getChildren()
                .clear();


        iniciarNovaConversaInterna();


        adicionarMensagem(
                "Olá! 🌸\n\n" +
                        "Nova conversa iniciada.\n" +
                        "Como posso ajudar você?",
                false,
                null,
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
    // APLICAR TEMA
    // ============================================================

    private void aplicarTema() {

        Scene scene =
                botaoTema.getScene();

        if (scene == null) {
            return;
        }

        String caminhoCss =
                temaEscuro
                        ? "/com/example/chatdesktop/css/chat-dark.css"
                        : "/com/example/chatdesktop/css/chat.css";

        URL css =
                getClass().getResource(
                        caminhoCss
                );

        if (css == null) {

            System.err.println(
                    "Erro: CSS não encontrado: " + caminhoCss
            );

            return;
        }

        scene.getStylesheets().clear();

        scene.getStylesheets().add(
                css.toExternalForm()
        );

        botaoTema.setText(
                temaEscuro ? "☀" : "🌙"
        );
    }


    // ============================================================
    // ALTERNAR TEMA
    // ============================================================

    @FXML
    private void alternarTema() {

        temaEscuro =
                !temaEscuro;

        aplicarTema();

        temaService.salvarTema(
                temaEscuro
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


        campoMensagem.clear();


        adicionarMensagem(
                mensagem,
                true,
                null,
                null
        );


        historico.add(
                new ChatMessage(
                        "user",
                        mensagem
                )
        );


        bloquearInterface();


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
                    resultado.getOrigem(),
                    resultado.getFonte()
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
    // REGENERAR RESPOSTA
    // ============================================================

    private void regenerarResposta() {

        if (historico == null ||
                historico.size() < 2) {

            return;
        }


        ChatMessage ultimaMensagem =
                historico.get(
                        historico.size() - 1
                );


        if (!"assistant".equals(
                ultimaMensagem.getRole()
        )) {

            return;
        }


        historico.remove(
                historico.size() - 1
        );


        if (ultimoContainerIA != null) {

            messagesBox
                    .getChildren()
                    .remove(
                            ultimoContainerIA
                    );

            ultimoContainerIA = null;
        }


        botaoRegenerarResposta = null;


        if (conversaAtual != null) {

            conversaAtual.setHistorico(
                    historico
            );
        }


        bloquearInterface();


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
                    null,
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
            GroqService.OrigemResposta origemResposta,
            String fonte
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

        } else {

            // ====================================================
            // MENSAGEM DA IA
            // ====================================================

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
            // FONTE DO CONHECIMENTO
            // ============================================================

            if (fonte != null &&
                    !fonte.isBlank()) {

                Label labelFonte =
                        new Label(
                                "📄 Fonte: " + fonte
                        );


                labelFonte.getStyleClass()
                        .add("fonte-resposta");


                caixaResposta
                        .getChildren()
                        .add(labelFonte);
            }


            // ====================================================
            // BOTÃO COPIAR
            // ====================================================

            caixaResposta
                    .getChildren()
                    .add(botaoCopiar);


            // ====================================================
            // BOTÃO REGENERAR
            // ====================================================

            Button novoBotaoRegenerar =
                    new Button(
                            "🔄 Regenerar resposta"
                    );


            novoBotaoRegenerar
                    .getStyleClass()
                    .add(
                            "botao-regenerar"
                    );


            novoBotaoRegenerar.setOnAction(
                    event ->
                            regenerarResposta()
            );


            caixaResposta
                    .getChildren()
                    .add(
                            novoBotaoRegenerar
                    );


            botaoRegenerarResposta =
                    novoBotaoRegenerar;


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


            // ====================================================
            // GUARDAR ÚLTIMA RESPOSTA
            // ====================================================

            ultimoContainerIA =
                    container;
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

        if (botaoRegenerarResposta != null) {

            botaoRegenerarResposta.setDisable(true);
        }
    }


    // ============================================================
    // LIBERAR INTERFACE
    // ============================================================

    private void liberarInterface() {

        campoMensagem.setDisable(false);

        botaoEnviar.setDisable(false);

        if (botaoRegenerarResposta != null) {

            botaoRegenerarResposta.setDisable(false);
        }

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