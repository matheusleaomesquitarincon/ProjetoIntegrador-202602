package com.javastudio.api;

import com.javastudio.domain.Pergunta;
import com.javastudio.domain.QuestaoResposta;
import com.javastudio.domain.ResultadoQuiz;
import com.javastudio.service.QuizService;
import com.javastudio.service.SessaoManager;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QuizHttpServer {
    private static final Pattern RESPOSTAS = Pattern.compile("\"respostas\"\\s*:\\s*\"((?:\\\\.|[^\"])*)\"");
    private final QuizService service;
    private final SessaoManager sessoes;

    public QuizHttpServer(QuizService service, SessaoManager sessoes, HttpServer server) {
        this.service = service;
        this.sessoes = sessoes;
        server.createContext("/api/quiz", this::handle);
    }

    private void handle(HttpExchange exchange) throws IOException {
        addCors(exchange);
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) { responder(exchange, 204, ""); return; }
        String emailUsuario = usuarioAutenticado(exchange);
        if (emailUsuario == null) { responder(exchange, 401, "{\"erro\":\"Sessao invalida.\"}"); return; }
        try {
            String[] partes = exchange.getRequestURI().getPath().split("/");
            if (partes.length == 4 && "pontuacao".equals(partes[3])) {
                responder(exchange, 200, pontuacaoJson(emailUsuario));
                return;
            }
            if (partes.length == 5 && "GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                responder(exchange, 200, perguntasJson(service.perguntas(partes[3], partes[4])));
                return;
            }
            if (partes.length == 6 && "corrigir".equals(partes[5]) && "POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                corrigir(exchange, emailUsuario, partes[3], partes[4]);
                return;
            }
            responder(exchange, 404, "{\"erro\":\"Rota nao encontrada\"}");
        } catch (IllegalArgumentException exception) {
            responder(exchange, 404, "{\"erro\":\"" + escapar(exception.getMessage()) + "\"}");
        } catch (Exception exception) {
            responder(exchange, 500, "{\"erro\":\"Erro interno do servidor\"}");
        }
    }

    private void corrigir(HttpExchange exchange, String emailUsuario, String topico, String dificuldade) throws IOException {
        Map<Integer, String> respostas = lerRespostas(corpo(exchange));
        ResultadoQuiz resultado = service.corrigir(emailUsuario, topico, dificuldade, respostas);
        responder(exchange, 200, resultadoJson(resultado));
    }

    private Map<Integer, String> lerRespostas(String corpo) {
        Matcher matcher = RESPOSTAS.matcher(corpo);
        Map<Integer, String> respostas = new LinkedHashMap<>();
        if (!matcher.find()) return respostas;
        String valor = decodificar(matcher.group(1));
        if (valor.isBlank()) return respostas;
        for (String item : valor.split(",")) {
            String[] partes = item.split(":", -1);
            if (partes.length != 2) continue;
            try {
                respostas.put(Integer.parseInt(partes[0].trim()), partes[1].trim());
            } catch (NumberFormatException ignored) { }
        }
        return respostas;
    }

    private String perguntasJson(List<Pergunta> perguntas) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < perguntas.size(); i++) {
            if (i > 0) json.append(",");
            Pergunta pergunta = perguntas.get(i);
            json.append("{\"id\":").append(pergunta.getId())
                    .append(",\"enunciado\":\"").append(escapar(pergunta.getEnunciado())).append("\"")
                    .append(",\"alternativas\":").append(alternativasJson(pergunta.getAlternativas()))
                    .append("}");
        }
        return json.append("]").toString();
    }

    private String resultadoJson(ResultadoQuiz resultado) {
        Map<Integer, Pergunta> porId = new LinkedHashMap<>();
        for (Pergunta pergunta : service.perguntas(resultado.getTopico(), resultado.getDificuldade())) porId.put(pergunta.getId(), pergunta);

        StringBuilder json = new StringBuilder("{");
        json.append("\"acertos\":").append(resultado.getAcertos())
                .append(",\"total\":").append(resultado.getTotal())
                .append(",\"pontosGanhos\":").append(resultado.getPontosGanhos())
                .append(",\"pontuacaoTotal\":").append(service.pontuacaoTotal(resultado.getUsuarioEmail()))
                .append(",\"detalhes\":[");
        List<QuestaoResposta> detalhes = resultado.getDetalhes();
        for (int i = 0; i < detalhes.size(); i++) {
            if (i > 0) json.append(",");
            QuestaoResposta detalhe = detalhes.get(i);
            Pergunta pergunta = porId.get(detalhe.getPerguntaId());
            json.append("{\"perguntaId\":").append(detalhe.getPerguntaId())
                    .append(",\"enunciado\":\"").append(escapar(pergunta.getEnunciado())).append("\"")
                    .append(",\"alternativas\":").append(alternativasJson(pergunta.getAlternativas()))
                    .append(",\"respostaDada\":\"").append(escapar(detalhe.getRespostaDada())).append("\"")
                    .append(",\"respostaCorreta\":\"").append(pergunta.getLetraCorreta()).append("\"")
                    .append(",\"correta\":").append(detalhe.isCorreta())
                    .append(",\"explicacao\":\"").append(escapar(pergunta.getExplicacao())).append("\"")
                    .append("}");
        }
        return json.append("]}").toString();
    }

    private String pontuacaoJson(String emailUsuario) {
        List<ResultadoQuiz> historico = service.historico(emailUsuario);
        StringBuilder json = new StringBuilder("{");
        json.append("\"pontuacaoTotal\":").append(service.pontuacaoTotal(emailUsuario)).append(",\"tentativas\":[");
        for (int i = 0; i < historico.size(); i++) {
            if (i > 0) json.append(",");
            ResultadoQuiz resultado = historico.get(historico.size() - 1 - i);
            json.append("{\"topico\":\"").append(escapar(resultado.getTopico())).append("\"")
                    .append(",\"dificuldade\":\"").append(escapar(resultado.getDificuldade())).append("\"")
                    .append(",\"acertos\":").append(resultado.getAcertos())
                    .append(",\"total\":").append(resultado.getTotal())
                    .append(",\"pontosGanhos\":").append(resultado.getPontosGanhos())
                    .append(",\"data\":\"").append(resultado.getData()).append("\"}");
        }
        return json.append("]}").toString();
    }

    private String alternativasJson(List<String> alternativas) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < alternativas.size(); i++) {
            if (i > 0) json.append(",");
            json.append("{\"letra\":\"").append((char) ('A' + i)).append("\",\"texto\":\"").append(escapar(alternativas.get(i))).append("\"}");
        }
        return json.append("]").toString();
    }

    private String corpo(HttpExchange exchange) throws IOException { try (InputStream input = exchange.getRequestBody()) { return new String(input.readAllBytes(), StandardCharsets.UTF_8); } }
    private String escapar(String valor) { return valor == null ? "" : valor.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r"); }
    private String decodificar(String valor) { return valor.replace("\\\"", "\"").replace("\\n", "\n").replace("\\r", "\r").replace("\\\\", "\\"); }

    private String usuarioAutenticado(HttpExchange exchange) {
        String header = exchange.getRequestHeaders().getFirst("Cookie");
        if (header == null) return null;
        for (String item : header.split(";")) {
            String[] partes = item.trim().split("=", 2);
            if (partes.length == 2 && partes[0].equals("JSESSIONID")) return sessoes.usuario(partes[1]).orElse(null);
        }
        return null;
    }

    private void addCors(HttpExchange exchange) {
        String origin = exchange.getRequestHeaders().getFirst("Origin");
        if ("http://127.0.0.1:5173".equals(origin) || "http://localhost:5173".equals(origin)) exchange.getResponseHeaders().add("Access-Control-Allow-Origin", origin);
        exchange.getResponseHeaders().add("Access-Control-Allow-Credentials", "true");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
    }

    private void responder(HttpExchange exchange, int status, String corpo) throws IOException {
        byte[] bytes = corpo.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }
}
