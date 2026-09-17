package com.javastudio.repository;

import com.javastudio.domain.Nota;
import java.util.List;
import java.util.Optional;

public interface NotaRepository {
    Nota salvar(Nota nota);
    Optional<Nota> buscarPorId(long id);
    List<Nota> listarTodas();
}