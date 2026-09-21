package com.javastudio.api;

import com.javastudio.domain.Usuario;
import com.javastudio.service.AuthService;
import com.javastudio.service.SessaoManager;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AuthHttpServer {
    private static final Pattern FIELD = Pattern.compile("\\\"(email|senha|confirmacao)\\\"\\s*:\\s*\\\"((?:\\\\.|[^\\\"])*)\\\"");
    private final AuthService service;
    private final SessaoManager sessoes;

    public AuthHttpServer(HttpServer server, AuthService service, SessaoManager sessoes) {
        this.service = service;
        this.sessoes = sessoes;
        server.createContext("/api/auth", this::handle);
    }

    private void handle(HttpExchange exchange) throws IOException {
        addCors(exchange);
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) { responder(exchange, 204, ""); return; }
        String rota = exchange.getRequestURI().getPath().substring("/api/auth".length());
        try {
            switch (rota) {
                case "/cadastro" -> cadastro(exchange);
                case "/login" -> login(exchange);
                case "/logout" -> logout(exchange);
                case "/me" -> me(exchange);
                default -> responder(exchange, 404, "{\"erro\":\"Rota nao encontrada\"}");
            }
        } catch (IllegalArgumentException exception) {
            responder(exchange, 400, "{\"erro\":\"" + escapar(exception.getMessage()) + "\"}");
        } catch (Exception exception) {
            responder(exchange, 500, "{\"erro\":\"Erro interno do servidor\"}");
        }
    }

    private void cadastro(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) { responder(exchange, 405, ""); return; }
        Campos campos = campos(corpo(exchange));
        if (campos.senha() == null || !campos.senha().equals(campos.confirmacao())) throw new IllegalArgumentException("As senhas precisam ser iguais.");
        Usuario usuario = service.cadastrar(campos.email(), campos.senha());
        responder(exchange, 201, usuarioJson(usuario));
    }

    private void login(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) { responder(exchange, 405, ""); return; }
        Campos campos = campos(corpo(exchange));
        Usuario usuario;
        try {
            usuario = service.autenticar(campos.email(), campos.senha());
        } catch (IllegalArgumentException exception) {
            responder(exchange, 401, "{\"erro\":\"E-mail ou senha invalidos.\"}");
            return;
        }
        String token = sessoes.criar(usuario.getEmail());
        exchange.getResponseHeaders().add("Set-Cookie", "JSESSIONID=" + token + "; Path=/; HttpOnly; SameSite=Lax");
        responder(exchange, 200, usuarioJson(usuario));
    }

    private void logout(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) { responder(exchange, 405, ""); return; }
        sessoes.invalidar(cookie(exchange));
        exchange.getResponseHeaders().add("Set-Cookie", "JSESSIONID=; Path=/; Max-Age=0; HttpOnly; SameSite=Lax");
        responder(exchange, 204, "");
    }

    private void me(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) { responder(exchange, 405, ""); return; }
        String email = sessoes.usuario(cookie(exchange)).orElse(null);
        if (email == null) { responder(exchange, 401, "{\"erro\":\"Sessao invalida.\"}"); return; }
        responder(exchange, 200, "{\"email\":\"" + escapar(email) + "\"}");
    }

    private String cookie(HttpExchange exchange) {
        String header = exchange.getRequestHeaders().getFirst("Cookie");
        if (header == null) return null;
        for (String item : header.split(";")) { String[] partes = item.trim().split("=", 2); if (partes.length == 2 && partes[0].equals("JSESSIONID")) return partes[1]; }
        return null;
    }

    private Campos campos(String corpo) {
        Matcher matcher = FIELD.matcher(corpo); String email = null, senha = null, confirmacao = null;
        while (matcher.find()) { if (matcher.group(1).equals("email")) email = decodificar(matcher.group(2)); else if (matcher.group(1).equals("senha")) senha = decodificar(matcher.group(2)); else confirmacao = decodificar(matcher.group(2)); }
        return new Campos(email, senha, confirmacao);
    }

    private String corpo(HttpExchange exchange) throws IOException { try (InputStream input = exchange.getRequestBody()) { return new String(input.readAllBytes(), StandardCharsets.UTF_8); } }
    private String usuarioJson(Usuario usuario) { return "{\"email\":\"" + escapar(usuario.getEmail()) + "\"}"; }
    private String escapar(String valor) { return valor == null ? "" : valor.replace("\\", "\\\\").replace("\"", "\\\""); }
    private String decodificar(String valor) { return valor.replace("\\\"", "\"").replace("\\\\", "\\"); }
    private void addCors(HttpExchange exchange) { String origin = exchange.getRequestHeaders().getFirst("Origin"); if ("http://127.0.0.1:5173".equals(origin) || "http://localhost:5173".equals(origin)) exchange.getResponseHeaders().add("Access-Control-Allow-Origin", origin); exchange.getResponseHeaders().add("Access-Control-Allow-Credentials", "true"); exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS"); exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type"); }
    private void responder(HttpExchange exchange, int status, String corpo) throws IOException { byte[] bytes = corpo.getBytes(StandardCharsets.UTF_8); exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8"); exchange.sendResponseHeaders(status, bytes.length); exchange.getResponseBody().write(bytes); exchange.close(); }
    private record Campos(String email, String senha, String confirmacao) {}
}