package com.javastudio;

import com.javastudio.api.NotaHttpServer;
import com.javastudio.domain.Material;
import com.javastudio.domain.MaterialJava;
import com.javastudio.repository.NotaRepositoryArquivo;
import com.javastudio.service.NotaService;
import java.nio.file.Path;

public class Application {
    public static void main(String[] args) {
        NotaService notaService = new NotaService(new NotaRepositoryArquivo(Path.of("data", "notas.db")));
        Material material = new MaterialJava("Fundamentos", "Iniciante", "Conteudo reservado.");
        System.out.println(material.getTopico() + ": " + material.gerarConteudo());
        try {
            new NotaHttpServer(notaService, 8080).iniciar();
        } catch (Exception exception) {
            throw new IllegalStateException("Nao foi possivel iniciar o backend.", exception);
        }
    }
}