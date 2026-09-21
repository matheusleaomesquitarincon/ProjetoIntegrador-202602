package com.javastudio.service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class SessaoManager {
    private final Map<String, String> sessoes = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();

    public String criar(String email) {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        sessoes.put(token, email);
        return token;
    }

    public Optional<String> usuario(String token) { return token == null ? Optional.empty() : Optional.ofNullable(sessoes.get(token)); }
    public void invalidar(String token) { if (token != null) sessoes.remove(token); }
}