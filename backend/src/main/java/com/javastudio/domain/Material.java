package com.javastudio.domain;

public abstract class Material {
    private final String topico;
    private final String dificuldade;

    protected Material(String topico, String dificuldade) {
        this.topico = topico;
        this.dificuldade = dificuldade;
    }

    public abstract String gerarConteudo();

    public String getTopico() { return topico; }
    public String getDificuldade() { return dificuldade; }
}