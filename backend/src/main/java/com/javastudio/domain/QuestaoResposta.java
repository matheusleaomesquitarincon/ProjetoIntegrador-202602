package com.javastudio.domain;

public class QuestaoResposta {
    private final int perguntaId;
    private final String respostaDada;
    private final boolean correta;

    public QuestaoResposta(int perguntaId, String respostaDada, boolean correta) {
        this.perguntaId = perguntaId;
        this.respostaDada = respostaDada;
        this.correta = correta;
    }

    public int getPerguntaId() { return perguntaId; }
    public String getRespostaDada() { return respostaDada; }
    public boolean isCorreta() { return correta; }
}
