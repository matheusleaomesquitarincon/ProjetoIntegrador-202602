package com.javastudio.repository;

import com.javastudio.domain.Usuario;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

public class UsuarioRepositoryArquivo implements UsuarioRepository {
    private final Path arquivo;
    private final List<Usuario> usuarios = new ArrayList<>();

    public UsuarioRepositoryArquivo(Path arquivo) {
        this.arquivo = arquivo;
        carregar();
    }

    @Override
    public synchronized Usuario salvar(Usuario usuario) {
        usuarios.removeIf(item -> item.getEmail().equals(usuario.getEmail()));
        usuarios.add(usuario);
        persistir();
        return usuario;
    }

    @Override
    public synchronized Optional<Usuario> buscarPorEmail(String email) {
        return usuarios.stream().filter(usuario -> usuario.getEmail().equals(email)).findFirst();
    }

    @Override
    public synchronized List<Usuario> listarTodos() { return List.copyOf(usuarios); }

    private void carregar() {
        if (!Files.exists(arquivo)) return;
        try {
            for (String linha : Files.readAllLines(arquivo, StandardCharsets.UTF_8)) {
                String[] campos = linha.split("\t", -1);
                if (campos.length == 2) usuarios.add(new Usuario(decodificar(campos[0]), decodificar(campos[1])));
            }
        } catch (IOException | RuntimeException exception) {
            throw new IllegalStateException("Nao foi possivel carregar os usuarios.", exception);
        }
    }

    private void persistir() {
        try {
            if (arquivo.getParent() != null) Files.createDirectories(arquivo.getParent());
            List<String> linhas = usuarios.stream().map(usuario -> codificar(usuario.getEmail()) + "\t" + codificar(usuario.getSenhaHash())).toList();
            Files.write(arquivo, linhas, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Nao foi possivel salvar os usuarios.", exception);
        }
    }

    private String codificar(String valor) { return Base64.getEncoder().encodeToString(valor.getBytes(StandardCharsets.UTF_8)); }
    private String decodificar(String valor) { return new String(Base64.getDecoder().decode(valor), StandardCharsets.UTF_8); }
}