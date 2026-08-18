package com.example.chatdesktop;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main extends Application {

        private static final String GROQ_URL =
                "https://api.groq.com/openai/v1/chat/completions";

        private static final String GROQ_API_KEY =
                "GROQ_API_KEY";

        private static final String MODEL =
                "openai/gpt-oss-20b";


    // ============================================================
    // COMPONENTES
    // ============================================================

    private VBox messagesBox;
    private TextField messageInput;
    private Button sendButton;
    private Label statusLabel;
    private ScrollPane scrollPane;


    // ============================================================
    // HTTP CLIENT
    // ============================================================

    private final HttpClient httpClient =
            HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(20))
                    .build();


    // ============================================================
    // JAVAFX
    // ============================================================

    @Override
    public void start(Stage stage) {

        // --------------------------------------------------------
        // TÍTULO
        // --------------------------------------------------------

        Label title = new Label("Groq Chat");

        title.setStyle("""
                -fx-font-size: 24px;
                -fx-font-weight: bold;
                -fx-text-fill: white;
                """);


        Label subtitle = new Label(
                "Java 20 + JavaFX + Groq"
        );

        subtitle.setStyle("""
                -fx-font-size: 13px;
                -fx-text-fill: #9ca3af;
                """);


        VBox titleBox = new VBox(
                3,
                title,
                subtitle
        );


        // --------------------------------------------------------
        // HEADER
        // --------------------------------------------------------

        HBox header = new HBox(
                titleBox
        );

        header.setAlignment(
                Pos.CENTER_LEFT
        );

        header.setPadding(
                new Insets(15, 20, 15, 20)
        );

        header.setStyle("""
                -fx-background-color: #111827;
                """);


        // --------------------------------------------------------
        // MENSAGENS
        // --------------------------------------------------------

        messagesBox = new VBox(12);

        messagesBox.setPadding(
                new Insets(20)
        );

        messagesBox.setStyle("""
                -fx-background-color: #0f172a;
                """);


        scrollPane = new ScrollPane(
                messagesBox
        );

        scrollPane.setFitToWidth(true);

        scrollPane.setHbarPolicy(
                ScrollPane.ScrollBarPolicy.NEVER
        );

        scrollPane.setVbarPolicy(
                ScrollPane.ScrollBarPolicy.AS_NEEDED
        );

        scrollPane.setStyle("""
                -fx-background-color: #0f172a;
                -fx-border-color: transparent;
                """);


        // --------------------------------------------------------
        // INPUT
        // --------------------------------------------------------

        messageInput = new TextField();

        messageInput.setPromptText(
                "Digite sua mensagem..."
        );

        messageInput.setStyle("""
                -fx-background-color: #1e293b;
                -fx-text-fill: white;
                -fx-prompt-text-fill: #64748b;
                -fx-background-radius: 10px;
                -fx-border-radius: 10px;
                -fx-border-color: #334155;
                -fx-padding: 12px;
                -fx-font-size: 14px;
                """);


        // --------------------------------------------------------
        // BOTÃO
        // --------------------------------------------------------

        sendButton = new Button(
                "Enviar"
        );

        sendButton.setStyle("""
                -fx-background-color: #6366f1;
                -fx-text-fill: white;
                -fx-font-weight: bold;
                -fx-background-radius: 10px;
                -fx-padding: 12px 20px;
                -fx-cursor: hand;
                """);


        // --------------------------------------------------------
        // STATUS
        // --------------------------------------------------------

        statusLabel = new Label(
                "Pronto"
        );

        statusLabel.setStyle("""
                -fx-text-fill: #64748b;
                -fx-font-size: 11px;
                """);


        // --------------------------------------------------------
        // INPUT BOX
        // --------------------------------------------------------

        HBox inputBox = new HBox(
                10,
                messageInput,
                sendButton
        );

        inputBox.setAlignment(
                Pos.CENTER
        );

        inputBox.setPadding(
                new Insets(15, 20, 8, 20)
        );

        HBox.setHgrow(
                messageInput,
                Priority.ALWAYS
        );


        // --------------------------------------------------------
        // BOTTOM
        // --------------------------------------------------------

        VBox bottomBox = new VBox();

        bottomBox.setStyle("""
                -fx-background-color: #111827;
                """);

        bottomBox.getChildren().addAll(
                inputBox,
                statusLabel
        );

        VBox.setMargin(
                statusLabel,
                new Insets(0, 20, 10, 20)
        );


        // --------------------------------------------------------
        // ROOT
        // --------------------------------------------------------

        BorderPane root =
                new BorderPane();

        root.setTop(header);
        root.setCenter(scrollPane);
        root.setBottom(bottomBox);

        root.setStyle("""
                -fx-background-color: #0f172a;
                """);


        // --------------------------------------------------------
        // EVENTOS
        // --------------------------------------------------------

        sendButton.setOnAction(
                event -> sendMessage()
        );

        messageInput.setOnAction(
                event -> sendMessage()
        );


        // --------------------------------------------------------
        // MENSAGEM INICIAL
        // --------------------------------------------------------

        addMessage(
                "Olá! 👋\n\n" +
                        "Eu sou seu assistente conectado à Groq.\n" +
                        "Digite uma mensagem para começar.",
                false
        );


        // --------------------------------------------------------
        // SCENE
        // --------------------------------------------------------

        Scene scene = new Scene(
                root,
                900,
                650
        );


        stage.setTitle(
                "Groq Chat"
        );

        stage.setScene(
                scene
        );

        stage.show();
    }


    // ============================================================
    // ENVIAR MENSAGEM
    // ============================================================

    private void sendMessage() {

        String message =
                messageInput.getText().trim();


        // Não enviar mensagem vazia
        if (message.isEmpty()) {
            return;
        }


        // --------------------------------------------------------
        // VERIFICAR CHAVE
        // --------------------------------------------------------

        if (GROQ_API_KEY.equals("COLE_SUA_NOVA_CHAVE_AQUI")
                || GROQ_API_KEY.isBlank()) {

            addMessage(
                    "ERRO: você ainda não colocou a chave da Groq "
                            + "no código.\n\n"
                            + "Abra a Main.java e substitua "
                            + "COLE_SUA_NOVA_CHAVE_AQUI pela sua "
                            + "nova chave da Groq.",
                    false
            );

            return;
        }


        // --------------------------------------------------------
        // MOSTRAR MENSAGEM DO USUÁRIO
        // --------------------------------------------------------

        addMessage(
                message,
                true
        );


        // Limpar campo
        messageInput.clear();


        // Desativar botão
        sendButton.setDisable(true);


        // Atualizar status
        statusLabel.setText(
                "Groq está pensando..."
        );


        // --------------------------------------------------------
        // THREAD
        // --------------------------------------------------------

        Thread thread = new Thread(() -> {

            try {

                String response =
                        askGroq(message);


                Platform.runLater(() -> {

                    addMessage(
                            response,
                            false
                    );

                    sendButton.setDisable(
                            false
                    );

                    statusLabel.setText(
                            "Pronto"
                    );
                });


            } catch (Exception e) {

                Platform.runLater(() -> {

                    String errorMessage =
                            e.getMessage();

                    if (errorMessage == null ||
                            errorMessage.isBlank()) {

                        errorMessage =
                                "Erro desconhecido.";
                    }

                    addMessage(
                            "Erro ao conversar com a Groq:\n\n"
                                    + errorMessage,
                            false
                    );

                    sendButton.setDisable(
                            false
                    );

                    statusLabel.setText(
                            "Erro"
                    );
                });
            }

        });


        thread.setDaemon(true);

        thread.start();
    }


    // ============================================================
    // ENVIAR REQUISIÇÃO PARA GROQ
    // ============================================================

    private String askGroq(
            String message
    ) throws Exception {


        String escapedMessage =
                escapeJson(message);


        // --------------------------------------------------------
        // JSON DA REQUISIÇÃO
        // --------------------------------------------------------

        String json = """
                {
                  "model": "%s",
                  "messages": [
                    {
                      "role": "system",
                      "content": "Você é um assistente útil, inteligente e amigável. Responda sempre em português do Brasil."
                    },
                    {
                      "role": "user",
                      "content": "%s"
                    }
                  ],
                  "temperature": 0.7,
                  "max_tokens": 1024
                }
                """.formatted(
                MODEL,
                escapedMessage
        );


        // --------------------------------------------------------
        // REQUEST
        // --------------------------------------------------------

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(
                                URI.create(GROQ_URL)
                        )
                        .timeout(
                                Duration.ofSeconds(60)
                        )
                        .header(
                                "Authorization",
                                "Bearer " + GROQ_API_KEY
                        )
                        .header(
                                "Content-Type",
                                "application/json"
                        )
                        .POST(
                                HttpRequest.BodyPublishers.ofString(
                                        json,
                                        StandardCharsets.UTF_8
                                )
                        )
                        .build();


        // --------------------------------------------------------
        // ENVIAR
        // --------------------------------------------------------

        HttpResponse<String> response =
                httpClient.send(
                        request,
                        HttpResponse.BodyHandlers.ofString(
                                StandardCharsets.UTF_8
                        )
                );


        // --------------------------------------------------------
        // VERIFICAR STATUS
        // --------------------------------------------------------

        if (response.statusCode() < 200 ||
                response.statusCode() >= 300) {

            throw new RuntimeException(
                    "A Groq retornou HTTP "
                            + response.statusCode()
                            + "\n\n"
                            + response.body()
            );
        }


        // --------------------------------------------------------
        // EXTRAIR RESPOSTA
        // --------------------------------------------------------

        return extractContent(
                response.body()
        );
    }


    // ============================================================
    // EXTRAIR CONTENT DO JSON
    // ============================================================

    private String extractContent(
            String json
    ) {

        /*
         * A resposta da Groq possui a estrutura:
         *
         * choices[0].message.content
         *
         * Como estamos sem biblioteca JSON adicional,
         * usamos uma expressão regular simples.
         */

        Pattern pattern =
                Pattern.compile(
                        "\"content\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\""
                );


        Matcher matcher =
                pattern.matcher(json);


        if (!matcher.find()) {

            return "Não consegui encontrar a resposta da IA.\n\n"
                    + "Resposta recebida:\n"
                    + json;
        }


        String content =
                matcher.group(1);


        return unescapeJson(content);
    }


    // ============================================================
    // DESFAZER ESCAPES JSON
    // ============================================================

    private String unescapeJson(
            String text
    ) {

        StringBuilder result =
                new StringBuilder();

        boolean escaping = false;

        for (int i = 0; i < text.length(); i++) {

            char c = text.charAt(i);

            if (escaping) {

                switch (c) {

                    case 'n' -> result.append('\n');

                    case 'r' -> result.append('\r');

                    case 't' -> result.append('\t');

                    case 'b' -> result.append('\b');

                    case 'f' -> result.append('\f');

                    case '"' -> result.append('"');

                    case '\\' -> result.append('\\');

                    case '/' -> result.append('/');

                    default -> {
                        result.append('\\');
                        result.append(c);
                    }
                }

                escaping = false;

            } else {

                if (c == '\\') {

                    escaping = true;

                } else {

                    result.append(c);
                }
            }
        }


        if (escaping) {
            result.append('\\');
        }


        return result.toString();
    }


    // ============================================================
    // ESCAPAR TEXTO PARA JSON
    // ============================================================

    private String escapeJson(
            String text
    ) {

        StringBuilder result =
                new StringBuilder();

        for (int i = 0; i < text.length(); i++) {

            char c = text.charAt(i);

            switch (c) {

                case '\\' -> result.append("\\\\");

                case '"' -> result.append("\\\"");

                case '\n' -> result.append("\\n");

                case '\r' -> result.append("\\r");

                case '\t' -> result.append("\\t");

                case '\b' -> result.append("\\b");

                case '\f' -> result.append("\\f");

                default -> result.append(c);
            }
        }

        return result.toString();
    }


    // ============================================================
    // ADICIONAR MENSAGEM NA TELA
    // ============================================================

    private void addMessage(
            String message,
            boolean user
    ) {

        Label label =
                new Label(message);

        label.setWrapText(true);

        label.setMaxWidth(650);


        // --------------------------------------------------------
        // MENSAGEM DO USUÁRIO
        // --------------------------------------------------------

        if (user) {

            label.setStyle("""
                    -fx-background-color: #4f46e5;
                    -fx-text-fill: white;
                    -fx-background-radius: 15px;
                    -fx-padding: 12px 15px;
                    -fx-font-size: 14px;
                    """);


            HBox container =
                    new HBox(label);

            container.setAlignment(
                    Pos.CENTER_RIGHT
            );


            messagesBox.getChildren()
                    .add(container);


            // --------------------------------------------------------
            // MENSAGEM DA IA
            // --------------------------------------------------------

        } else {

            label.setStyle("""
                    -fx-background-color: #1e293b;
                    -fx-text-fill: #e2e8f0;
                    -fx-background-radius: 15px;
                    -fx-padding: 12px 15px;
                    -fx-font-size: 14px;
                    """);


            HBox container =
                    new HBox(label);

            container.setAlignment(
                    Pos.CENTER_LEFT
            );


            messagesBox.getChildren()
                    .add(container);
        }


        // --------------------------------------------------------
        // ROLAR PARA BAIXO
        // --------------------------------------------------------

        Platform.runLater(() -> {

            scrollPane.setVvalue(
                    1.0
            );
        });
    }


    // ============================================================
    // MAIN
    // ============================================================

    public static void main(
            String[] args
    ) {

        launch(args);
    }
}