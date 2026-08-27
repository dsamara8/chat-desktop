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
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

public class GroqService {

    private final HttpClient httpClient;

    private final Gson gson;

    private final ConhecimentoService conhecimentoService;


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

        conhecimentoService =
                new ConhecimentoService();
    }


    // ============================================================
    // ENVIAR MENSAGEM
    // ============================================================

    public CompletableFuture<String> enviarMensagem(
            List<ChatMessage> historico
    ) {

        return enviarMensagemComOrigem(
                historico
        ).thenApply(
                ResultadoResposta::getResposta
        );
    }


    // ============================================================
    // ENVIAR MENSAGEM COM ORIGEM
    // ============================================================

    public CompletableFuture<ResultadoResposta> enviarMensagemComOrigem(
            List<ChatMessage> historico
    ) {

        try {

            String apiKey =
                    GroqConfig.getApiKey();

            String conhecimento =
                    conhecimentoService
                            .carregarConhecimento();

            String pergunta =
                    obterUltimaPergunta(
                            historico
                    );

            OrigemResposta origem =
                    determinarOrigem(
                            pergunta,
                            conhecimento
                    );

            String json =
                    criarJson(
                            historico,
                            conhecimento,
                            origem
                    );


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
                    .thenApply(
                            resposta ->
                                    new ResultadoResposta(
                                            resposta,
                                            origem
                                    )
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
    // OBTER ÚLTIMA PERGUNTA
    // ============================================================

    private String obterUltimaPergunta(
            List<ChatMessage> historico
    ) {

        for (int i = historico.size() - 1;
             i >= 0;
             i--) {

            ChatMessage mensagem =
                    historico.get(i);

            if ("user".equals(
                    mensagem.getRole()
            )) {

                return mensagem.getContent();
            }
        }

        return "";
    }


    // ============================================================
    // DETERMINAR ORIGEM
    // ============================================================

    private OrigemResposta determinarOrigem(
            String pergunta,
            String conhecimento
    ) {

        if (perguntaEstaRelacionadaAoConhecimento(
                pergunta,
                conhecimento
        )) {

            return OrigemResposta.RAG;
        }

        return OrigemResposta.FALLBACK_LOCAL;
    }


    // ============================================================
    // VERIFICAR RELAÇÃO COM O CONHECIMENTO
    // ============================================================

    private boolean perguntaEstaRelacionadaAoConhecimento(
            String pergunta,
            String conhecimento
    ) {

        String perguntaNormalizada =
                normalizarTexto(
                        pergunta
                );

        String conhecimentoNormalizado =
                normalizarTexto(
                        conhecimento
                );


        Set<String> palavrasPergunta =
                Arrays.stream(
                                perguntaNormalizada
                                        .split("\\s+")
                        )
                        .filter(
                                palavra ->
                                        palavra.length() >= 3
                        )
                        .collect(
                                Collectors.toSet()
                        );


        if (palavrasPergunta.isEmpty()) {

            return false;
        }


        int palavrasEncontradas = 0;


        for (String palavra : palavrasPergunta) {

            if (conhecimentoNormalizado.contains(
                    palavra
            )) {

                palavrasEncontradas++;
            }
        }


        return palavrasEncontradas >= 1;
    }


    // ============================================================
    // NORMALIZAR TEXTO
    // ============================================================

    private String normalizarTexto(
            String texto
    ) {

        return texto
                .toLowerCase()
                .replaceAll(
                        "[^a-záàâãéêíóôõúç0-9\\s]",
                        " "
                )
                .replaceAll(
                        "\\s+",
                        " "
                )
                .trim();
    }


    // ============================================================
    // CRIAR JSON
    // ============================================================

    private String criarJson(
            List<ChatMessage> historico,
            String conhecimento,
            OrigemResposta origem
    ) {

        JsonObject json =
                new JsonObject();

        json.addProperty(
                "model",
                GroqConfig.MODEL
        );


        JsonArray mensagens =
                new JsonArray();


        // ========================================================
        // INSTRUÇÃO DO SISTEMA
        // ========================================================

        JsonObject mensagemSistema =
                new JsonObject();

        mensagemSistema.addProperty(
                "role",
                "system"
        );


        if (origem == OrigemResposta.RAG) {

            mensagemSistema.addProperty(
                    "content",
                    "Você é um assistente útil, educado e objetivo. " +
                            "Responda sempre em português do Brasil.\n\n" +
                            "Use o conhecimento abaixo para responder " +
                            "à pergunta do usuário quando ele for relevante.\n\n" +
                            "CONHECIMENTO:\n" +
                            conhecimento
            );

        } else {

            mensagemSistema.addProperty(
                    "content",
                    "Você é um assistente útil, educado e objetivo. " +
                            "Responda sempre em português do Brasil."
            );
        }


        mensagens.add(
                mensagemSistema
        );


        // ========================================================
        // ADICIONAR HISTÓRICO DA CONVERSA
        // ========================================================

        for (ChatMessage mensagem : historico) {

            if ("system".equals(
                    mensagem.getRole()
            )) {

                continue;
            }


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
                    json.getAsJsonArray(
                            "choices"
                    );


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


        return causa;
    }


    // ============================================================
    // ORIGEM DA RESPOSTA
    // ============================================================

    public enum OrigemResposta {

        RAG,
        INTERNET,
        FALLBACK_LOCAL
    }


    // ============================================================
    // RESULTADO DA RESPOSTA
    // ============================================================

    public static class ResultadoResposta {

        private final String resposta;

        private final OrigemResposta origem;


        public ResultadoResposta(
                String resposta,
                OrigemResposta origem
        ) {

            this.resposta =
                    resposta;

            this.origem =
                    origem;
        }


        public String getResposta() {

            return resposta;
        }


        public OrigemResposta getOrigem() {

            return origem;
        }
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