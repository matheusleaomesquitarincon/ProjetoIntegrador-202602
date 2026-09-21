# Nível Iniciante

## Introdução

O **Java Collections Framework** é um conjunto de interfaces e classes para armazenar e manipular grupos de objetos — listas, conjuntos, filas e mapas. Antes das collections, desenvolvedores precisavam trabalhar apenas com arrays, que têm tamanho fixo e recursos limitados. As collections resolvem isso oferecendo estruturas dinâmicas, com métodos prontos para busca, ordenação, inserção e remoção.

As três interfaces raiz mais usadas são:

- **`List`**: coleção ordenada, permite elementos duplicados, acesso por índice.
- **`Set`**: coleção que não permite elementos duplicados.
- **`Map`**: associação de chave-valor (não estende `Collection`, mas faz parte do framework).

## List e ArrayList

`ArrayList` é a implementação de `List` mais usada no dia a dia — internamente, mantém um array que cresce automaticamente conforme elementos são adicionados.

```java
import java.util.ArrayList;
import java.util.List;

public class ListaDemo {
    public static void main(String[] args) {
        List<String> frutas = new ArrayList<>();

        frutas.add("Maçã");
        frutas.add("Banana");
        frutas.add("Maçã"); // duplicata permitida em List

        System.out.println(frutas);          // [Maçã, Banana, Maçã]
        System.out.println(frutas.get(0));    // Maçã (acesso por índice)
        System.out.println(frutas.size());    // 3

        frutas.remove("Banana");              // remove pela primeira ocorrência do valor
        System.out.println(frutas);           // [Maçã, Maçã]

        frutas.remove(0);                     // remove pelo índice
        System.out.println(frutas);           // [Maçã]

        System.out.println(frutas.contains("Maçã")); // true
    }
}
```

Repare em `List<String> frutas = new ArrayList<>();` — é comum declarar a variável pelo tipo da **interface** (`List`) e instanciar a **implementação concreta** (`ArrayList`). Isso deixa o código mais flexível: se um dia for preciso trocar `ArrayList` por outra implementação de `List`, o restante do código que usa `frutas` não precisa mudar.

## Set e HashSet

`Set` garante que não existam elementos duplicados. `HashSet` é a implementação mais comum, baseada em uma tabela hash — não garante nenhuma ordem específica de iteração.

```java
import java.util.HashSet;
import java.util.Set;

public class ConjuntoDemo {
    public static void main(String[] args) {
        Set<String> nomes = new HashSet<>();

        nomes.add("Ana");
        nomes.add("Bruno");
        nomes.add("Ana"); // ignorado — já existe

        System.out.println(nomes);          // [Bruno, Ana] (ordem não garantida)
        System.out.println(nomes.size());    // 2
        System.out.println(nomes.contains("Ana")); // true
    }
}
```

`Set` é útil sempre que a regra de negócio exige unicidade — por exemplo, uma lista de e-mails únicos cadastrados, ou tags sem repetição.

## Map e HashMap

`Map` armazena pares chave-valor, onde cada chave é única e mapeia para exatamente um valor.

```java
import java.util.HashMap;
import java.util.Map;

public class MapaDemo {
    public static void main(String[] args) {
        Map<String, Integer> precos = new HashMap<>();

        precos.put("Maçã", 3);
        precos.put("Banana", 2);
        precos.put("Maçã", 4); // sobrescreve o valor anterior da chave "Maçã"

        System.out.println(precos.get("Maçã"));   // 4
        System.out.println(precos.get("Uva"));    // null — chave não existe
        System.out.println(precos.containsKey("Banana")); // true
        System.out.println(precos.size());         // 2

        for (Map.Entry<String, Integer> entrada : precos.entrySet()) {
            System.out.println(entrada.getKey() + " custa R$" + entrada.getValue());
        }
    }
}
```

## Percorrendo collections

A forma mais simples e segura de percorrer qualquer `Collection` (listas e conjuntos) é o `for-each`:

```java
List<String> frutas = List.of("Maçã", "Banana", "Uva");

for (String fruta : frutas) {
    System.out.println(fruta);
}
```

Para `Map`, percorre-se geralmente via `entrySet()` (chave e valor juntos), `keySet()` (só chaves) ou `values()` (só valores):

```java
Map<String, Integer> idades = Map.of("Ana", 25, "Bruno", 30);

for (String nome : idades.keySet()) {
    System.out.println(nome);
}

for (Integer idade : idades.values()) {
    System.out.println(idade);
}
```

## Resumo do nível iniciante

- `List` mantém ordem e permite duplicatas; `ArrayList` é a implementação mais comum.
- `Set` não permite duplicatas; `HashSet` não garante ordem de iteração.
- `Map` associa chaves únicas a valores; `HashMap` é a implementação mais comum.
- É boa prática declarar variáveis pelo tipo da interface (`List`, `Set`, `Map`) e instanciar pela implementação concreta.
- `for-each` é a forma mais simples de percorrer coleções.

## Leituras complementares

- Oracle — *The Java Tutorials: Collections*.
- Baeldung — "A Guide to Java ArrayList", "Guide to HashMap", "Guide to HashSet".
- Livro: *Java: How to Program*, Deitel & Deitel (capítulo sobre Collections).

---

