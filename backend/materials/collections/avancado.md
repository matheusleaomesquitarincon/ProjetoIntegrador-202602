# Nível Avançado

## Introdução

Este nível explora o funcionamento **interno** das estruturas mais usadas (especialmente `HashMap`), as estruturas concorrentes usadas em aplicações multithread, recursos avançados da Stream API, e considerações de complexidade algorítmica (Big-O) para escolher a estrutura certa com base em requisitos reais de performance.

## Estrutura interna do HashMap

Um `HashMap` armazena pares chave-valor em um array interno de **buckets** (posições). A posição de cada entrada é calculada a partir do `hashCode()` da chave:

1. O `hashCode()` da chave é calculado.
2. Esse hash passa por uma função de espalhamento adicional (para reduzir colisões) e é reduzido ao tamanho atual do array de buckets via módulo.
3. A entrada é armazenada nesse bucket.

Quando duas chaves diferentes produzem o mesmo índice de bucket (**colisão de hash**), o Java lida com isso de duas formas, dependendo da versão:

- **Antes do Java 8**: cada bucket era uma lista encadeada — em caso de colisão, novas entradas eram simplesmente adicionadas à lista, com busca O(n) dentro do bucket.
- **Desde o Java 8**: se um bucket específico acumula muitas colisões (limite padrão de 8 entradas), a lista encadeada daquele bucket é convertida internamente em uma **árvore rubro-negra balanceada**, reduzindo a busca de O(n) para O(log n) **naquele bucket específico** — proteção contra ataques que exploram colisões de hash propositais para degradar performance.

```java
public class HashCollisionDemo {
    static class ChaveRuim {
        int valor;
        ChaveRuim(int valor) { this.valor = valor; }

        @Override
        public int hashCode() {
            return 1; // hashCode fixo — força TODAS as chaves para o mesmo bucket
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof ChaveRuim c && c.valor == this.valor;
        }
    }

    public static void main(String[] args) {
        Map<ChaveRuim, String> mapa = new HashMap<>();
        for (int i = 0; i < 20; i++) {
            mapa.put(new ChaveRuim(i), "valor" + i);
            // todas as 20 entradas caem no MESMO bucket, forçando O(log n) via árvore
        }
        System.out.println(mapa.size()); // 20
    }
}
```

### Fator de carga e redimensionamento (rehashing)

`HashMap` tem uma **capacidade inicial** (padrão: 16) e um **fator de carga** (padrão: 0.75). Quando o número de entradas ultrapassa `capacidade × fator de carga`, o array interno é redimensionado (geralmente dobrado) e **todas** as entradas são recolocadas (*rehashing*) — uma operação O(n) que ocorre ocasionalmente, mas é amortizada ao longo de muitas inserções.

```java
// Se você sabe de antemão que vai inserir ~1000 elementos,
// definir a capacidade inicial evita múltiplos rehashings
Map<String, Integer> mapaOtimizado = new HashMap<>(1500); // ~1000 / 0.75 arredondado
```

## Collections concorrentes

Estruturas como `HashMap` e `ArrayList` **não são thread-safe**: usá-las simultaneamente a partir de múltiplas threads sem sincronização externa pode causar corrupção de dados ou exceções (`ConcurrentModificationException` inclusive fora dos casos de `for-each`).

### ConcurrentHashMap

Permite leitura e escrita concorrente com alta performance, dividindo internamente a estrutura em segmentos (ou, desde o Java 8, usando um mecanismo mais granular de sincronização por bucket) — evita bloquear o mapa inteiro para cada operação.

```java
import java.util.concurrent.ConcurrentHashMap;

public class ConcorrenciaDemo {
    public static void main(String[] args) throws InterruptedException {
        ConcurrentHashMap<String, Integer> contador = new ConcurrentHashMap<>();

        Runnable tarefa = () -> {
            for (int i = 0; i < 10000; i++) {
                // merge: operação atômica de "ler, calcular, escrever"
                contador.merge("chave", 1, Integer::sum);
            }
        };

        Thread t1 = new Thread(tarefa);
        Thread t2 = new Thread(tarefa);
        t1.start();
        t2.start();
        t1.join();
        t2.join();

        System.out.println(contador.get("chave")); // 20000, sempre correto
    }
}
```

Se o mesmo código usasse um `HashMap` comum, o resultado final seria **imprevisível** e provavelmente menor que 20000 — duas threads podem ler o mesmo valor antes de uma delas escrever, perdendo incrementos (*race condition*).

### CopyOnWriteArrayList

Indicada para cenários com **muitas leituras e poucas escritas** concorrentes. A cada escrita (`add`, `remove`), uma cópia completa do array interno é criada — leituras nunca são bloqueadas, mas escritas são custosas.

```java
import java.util.concurrent.CopyOnWriteArrayList;

CopyOnWriteArrayList<String> listaSegura = new CopyOnWriteArrayList<>();
listaSegura.add("evento1");
// Pode ser iterada com segurança mesmo que outra thread adicione elementos durante a iteração
for (String evento : listaSegura) {
    System.out.println(evento);
}
```

### Outras opções

- **`Collections.synchronizedMap(new HashMap<>())`**: forma antiga de "envolver" uma collection comum tornando cada método sincronizado (bloqueio total, menos eficiente que `ConcurrentHashMap`).
- **`BlockingQueue`** (ex: `LinkedBlockingQueue`): usada em cenários produtor-consumidor, onde threads podem bloquear esperando por espaço/itens disponíveis.

## Streams avançados: Collectors customizados e paralelismo

Além de `Collectors.toList()`, a classe `Collectors` oferece agregações mais ricas:

```java
import java.util.*;
import java.util.stream.*;

record Funcionario(String nome, String departamento, double salario) {}

public class CollectorsAvancado {
    public static void main(String[] args) {
        List<Funcionario> funcionarios = List.of(
            new Funcionario("Ana", "TI", 6000),
            new Funcionario("Bruno", "TI", 5500),
            new Funcionario("Carla", "RH", 4500),
            new Funcionario("Diego", "RH", 4800)
        );

        // Agrupar por departamento
        Map<String, List<Funcionario>> porDepartamento = funcionarios.stream()
                .collect(Collectors.groupingBy(Funcionario::departamento));
        System.out.println(porDepartamento.keySet()); // [TI, RH]

        // Agrupar e já calcular a média salarial de cada grupo
        Map<String, Double> mediaPorDepartamento = funcionarios.stream()
                .collect(Collectors.groupingBy(
                        Funcionario::departamento,
                        Collectors.averagingDouble(Funcionario::salario)
                ));
        System.out.println(mediaPorDepartamento); // {TI=5750.0, RH=4650.0}

        // Particionar (caso especial de agrupamento binário: true/false)
        Map<Boolean, List<Funcionario>> altoSalario = funcionarios.stream()
                .collect(Collectors.partitioningBy(f -> f.salario() > 5000));
        System.out.println(altoSalario.get(true).size()); // 2
    }
}
```

### parallelStream()

Para grandes volumes de dados e operações independentes entre si, `parallelStream()` divide o processamento entre múltiplas threads automaticamente (usando o `ForkJoinPool` comum da JVM):

```java
List<Integer> numerosGrandes = IntStream.rangeClosed(1, 10_000_000)
        .boxed()
        .collect(Collectors.toList());

long somaParalela = numerosGrandes.parallelStream()
        .mapToLong(Integer::longValue)
        .sum();
```

**Cuidados com `parallelStream()`**: só compensa para volumes grandes de dados e operações sem efeitos colaterais (evitar modificar variáveis externas dentro do stream); para coleções pequenas, o overhead de gerenciar threads pode tornar o paralelismo **mais lento** que um stream sequencial comum.

## Complexidade (Big-O) das operações principais

| Estrutura | Acesso | Busca | Inserção | Remoção |
|---|---|---|---|---|
| `ArrayList` | O(1) | O(n) | O(1) amortizado (fim) / O(n) (meio) | O(n) |
| `LinkedList` | O(n) | O(n) | O(1) (pontas) | O(1) (com referência ao nó) |
| `HashMap` / `HashSet` | — | O(1) médio, O(log n) pior caso | O(1) médio | O(1) médio |
| `TreeMap` / `TreeSet` | — | O(log n) | O(log n) | O(log n) |

Escolher a estrutura errada para o volume e padrão de acesso de dados é uma das causas mais comuns de problemas de performance em aplicações Java — por exemplo, usar `ArrayList.contains()` repetidamente em um loop grande (busca O(n) a cada chamada) quando um `HashSet.contains()` (O(1) médio) resolveria o mesmo problema de forma muito mais eficiente.

## Resumo do nível avançado

- `HashMap` usa buckets calculados a partir do hash da chave; colisões viram listas (ou árvores balanceadas, desde o Java 8, quando um bucket acumula muitas colisões).
- O redimensionamento (*rehashing*) do `HashMap` é O(n), mas amortizado ao longo de várias inserções; definir capacidade inicial evita rehashings desnecessários.
- `ConcurrentHashMap` e `CopyOnWriteArrayList` são alternativas thread-safe eficientes a `HashMap`/`ArrayList` em cenários concorrentes.
- `Collectors.groupingBy` e `partitioningBy` permitem agregações ricas; `parallelStream()` acelera processamento de grandes volumes, mas tem overhead que só compensa em escala.
- Escolher a estrutura de dados correta com base na complexidade (Big-O) de cada operação é decisivo para a performance da aplicação.

## Leituras complementares

- Artigo: "Java HashMap Internal Implementation" (Baeldung, GeeksforGeeks).
- Baeldung — "Guide to ConcurrentHashMap", "Guide to CopyOnWriteArrayList", "Collectors in Java", "Guide to Java Parallel Streams".
- Documentação oficial: pacote `java.util.concurrent` (Oracle Docs), com contribuições originais de Doug Lea.
- Livro: *Java Concurrency in Practice*, Brian Goetz.

