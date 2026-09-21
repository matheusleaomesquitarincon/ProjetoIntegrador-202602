# Nível Avançado

## Introdução

Este nível aborda o impacto de exceções na **performance**, seu comportamento em cenários de **programação assíncrona/concorrente**, técnicas de **encadeamento** (*exception chaining*) para preservar contexto entre camadas de uma aplicação, e estratégias arquiteturais de tratamento centralizado usadas em APIs profissionais.

## O custo de performance de exceções

Toda instância de `Throwable` captura, no momento de sua criação, o **stack trace** completo da pilha de chamadas até aquele ponto — via método `fillInStackTrace()`. Essa operação não é gratuita: percorrer e registrar toda a pilha de execução tem custo de CPU perceptível quando exceções são lançadas com muita frequência (por exemplo, em loops de alta performance).

```java
public class CustoExcecaoDemo {
    public static void main(String[] args) {
        long inicio = System.nanoTime();
        for (int i = 0; i < 100_000; i++) {
            try {
                throw new RuntimeException("teste");
            } catch (RuntimeException e) {
                // captura o stack trace completo a cada iteração — custoso
            }
        }
        long fim = System.nanoTime();
        System.out.println("Tempo com stack trace: " + (fim - inicio) / 1_000_000 + "ms");
    }
}
```

Para exceções usadas em cenários de altíssima frequência (ex: validações internas de bibliotecas de baixa latência), é possível **sobrescrever `fillInStackTrace()`** para evitar essa captura, aceitando a perda de informação de depuração em troca de performance:

```java
class ExcecaoRapida extends RuntimeException {
    public ExcecaoRapida(String mensagem) {
        super(mensagem, null, false, false);
        // os dois últimos parâmetros do construtor de Throwable:
        // enableSuppression = false, writableStackTrace = false
    }
}
```

Essa é uma otimização de **último recurso**, usada em bibliotecas muito específicas (como algumas implementações internas de frameworks reativos) — na esmagadora maioria dos casos, o custo do stack trace é irrelevante perto do resto do processamento de uma aplicação.

## Exceções em código assíncrono

Em processamento assíncrono com `CompletableFuture`, exceções lançadas dentro de uma tarefa não se propagam da forma tradicional (`try/catch` síncrono) — elas ficam "encapsuladas" no resultado futuro e precisam ser tratadas com métodos específicos.

```java
import java.util.concurrent.*;

public class AssincronoDemo {
    public static void main(String[] args) {
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            if (Math.random() < 1) { // sempre true, só para forçar o exemplo
                throw new IllegalStateException("Falha ao calcular valor");
            }
            return 42;
        });

        // exceptionally: fornece um valor de fallback em caso de erro
        CompletableFuture<Integer> comFallback = future.exceptionally(ex -> {
            System.out.println("Erro capturado: " + ex.getCause().getMessage());
            return -1;
        });

        System.out.println(comFallback.join()); // -1

        // handle: trata sucesso e erro no mesmo lugar
        future.handle((resultado, erro) -> {
            if (erro != null) {
                System.out.println("Falhou: " + erro.getMessage());
                return -1;
            }
            return resultado;
        });
    }
}
```

Um detalhe importante: quando uma exceção "escapa" de dentro de um `supplyAsync`, ela é envolvida em um `CompletionException`, e a exceção original fica acessível via `getCause()`. Chamar `future.join()` sem tratamento propaga essa `CompletionException` (não checked, então não exige `try/catch` obrigatório, mas ainda pode quebrar a aplicação se não tratada).

Ao usar `join()` versus `get()`:
- `get()` lança `ExecutionException` (checked) — exige tratamento obrigatório ou `throws`.
- `join()` lança `CompletionException` (unchecked) — mais conveniente em contextos funcionais (streams, lambdas), mas sem forçar tratamento em compilação.

## Exception chaining (encadeamento de causas)

Quando uma exceção é capturada em uma camada e uma **nova** exceção é lançada em seu lugar (por exemplo, transformando uma `SQLException` de baixo nível em uma exceção de domínio mais significativa), é fundamental preservar a exceção original como **causa**, para não perder informação de depuração:

```java
class RepositorioException extends RuntimeException {
    public RepositorioException(String mensagem, Throwable causa) {
        super(mensagem, causa); // preserva a causa raiz
    }
}

class PedidoRepositorio {
    public void salvar(Object pedido) {
        try {
            executarSql(pedido);
        } catch (java.sql.SQLException e) {
            throw new RepositorioException("Falha ao salvar pedido no banco de dados", e);
        }
    }

    private void executarSql(Object pedido) throws java.sql.SQLException {
        throw new java.sql.SQLException("Timeout de conexão");
    }
}
```

```java
public class Main {
    public static void main(String[] args) {
        try {
            new PedidoRepositorio().salvar(new Object());
        } catch (RepositorioException e) {
            System.out.println("Erro: " + e.getMessage());
            System.out.println("Causa raiz: " + e.getCause().getMessage());
            // e.printStackTrace() mostraria AMBOS os stack traces encadeados,
            // com "Caused by:" indicando a exceção original
        }
    }
}
```

Isso é especialmente importante em arquiteturas em camadas (Controller → Service → Repository), onde um erro de baixo nível (banco de dados, rede, arquivo) precisa ser traduzido para uma exceção que faça sentido na camada de negócio, sem que a causa técnica original se perca.

## Tratamento centralizado em APIs (contexto Spring)

Em aplicações web modernas, é considerado boa prática **não espalhar** blocos `try/catch` por todos os controllers. Em vez disso, usa-se um manipulador global de exceções que intercepta erros lançados em qualquer parte da aplicação e os traduz em respostas HTTP apropriadas.

```java
// Exemplo conceitual com Spring (detalhado no material de Spring Boot)
@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(SaldoInsuficienteException.class)
    public ResponseEntity<String> tratarSaldoInsuficiente(SaldoInsuficienteException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> tratarErroGenerico(Exception e) {
        // fallback para qualquer erro não previsto explicitamente
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erro interno inesperado.");
    }
}
```

Essa abordagem centraliza a política de tratamento de erros em um único lugar, garante respostas consistentes para o cliente da API e mantém os controllers focados apenas na lógica de negócio, sem poluição de `try/catch` repetitivo.

## Considerações sobre logging de exceções

Em sistemas de produção, o tratamento de exceção quase sempre envolve **logging estruturado** — não apenas `e.printStackTrace()` (que escreve diretamente no console/erro padrão, sem controle de nível, formato ou destino):

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class PedidoService {
    private static final Logger logger = LoggerFactory.getLogger(PedidoService.class);

    public void processar(Long pedidoId) {
        try {
            // lógica de processamento
        } catch (Exception e) {
            // Passar a exceção como argumento registra o stack trace completo,
            // além da mensagem — prática recomendada com SLF4J/Logback
            logger.error("Falha ao processar pedido {}", pedidoId, e);
            throw e; // ou envolver em uma exceção de domínio, conforme o caso
        }
    }
}
```

Frameworks de logging como **SLF4J** (fachada) com **Logback** ou **Log4j2** (implementações) são o padrão de mercado em aplicações Java corporativas, permitindo configurar níveis (`DEBUG`, `INFO`, `WARN`, `ERROR`), formatos e destinos (arquivo, console, sistemas de agregação de logs) sem alterar código.

## Resumo do nível avançado

- Criar uma exceção captura o stack trace completo, o que tem custo de performance mensurável em cenários de altíssima frequência.
- Exceções em código assíncrono (`CompletableFuture`) não seguem o fluxo tradicional de `try/catch`; usam `exceptionally`, `handle` e ficam envolvidas em `CompletionException`/`ExecutionException`.
- *Exception chaining* (`super(mensagem, causa)`) preserva a causa raiz ao traduzir exceções entre camadas, essencial para depuração em arquiteturas em camadas.
- Tratamento centralizado (ex: `@RestControllerAdvice` no Spring) evita duplicação de `try/catch` e padroniza respostas de erro em APIs.
- Logging estruturado com SLF4J/Logback é a prática padrão em produção, superior a `printStackTrace()`.

## Leituras complementares

- Baeldung — "Exception Handling in Java CompletableFuture", "Guide to Custom Exception Chaining", "Global Exception Handler in Spring Boot".
- Livro: *Effective Java*, Joshua Bloch — itens sobre exceções e desempenho.
- Livro: *Java Concurrency in Practice*, Brian Goetz — tratamento de erros em contextos concorrentes.
- Documentação oficial: SLF4J (slf4j.org) e Logback (logback.qos.ch).

