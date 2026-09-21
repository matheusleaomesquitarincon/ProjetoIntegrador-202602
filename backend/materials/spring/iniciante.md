# Nível Iniciante

## Introdução

**Spring Framework** é um dos frameworks mais influentes do ecossistema Java, criado para simplificar o desenvolvimento de aplicações corporativas através de um conceito central: **Inversão de Controle (IoC)** com **Injeção de Dependência (DI)**. Apesar de poderoso, o Spring "puro" exigia muita configuração manual (arquivos XML extensos, configuração detalhada de servidores).

**Spring Boot**, lançado em 2014, resolve esse problema oferecendo **autoconfiguração**: com base nas bibliotecas presentes no projeto, o Spring Boot configura automaticamente a maior parte da infraestrutura necessária, permitindo criar uma aplicação funcional com o mínimo de configuração manual — incluindo um **servidor web embutido** (Tomcat, por padrão), eliminando a necessidade de instalar e configurar um servidor externo.

## Criando um projeto Spring Boot

A forma mais comum de iniciar um projeto é através do **Spring Initializr** (start.spring.io), uma ferramenta web que gera a estrutura básica do projeto com as dependências escolhidas (Web, JPA, Segurança, etc.), pronta para importar em uma IDE.

A estrutura básica de um projeto gerado inclui:

```
meu-projeto/
├── src/
│   ├── main/
│   │   ├── java/com/exemplo/meuprojeto/
│   │   │   └── MeuProjetoApplication.java  ← classe principal
│   │   └── resources/
│   │       └── application.properties       ← configurações
│   └── test/
├── pom.xml (Maven) ou build.gradle (Gradle)
```

## A classe principal e a anotação @SpringBootApplication

```java
package com.exemplo.meuprojeto;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MeuProjetoApplication {
    public static void main(String[] args) {
        SpringApplication.run(MeuProjetoApplication.class, args);
    }
}
```

A anotação `@SpringBootApplication` é, na verdade, uma combinação de três anotações:

- `@Configuration`: marca a classe como fonte de definições de beans (componentes gerenciados pelo Spring).
- `@EnableAutoConfiguration`: ativa a autoconfiguração baseada nas dependências do projeto.
- `@ComponentScan`: instrui o Spring a procurar automaticamente por outras classes anotadas (`@Component`, `@Service`, `@Controller`, etc.) no mesmo pacote e subpacotes.

Ao executar `SpringApplication.run(...)`, o Spring Boot sobe um **contexto de aplicação** completo, incluindo um servidor Tomcat embutido (se a dependência Web estiver presente), pronto para receber requisições HTTP — tudo isso sem nenhuma configuração adicional de servidor.

## Criando um endpoint REST simples

```java
package com.exemplo.meuprojeto;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SaudacaoController {

    @GetMapping("/ola")
    public String saudar() {
        return "Olá, mundo!";
    }

    @GetMapping("/ola/{nome}")
    public String saudarPorNome(@PathVariable String nome) {
        return "Olá, " + nome + "!";
    }
}
```

- `@RestController`: combina `@Controller` (marca a classe como um componente web) com `@ResponseBody` (indica que o retorno dos métodos deve ser escrito diretamente no corpo da resposta HTTP, geralmente como JSON, em vez de ser resolvido como um nome de página/view).
- `@GetMapping("/ola")`: mapeia requisições HTTP GET para a URL `/ola` a esse método.
- `@PathVariable`: extrai um valor diretamente da URL (por exemplo, `/ola/Maria` faria `nome` receber `"Maria"`).

Ao rodar a aplicação (por exemplo, via `mvn spring-boot:run` ou executando a classe principal na IDE) e acessar `http://localhost:8080/ola` no navegador, a resposta `Olá, mundo!` é exibida.

## Configuração via application.properties

O arquivo `application.properties` (ou `application.yml`, em formato YAML) centraliza configurações da aplicação:

```properties
server.port=8081
spring.application.name=meu-projeto

# Exemplo de configuração de banco de dados (será usado nos próximos níveis)
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.username=sa
```

Equivalente em YAML (`application.yml`):

```yaml
server:
  port: 8081
spring:
  application:
    name: meu-projeto
```

## Verbos HTTP e anotações de mapeamento

Além de `@GetMapping`, o Spring oferece anotações para os demais verbos HTTP:

```java
@RestController
@RequestMapping("/tarefas")
public class TarefaController {

    @GetMapping
    public String listar() {
        return "Lista de tarefas";
    }

    @PostMapping
    public String criar() {
        return "Tarefa criada";
    }

    @PutMapping("/{id}")
    public String atualizar(@PathVariable Long id) {
        return "Tarefa " + id + " atualizada";
    }

    @DeleteMapping("/{id}")
    public String remover(@PathVariable Long id) {
        return "Tarefa " + id + " removida";
    }
}
```

`@RequestMapping("/tarefas")` no nível da classe define um prefixo comum para todas as rotas do controller, evitando repetição.

## Resumo do nível iniciante

- Spring Boot simplifica o Spring Framework com autoconfiguração e servidor embutido, eliminando configuração manual extensa.
- `@SpringBootApplication` combina `@Configuration`, `@EnableAutoConfiguration` e `@ComponentScan`.
- `@RestController` + `@GetMapping`/`@PostMapping`/etc. criam endpoints REST de forma declarativa.
- `application.properties`/`application.yml` centralizam configurações da aplicação.
- `@PathVariable` extrai valores diretamente da URL da requisição.

## Leituras complementares

- Documentação oficial: *Spring Boot Reference Documentation* (docs.spring.io/spring-boot).
- Baeldung — "Spring Boot Tutorial - Bootstrap a Simple Application".
- Ferramenta: Spring Initializr (start.spring.io).

---

