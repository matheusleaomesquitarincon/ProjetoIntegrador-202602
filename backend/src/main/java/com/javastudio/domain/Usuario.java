package com.javastudio.domain;

public class Usuario {
    private final String email;
    private final String senhaHash;

    public Usuario(String email, String senhaHash) {
        this.email = email;
        this.senhaHash = senhaHash;
    }

    public String getEmail() { return email; }
    public String getSenhaHash() { return senhaHash; }
}