package com.javastudio.domain;

import java.util.List;

public class Pergunta {
    private final int id;
    private final String enunciado;
    private final List<String> alternativas;
    private final int respostaCorreta;
    private final String explicacao;

    public Pergunta(int id, String enunciado, List<String> alternativas, int respostaCorreta, String explicacao) {
        this.id = id;
        this.enunciado = enunciado;
        this.alternativas = alternativas;
        this.respostaCorreta = respostaCorreta;
        this.explicacao = explicacao;
    }

    public int getId() { return id; }
    public String getEnunciado() { return enunciado; }
    public List<String> getAlternativas() { return alternativas; }
    public String getLetraCorreta() { return String.valueOf((char) ('A' + respostaCorreta)); }
    public String getExplicacao() { return explicacao; }
}
