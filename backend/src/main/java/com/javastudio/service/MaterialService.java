package com.javastudio.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

public class MaterialService {
    private static final Map<String, String> DIFICULDADES = Map.of(
            "iniciante", "iniciante.md",
            "intermediario", "intermediario.md",
            "avancado", "avancado.md"
    );
    private static final Set<String> TOPICOS = Set.of("fundamentos", "orientacao-a-objetos", "collections", "excecoes", "spring");

    private final Path materiaisPath;

    public MaterialService(Path materiaisPath) {
        this.materiaisPath = materiaisPath;
    }

    public String buscar(String topico, String dificuldade) {
        String arquivo = DIFICULDADES.get(dificuldade);
        if (arquivo == null || !TOPICOS.contains(topico)) {
            throw new IllegalArgumentException("Material nao encontrado.");
        }
        try {
            return Files.readString(materiaisPath.resolve(topico).resolve(arquivo), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Nao foi possivel carregar o material.", exception);
        }
    }
}