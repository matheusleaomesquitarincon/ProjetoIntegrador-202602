package com.javastudio.repository;

import com.javastudio.domain.ResultadoQuiz;
import java.util.List;

public interface QuizResultadoRepository {
    ResultadoQuiz salvar(ResultadoQuiz resultado);
    List<ResultadoQuiz> listarPorUsuario(String email);
}
