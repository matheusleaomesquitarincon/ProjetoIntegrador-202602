# Nível Intermediário

## Introdução

Com a base de classes e encapsulamento consolidada, este nível avança para os dois pilares restantes da OO — **herança** e **polimorfismo** — além de discutir interfaces, classes abstratas e quando usar cada recurso.

## Herança

Herança permite que uma classe (subclasse/filha) reaproveite atributos e métodos de outra (superclasse/pai), usando a palavra-chave `extends`.

```java
class Animal {
    protected String nome;

    public Animal(String nome) {
        this.nome = nome;
    }

    public void emitirSom() {
        System.out.println(nome + " faz um som genérico.");
    }
}

class Cachorro extends Animal {
    public Cachorro(String nome) {
        super(nome); // chama o construtor da superclasse
    }

    @Override
    public void emitirSom() {
        System.out.println(nome + " late: Au au!");
    }
}
```

```java
public class Main {
    public static void main(String[] args) {
        Animal genericoAnimal = new Animal("Bicho");
        genericoAnimal.emitirSom(); // Bicho faz um som genérico.

        Cachorro rex = new Cachorro("Rex");
        rex.emitirSom(); // Rex late: Au au!
    }
}
```

A palavra-chave `super` é usada para:

1. Chamar o construtor da superclasse (`super(nome)`), sempre como primeira linha do construtor da subclasse.
2. Chamar uma versão específica de um método sobrescrito: `super.emitirSom()`.

Em Java, uma classe só pode estender **uma única** superclasse (herança simples de classes — diferente de C++). Isso evita o "problema do diamante" mas limita reaproveitamento apenas por herança; para isso, interfaces ajudam.

## Polimorfismo

Polimorfismo significa "muitas formas" — a capacidade de tratar objetos de tipos diferentes através de uma referência comum (geralmente da superclasse ou interface), com cada tipo concreto respondendo de forma diferente à mesma chamada de método.

```java
public class Main {
    public static void main(String[] args) {
        Animal[] animais = {
            new Animal("Genérico"),
            new Cachorro("Rex"),
            new Gato("Whiskers")
        };

        for (Animal a : animais) {
            a.emitirSom(); // cada um chama sua própria versão sobrescrita
        }
    }
}

class Gato extends Animal {
    public Gato(String nome) {
        super(nome);
    }

    @Override
    public void emitirSom() {
        System.out.println(nome + " mia: Miau!");
    }
}
```

A anotação `@Override` não é obrigatória, mas é fortemente recomendada: ela faz o compilador verificar que o método realmente sobrescreve um método da superclasse, evitando erros de digitação no nome do método que criariam um método novo em vez de sobrescrever o existente.

### Sobrecarga vs Sobrescrita

É comum confundir dois conceitos:

- **Sobrecarga (overloading)**: múltiplos métodos com o **mesmo nome** mas assinaturas diferentes (parâmetros diferentes), na mesma classe. Resolvida em tempo de **compilação**.
- **Sobrescrita (overriding)**: uma subclasse redefine um método herdado da superclasse, com a **mesma assinatura**. Resolvida em tempo de **execução** (polimorfismo dinâmico).

```java
class Calculadora {
    // Sobrecarga: mesmo nome, parâmetros diferentes
    int somar(int a, int b) {
        return a + b;
    }

    double somar(double a, double b) {
        return a + b;
    }
}
```

## Interfaces

Uma interface define um **contrato**: um conjunto de métodos que uma classe se compromete a implementar, sem ditar como isso deve ser feito.

```java
interface Pagavel {
    double calcularValorPagamento();
}

class Funcionario implements Pagavel {
    private double salarioBase;

    public Funcionario(double salarioBase) {
        this.salarioBase = salarioBase;
    }

    @Override
    public double calcularValorPagamento() {
        return salarioBase;
    }
}

class Freelancer implements Pagavel {
    private double valorHora;
    private int horasTrabalhadas;

    public Freelancer(double valorHora, int horasTrabalhadas) {
        this.valorHora = valorHora;
        this.horasTrabalhadas = horasTrabalhadas;
    }

    @Override
    public double calcularValorPagamento() {
        return valorHora * horasTrabalhadas;
    }
}
```

```java
public class Main {
    public static void main(String[] args) {
        List<Pagavel> folhaDePagamento = List.of(
            new Funcionario(3000),
            new Freelancer(50, 40)
        );

        for (Pagavel p : folhaDePagamento) {
            System.out.println("Pagamento: " + p.calcularValorPagamento());
        }
    }
}
```

Diferente de classes, uma classe **pode implementar múltiplas interfaces** — Java resolve dessa forma a ausência de herança múltipla de classes.

Desde o Java 8, interfaces podem ter métodos `default` (com implementação padrão) e métodos `static`:

```java
interface Pagavel {
    double calcularValorPagamento();

    default void exibirPagamento() {
        System.out.println("Valor a pagar: " + calcularValorPagamento());
    }

    static Pagavel salarioFixo(double valor) {
        return () -> valor; // implementação via lambda (interface funcional)
    }
}
```

## Classes abstratas

Uma classe abstrata é uma classe que **não pode ser instanciada diretamente** e pode conter tanto métodos abstratos (sem corpo, para as subclasses implementarem) quanto métodos concretos (com implementação compartilhada).

```java
abstract class FormaGeometrica {
    abstract double calcularArea(); // método abstrato — subclasses DEVEM implementar

    void exibirArea() { // método concreto — compartilhado por todas as subclasses
        System.out.println("Área: " + calcularArea());
    }
}

class Retangulo extends FormaGeometrica {
    private double largura, altura;

    public Retangulo(double largura, double altura) {
        this.largura = largura;
        this.altura = altura;
    }

    @Override
    double calcularArea() {
        return largura * altura;
    }
}
```

### Interface vs Classe abstrata: quando usar cada uma?

| Critério | Interface | Classe abstrata |
|---|---|---|
| Herança múltipla | Sim (várias interfaces) | Não (só uma superclasse) |
| Estado (atributos de instância) | Não (só constantes `static final`) | Sim |
| Construtor | Não | Sim |
| Uso típico | Definir um contrato/comportamento comum entre classes não relacionadas | Compartilhar código entre classes fortemente relacionadas |

Regra prática: use interface para definir "o que uma classe pode fazer" (capacidade); use classe abstrata quando várias classes compartilham uma implementação base comum e uma relação clara de "é um".

## Resumo do nível intermediário

- Herança (`extends`) reaproveita comportamento; `super` acessa construtor/métodos da superclasse.
- Polimorfismo permite tratar objetos diferentes de forma uniforme via referência comum.
- Sobrecarga (mesmo nome, parâmetros diferentes) é resolvida em compilação; sobrescrita (mesma assinatura em subclasse) é resolvida em execução.
- Interfaces definem contratos e suportam herança múltipla; desde o Java 8 podem ter métodos `default` e `static`.
- Classes abstratas compartilham estado e implementação parcial entre classes relacionadas.

## Leituras complementares

- Baeldung — "Interfaces in Java", "Abstract Class vs Interface in Java", "Method Overloading vs Overriding".
- Oracle Docs — *Interfaces and Inheritance*.
- Livro: *Head First Design Patterns* (capítulos introdutórios sobre polimorfismo aplicado).

---

