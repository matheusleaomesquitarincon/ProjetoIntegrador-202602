# Nível Avançado

## Introdução

Neste nível, o foco sai da sintaxe e passa para o **funcionamento interno da JVM**: como a memória é organizada, como o Garbage Collector opera, como classes são carregadas e como o compilador JIT otimiza código em tempo de execução. Esse conhecimento é essencial para diagnosticar problemas de performance, vazamentos de memória e para escrever código realmente eficiente em produção.

## Arquitetura da JVM

A JVM é composta por três subsistemas principais:

1. **Class Loader Subsystem**: carrega, verifica e inicializa classes `.class`.
2. **Runtime Data Areas**: as áreas de memória usadas durante a execução (detalhado a seguir).
3. **Execution Engine**: interpreta o bytecode e o compila via JIT quando vantajoso; inclui o Garbage Collector.

## Áreas de memória (Runtime Data Areas)

### Heap

Área onde **todos os objetos** (instâncias de classes e arrays) são alocados. É compartilhada entre todas as threads da aplicação. O Heap é dividido, na maioria das implementações modernas (como o G1), em gerações:

- **Young Generation**: onde objetos recém-criados nascem. Subdividida em *Eden* e duas áreas *Survivor* (S0, S1).
- **Old Generation (Tenured)**: objetos que sobrevivem a vários ciclos de coleta na Young Generation são promovidos para cá.

```java
public class HeapDemo {
    public static void main(String[] args) {
        // 'obj' é uma referência armazenada na Stack do método main
        // O objeto real (instância de Object) fica no Heap
        Object obj = new Object();
    }
}
```

### Stack

Cada thread tem sua própria Stack, que armazena *frames* de método: variáveis locais, parâmetros e referências a objetos (mas não os objetos em si). Quando um método termina, seu frame é removido da Stack.

### Metaspace (substituiu o PermGen desde o Java 8)

Armazena metadados de classes: estrutura da classe, métodos, informações de tipos genéricos. Diferente do antigo *PermGen* (que tinha tamanho fixo e causava `OutOfMemoryError: PermGen space` com frequência), o Metaspace usa memória nativa do sistema operacional e cresce dinamicamente (com limite configurável via `-XX:MaxMetaspaceSize`).

### PC Register e Native Method Stack

Cada thread também tem um *Program Counter Register* (indica a instrução bytecode atual sendo executada) e uma *Native Method Stack* (para chamadas a código nativo via JNI).

## Garbage Collection

O Garbage Collector (GC) é responsável por identificar e liberar memória de objetos que não são mais alcançáveis (não há mais nenhuma referência ativa apontando para eles).

### Algoritmo básico: Mark and Sweep

1. **Mark**: percorre o grafo de objetos a partir das *GC Roots* (variáveis locais em Stacks ativas, campos estáticos, etc.) marcando tudo que é alcançável.
2. **Sweep**: libera a memória de tudo que não foi marcado.

### Coletores modernos

- **Serial GC**: single-threaded, indicado para aplicações pequenas com pouca memória.
- **Parallel GC**: usa múltiplas threads para o Mark and Sweep, focado em throughput.
- **G1 (Garbage First)**: padrão desde o Java 9. Divide o Heap em regiões e prioriza a coleta de regiões com mais lixo, buscando pausas previsíveis.
- **ZGC** e **Shenandoah**: coletores de baixíssima latência (pausas na casa de milissegundos), voltados para aplicações que exigem tempo de resposta extremamente consistente, mesmo com Heaps de centenas de gigabytes.

```bash
# Exemplo de flags JVM para escolher o coletor G1 e configurar pausas alvo
java -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -jar aplicacao.jar
```

### Por que entender GC importa na prática

Um vazamento de memória em Java geralmente não é "memória não liberada" (não existe `free()` manual), mas sim **referências esquecidas** que impedem o GC de coletar objetos que deveriam ter sido descartados — por exemplo, um `static` `Map` que só cresce e nunca remove entradas antigas.

## Class Loading

O carregamento de classes segue o **modelo de delegação hierárquica**:

1. **Bootstrap ClassLoader**: carrega classes core do Java (`java.lang.*`, `java.util.*`), escrito em código nativo.
2. **Platform/Extension ClassLoader**: carrega classes de extensões da plataforma.
3. **Application ClassLoader (System ClassLoader)**: carrega as classes da aplicação (classpath do usuário).

Quando uma classe precisa ser carregada, o Application ClassLoader delega primeiro ao seu pai (Extension), que delega ao Bootstrap. Só se nenhum ancestral conseguir carregar a classe, o loader atual tenta carregá-la ele mesmo. Esse modelo evita que uma aplicação sobrescreva acidentalmente classes fundamentais do Java.

O processo de carregamento tem três fases: **Loading** (leitura do `.class` e criação do objeto `Class`), **Linking** (verificação de bytecode, preparação de campos estáticos, resolução de referências simbólicas) e **Initialization** (execução de blocos estáticos e inicializadores de campos estáticos).

## JIT Compiler (Just-In-Time)

Diferente de linguagens totalmente compiladas (C/C++) ou totalmente interpretadas, Java usa uma abordagem híbrida: o bytecode é inicialmente **interpretado**, mas trechos de código executados com muita frequência (chamados de *hot spots* — daí o nome "HotSpot JVM") são compilados para código de máquina nativo pelo **JIT Compiler**, otimizando a execução em tempo real.

Otimizações típicas do JIT:

- **Inlining**: substitui a chamada de um método pequeno pelo próprio corpo do método, eliminando o overhead da chamada.
- **Escape Analysis**: determina se um objeto "escapa" do método onde foi criado (é retornado, armazenado em campo, etc.). Se não escapa, a JVM pode alocá-lo na Stack em vez do Heap, ou até eliminar a alocação completamente — otimização impossível em análise puramente estática.
- **Loop unrolling**: reduz o overhead de controle de loops repetindo o corpo do loop múltiplas vezes por iteração do laço de controle.

```java
public class EscapeAnalysisDemo {
    public static void main(String[] args) {
        long soma = 0;
        for (int i = 0; i < 1_000_000; i++) {
            soma += calcularQuadrado(i); // 'Ponto' criado aqui pode nunca ir para o Heap
        }
        System.out.println(soma);
    }

    static int calcularQuadrado(int valor) {
        Ponto p = new Ponto(valor, valor); // se 'p' não escapa do método, JIT pode otimizar
        return p.x * p.y;
    }
}

class Ponto {
    int x, y;
    Ponto(int x, int y) { this.x = x; this.y = y; }
}
```

## Imutabilidade e `final` em profundidade

A palavra-chave `final` tem três usos distintos:

```java
// 1. Variável final: não pode ser reatribuída após inicialização
final int MAX_TENTATIVAS = 3;

// 2. Método final: não pode ser sobrescrito por subclasses
class Base {
    final void metodoCritico() { }
}

// 3. Classe final: não pode ser estendida
final class Utilitarios { }
```

É importante notar que `final` em uma referência a objeto impede a **reatribuição da referência**, mas não impede a **mutação do objeto** apontado:

```java
final List<String> lista = new ArrayList<>();
lista.add("item"); // permitido — o objeto é mutável
// lista = new ArrayList<>(); // erro de compilação — não pode reatribuir
```

Para imutabilidade real, é preciso que a própria classe seja projetada para não expor métodos que alterem seu estado interno — como fazem `String` e os *records* (Java 16+).

## Resumo do nível avançado

- A JVM organiza memória em Heap (objetos), Stack (por thread, variáveis locais), Metaspace (metadados de classes) e outras áreas menores.
- O Garbage Collector identifica objetos inalcançáveis a partir das GC Roots; coletores modernos (G1, ZGC) equilibram throughput e latência.
- O carregamento de classes segue delegação hierárquica entre Bootstrap, Extension e Application ClassLoaders.
- O JIT Compiler otimiza em tempo real código executado com frequência, usando técnicas como inlining e escape analysis.
- `final` tem três aplicações (variável, método, classe) e não garante, por si só, imutabilidade de objetos.

## Leituras complementares

- Oracle — *The Java Virtual Machine Specification* (docs.oracle.com/javase/specs).
- Baeldung — "JVM Garbage Collectors", "Class Loaders in Java", "JIT Compiler in Java".
- Livro: *Java Performance: The Definitive Guide*, Scott Oaks (O'Reilly).
- Artigo: "Understanding Java Garbage Collection" (várias publicações técnicas, incluindo blog da Oracle).

