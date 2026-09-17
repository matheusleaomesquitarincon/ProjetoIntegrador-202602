package com.javastudio.repository;

import com.javastudio.domain.Nota;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class NotaRepositoryMemoria implements NotaRepository {
    private final List<Nota> notas = new ArrayList<>();

    @Override
    public Nota salvar(Nota nota) {
        notas.removeIf(item -> item.getId() == nota.getId());
        notas.add(nota);
        return nota;
    }

    @Override
    public Optional<Nota> buscarPorId(long id) {
        return notas.stream().filter(nota -> nota.getId() == id).findFirst();
    }

    @Override
    public List<Nota> listarTodas() {
        return List.copyOf(notas);
    }
}