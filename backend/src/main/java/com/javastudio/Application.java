package com.javastudio;

import com.javastudio.domain.Material;
import com.javastudio.domain.MaterialJava;
import com.javastudio.repository.NotaRepositoryMemoria;
import com.javastudio.service.NotaService;

public class Application {
    public static void main(String[] args) {
        NotaService notaService = new NotaService(new NotaRepositoryMemoria());
        notaService.criar(1, "JVM", "Anotar o funcionamento da JVM.");

        Material material = new MaterialJava("Fundamentos", "Iniciante", "Conteudo reservado.");
        System.out.println(material.getTopico() + ": " + material.gerarConteudo());
        System.out.println("Notas salvas: " + notaService.listar().size());
    }
}