package com.example.chatdesktop.service;

import com.example.chatdesktop.config.GroqConfig;
import com.example.chatdesktop.model.ChatMessage;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.ConnectException;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class GroqService {

    private final HttpClient httpClient;

    private final Gson gson;


    // ============================================================
    // CONSTRUTOR
    // ============================================================

    public GroqService() {

        httpClient =
                HttpClient
                        .newBuilder()
                        .connectTimeout(
                                Duration.ofSeconds(20)
                        )
                        .build();

        gson = new Gson();
    }


    // ============================================================
    // ENVIAR MENSAGEM
    // ============================================================

    public CompletableFuture<String> enviarMensagem(
            List<ChatMessage> historico
    ) {

        try {

            String apiKey =
                    GroqConfig.getApiKey();

            String json =
                    criarJson(historico);


            HttpRequest request =
                    HttpRequest
                            .newBuilder()
                            .uri(
                                    URI.create(
                                            GroqConfig.API_URL
                                    )
                            )
                            .timeout(
                                    Duration.ofSeconds(60)
                            )
                            .header(
                                    "Authorization",
                                    "Bearer " + apiKey
                            )
                            .header(
                                    "Content-Type",
                                    "application/json"
                            )
                            .POST(
                                    HttpRequest
                                            .BodyPublishers
                                            .ofString(
                                                    json,
                                                    StandardCharsets.UTF_8
                                            )
                            )
                            .build();


            return httpClient
                    .sendAsync(
                            request,
                            HttpResponse.BodyHandlers.ofString(
                                    StandardCharsets.UTF_8
                            )
                    )
                    .thenApply(
                            this::processarResposta
                    )
                    .exceptionallyCompose(
                            erro -> CompletableFuture.failedFuture(
                                    tratarErroComunicacao(erro)
                            )
                    );

        } catch (Exception e) {

            return CompletableFuture.failedFuture(e);
        }
    }


    // ============================================================
    // CRIAR JSON
    // ============================================================

    private String criarJson(
            List<ChatMessage> historico
    ) {

        JsonObject json =
                new JsonObject();

        json.addProperty(
                "model",
                GroqConfig.MODEL
        );


        JsonArray mensagens =
                new JsonArray();


        for (ChatMessage mensagem : historico) {

            JsonObject item =
                    new JsonObject();

            item.addProperty(
                    "role",
                    mensagem.getRole()
            );

            item.addProperty(
                    "content",
                    mensagem.getContent()
            );

            mensagens.add(item);
        }


        json.add(
                "messages",
                mensagens
        );


        return gson.toJson(json);
    }


    // ============================================================
    // PROCESSAR RESPOSTA
    // ============================================================

    private String processarResposta(
            HttpResponse<String> response
    ) {

        int status =
                response.statusCode();


        // ========================================================
        // CHAVE INVÁLIDA
        // ========================================================

        if (status == 401) {

            throw new GroqException(
                    "CHAVE_INVALIDA",
                    "🔑 A chave da Groq é inválida.\n\n" +
                            "Verifique a variável GROQ_API_KEY " +
                            "e tente novamente."
            );
        }


        // ========================================================
        // LIMITE DA API
        // ========================================================

        if (status == 429) {

            throw new GroqException(
                    "LIMITE_API",
                    "🚦 O limite da API da Groq foi atingido.\n\n" +
                            "Aguarde alguns instantes e tente novamente."
            );
        }


        // ========================================================
        // ERRO DE SERVIDOR
        // ========================================================

        if (status == 500 ||
                status == 502 ||
                status == 503 ||
                status == 504) {

            throw new GroqException(
                    "SERVIDOR",
                    "🔧 A Groq está apresentando um problema " +
                            "temporário.\n\n" +
                            "Tente novamente em alguns instantes."
            );
        }


        // ========================================================
        // OUTROS ERROS
        // ========================================================

        if (status < 200 || status >= 300) {

            throw new GroqException(
                    "API",
                    "⚠️ A Groq não conseguiu processar sua solicitação.\n\n" +
                            "Código HTTP: " + status
            );
        }


        // ========================================================
        // LER RESPOSTA
        // ========================================================

        try {

            JsonObject json =
                    JsonParser
                            .parseString(
                                    response.body()
                            )
                            .getAsJsonObject();


            JsonArray choices =
                    json.getAsJsonArray("choices");


            if (choices == null ||
                    choices.isEmpty()) {

                throw new GroqException(
                        "RESPOSTA",
                        "⚠️ A Groq não retornou nenhuma resposta."
                );
            }


            return choices
                    .get(0)
                    .getAsJsonObject()
                    .getAsJsonObject("message")
                    .get("content")
                    .getAsString();

        } catch (GroqException e) {

            throw e;

        } catch (Exception e) {

            throw new GroqException(
                    "RESPOSTA",
                    "⚠️ A resposta recebida da Groq " +
                            "não pôde ser interpretada."
            );
        }
    }


    // ============================================================
    // TRATAR ERROS DE COMUNICAÇÃO
    // ============================================================

    private Throwable tratarErroComunicacao(
            Throwable erro
    ) {

        Throwable causa =
                erro;


        if (erro instanceof CompletionException &&
                erro.getCause() != null) {

            causa =
                    erro.getCause();
        }


        // ========================================================
        // INTERNET / DNS
        // ========================================================

        if (causa instanceof UnknownHostException ||
                causa instanceof ConnectException) {

            return new GroqException(
                    "INTERNET",
                    "🌐 Não foi possível conectar à Groq.\n\n" +
                            "Verifique sua conexão com a internet " +
                            "e tente novamente."
            );
        }


        // ========================================================
        // TIMEOUT
        // ========================================================

        if (causa instanceof java.net.http.HttpTimeoutException) {

            return new GroqException(
                    "TIMEOUT",
                    "⏱️ A comunicação com a Groq demorou demais.\n\n" +
                            "Verifique sua internet e tente novamente."
            );
        }


        // ========================================================
        // ERRO DE CHAVE
        // ========================================================

        if (causa instanceof IllegalStateException) {

            return new GroqException(
                    "CONFIGURACAO",
                    "🔑 A chave da Groq não foi configurada.\n\n" +
                            "Configure a variável GROQ_API_KEY."
            );
        }


        // ========================================================
        // OUTRO ERRO
        // ========================================================

        return causa;
    }


    // ============================================================
    // CLASSE DE ERRO DA GROQ
    // ============================================================

    public static class GroqException
            extends RuntimeException {

        private final String tipo;


        public GroqException(
                String tipo,
                String mensagem
        ) {

            super(mensagem);

            this.tipo =
                    tipo;
        }


        public String getTipo() {

            return tipo;
        }
    }
}