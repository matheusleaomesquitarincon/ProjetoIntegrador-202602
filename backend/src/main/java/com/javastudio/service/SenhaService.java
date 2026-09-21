package com.javastudio.service;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class SenhaService {
    private static final int ITERACOES = 120_000;
    private static final int TAMANHO_SALT = 16;
    private static final int TAMANHO_HASH = 256;
    private final SecureRandom random = new SecureRandom();

    public String gerarHash(String senha) {
        byte[] salt = new byte[TAMANHO_SALT];
        random.nextBytes(salt);
        return ITERACOES + ":" + codificar(salt) + ":" + codificar(derivar(senha, salt, ITERACOES));
    }

    public boolean verificar(String senha, String armazenado) {
        try {
            String[] partes = armazenado.split(":");
            byte[] salt = decodificar(partes[1]);
            byte[] esperado = decodificar(partes[2]);
            byte[] atual = derivar(senha, salt, Integer.parseInt(partes[0]));
            return MessageDigest.isEqual(esperado, atual);
        } catch (RuntimeException exception) {
            return false;
        }
    }

    private byte[] derivar(String senha, byte[] salt, int iteracoes) {
        try {
            PBEKeySpec spec = new PBEKeySpec(senha.toCharArray(), salt, iteracoes, TAMANHO_HASH);
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Nao foi possivel proteger a senha.", exception);
        }
    }

    private String codificar(byte[] valor) { return Base64.getEncoder().encodeToString(valor); }
    private byte[] decodificar(String valor) { return Base64.getDecoder().decode(valor); }
}