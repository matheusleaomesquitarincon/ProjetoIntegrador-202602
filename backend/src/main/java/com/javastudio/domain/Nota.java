package com.javastudio.domain;

import java.time.LocalDateTime;

public class Nota {
    private final long id;
    private String titulo;
    private String conteudo;
    private LocalDateTime atualizadaEm;

    public Nota(long id, String titulo, String conteudo) {
        this.id = id;
        atualizar(titulo, conteudo);
    }

    public void atualizar(String titulo, String conteudo) {
        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException("O titulo da nota e obrigatorio.");
        }
        if (conteudo == null || conteudo.isBlank()) {
            throw new IllegalArgumentException("O conteudo da nota e obrigatorio.");
        }
        this.titulo = titulo.trim();
        this.conteudo = conteudo.trim();
        this.atualizadaEm = LocalDateTime.now();
    }

    public long getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getConteudo() { return conteudo; }
    public LocalDateTime getAtualizadaEm() { return atualizadaEm; }
}