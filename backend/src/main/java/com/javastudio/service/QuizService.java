package com.javastudio.service;

import com.javastudio.domain.Pergunta;
import com.javastudio.domain.QuestaoResposta;
import com.javastudio.domain.ResultadoQuiz;
import com.javastudio.repository.QuizResultadoRepository;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

public class QuizService {
    private static final Set<String> TOPICOS = Set.of("fundamentos", "orientacao-a-objetos", "collections", "excecoes", "spring");
    private static final Map<String, String> DIFICULDADES = Map.of(
            "iniciante", "iniciante.txt", "intermediario", "intermediario.txt", "avancado", "avancado.txt");
    private static final int PONTOS_POR_ACERTO = 10;

    private final Path quizPath;
    private final QuizResultadoRepository repository;
    private final AtomicLong sequencia = new AtomicLong(System.currentTimeMillis());

    public QuizService(Path quizPath, QuizResultadoRepository repository) {
        this.quizPath = quizPath;
        this.repository = repository;
    }

    public List<Pergunta> perguntas(String topico, String dificuldade) {
        String arquivo = DIFICULDADES.get(dificuldade);
        if (arquivo == null || !TOPICOS.contains(topico)) throw new IllegalArgumentException("Quiz nao encontrado.");
        try {
            return parsear(Files.readString(quizPath.resolve(topico).resolve(arquivo), StandardCharsets.UTF_8));
        } catch (IOException exception) {
            throw new IllegalStateException("Nao foi possivel carregar o quiz.", exception);
        }
    }

    public ResultadoQuiz corrigir(String usuarioEmail, String topico, String dificuldade, Map<Integer, String> respostas) {
        List<Pergunta> perguntas = perguntas(topico, dificuldade);
        List<QuestaoResposta> detalhes = new ArrayList<>();
        int acertos = 0;
        for (Pergunta pergunta : perguntas) {
            String dada = respostas.getOrDefault(pergunta.getId(), "");
            boolean correta = dada.equalsIgnoreCase(pergunta.getLetraCorreta());
            if (correta) acertos++;
            detalhes.add(new QuestaoResposta(pergunta.getId(), dada, correta));
        }
        ResultadoQuiz resultado = new ResultadoQuiz(sequencia.incrementAndGet(), usuarioEmail, topico, dificuldade,
                LocalDateTime.now(), acertos, perguntas.size(), acertos * PONTOS_POR_ACERTO, detalhes);
        return repository.salvar(resultado);
    }

    public List<ResultadoQuiz> historico(String usuarioEmail) {
        return repository.listarPorUsuario(usuarioEmail);
    }

    public int pontuacaoTotal(String usuarioEmail) {
        return historico(usuarioEmail).stream().mapToInt(ResultadoQuiz::getPontosGanhos).sum();
    }

    private List<Pergunta> parsear(String conteudo) {
        List<Pergunta> perguntas = new ArrayList<>();
        int id = 1;
        for (String bloco : conteudo.split("(?m)^---\\s*$")) {
            if (bloco.isBlank()) continue;
            String enunciado = null;
            List<String> alternativas = new ArrayList<>();
            int respostaCorreta = -1;
            String explicacao = null;
            for (String linhaBruta : bloco.split("\n")) {
                String linha = linhaBruta.trim();
                if (linha.isEmpty()) continue;
                if (linha.startsWith("P:")) enunciado = linha.substring(2).trim();
                else if (linha.startsWith("A:") || linha.startsWith("B:") || linha.startsWith("C:") || linha.startsWith("D:")) {
                    alternativas.add(linha.substring(2).trim());
                } else if (linha.startsWith("R:")) {
                    respostaCorreta = "ABCD".indexOf(linha.substring(2).trim().toUpperCase());
                } else if (linha.startsWith("E:")) {
                    explicacao = linha.substring(2).trim();
                }
            }
            if (enunciado == null || alternativas.size() != 4 || respostaCorreta < 0 || explicacao == null) {
                throw new IllegalStateException("Quiz malformado.");
            }
            perguntas.add(new Pergunta(id++, enunciado, alternativas, respostaCorreta, explicacao));
        }
        return perguntas;
    }
}
