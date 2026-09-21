# Nível Intermediário

## Introdução

Depois de conhecer as três estruturas básicas, é importante entender **quando** usar cada implementação concreta, como percorrer collections com segurança durante modificações, e como processar dados de forma declarativa usando a **Stream API**, introduzida no Java 8.

## Escolhendo a implementação certa de List

| Implementação | Acesso por índice | Inserção/remoção no meio | Uso típico |
|---|---|---|---|
| `ArrayList` | O(1) — muito rápido | O(n) — precisa deslocar elementos | Leitura frequente, poucas inserções no meio |
| `LinkedList` | O(n) — precisa percorrer | O(1) — se já tem referência ao nó | Muitas inserções/remoções nas pontas ou no meio |

```java
import java.util.LinkedList;
import java.util.List;

List<String> fila = new LinkedList<>();
fila.add("Primeiro");
fila.add("Segundo");
((LinkedList<String>) fila).addFirst("Novo Primeiro"); // O(1) em LinkedList

System.out.println(fila); // [Novo Primeiro, Primeiro, Segundo]
```

Na prática, `ArrayList` é a escolha padrão para a maioria dos casos — `LinkedList` só compensa quando há inserções/remoções muito frequentes nas extremidades (nesses casos, `ArrayDeque` costuma ser ainda mais eficiente que `LinkedList`).

## Escolhendo a implementação certa de Map

| Implementação | Ordem de iteração | Performance | Uso típico |
|---|---|---|---|
| `HashMap` | Não garantida | Mais rápida (O(1) médio) | Caso geral, quando ordem não importa |
| `LinkedHashMap` | Ordem de inserção | Ligeiramente mais lenta que HashMap | Quando a ordem de inserção precisa ser preservada |
| `TreeMap` | Ordem natural das chaves (ou Comparator) | O(log n) | Quando é preciso iterar em ordem ordenada |

```java
import java.util.*;

Map<String, Integer> hash = new HashMap<>();
Map<String, Integer> linked = new LinkedHashMap<>();
Map<String, Integer> tree = new TreeMap<>();

for (Map<String, Integer> mapa : List.of(hash, linked, tree)) {
    mapa.put("banana", 2);
    mapa.put("abacaxi", 5);
    mapa.put("cereja", 3);
}

System.out.println(hash);   // ordem imprevisível
System.out.println(linked); // {banana=2, abacaxi=5, cereja=3} — ordem de inserção
System.out.println(tree);   // {abacaxi=5, banana=2, cereja=3} — ordem alfabética
```

O mesmo padrão existe para `Set`: `HashSet` (sem ordem), `LinkedHashSet` (ordem de inserção), `TreeSet` (ordem natural).

## Iteração segura: Iterator e ConcurrentModificationException

Um erro muito comum é tentar remover elementos de uma lista **durante** um `for-each`:

```java
List<Integer> numeros = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6));

for (Integer n : numeros) {
    if (n % 2 == 0) {
        numeros.remove(n); // ERRO em tempo de execução!
    }
}
```

Esse código lança `ConcurrentModificationException`, porque o `for-each` usa internamente um `Iterator`, e modificar a lista diretamente enquanto o iterador a percorre invalida seu estado interno.

A forma correta é usar o próprio `Iterator` e seu método `remove()`:

```java
Iterator<Integer> it = numeros.iterator();
while (it.hasNext()) {
    Integer n = it.next();
    if (n % 2 == 0) {
        it.remove(); // remoção segura durante a iteração
    }
}
System.out.println(numeros); // [1, 3, 5]
```

Alternativa moderna e mais concisa, usando `removeIf` (Java 8+):

```java
numeros.removeIf(n -> n % 2 == 0);
```

## Introdução à Stream API

A Stream API (Java 8+) permite processar collections de forma **declarativa**: descreve-se *o que* deve ser feito (filtrar, transformar, agregar), não *como* iterar manualmente.

```java
import java.util.*;
import java.util.stream.*;

public class StreamDemo {
    public static void main(String[] args) {
        List<Integer> numeros = List.of(5, 3, 8, 1, 9, 4, 7);

        List<Integer> paresOrdenados = numeros.stream()
                .filter(n -> n % 2 == 0)  // mantém apenas pares
                .sorted()                  // ordena crescente
                .collect(Collectors.toList());

        System.out.println(paresOrdenados); // [4, 8]

        long quantidadeMaiorQue5 = numeros.stream()
                .filter(n -> n > 5)
                .count();

        System.out.println(quantidadeMaiorQue5); // 3

        int soma = numeros.stream()
                .mapToInt(Integer::intValue)
                .sum();

        System.out.println(soma); // 37
    }
}
```

Operações comuns de Stream:

- `filter(condição)`: mantém apenas elementos que atendem a um critério.
- `map(função)`: transforma cada elemento.
- `sorted()`: ordena os elementos.
- `collect(Collectors.toList())`: converte o stream de volta em uma `List`.
- `count()`, `sum()`, `max()`, `min()`: operações de agregação.
- `forEach(ação)`: executa uma ação para cada elemento (similar a for-each, mas em estilo funcional).

```java
List<String> nomes = List.of("ana", "bruno", "carla");

List<String> nomesMaiusculos = nomes.stream()
        .map(String::toUpperCase)
        .collect(Collectors.toList());

System.out.println(nomesMaiusculos); // [ANA, BRUNO, CARLA]
```

Um ponto importante: **streams não modificam a collection original** — sempre produzem um novo resultado (nova lista, novo valor agregado, etc.).

## Ordenação customizada com Comparator

Além do `sort()` padrão, é possível ordenar coleções por critérios customizados usando `Comparator`:

```java
import java.util.*;

record Pessoa(String nome, int idade) {}

public class ComparatorDemo {
    public static void main(String[] args) {
        List<Pessoa> pessoas = new ArrayList<>(List.of(
            new Pessoa("Carla", 25),
            new Pessoa("Ana", 30),
            new Pessoa("Bruno", 20)
        ));

        pessoas.sort(Comparator.comparing(Pessoa::nome));
        System.out.println(pessoas); // ordenado por nome

        pessoas.sort(Comparator.comparingInt(Pessoa::idade).reversed());
        System.out.println(pessoas); // ordenado por idade, decrescente
    }
}
```

## Resumo do nível intermediário

- `ArrayList` é geralmente preferível a `LinkedList`, exceto em cenários específicos de inserção/remoção intensiva nas pontas.
- `LinkedHashMap`/`LinkedHashSet` preservam ordem de inserção; `TreeMap`/`TreeSet` mantêm ordem natural (ou por `Comparator`).
- Modificar uma collection diretamente durante um `for-each` lança `ConcurrentModificationException` — use `Iterator.remove()` ou `removeIf()`.
- A Stream API permite processar collections de forma declarativa com `filter`, `map`, `sorted`, `collect`, entre outros.
- `Comparator` permite ordenação customizada e encadeável, sem alterar a classe do objeto.

## Leituras complementares

- Baeldung — "Java 8 Streams", "HashMap vs TreeMap vs LinkedHashMap", "Guide to Iterator in Java".
- Oracle Docs — *java.util.stream Package Summary*.
- Artigo: "When to Use LinkedList over ArrayList in Java" (Baeldung).

---

