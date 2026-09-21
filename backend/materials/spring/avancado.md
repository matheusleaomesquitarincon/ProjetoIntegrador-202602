# Nível Avançado

## Introdução

Este nível cobre tópicos essenciais para aplicações Spring Boot em produção: tratamento **centralizado** de erros, fundamentos de **segurança** com Spring Security, **programação reativa** com Spring WebFlux para cenários de alta concorrência, e **observabilidade** (métricas, health checks, tracing) — pilares de uma API robusta e pronta para ambientes corporativos de larga escala.

## Tratamento global de exceções

Em vez de espalhar `try/catch` por todos os controllers, o Spring permite centralizar o tratamento de exceções usando `@RestControllerAdvice` combinado com `@ExceptionHandler`.

```java
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProdutoNaoEncontradoException.class)
    public ResponseEntity<ErroResposta> tratarNaoEncontrado(ProdutoNaoEncontradoException ex) {
        ErroResposta erro = new ErroResposta(ex.getMessage(), HttpStatus.NOT_FOUND.value());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erro);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResposta> tratarValidacao(MethodArgumentNotValidException ex) {
        String mensagem = ex.getBindingResult().getFieldErrors().stream()
                .map(erro -> erro.getField() + ": " + erro.getDefaultMessage())
                .reduce("", (a, b) -> a + "; " + b);
        ErroResposta erro = new ErroResposta(mensagem, HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.badRequest().body(erro);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResposta> tratarErroGenerico(Exception ex) {
        ErroResposta erro = new ErroResposta("Erro interno inesperado", 500);
        return ResponseEntity.internalServerError().body(erro);
    }
}

record ErroResposta(String mensagem, int status) {}
```

Essa abordagem garante que **toda** exceção lançada em qualquer controller da aplicação seja capturada e transformada em uma resposta HTTP padronizada e consistente, sem duplicar lógica de tratamento em cada endpoint individualmente.

## Fundamentos de Spring Security

**Spring Security** é o módulo responsável por autenticação e autorização. Uma configuração básica, moderna (baseada em componentes, sem XML) para proteger endpoints:

```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/publico/**").permitAll()   // acesso livre
                .requestMatchers("/admin/**").hasRole("ADMIN") // exige papel ADMIN
                .anyRequest().authenticated()                  // demais rotas exigem login
            )
            .csrf(csrf -> csrf.disable()); // comum desabilitar CSRF em APIs stateless com JWT

        return http.build();
    }
}
```

### Autenticação com JWT (JSON Web Token)

Em APIs REST *stateless*, o padrão mais comum é autenticação via **JWT**: o cliente faz login uma vez, recebe um token assinado, e passa a enviá-lo em cada requisição subsequente (geralmente no header `Authorization: Bearer <token>`), sem que o servidor precise manter sessão.

```java
// Exemplo conceitual simplificado de um filtro de autenticação JWT
public class JwtAuthFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            if (jwtService.validarToken(token)) {
                // extrai informações do usuário do token e popula o SecurityContext
                Authentication auth = jwtService.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        filterChain.doFilter(request, response); // continua a cadeia de filtros
    }
}
```

Esse filtro é registrado na cadeia de segurança do Spring, sendo executado antes que a requisição alcance o controller — garantindo que apenas requisições com token válido sejam autenticadas.

## Programação reativa com Spring WebFlux

O modelo tradicional do Spring MVC é **bloqueante**: cada requisição ocupa uma thread até a resposta ser gerada. Em cenários de altíssima concorrência com muitas operações de I/O (chamadas a bancos de dados, APIs externas), isso pode esgotar o pool de threads disponível.

**Spring WebFlux** oferece um modelo **reativo e não bloqueante**, baseado na especificação *Reactive Streams*, usando os tipos `Mono` (0 ou 1 elemento) e `Flux` (0 a N elementos):

```java
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/produtos-reativos")
public class ProdutoReativoController {

    private final ProdutoReativoRepository repository;

    public ProdutoReativoController(ProdutoReativoRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public Flux<Produto> listarTodos() {
        return repository.findAll(); // retorna um fluxo assíncrono de produtos
    }

    @GetMapping("/{id}")
    public Mono<Produto> buscarPorId(@PathVariable Long id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new ProdutoNaoEncontradoException(id)));
    }

    @PostMapping
    public Mono<Produto> criar(@Valid @RequestBody Mono<Produto> produtoMono) {
        return produtoMono.flatMap(repository::save);
    }
}
```

Nesse modelo, as threads não ficam bloqueadas esperando operações de I/O — em vez disso, o processamento continua de forma assíncrona e a thread é liberada para atender outras requisições enquanto aguarda o resultado (por exemplo, de uma consulta ao banco). Isso permite atender um número muito maior de requisições simultâneas com um número menor de threads, comparado ao modelo tradicional bloqueante do Spring MVC.

**Quando usar WebFlux**: cenários de alta concorrência com muitas chamadas de I/O (microsserviços que fazem muitas chamadas a outros serviços, streaming de dados). **Quando manter Spring MVC**: a maioria das aplicações CRUD tradicionais, onde a complexidade adicional do modelo reativo não traz benefício proporcional — WebFlux exige repensar toda a cadeia (incluindo o driver do banco de dados, que precisa ser reativo, como o R2DBC em vez do JDBC tradicional).

## Observabilidade: Actuator e métricas

**Spring Boot Actuator** expõe endpoints prontos para monitoramento e gerenciamento da aplicação em produção:

```properties
# application.properties
management.endpoints.web.exposure.include=health,info,metrics,prometheus
```

Endpoints comuns disponibilizados automaticamente:

- `/actuator/health`: indica se a aplicação (e suas dependências, como banco de dados) está saudável.
- `/actuator/metrics`: métricas variadas (uso de memória, tempo de resposta de endpoints, etc.).
- `/actuator/info`: informações customizáveis sobre a build da aplicação.

### Micrometer

O **Micrometer** é a biblioteca usada internamente pelo Spring Boot para coletar métricas, funcionando como uma fachada que pode exportar dados para diferentes sistemas de monitoramento (Prometheus, Datadog, New Relic, etc.):

```java
import io.micrometer.core.instrument.MeterRegistry;

@Service
public class PedidoService {
    private final MeterRegistry meterRegistry;

    public PedidoService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void processarPedido() {
        meterRegistry.counter("pedidos.processados").increment();
        // métrica customizada, visível em /actuator/metrics/pedidos.processados
        // e exportável para Prometheus/Grafana, por exemplo
    }
}
```

### Tracing distribuído

Em arquiteturas de microsserviços, uma única requisição de usuário pode atravessar vários serviços diferentes. O **tracing distribuído** (com ferramentas como Zipkin ou Jaeger, integradas via Micrometer Tracing) permite rastrear o caminho completo de uma requisição através de múltiplos serviços, correlacionando logs e medindo latência em cada etapa — essencial para diagnosticar problemas de performance em sistemas distribuídos complexos.

## Resumo do nível avançado

- `@RestControllerAdvice` + `@ExceptionHandler` centralizam o tratamento de erros, padronizando respostas HTTP em toda a aplicação.
- Spring Security protege endpoints via configuração declarativa; JWT é o padrão comum para autenticação stateless em APIs REST.
- Spring WebFlux oferece um modelo reativo não bloqueante (`Mono`/`Flux`), vantajoso em cenários de alta concorrência com muito I/O, ao custo de maior complexidade.
- Spring Boot Actuator expõe endpoints de saúde e métricas prontos para monitoramento em produção.
- Micrometer coleta métricas customizadas exportáveis para ferramentas como Prometheus; tracing distribuído correlaciona requisições através de múltiplos microsserviços.

## Leituras complementares

- Documentação oficial: *Spring Security Reference*, *Spring WebFlux Reference*, *Spring Boot Actuator*.
- Baeldung — "Exception Handling in Spring", "Spring Boot Security", "Spring WebFlux Guide", "Getting Started with Micrometer".
- Artigo: "Distributed Tracing with Spring Boot and Zipkin" (Baeldung).
- Livro: *Cloud Native Spring in Action*, Thomas Vitale (Manning).

