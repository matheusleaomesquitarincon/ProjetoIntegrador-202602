package com.javastudio;

import com.javastudio.api.AuthHttpServer;
import com.javastudio.api.NotaHttpServer;
import com.javastudio.domain.Material;
import com.javastudio.domain.MaterialJava;
import com.javastudio.repository.NotaRepositoryArquivo;
import com.javastudio.repository.UsuarioRepositoryArquivo;
import com.javastudio.service.AuthService;
import com.javastudio.service.MaterialService;
import com.javastudio.service.NotaService;
import com.javastudio.service.SenhaService;
import com.javastudio.service.SessaoManager;
import java.nio.file.Path;

public class Application {
    public static void main(String[] args) {
        NotaService notaService = new NotaService(new NotaRepositoryArquivo(Path.of("data", "notas.db")));
        SessaoManager sessoes = new SessaoManager();
        Material material = new MaterialJava("Fundamentos", "Iniciante", "Conteudo reservado.");
        System.out.println(material.getTopico() + ": " + material.gerarConteudo());
        try {
            com.sun.net.httpserver.HttpServer server = com.sun.net.httpserver.HttpServer.create(new java.net.InetSocketAddress(8080), 0);
            new AuthHttpServer(server, new AuthService(new UsuarioRepositoryArquivo(Path.of("data", "usuarios.db")), new SenhaService()), sessoes);
            NotaHttpServer notaHttpServer = new NotaHttpServer(notaService, new MaterialService(Path.of("materials")), sessoes, 8080, server);
            com.javastudio.service.QuizService quizService = new com.javastudio.service.QuizService(
                    Path.of("quiz"), new com.javastudio.repository.QuizResultadoRepositoryArquivo(Path.of("data", "quiz_resultados.db")));
            new com.javastudio.api.QuizHttpServer(quizService, sessoes, server);
            notaHttpServer.iniciar();
        } catch (Exception exception) {
            throw new IllegalStateException("Nao foi possivel iniciar o backend.", exception);
        }
    }
}