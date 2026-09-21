# Nível Iniciante

## Introdução

Java é uma linguagem de programação orientada a objetos, criada pela Sun Microsystems (hoje propriedade da Oracle) em 1995. Seu principal diferencial na época do lançamento foi o lema *"write once, run anywhere"* (escreva uma vez, rode em qualquer lugar): o código Java não é compilado diretamente para instruções de máquina de um processador específico, mas sim para um formato intermediário chamado **bytecode**, que é executado pela **JVM (Java Virtual Machine)**. Isso significa que o mesmo programa compilado roda em Windows, Linux ou macOS, desde que exista uma JVM compatível instalada.

Hoje, Java é uma das linguagens mais usadas no mundo corporativo, especialmente em back-end de sistemas web, aplicações bancárias, sistemas de grande porte (Android também usa uma variante do Java/Kotlin) e é a base de frameworks populares como Spring.

Antes de escrever qualquer código, é importante entender três siglas:

- **JDK (Java Development Kit)**: contém tudo que é necessário para desenvolver em Java — compilador (`javac`), ferramentas e a JRE.
- **JRE (Java Runtime Environment)**: contém a JVM e as bibliotecas necessárias para *executar* programas Java (mas não para compilar).
- **JVM (Java Virtual Machine)**: a máquina virtual que efetivamente interpreta/executa o bytecode.

Para desenvolver, você precisa do JDK instalado. Para apenas rodar um programa já compilado, basta a JRE.

## Estrutura básica de um programa Java

Todo programa Java executável precisa de uma classe com um método `main`, que é o ponto de entrada da aplicação:

```java
public class Main {
    public static void main(String[] args) {
        System.out.println("Olá, mundo!");
    }
}
```

Analisando essa estrutura:

- `public class Main`: declara uma classe pública chamada `Main`. Em Java, o nome do arquivo `.java` deve ser igual ao nome da classe pública que ele contém (`Main.java`).
- `public static void main(String[] args)`: assinatura obrigatória do método principal.
  - `public`: acessível de qualquer lugar (a JVM precisa conseguir chamá-lo de fora da classe).
  - `static`: pertence à classe, não a uma instância — a JVM chama esse método sem precisar criar um objeto `Main` primeiro.
  - `void`: não retorna valor.
  - `String[] args`: array de argumentos passados via linha de comando.
- `System.out.println(...)`: imprime uma linha no console.

## Variáveis e tipos primitivos

Java é uma linguagem **fortemente tipada** — toda variável tem um tipo definido em tempo de compilação, e esse tipo não muda.

Os oito tipos primitivos são:

| Tipo      | Tamanho  | Exemplo de uso                  |
|-----------|----------|----------------------------------|
| `byte`    | 8 bits   | valores pequenos (-128 a 127)   |
| `short`   | 16 bits  | inteiros pequenos                |
| `int`     | 32 bits  | inteiros em geral (mais comum)   |
| `long`    | 64 bits  | inteiros muito grandes           |
| `float`   | 32 bits  | ponto flutuante (menos preciso)  |
| `double`  | 64 bits  | ponto flutuante (mais comum)     |
| `char`    | 16 bits  | um único caractere Unicode       |
| `boolean` | 1 bit*   | `true` ou `false`                |

```java
public class Variaveis {
    public static void main(String[] args) {
        int idade = 25;
        double altura = 1.78;
        char inicial = 'M';
        boolean ativo = true;
        long populacaoMundial = 8_100_000_000L; // 'L' indica literal long
        float pi = 3.14f;                        // 'f' indica literal float

        System.out.println("Idade: " + idade);
        System.out.println("Altura: " + altura);
        System.out.println("Inicial: " + inicial);
        System.out.println("Ativo? " + ativo);
    }
}
```

Repare no uso de `_` como separador visual em `8_100_000_000L` — recurso puramente estético, ignorado pelo compilador, útil para legibilidade de números grandes.

Além dos primitivos, existe o tipo `String`, que **não é primitivo** — é uma classe. Strings em Java são **imutáveis**: uma vez criada, o conteúdo de uma `String` nunca muda; operações como concatenação sempre geram uma nova `String`.

```java
String nome = "Matheus";
String saudacao = "Olá, " + nome + "!";
System.out.println(saudacao); // Olá, Matheus!
```

## Operadores

Java oferece os operadores tradicionais:

- **Aritméticos**: `+`, `-`, `*`, `/`, `%` (resto da divisão).
- **Relacionais**: `==`, `!=`, `>`, `<`, `>=`, `<=`.
- **Lógicos**: `&&` (E), `||` (OU), `!` (negação).
- **Atribuição**: `=`, `+=`, `-=`, `*=`, `/=`.
- **Incremento/decremento**: `++`, `--`.

```java
int a = 10, b = 3;
System.out.println(a / b);   // 3 (divisão inteira)
System.out.println(a % b);   // 1 (resto)
System.out.println(a / (double) b); // 3.333... (forçando double)

boolean resultado = (a > 5) && (b < 5);
System.out.println(resultado); // true
```

Atenção: a divisão entre dois `int` sempre trunca a parte decimal. Para obter um resultado com casas decimais, ao menos um dos operandos precisa ser `double` ou `float`.

## Estruturas condicionais

### if / else

```java
int nota = 75;

if (nota >= 90) {
    System.out.println("Conceito A");
} else if (nota >= 70) {
    System.out.println("Conceito B");
} else {
    System.out.println("Conceito C");
}
```

### switch

O `switch` tradicional compara um valor contra vários casos possíveis:

```java
int diaSemana = 3;
String nomeDia;

switch (diaSemana) {
    case 1:
        nomeDia = "Segunda";
        break;
    case 2:
        nomeDia = "Terça";
        break;
    case 3:
        nomeDia = "Quarta";
        break;
    default:
        nomeDia = "Dia inválido";
}

System.out.println(nomeDia); // Quarta
```

O `break` é essencial: sem ele, a execução "cai" para o próximo `case` (comportamento chamado de *fall-through*), o que costuma ser um erro comum para iniciantes.

## Estruturas de repetição

### for

Ideal quando se sabe quantas vezes o laço deve rodar:

```java
for (int i = 0; i < 5; i++) {
    System.out.println("Iteração " + i);
}
```

### while

Ideal quando a condição de parada depende de algo que só se sabe em tempo de execução:

```java
int contador = 0;
while (contador < 3) {
    System.out.println("Contador: " + contador);
    contador++;
}
```

### do-while

Garante que o bloco execute **pelo menos uma vez**, pois a condição é verificada ao final:

```java
int numero = 10;
do {
    System.out.println("Executou pelo menos uma vez, número = " + numero);
    numero++;
} while (numero < 5);
```

### for-each

Usado para percorrer arrays e coleções sem precisar controlar o índice manualmente:

```java
int[] numeros = {10, 20, 30};
for (int n : numeros) {
    System.out.println(n);
}
```

## Arrays

Um array é uma estrutura de tamanho fixo que armazena vários valores do mesmo tipo:

```java
public class ArraysDemo {
    public static void main(String[] args) {
        int[] idades = new int[3]; // array de 3 posições, valores iniciados em 0
        idades[0] = 20;
        idades[1] = 25;
        idades[2] = 30;

        String[] nomes = {"Ana", "Bruno", "Carla"}; // inicialização direta

        System.out.println(idades.length); // 3
        System.out.println(nomes[1]);       // Bruno
    }
}
```

Tentar acessar um índice fora dos limites do array (por exemplo, `idades[5]` em um array de tamanho 3) lança uma `ArrayIndexOutOfBoundsException` em tempo de execução — não é um erro detectado na compilação.

## Métodos

Métodos organizam código em blocos reutilizáveis:

```java
public class Calculadora {
    public static void main(String[] args) {
        int resultado = somar(5, 7);
        System.out.println("Soma: " + resultado);
    }

    static int somar(int a, int b) {
        return a + b;
    }
}
```

Um método é composto por: modificador de acesso, tipo de retorno, nome, parâmetros (entre parênteses) e corpo. Se o método não retorna nada, o tipo de retorno é `void`.

## Comentários

```java
// Comentário de uma linha

/*
 * Comentário
 * de múltiplas linhas
 */

/**
 * Comentário de documentação (Javadoc).
 * Usado para gerar documentação automática da API.
 */
```

## Resumo do nível iniciante

- Java compila para bytecode, executado pela JVM.
- Todo programa executável precisa de um método `main`.
- Existem 8 tipos primitivos; `String` é uma classe imutável.
- Estruturas de controle (`if`, `switch`, `for`, `while`) seguem uma sintaxe parecida com C/C++.
- Arrays têm tamanho fixo definido na criação.
- Métodos organizam e reutilizam lógica.

## Leituras complementares

- Oracle — *The Java Tutorials: Language Basics* (docs.oracle.com/javase/tutorial).
- Baeldung — "Java Basics" e "Introduction to Java".
- Livro: *Java: How to Program*, Deitel & Deitel (capítulos iniciais).

---

