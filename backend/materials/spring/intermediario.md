# Nível Intermediário

## Introdução

Uma aplicação Spring Boot real vai muito além de endpoints simples: envolve **injeção de dependência** organizada em camadas, **persistência de dados** com Spring Data JPA, e **validação** de entrada de dados. Este nível cobre a arquitetura típica de uma API REST profissional.

## Injeção de Dependência (IoC Container)

O princípio central do Spring é a **Inversão de Controle**: em vez de uma classe criar suas próprias dependências (`new MinhaClasse()`), ela **declara** o que precisa, e o *container* do Spring (o "IoC Container") se encarrega de criar e "injetar" essas dependências automaticamente.

Classes gerenciadas pelo Spring são chamadas de **beans**. As principais anotações de estereótipo para marcar uma classe como bean são:

- `@Component`: anotação genérica para qualquer bean gerenciado.
- `@Service`: usada em classes da camada de lógica de negócio.
- `@Repository`: usada em classes de acesso a dados; também traduz exceções específicas de persistência em exceções do Spring.
- `@Controller`/`@RestController`: usadas em classes que expõem endpoints web.

```java
@Service
public class ProdutoService {

    private final ProdutoRepository repository; // dependência declarada

    // Injeção via construtor — abordagem recomendada
    public ProdutoService(ProdutoRepository repository) {
        this.repository = repository;
    }

    public Produto buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));
    }
}
```

### Injeção via construtor vs @Autowired em campo

```java
// Forma recomendada: injeção via construtor (imutável, testável, dependências explícitas)
@Service
public class PedidoService {
    private final ProdutoService produtoService;

    public PedidoService(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }
}

// Forma desencorajada: injeção direta no campo
@Service
public class PedidoServiceAntigo {
    @Autowired
    private ProdutoService produtoService; // dificulta testes unitários e esconde dependências
}
```

A injeção via construtor é preferida porque: (1) torna a classe **imutável** (campo `final`), (2) deixa explícitas todas as dependências na assinatura do construtor, e (3) facilita testes unitários, já que dependências podem ser passadas manualmente (ou via mocks) sem precisar do container Spring.

## Arquitetura em camadas

A organização mais comum em aplicações Spring Boot segue o padrão de três camadas:

```
Controller  →  Service  →  Repository  →  Banco de dados
(HTTP/REST)   (regras de    (acesso a
              negócio)       dados)
```

- **Controller**: recebe requisições HTTP, delega para o Service, retorna respostas.
- **Service**: contém a lógica de negócio, orquestra chamadas a um ou mais repositórios.
- **Repository**: responsável exclusivamente pelo acesso a dados (banco de dados, APIs externas, etc.).

## Spring Data JPA

O **Spring Data JPA** elimina a necessidade de escrever manualmente consultas SQL básicas (CRUD), gerando implementações automaticamente a partir de interfaces.

### Definindo uma entidade

```java
import jakarta.persistence.*;

@Entity
@Table(name = "produtos")
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    private double preco;

    // Construtor padrão exigido pelo JPA
    public Produto() {}

    public Produto(String nome, double preco) {
        this.nome = nome;
        this.preco = preco;
    }

    // getters e setters
    public Long getId() { return id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public double getPreco() { return preco; }
    public void setPreco(double preco) { this.preco = preco; }
}
```

- `@Entity`: marca a classe como uma entidade JPA, mapeada para uma tabela.
- `@Id`: define o campo como chave primária.
- `@GeneratedValue(strategy = GenerationType.IDENTITY)`: delega ao banco a geração automática do ID (auto-incremento).
- `@Column`: customiza detalhes da coluna (nome, restrições).

### Definindo o repositório

```java
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    // Métodos de CRUD básico (save, findById, findAll, deleteById...) já vêm prontos!

    // Query methods: o Spring Data gera a query automaticamente a partir do nome do método
    List<Produto> findByNomeContaining(String trecho);
    List<Produto> findByPrecoGreaterThan(double valorMinimo);
}
```

Basta declarar a interface estendendo `JpaRepository<Entidade, TipoDaChave>` — o Spring Data JPA gera a implementação em tempo de execução, incluindo métodos derivados do nome (*query methods*) como `findByNomeContaining`, que o framework traduz automaticamente para uma consulta SQL equivalente a `WHERE nome LIKE %trecho%`.

### Juntando tudo: Controller + Service + Repository

```java
@RestController
@RequestMapping("/produtos")
public class ProdutoController {

    private final ProdutoService service;

    public ProdutoController(ProdutoService service) {
        this.service = service;
    }

    @GetMapping
    public List<Produto> listarTodos() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    public Produto buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    public Produto criar(@RequestBody Produto produto) {
        return service.salvar(produto);
    }
}

@Service
class ProdutoService {
    private final ProdutoRepository repository;

    ProdutoService(ProdutoRepository repository) {
        this.repository = repository;
    }

    List<Produto> listarTodos() {
        return repository.findAll();
    }

    Produto buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado: " + id));
    }

    Produto salvar(Produto produto) {
        return repository.save(produto);
    }
}
```

`@RequestBody` indica que o corpo da requisição HTTP (geralmente JSON) deve ser convertido automaticamente para um objeto `Produto` — o Spring usa a biblioteca Jackson internamente para essa conversão.

## Validação de dados de entrada

Validar dados recebidos de clientes externos é essencial. O Spring integra-se facilmente com a especificação **Bean Validation** (`jakarta.validation`):

```java
import jakarta.validation.constraints.*;

@Entity
public class Produto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome é obrigatório")
    @Size(min = 3, max = 100, message = "O nome deve ter entre 3 e 100 caracteres")
    private String nome;

    @Positive(message = "O preço deve ser maior que zero")
    private double preco;

    // getters e setters
}
```

```java
@PostMapping
public Produto criar(@Valid @RequestBody Produto produto) {
    return service.salvar(produto);
}
```

A anotação `@Valid` ativa a validação das anotações definidas na entidade (`@NotBlank`, `@Size`, `@Positive`, etc.) antes de o método ser executado. Se a validação falhar, o Spring automaticamente retorna uma resposta HTTP 400 (Bad Request) com detalhes do erro — sem que seja necessário escrever essa lógica manualmente.

Outras anotações comuns de validação: `@NotNull`, `@Email`, `@Min`/`@Max`, `@Past`/`@Future` (para datas).

## Resumo do nível intermediário

- Spring gerencia dependências através de um IoC Container; classes gerenciadas são "beans" (`@Component`, `@Service`, `@Repository`, `@RestController`).
- Injeção via construtor é a prática recomendada, favorecendo imutabilidade e testabilidade.
- A arquitetura em camadas (Controller → Service → Repository) separa responsabilidades de forma clara.
- Spring Data JPA elimina boilerplate de CRUD através de interfaces `JpaRepository`, incluindo *query methods* derivados do nome do método.
- `@Valid` com anotações de `jakarta.validation` automatiza a validação de dados de entrada.

## Leituras complementares

- Baeldung — "Spring Data JPA Tutorial", "Spring Boot Validation", "Constructor vs Field Injection".
- Documentação oficial: *Spring Data JPA — Reference Documentation*.
- Documentação oficial: *Jakarta Bean Validation Specification*.

---

