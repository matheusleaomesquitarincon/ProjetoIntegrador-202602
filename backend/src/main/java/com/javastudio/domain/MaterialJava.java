package com.javastudio.domain;

public class MaterialJava extends Material {
    private final String conteudo;

    public MaterialJava(String topico, String dificuldade, String conteudo) {
        super(topico, dificuldade);
        this.conteudo = conteudo;
    }

    @Override
    public String gerarConteudo() {
        return conteudo;
    }
}