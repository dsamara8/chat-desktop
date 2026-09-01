package com.example.chatdesktop.service;

import java.util.prefs.Preferences;

public class TemaService {

    private static final String CHAVE_TEMA = "temaEscuro";

    private final Preferences preferencias;

    public TemaService() {
        preferencias = Preferences.userNodeForPackage(TemaService.class);
    }

    public void salvarTema(boolean temaEscuro) {
        preferencias.putBoolean(CHAVE_TEMA, temaEscuro);
    }

    public boolean carregarTema() {
        return preferencias.getBoolean(CHAVE_TEMA, false);
    }
}
