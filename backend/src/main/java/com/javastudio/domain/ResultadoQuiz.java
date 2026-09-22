package com.javastudio.domain;

import java.time.LocalDateTime;
import java.util.List;

public class ResultadoQuiz {
    private final long id;
    private final String usuarioEmail;
    private final String topico;
    private final String dificuldade;
    private final LocalDateTime data;
    private final int acertos;
    private final int total;
    private final int pontosGanhos;
    private final List<QuestaoResposta> detalhes;

    public ResultadoQuiz(long id, String usuarioEmail, String topico, String dificuldade, LocalDateTime data,
                          int acertos, int total, int pontosGanhos, List<QuestaoResposta> detalhes) {
        this.id = id;
        this.usuarioEmail = usuarioEmail;
        this.topico = topico;
        this.dificuldade = dificuldade;
        this.data = data;
        this.acertos = acertos;
        this.total = total;
        this.pontosGanhos = pontosGanhos;
        this.detalhes = detalhes;
    }

    public long getId() { return id; }
    public String getUsuarioEmail() { return usuarioEmail; }
    public String getTopico() { return topico; }
    public String getDificuldade() { return dificuldade; }
    public LocalDateTime getData() { return data; }
    public int getAcertos() { return acertos; }
    public int getTotal() { return total; }
    public int getPontosGanhos() { return pontosGanhos; }
    public List<QuestaoResposta> getDetalhes() { return detalhes; }
}
