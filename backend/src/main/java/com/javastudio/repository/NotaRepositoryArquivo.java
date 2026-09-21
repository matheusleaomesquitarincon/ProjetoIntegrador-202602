package com.javastudio.repository;

import com.javastudio.domain.Nota;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

public class NotaRepositoryArquivo implements NotaRepository {
    private final Path arquivo;
    private final List<Nota> notas = new ArrayList<>();

    public NotaRepositoryArquivo(Path arquivo) {
        this.arquivo = arquivo;
        carregar();
    }

    @Override
    public synchronized Nota salvar(Nota nota) {
        notas.removeIf(item -> item.getId() == nota.getId());
        notas.add(nota);
        persistir();
        return nota;
    }

    @Override
    public synchronized Optional<Nota> buscarPorId(long id) {
        return notas.stream().filter(nota -> nota.getId() == id).findFirst();
    }

    @Override
    public synchronized List<Nota> listarTodas() {
        return List.copyOf(notas);
    }

    @Override
    public synchronized void apagar(long id) {
        notas.removeIf(nota -> nota.getId() == id);
        persistir();
    }

    private void carregar() {
        if (!Files.exists(arquivo)) return;
        try {
            for (String linha : Files.readAllLines(arquivo, StandardCharsets.UTF_8)) {
                String[] campos = linha.split("\\t", -1);
                if (campos.length == 3) {
                    notas.add(new Nota(Long.parseLong(campos[0]), decodificar(campos[1]), decodificar(campos[2])));
                }
            }
        } catch (IOException | RuntimeException exception) {
            throw new IllegalStateException("Nao foi possivel carregar as anotacoes.", exception);
        }
    }

    private void persistir() {
        try {
            if (arquivo.getParent() != null) Files.createDirectories(arquivo.getParent());
            List<String> linhas = notas.stream()
                    .map(nota -> nota.getId() + "\t" + codificar(nota.getTitulo()) + "\t" + codificar(nota.getConteudo()))
                    .toList();
            Files.write(arquivo, linhas, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Nao foi possivel salvar as anotacoes.", exception);
        }
    }

    private String codificar(String valor) { return Base64.getEncoder().encodeToString(valor.getBytes(StandardCharsets.UTF_8)); }
    private String decodificar(String valor) { return new String(Base64.getDecoder().decode(valor), StandardCharsets.UTF_8); }
}