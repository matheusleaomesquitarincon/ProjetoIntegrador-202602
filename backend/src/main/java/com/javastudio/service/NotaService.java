package com.javastudio.service;

import com.javastudio.domain.Nota;
import com.javastudio.repository.NotaRepository;
import java.util.List;

public class NotaService {
    private final NotaRepository repository;

    public NotaService(NotaRepository repository) {
        this.repository = repository;
    }

    public Nota criar(long id, String titulo, String conteudo) {
        return repository.salvar(new Nota(id, titulo, conteudo));
    }

    public Nota atualizar(long id, String titulo, String conteudo) {
        Nota nota = repository.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Nota nao encontrada."));
        nota.atualizar(titulo, conteudo);
        return repository.salvar(nota);
    }

    public List<Nota> listar() {
        return repository.listarTodas();
    }
}