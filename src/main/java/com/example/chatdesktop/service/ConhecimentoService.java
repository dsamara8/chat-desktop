package com.example.chatdesktop.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class ConhecimentoService {

    public String carregarConhecimento() {

        try (InputStream inputStream =
                     getClass().getResourceAsStream("/conhecimento.txt")) {

            if (inputStream == null) {
                throw new IllegalStateException(
                        "Arquivo conhecimento.txt não encontrado."
                );
            }

            return new String(
                    inputStream.readAllBytes(),
                    StandardCharsets.UTF_8
            );

        } catch (IOException e) {

            throw new IllegalStateException(
                    "Não foi possível ler o arquivo conhecimento.txt.",
                    e
            );
        }
    }
}
