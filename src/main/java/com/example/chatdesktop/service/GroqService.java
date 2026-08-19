package com.example.chatdesktop.service;

import com.example.chatdesktop.config.GroqConfig;
import com.example.chatdesktop.model.ChatMessage;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class GroqService {

    private final HttpClient httpClient;
    private final Gson gson;

    public GroqService() {

        httpClient = HttpClient
                .newBuilder()
                .connectTimeout(
                        Duration.ofSeconds(20)
                )
                .build();

        gson = new Gson();
    }

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
                    );

        } catch (Exception e) {

            return CompletableFuture.failedFuture(e);
        }
    }

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

    private String processarResposta(
            HttpResponse<String> response
    ) {

        if (response.statusCode() < 200
                || response.statusCode() >= 300) {

            throw new RuntimeException(
                    "Erro da Groq.\n"
                            + "HTTP: "
                            + response.statusCode()
                            + "\n\n"
                            + response.body()
            );
        }

        JsonObject json =
                JsonParser
                        .parseString(
                                response.body()
                        )
                        .getAsJsonObject();

        JsonArray choices =
                json.getAsJsonArray("choices");

        if (choices == null
                || choices.isEmpty()) {

            throw new RuntimeException(
                    "A Groq não retornou nenhuma resposta."
            );
        }

        return choices
                .get(0)
                .getAsJsonObject()
                .getAsJsonObject("message")
                .get("content")
                .getAsString();
    }
}