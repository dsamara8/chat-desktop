package com.example.chatdesktop.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class ConhecimentoService {

    // ============================================================
    // NOME DO ARQUIVO DE CONHECIMENTO
    // ============================================================

    private static final String NOME_ARQUIVO =
            "conhecimento.txt";


    // ============================================================
    // CARREGAR CONHECIMENTO
    // ============================================================

    public String carregarConhecimento() {

        try (InputStream inputStream =
                     getClass().getResourceAsStream(
                             "/" + NOME_ARQUIVO
                     )) {

            if (inputStream == null) {

                throw new IllegalStateException(
                        "Arquivo " +
                                NOME_ARQUIVO +
                                " não encontrado."
                );
            }


            return new String(
                    inputStream.readAllBytes(),
                    StandardCharsets.UTF_8
            );

        } catch (IOException e) {

            throw new IllegalStateException(
                    "Não foi possível ler o arquivo " +
                            NOME_ARQUIVO +
                            ".",
                    e
            );
        }
    }


    // ============================================================
    // OBTER NOME DO ARQUIVO
    // ============================================================

    public String getNomeArquivo() {

        return NOME_ARQUIVO;
    }
}