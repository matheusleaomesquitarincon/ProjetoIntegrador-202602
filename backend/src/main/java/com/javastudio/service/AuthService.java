package com.javastudio.service;

import com.javastudio.domain.Usuario;
import com.javastudio.repository.UsuarioRepository;
import java.util.Locale;
import java.util.regex.Pattern;

public class AuthService {
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private final UsuarioRepository repository;
    private final SenhaService senhaService;

    public AuthService(UsuarioRepository repository, SenhaService senhaService) {
        this.repository = repository;
        this.senhaService = senhaService;
    }

    public Usuario cadastrar(String email, String senha) {
        String emailNormalizado = validar(email, senha);
        if (repository.buscarPorEmail(emailNormalizado).isPresent()) throw new IllegalArgumentException("Este e-mail ja esta cadastrado.");
        return repository.salvar(new Usuario(emailNormalizado, senhaService.gerarHash(senha)));
    }

    public Usuario autenticar(String email, String senha) {
        String emailNormalizado = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        return repository.buscarPorEmail(emailNormalizado)
                .filter(usuario -> senhaService.verificar(senha == null ? "" : senha, usuario.getSenhaHash()))
                .orElseThrow(() -> new IllegalArgumentException("E-mail ou senha invalidos."));
    }

    private String validar(String email, String senha) {
        String normalizado = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        if (!EMAIL.matcher(normalizado).matches()) throw new IllegalArgumentException("Informe um e-mail valido.");
        if (senha == null || senha.length() < 8) throw new IllegalArgumentException("A senha deve ter pelo menos 8 caracteres.");
        return normalizado;
    }
}