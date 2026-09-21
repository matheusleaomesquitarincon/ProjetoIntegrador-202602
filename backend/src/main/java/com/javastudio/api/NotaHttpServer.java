package com.javastudio.api;

import com.javastudio.domain.Nota;
import com.javastudio.service.MaterialService;
import com.javastudio.service.NotaService;
import com.javastudio.service.SessaoManager;
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
    private final MaterialService materialService;
    private final HttpServer server;
    private final SessaoManager sessoes;

    public NotaHttpServer(NotaService service, MaterialService materialService, SessaoManager sessoes, int port) throws IOException {
        this(service, materialService, sessoes, HttpServer.create(new InetSocketAddress(port), 0));
    }

    public NotaHttpServer(NotaService service, MaterialService materialService, SessaoManager sessoes, int port, HttpServer server) throws IOException {
        this(service, materialService, sessoes, server);
    }

    private NotaHttpServer(NotaService service, MaterialService materialService, SessaoManager sessoes, HttpServer server) {
        this.service = service;
        this.materialService = materialService;
        this.sessoes = sessoes;
        this.server = server;
        server.createContext("/api/notas", this::handle);
        server.createContext("/api/materiais", this::handleMaterial);
    }

    private void handleMaterial(HttpExchange exchange) throws IOException {
        addCors(exchange);
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) { responder(exchange, 204, ""); return; }
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) { responder(exchange, 405, "{\"erro\":\"Metodo nao permitido\"}"); return; }
        try {
            String[] partes = exchange.getRequestURI().getPath().split("/");
            if (partes.length != 5 && partes.length != 6) throw new IllegalArgumentException("Informe topico e dificuldade.");
            String markdown = materialService.buscar(partes[3], partes[4]);
            if (partes.length == 6 && partes[5].equals("md")) {
                byte[] arquivo = markdown.getBytes(StandardCharsets.UTF_8);
                responderArquivo(exchange, 200, arquivo, partes[3] + "-" + partes[4] + ".md", "text/markdown; charset=UTF-8");
                return;
            }
            responder(exchange, 200, "{\"topico\":\"" + escapar(partes[3]) + "\",\"dificuldade\":\"" + escapar(partes[4]) + "\",\"conteudo\":\"" + escapar(markdown) + "\"}");
        } catch (IllegalArgumentException exception) {
            responder(exchange, 404, "{\"erro\":\"" + escapar(exception.getMessage()) + "\"}");
        }
    }

    public void iniciar() {
        server.start();
        System.out.println("Backend Java em http://localhost:8080");
    }

    private void handle(HttpExchange exchange) throws IOException {
        addCors(exchange);
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) { responder(exchange, 204, ""); return; }
        if (!autenticado(exchange)) { responder(exchange, 401, "{\"erro\":\"Sessao invalida.\"}"); return; }
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
    private boolean autenticado(HttpExchange exchange) { String header = exchange.getRequestHeaders().getFirst("Cookie"); if (header == null) return false; for (String item : header.split(";")) { String[] partes = item.trim().split("=", 2); if (partes.length == 2 && partes[0].equals("JSESSIONID")) return sessoes.usuario(partes[1]).isPresent(); } return false; }
    private void addCors(HttpExchange exchange) { String origin = exchange.getRequestHeaders().getFirst("Origin"); if ("http://127.0.0.1:5173".equals(origin) || "http://localhost:5173".equals(origin)) exchange.getResponseHeaders().add("Access-Control-Allow-Origin", origin); exchange.getResponseHeaders().add("Access-Control-Allow-Credentials", "true"); exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS"); exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type"); }
    private void responder(HttpExchange exchange, int status, String corpo) throws IOException { byte[] bytes = corpo.getBytes(StandardCharsets.UTF_8); exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8"); exchange.sendResponseHeaders(status, bytes.length); exchange.getResponseBody().write(bytes); exchange.close(); }
    private void responderArquivo(HttpExchange exchange, int status, byte[] bytes, String nomeArquivo, String tipo) throws IOException { exchange.getResponseHeaders().set("Content-Type", tipo); exchange.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"" + nomeArquivo + "\""); exchange.sendResponseHeaders(status, bytes.length); exchange.getResponseBody().write(bytes); exchange.close(); }
    private record Campos(String titulo, String conteudo) {}
}