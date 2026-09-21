# Nível Intermediário

## Introdução

Uma vez dominada a sintaxe básica, o próximo passo é entender detalhes que impactam diretamente a qualidade e a performance do código: como o Java gerencia tipos primitivos versus objetos, como strings são realmente armazenadas em memória, e recursos de sintaxe mais avançados como *varargs*, arrays multidimensionais e blocos estáticos.

## Wrapper Classes e Autoboxing

Cada tipo primitivo possui uma classe *wrapper* correspondente:

| Primitivo | Wrapper     |
|-----------|-------------|
| `int`     | `Integer`   |
| `double`  | `Double`    |
| `boolean` | `Boolean`   |
| `char`    | `Character` |
| `long`    | `Long`      |
| `float`   | `Float`     |
| `byte`    | `Byte`      |
| `short`   | `Short`     |

Wrapper classes existem porque em muitos contextos Java exige um **objeto**, não um primitivo — por exemplo, dentro de `Collections` genéricas (`List<Integer>`, nunca `List<int>`).

Desde o Java 5, o compilador converte automaticamente entre primitivo e wrapper — processo chamado **autoboxing** (primitivo → objeto) e **unboxing** (objeto → primitivo):

```java
List<Integer> numeros = new ArrayList<>();
numeros.add(10);      // autoboxing: int 10 vira Integer.valueOf(10)
int primeiro = numeros.get(0); // unboxing: Integer vira int
```

**Cuidado com dois problemas comuns:**

1. **`NullPointerException` em unboxing**: se um `Integer` for `null` e você tentar usá-lo como `int`, ocorre uma `NullPointerException`.

```java
Integer valor = null;
int x = valor; // lança NullPointerException
```

2. **Comparação com `==` em wrappers**: `Integer` é um objeto, então `==` compara referências, não valores — exceto para um cache interno de valores entre -128 e 127:

```java
Integer a = 100;
Integer b = 100;
System.out.println(a == b); // true (dentro do cache -128 a 127)

Integer c = 200;
Integer d = 200;
System.out.println(c == d); // false (fora do cache, objetos diferentes)
System.out.println(c.equals(d)); // true (correto: sempre usar equals)
```

**Regra prática**: sempre compare wrappers com `.equals()`, nunca com `==`.

## Strings em profundidade

Strings em Java são armazenadas de forma especial: literais de string ficam em uma área da memória chamada **String Pool** (dentro do Heap), permitindo reaproveitamento de literais idênticos:

```java
String a = "java";
String b = "java";
System.out.println(a == b); // true — apontam para o mesmo objeto no pool

String c = new String("java");
System.out.println(a == c); // false — new força criação de um objeto novo fora do pool
System.out.println(a.equals(c)); // true — mesmo conteúdo
```

Como strings são imutáveis, cada operação de concatenação em um loop cria uma nova `String`, descartando a anterior — isso é ineficiente para muitas concatenações:

```java
// Ineficiente: cria N objetos String intermediários
String resultado = "";
for (int i = 0; i < 10000; i++) {
    resultado += i; // cria uma nova String a cada iteração
}
```

Para esses casos, usa-se `StringBuilder` (não sincronizado, mais rápido) ou `StringBuffer` (sincronizado, thread-safe, porém mais lento):

```java
StringBuilder sb = new StringBuilder();
for (int i = 0; i < 10000; i++) {
    sb.append(i); // modifica o buffer interno, sem criar novos objetos a cada passo
}
String resultado = sb.toString();
```

Métodos úteis de `String`:

```java
String texto = "  Java é incrível  ";
System.out.println(texto.trim());             // remove espaços das pontas
System.out.println(texto.toUpperCase());       // maiúsculas
System.out.println(texto.contains("incrível")); // true
System.out.println(texto.replace("Java", "Kotlin"));
System.out.println(texto.trim().split(" ").length); // divide por espaço
```

## Arrays multidimensionais

Java suporta arrays de arrays (não são matrizes "verdadeiras" como em outras linguagens, mas arrays aninhados):

```java
int[][] matriz = new int[3][3]; // 3x3 preenchida com zeros

for (int i = 0; i < matriz.length; i++) {
    for (int j = 0; j < matriz[i].length; j++) {
        matriz[i][j] = i * 3 + j;
    }
}

for (int[] linha : matriz) {
    for (int valor : linha) {
        System.out.print(valor + " ");
    }
    System.out.println();
}
```

Também é possível criar arrays "irregulares" (*jagged arrays*), onde cada linha tem um tamanho diferente:

```java
int[][] irregular = new int[3][];
irregular[0] = new int[]{1};
irregular[1] = new int[]{1, 2};
irregular[2] = new int[]{1, 2, 3};
```

## Varargs

*Varargs* (número variável de argumentos) permite chamar um método com quantos argumentos forem necessários, sem precisar sobrecarregar métodos manualmente:

```java
public class VarargsDemo {
    public static void main(String[] args) {
        System.out.println(somar());           // 0
        System.out.println(somar(1));           // 1
        System.out.println(somar(1, 2, 3, 4));  // 10
    }

    static int somar(int... numeros) {
        int total = 0;
        for (int n : numeros) {
            total += n;
        }
        return total;
    }
}
```

Internamente, `int... numeros` é tratado como um array `int[]`. Regras importantes: só pode haver um parâmetro varargs por método, e ele deve ser o último da lista de parâmetros.

## Escopo de variáveis e blocos estáticos

Java tem escopo de bloco: uma variável declarada dentro de `{}` só existe dentro daquele bloco.

Blocos estáticos são executados uma única vez, quando a classe é carregada pela JVM — úteis para inicializações complexas de campos estáticos:

```java
public class Configuracao {
    static final String VERSAO;

    static {
        VERSAO = carregarVersaoDoAmbiente();
        System.out.println("Bloco estático executado.");
    }

    static String carregarVersaoDoAmbiente() {
        return "1.0.0";
    }
}
```

## Passagem de parâmetros: valor vs referência

Um ponto frequentemente mal compreendido: **Java sempre passa parâmetros por valor.** Para tipos primitivos, o valor copiado é o próprio dado. Para objetos, o valor copiado é a **referência** (endereço) ao objeto — não o objeto em si.

```java
public class PassagemDemo {
    public static void main(String[] args) {
        int numero = 10;
        alterarPrimitivo(numero);
        System.out.println(numero); // 10 — não mudou

        int[] array = {1, 2, 3};
        alterarArray(array);
        System.out.println(array[0]); // 99 — mudou! (mesmo objeto referenciado)

        String texto = "original";
        alterarString(texto);
        System.out.println(texto); // "original" — não mudou (String é imutável)
    }

    static void alterarPrimitivo(int x) {
        x = 99; // altera só a cópia local
    }

    static void alterarArray(int[] arr) {
        arr[0] = 99; // altera o conteúdo do objeto apontado pela referência copiada
    }

    static void alterarString(String s) {
        s = "modificado"; // reatribui a variável local, não afeta o objeto original
    }
}
```

Este comportamento confunde muitos desenvolvedores vindos de outras linguagens: **a referência é copiada**, então mutações no objeto (como `arr[0] = 99`) são visíveis fora do método, mas reatribuições da variável local (`s = "modificado"`) não são.

## Resumo do nível intermediário

- Wrapper classes permitem que primitivos sejam usados como objetos; cuidado com `NullPointerException` no unboxing e com `==` vs `.equals()`.
- Strings são imutáveis e armazenadas em um pool; prefira `StringBuilder` para concatenações em loop.
- Arrays multidimensionais são arrays de arrays, podendo ser irregulares.
- Varargs simplificam métodos com número variável de argumentos.
- Java sempre passa parâmetros por valor — inclusive quando o valor é uma referência a objeto.

## Leituras complementares

- Baeldung — "Java String Pool", "Guide to Java Varargs", "Autoboxing and Unboxing in Java".
- Oracle Docs — *Autoboxing and Unboxing*, *Passing Information to a Method or a Constructor*.
- Artigo: "Why String is Immutable in Java" (diversas fontes técnicas, incluindo Baeldung e InfoWorld).

---

