package com.javastudio.repository;

import com.javastudio.domain.QuestaoResposta;
import com.javastudio.domain.ResultadoQuiz;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

public class QuizResultadoRepositoryArquivo implements QuizResultadoRepository {
    private final Path arquivo;
    private final List<ResultadoQuiz> resultados = new ArrayList<>();

    public QuizResultadoRepositoryArquivo(Path arquivo) {
        this.arquivo = arquivo;
        carregar();
    }

    @Override
    public synchronized ResultadoQuiz salvar(ResultadoQuiz resultado) {
        resultados.add(resultado);
        persistir();
        return resultado;
    }

    @Override
    public synchronized List<ResultadoQuiz> listarPorUsuario(String email) {
        return resultados.stream().filter(resultado -> resultado.getUsuarioEmail().equals(email)).toList();
    }

    private void carregar() {
        if (!Files.exists(arquivo)) return;
        try {
            for (String linha : Files.readAllLines(arquivo, StandardCharsets.UTF_8)) {
                String[] campos = linha.split("\t", -1);
                if (campos.length != 9) continue;
                long id = Long.parseLong(campos[0]);
                String email = decodificar(campos[1]);
                String topico = decodificar(campos[2]);
                String dificuldade = decodificar(campos[3]);
                LocalDateTime data = LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(campos[4])), ZoneId.systemDefault());
                int acertos = Integer.parseInt(campos[5]);
                int total = Integer.parseInt(campos[6]);
                int pontos = Integer.parseInt(campos[7]);
                List<QuestaoResposta> detalhes = decodificarDetalhes(campos[8]);
                resultados.add(new ResultadoQuiz(id, email, topico, dificuldade, data, acertos, total, pontos, detalhes));
            }
        } catch (IOException | RuntimeException exception) {
            throw new IllegalStateException("Nao foi possivel carregar os resultados do quiz.", exception);
        }
    }

    private void persistir() {
        try {
            if (arquivo.getParent() != null) Files.createDirectories(arquivo.getParent());
            List<String> linhas = resultados.stream().map(this::linha).toList();
            Files.write(arquivo, linhas, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Nao foi possivel salvar os resultados do quiz.", exception);
        }
    }

    private String linha(ResultadoQuiz resultado) {
        long epocaMillis = resultado.getData().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        return resultado.getId() + "\t" + codificar(resultado.getUsuarioEmail()) + "\t" + codificar(resultado.getTopico()) + "\t"
                + codificar(resultado.getDificuldade()) + "\t" + epocaMillis + "\t" + resultado.getAcertos() + "\t"
                + resultado.getTotal() + "\t" + resultado.getPontosGanhos() + "\t" + codificar(codificarDetalhes(resultado.getDetalhes()));
    }

    private String codificarDetalhes(List<QuestaoResposta> detalhes) {
        return detalhes.stream()
                .map(detalhe -> detalhe.getPerguntaId() + ":" + detalhe.getRespostaDada() + ":" + (detalhe.isCorreta() ? "1" : "0"))
                .collect(Collectors.joining(";"));
    }

    private List<QuestaoResposta> decodificarDetalhes(String valor) {
        String texto = decodificar(valor);
        List<QuestaoResposta> lista = new ArrayList<>();
        if (texto.isBlank()) return lista;
        for (String item : texto.split(";")) {
            String[] partes = item.split(":", -1);
            if (partes.length != 3) continue;
            lista.add(new QuestaoResposta(Integer.parseInt(partes[0]), partes[1], "1".equals(partes[2])));
        }
        return lista;
    }

    private String codificar(String valor) { return Base64.getEncoder().encodeToString(valor.getBytes(StandardCharsets.UTF_8)); }
    private String decodificar(String valor) { return new String(Base64.getDecoder().decode(valor), StandardCharsets.UTF_8); }
}
