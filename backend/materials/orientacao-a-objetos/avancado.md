# Nível Avançado

## Introdução

Este nível explora tópicos que separam código OO "funcional" de código OO **bem projetado**: o princípio de preferir composição à herança, contratos formais como `equals`/`hashCode`, ordenação customizada, e os recursos mais modernos da linguagem (records, sealed classes) que mudam a forma como certos problemas de modelagem são resolvidos hoje em dia.

## Composição sobre herança

Herança cria um acoplamento forte: mudanças na superclasse podem quebrar subclasses de forma inesperada (o chamado *fragile base class problem*). Uma alternativa frequentemente mais flexível é **composição**: uma classe contém uma referência a outra e delega comportamento a ela, em vez de herdar.

```java
// Abordagem por herança (menos flexível)
class Pato extends Ave {
    // se Ave tiver um método voar() que não faz sentido para todo Pato...
}

// Abordagem por composição (mais flexível)
interface ComportamentoVoo {
    void voar();
}

class VooComAsas implements ComportamentoVoo {
    public void voar() { System.out.println("Voando com asas!"); }
}

class NaoVoa implements ComportamentoVoo {
    public void voar() { System.out.println("Este animal não voa."); }
}

class Pato {
    private ComportamentoVoo comportamentoVoo;

    public Pato(ComportamentoVoo comportamentoVoo) {
        this.comportamentoVoo = comportamentoVoo;
    }

    public void voar() {
        comportamentoVoo.voar(); // delega a responsabilidade
    }
}
```

```java
Pato patoNormal = new Pato(new VooComAsas());
Pato patoDeBorracha = new Pato(new NaoVoa());
```

A vantagem: o comportamento de voo pode ser **trocado em tempo de execução** e reutilizado por qualquer outra classe, sem duplicar código e sem hierarquias rígidas. Este é o princípio por trás de vários *design patterns* (Strategy, Decorator, entre outros).

**Regra prática (do livro *Effective Java*)**: "Favor composition over inheritance" — herança deve ser reservada para relações genuínas de "é um" (*is-a*), onde a subclasse realmente é um caso especial da superclasse e não vai quebrar o contrato dela (Princípio de Substituição de Liskov).

## O contrato `equals()` e `hashCode()`

Por padrão, `equals()` herdado de `Object` compara **referências** (equivalente a `==`). Para comparar objetos por seu conteúdo lógico, é preciso sobrescrever `equals()` — e, sempre que isso for feito, `hashCode()` também deve ser sobrescrito de forma consistente.

```java
public class Ponto {
    private final int x, y;

    public Ponto(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Ponto outro = (Ponto) obj;
        return x == outro.x && y == outro.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
```

**O contrato entre `equals` e `hashCode`**: se `a.equals(b)` retorna `true`, então `a.hashCode() == b.hashCode()` **obrigatoriamente** deve ser verdadeiro. O inverso não é exigido (dois objetos podem ter o mesmo hash sem serem iguais — uma colisão de hash).

Por que isso importa na prática: estruturas como `HashMap` e `HashSet` usam `hashCode()` para localizar o "bucket" onde um objeto deve estar, e depois usam `equals()` para confirmar a identidade dentro daquele bucket. Se você sobrescrever apenas `equals()` sem `hashCode()`, dois objetos "iguais" podem cair em buckets diferentes e o `HashSet` vai tratá-los (incorretamente) como distintos.

```java
Set<Ponto> pontos = new HashSet<>();
pontos.add(new Ponto(1, 2));
System.out.println(pontos.contains(new Ponto(1, 2))); // true, SE equals/hashCode implementados corretamente
```

## `Comparable` e `Comparator`

Para permitir que objetos sejam ordenados (por exemplo, com `Collections.sort()`), Java oferece duas abordagens:

### Comparable — ordenação natural, definida na própria classe

```java
class Produto implements Comparable<Produto> {
    private String nome;
    private double preco;

    public Produto(String nome, double preco) {
        this.nome = nome;
        this.preco = preco;
    }

    @Override
    public int compareTo(Produto outro) {
        return Double.compare(this.preco, outro.preco); // ordena por preço, crescente
    }

    public String getNome() { return nome; }
    public double getPreco() { return preco; }
}
```

```java
List<Produto> produtos = new ArrayList<>(List.of(
    new Produto("Mouse", 80.0),
    new Produto("Notebook", 3500.0),
    new Produto("Teclado", 150.0)
));

Collections.sort(produtos); // usa compareTo() definido na classe
```

### Comparator — ordenação externa, flexível, múltiplas estratégias

```java
Comparator<Produto> porNome = Comparator.comparing(Produto::getNome);
Comparator<Produto> porPrecoDecrescente = Comparator.comparing(Produto::getPreco).reversed();

produtos.sort(porNome);
produtos.sort(porPrecoDecrescente);

// Encadeando critérios: por preço, e em empate, por nome
produtos.sort(Comparator.comparing(Produto::getPreco).thenComparing(Produto::getNome));
```

Use `Comparable` quando existe **uma** ordenação natural e óbvia para o tipo (ex: números por valor, datas cronologicamente). Use `Comparator` quando existem **múltiplas formas válidas** de ordenar o mesmo tipo, dependendo do contexto de uso.

## Records (Java 16+)

*Records* são um recurso que elimina o boilerplate de classes que servem apenas para carregar dados imutáveis (DTOs, *value objects*). O compilador gera automaticamente construtor, getters (sem prefixo `get`), `equals()`, `hashCode()` e `toString()`.

```java
record Ponto(int x, int y) {}

// Equivalente, escrito manualmente, exigiria ~40 linhas:
// construtor, x(), y(), equals(), hashCode(), toString()
```

```java
Ponto p1 = new Ponto(1, 2);
Ponto p2 = new Ponto(1, 2);

System.out.println(p1.x());          // 1 (getter sem prefixo "get")
System.out.println(p1.equals(p2));   // true — equals gerado automaticamente
System.out.println(p1);              // Ponto[x=1, y=2] — toString gerado
```

Records também suportam validação no construtor (*compact constructor*):

```java
record FaixaEtaria(int min, int max) {
    FaixaEtaria {
        if (min > max) {
            throw new IllegalArgumentException("min não pode ser maior que max");
        }
    }
}
```

Records são implicitamente `final` (não podem ser estendidos) e todos os seus campos são implicitamente `final` — reforçando o uso para modelagem de dados imutáveis.

## Sealed classes (Java 17+)

*Sealed classes* restringem quais classes podem estender ou implementar um tipo, dando mais controle sobre hierarquias fechadas — útil para modelar um conjunto finito e conhecido de variações (parecido com *enums* mais ricos).

```java
sealed interface Forma permits Circulo, Quadrado, Triangulo {}

record Circulo(double raio) implements Forma {}
record Quadrado(double lado) implements Forma {}
record Triangulo(double base, double altura) implements Forma {}
```

```java
static double calcularArea(Forma forma) {
    return switch (forma) {
        case Circulo c -> Math.PI * c.raio() * c.raio();
        case Quadrado q -> q.lado() * q.lado();
        case Triangulo t -> (t.base() * t.altura()) / 2;
    };
}
```

Como o compilador sabe que `Forma` só pode ser `Circulo`, `Quadrado` ou `Triangulo`, o `switch` acima pode dispensar uma cláusula `default` — o compilador garante exaustividade em tempo de compilação, algo que interfaces comuns não permitem.

## Design Patterns baseados em OO

Alguns padrões de projeto clássicos (do livro *Design Patterns* da "Gang of Four") aparecem constantemente em código Java profissional:

**Strategy** — encapsula algoritmos intercambiáveis, geralmente via interface funcional:

```java
interface DescontoStrategy {
    double aplicar(double valor);
}

class Pedido {
    private double valor;
    private DescontoStrategy desconto;

    public Pedido(double valor, DescontoStrategy desconto) {
        this.valor = valor;
        this.desconto = desconto;
    }

    public double total() {
        return desconto.aplicar(valor);
    }
}

Pedido pedidoComDesconto = new Pedido(100.0, v -> v * 0.9); // lambda como estratégia
```

**Factory Method** — centraliza a lógica de criação de objetos, escondendo detalhes de instanciação:

```java
interface Notificacao {
    void enviar(String mensagem);
}

class NotificacaoEmail implements Notificacao {
    public void enviar(String mensagem) { System.out.println("Email: " + mensagem); }
}

class NotificacaoSMS implements Notificacao {
    public void enviar(String mensagem) { System.out.println("SMS: " + mensagem); }
}

class NotificacaoFactory {
    static Notificacao criar(String tipo) {
        return switch (tipo) {
            case "email" -> new NotificacaoEmail();
            case "sms" -> new NotificacaoSMS();
            default -> throw new IllegalArgumentException("Tipo desconhecido: " + tipo);
        };
    }
}
```

**Observer** — permite que objetos sejam notificados automaticamente de mudanças em outro objeto (base de sistemas de eventos e frameworks como Spring):

```java
interface Observador {
    void notificar(String evento);
}

class GerenciadorDeEventos {
    private List<Observador> observadores = new ArrayList<>();

    void inscrever(Observador o) {
        observadores.add(o);
    }

    void dispararEvento(String evento) {
        for (Observador o : observadores) {
            o.notificar(evento);
        }
    }
}
```

## Resumo do nível avançado

- Composição costuma ser mais flexível que herança, evitando acoplamento rígido entre classes.
- `equals()` e `hashCode()` precisam ser sobrescritos juntos e de forma consistente para funcionar corretamente com `HashMap`/`HashSet`.
- `Comparable` define ordenação natural única; `Comparator` permite múltiplas estratégias de ordenação externas.
- Records eliminam boilerplate para tipos de dados imutáveis; sealed classes restringem hierarquias a um conjunto finito e conhecido de subtipos.
- Design patterns como Strategy, Factory e Observer são aplicações práticas e recorrentes dos pilares de OO.

## Leituras complementares

- Livro: *Effective Java*, Joshua Bloch — itens sobre `equals`/`hashCode`, composição vs herança, e uso de interfaces.
- Baeldung — "Guide to Java Records", "Sealed Classes and Interfaces in Java", "Design Patterns in Java".
- Livro: *Head First Design Patterns* (capítulos completos sobre Strategy, Factory e Observer).
- Oracle Docs — *Record Classes*, *Sealed Classes*.

