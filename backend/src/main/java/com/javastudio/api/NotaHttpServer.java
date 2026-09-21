package com.javastudio.api;

import com.javastudio.domain.Nota;
import com.javastudio.service.NotaService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NotaHttpServer {
    private static final Pattern FIELD = Pattern.compile("\\\"(titulo|conteudo)\\\"\\s*:\\s*\\\"((?:\\\\.|[^\\\"])*)\\\"");
    private final NotaService service;
    private final HttpServer server;

    public NotaHttpServer(NotaService service, int port) throws IOException {
        this.service = service;
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api/notas", this::handle);
    }

    public void iniciar() {
        server.start();
        System.out.println("Backend Java em http://localhost:8080");
    }

    private void handle(HttpExchange exchange) throws IOException {
        addCors(exchange);
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) { responder(exchange, 204, ""); return; }
        try {
            String path = exchange.getRequestURI().getPath();
            String idText = path.substring("/api/notas".length()).replace("/", "");
            switch (exchange.getRequestMethod()) {
                case "GET" -> responder(exchange, 200, idText.isBlank() ? listarJson() : notaJson(service.listar().stream().filter(nota -> nota.getId() == Long.parseLong(idText)).findFirst().orElseThrow()));
                case "POST" -> criar(exchange);
                case "PUT" -> editar(exchange, Long.parseLong(idText));
                case "DELETE" -> apagar(exchange, Long.parseLong(idText));
                default -> responder(exchange, 405, "{\"erro\":\"Metodo nao permitido\"}");
            }
        } catch (IllegalArgumentException exception) {
            responder(exchange, 400, "{\"erro\":\"" + escapar(exception.getMessage()) + "\"}");
        } catch (Exception exception) {
            responder(exchange, 500, "{\"erro\":\"Erro interno do servidor\"}");
        }
    }

    private void criar(HttpExchange exchange) throws IOException {
        Campos campos = campos(corpo(exchange));
        Nota nota = service.criar(System.currentTimeMillis(), campos.titulo(), campos.conteudo());
        responder(exchange, 201, notaJson(nota));
    }

    private void editar(HttpExchange exchange, long id) throws IOException {
        Campos campos = campos(corpo(exchange));
        responder(exchange, 200, notaJson(service.atualizar(id, campos.titulo(), campos.conteudo())));
    }

    private void apagar(HttpExchange exchange, long id) throws IOException {
        service.apagar(id);
        responder(exchange, 204, "");
    }

    private String listarJson() { return service.listar().stream().map(this::notaJson).toList().toString().replace("=", ":"); }
    private String notaJson(Nota nota) { return "{\"id\":" + nota.getId() + ",\"titulo\":\"" + escapar(nota.getTitulo()) + "\",\"conteudo\":\"" + escapar(nota.getConteudo()) + "\"}"; }
    private Campos campos(String corpo) { Matcher matcher = FIELD.matcher(corpo); String titulo = null, conteudo = null; while (matcher.find()) { if (matcher.group(1).equals("titulo")) titulo = decodificar(matcher.group(2)); else conteudo = decodificar(matcher.group(2)); } return new Campos(titulo, conteudo); }
    private String corpo(HttpExchange exchange) throws IOException { try (InputStream input = exchange.getRequestBody()) { return new String(input.readAllBytes(), StandardCharsets.UTF_8); } }
    private String escapar(String valor) { return valor == null ? "" : valor.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r"); }
    private String decodificar(String valor) { return valor.replace("\\\"", "\"").replace("\\n", "\n").replace("\\r", "\r").replace("\\\\", "\\"); }
    private void addCors(HttpExchange exchange) { exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*"); exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS"); exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type"); }
    private void responder(HttpExchange exchange, int status, String corpo) throws IOException { byte[] bytes = corpo.getBytes(StandardCharsets.UTF_8); exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8"); exchange.sendResponseHeaders(status, bytes.length); exchange.getResponseBody().write(bytes); exchange.close(); }
    private record Campos(String titulo, String conteudo) {}
}