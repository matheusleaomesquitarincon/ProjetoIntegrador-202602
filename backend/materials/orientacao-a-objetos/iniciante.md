# Nível Iniciante

## Introdução

Java é uma linguagem **orientada a objetos** (OO) desde sua concepção. Isso significa que a forma de organizar código gira em torno de **classes** e **objetos**, modelando entidades do mundo real (ou abstrações de negócio) como estruturas que combinam **dados** (atributos) e **comportamento** (métodos).

Os quatro pilares clássicos da OO são:

1. **Abstração** — representar apenas os detalhes relevantes de uma entidade.
2. **Encapsulamento** — esconder o estado interno e expor apenas o necessário.
3. **Herança** — reaproveitar comportamento entre classes relacionadas.
4. **Polimorfismo** — tratar objetos de tipos diferentes de forma uniforme.

Este nível iniciante cobre classes, objetos, construtores e encapsulamento.

## Classes e objetos

Uma **classe** é um molde (blueprint) que define quais atributos e métodos um tipo de objeto terá. Um **objeto** é uma instância concreta criada a partir desse molde.

```java
public class Carro {
    // Atributos (estado)
    private String modelo;
    private int velocidadeAtual;

    // Método (comportamento)
    public void acelerar() {
        velocidadeAtual += 10;
    }

    public void exibirStatus() {
        System.out.println(modelo + " está a " + velocidadeAtual + " km/h");
    }
}
```

```java
public class Main {
    public static void main(String[] args) {
        Carro carro1 = new Carro(); // cria um objeto (instância)
        carro1.modelo = "Civic";     // (assumindo atributo público apenas para ilustrar)
        carro1.acelerar();
        carro1.exibirStatus();

        Carro carro2 = new Carro(); // outro objeto, independente do primeiro
        carro2.modelo = "Corolla";
        carro2.exibirStatus();
    }
}
```

Cada objeto tem seu próprio estado: alterar `carro1` não afeta `carro2`, mesmo que ambos venham da mesma classe.

## Construtores

Um construtor é um método especial, chamado automaticamente quando um objeto é criado com `new`. Ele tem o mesmo nome da classe e não possui tipo de retorno.

```java
public class Carro {
    private String modelo;
    private int velocidadeAtual;

    // Construtor
    public Carro(String modelo) {
        this.modelo = modelo;
        this.velocidadeAtual = 0; // valor inicial padrão
    }

    public void acelerar() {
        velocidadeAtual += 10;
    }

    public void exibirStatus() {
        System.out.println(modelo + " está a " + velocidadeAtual + " km/h");
    }
}
```

```java
Carro meuCarro = new Carro("Civic"); // já nasce com modelo definido
```

Note o uso de `this.modelo = modelo;` — `this` se refere ao objeto atual, necessário aqui porque o parâmetro do construtor tem o mesmo nome do atributo.

Se nenhuma classe definir um construtor, o Java fornece automaticamente um **construtor padrão** sem argumentos. Mas assim que você declara qualquer construtor, o construtor padrão deixa de existir automaticamente.

É possível ter múltiplos construtores (sobrecarga de construtores):

```java
public class Carro {
    private String modelo;
    private int velocidadeAtual;

    public Carro() {
        this("Modelo Genérico"); // chama o outro construtor
    }

    public Carro(String modelo) {
        this.modelo = modelo;
        this.velocidadeAtual = 0;
    }
}
```

## Encapsulamento

Encapsulamento significa esconder os detalhes internos de uma classe e expor apenas o que é necessário através de uma interface pública controlada. Na prática em Java, isso é feito tornando os atributos `private` e criando métodos públicos (**getters** e **setters**) para acessá-los de forma controlada.

```java
public class ContaBancaria {
    private double saldo; // ninguém de fora pode alterar o saldo diretamente

    public ContaBancaria(double saldoInicial) {
        this.saldo = saldoInicial;
    }

    public double getSaldo() {
        return saldo;
    }

    public void depositar(double valor) {
        if (valor > 0) {
            saldo += valor;
        }
    }

    public void sacar(double valor) {
        if (valor > 0 && valor <= saldo) {
            saldo -= valor;
        } else {
            System.out.println("Operação inválida.");
        }
    }
}
```

Sem encapsulamento, qualquer código externo poderia fazer `conta.saldo = -1000;`, um estado inválido para uma conta bancária. Com o atributo `private` e métodos que validam a operação, a classe garante que seu estado interno permaneça consistente.

### Modificadores de acesso

| Modificador  | Mesma classe | Mesmo pacote | Subclasse (outro pacote) | Qualquer lugar |
|--------------|:---:|:---:|:---:|:---:|
| `private`    | ✅  | ❌  | ❌  | ❌ |
| (default/pacote) | ✅ | ✅ | ❌ | ❌ |
| `protected`  | ✅  | ✅  | ✅  | ❌ |
| `public`     | ✅  | ✅  | ✅  | ✅ |

## O objeto `this`

`this` é uma referência ao próprio objeto em que o código está sendo executado. Usos comuns:

```java
public class Pessoa {
    private String nome;

    public Pessoa(String nome) {
        this.nome = nome; // diferencia o atributo do parâmetro de mesmo nome
    }

    public Pessoa comparar(Pessoa outra) {
        if (this.nome.equals(outra.nome)) {
            System.out.println("Mesmo nome!");
        }
        return this; // permite encadeamento de chamadas
    }
}
```

## Resumo do nível iniciante

- Classe é o molde; objeto é a instância criada a partir dele com `new`.
- Construtores inicializam o estado de um objeto e podem ser sobrecarregados.
- Encapsulamento protege o estado interno usando atributos `private` e métodos públicos controlados.
- `this` referencia o próprio objeto e resolve ambiguidade entre atributo e parâmetro.

## Leituras complementares

- Oracle — *The Java Tutorials: Classes and Objects*.
- Baeldung — "Encapsulation in Java", "Constructors in Java".
- Livro: *Java: How to Program*, Deitel & Deitel (capítulo sobre classes).

---

