package com.example.chatdesktop.model;

import java.util.ArrayList;
import java.util.List;

public class Conversa {

    private int id;

    private String titulo;

    private List<ChatMessage> historico;


    public Conversa(
            int id,
            String titulo,
            List<ChatMessage> historico
    ) {

        this.id = id;

        this.titulo = titulo;

        this.historico = historico;
    }


    public Conversa(
            String titulo,
            List<ChatMessage> historico
    ) {

        this(
                -1,
                titulo,
                historico
        );
    }


    public int getId() {
        return id;
    }


    public void setId(
            int id
    ) {

        this.id = id;
    }


    public String getTitulo() {
        return titulo;
    }


    public void setTitulo(
            String titulo
    ) {

        this.titulo = titulo;
    }


    public List<ChatMessage> getHistorico() {
        return historico;
    }


    public void setHistorico(
            List<ChatMessage> historico
    ) {

        this.historico = historico;
    }
}