# Nível Intermediário

## Introdução

Com o básico de `try/catch` dominado, este nível aborda como **criar exceções customizadas** para representar erros específicos do domínio de negócio, recursos de sintaxe mais modernos (*multi-catch*, *try-with-resources*) e boas práticas amplamente recomendadas por especialistas para tratamento de exceções em código profissional.

## Exceções customizadas

Criar exceções próprias torna o código mais expressivo e permite tratamento diferenciado por tipo de erro de negócio. Basta estender `Exception` (checked) ou `RuntimeException` (unchecked).

```java
// Exceção checked customizada — força quem chama a tratar ou declarar
class SaldoInsuficienteException extends Exception {
    public SaldoInsuficienteException(String mensagem) {
        super(mensagem);
    }
}

class ContaBancaria {
    private double saldo;

    public ContaBancaria(double saldoInicial) {
        this.saldo = saldoInicial;
    }

    public void sacar(double valor) throws SaldoInsuficienteException {
        if (valor > saldo) {
            throw new SaldoInsuficienteException(
                "Saldo insuficiente: disponível R$" + saldo + ", solicitado R$" + valor
            );
        }
        saldo -= valor;
    }
}
```

```java
public class Main {
    public static void main(String[] args) {
        ContaBancaria conta = new ContaBancaria(100.0);
        try {
            conta.sacar(200.0);
        } catch (SaldoInsuficienteException e) {
            System.out.println("Falha na operação: " + e.getMessage());
        }
    }
}
```

### Checked ou unchecked customizada?

- Use **checked** (`extends Exception`) quando o erro é uma condição que o código chamador **pode e deve** tratar de forma recuperável (ex: arquivo não encontrado, validação de negócio que impede a operação).
- Use **unchecked** (`extends RuntimeException`) quando o erro representa um **bug de programação** ou uma condição da qual normalmente não há recuperação em tempo de execução (ex: argumento inválido passado por engano, estado interno inconsistente).

```java
class ArgumentoInvalidoException extends RuntimeException {
    public ArgumentoInvalidoException(String mensagem) {
        super(mensagem);
    }
}

static void validarIdade(int idade) {
    if (idade < 0) {
        throw new ArgumentoInvalidoException("Idade não pode ser negativa: " + idade);
    }
}
```

Muitas equipes e frameworks modernos (incluindo o próprio Spring) tendem a preferir **unchecked** exceptions customizadas, evitando poluir assinaturas de métodos com múltiplas cláusulas `throws` — o tratamento fica centralizado em camadas específicas (como veremos no material de Spring Boot).

## Multi-catch

Quando o mesmo bloco de tratamento serve para múltiplos tipos de exceção, é possível combiná-los em um único `catch` usando `|`:

```java
public class MultiCatchDemo {
    public static void main(String[] args) {
        try {
            processar("abc");
        } catch (NumberFormatException | NullPointerException e) {
            System.out.println("Erro de entrada: " + e.getMessage());
        }
    }

    static void processar(String valor) {
        int numero = Integer.parseInt(valor); // pode lançar NumberFormatException
    }
}
```

Isso evita duplicar o mesmo código de tratamento em vários blocos `catch` separados, quando a ação a ser tomada é idêntica para os diferentes tipos de exceção.

## try-with-resources

Recursos como arquivos, conexões de banco de dados e sockets precisam ser **fechados** explicitamente após o uso, mesmo que ocorra uma exceção. Antes do Java 7, isso exigia um `finally` verboso:

```java
// Forma antiga (Java 6 e anteriores) — verbosa e propensa a erros
FileReader reader = null;
try {
    reader = new FileReader("dados.txt");
    // usar o reader
} catch (IOException e) {
    System.out.println("Erro: " + e.getMessage());
} finally {
    if (reader != null) {
        try {
            reader.close();
        } catch (IOException e) {
            // erro ao fechar, geralmente ignorado ou logado
        }
    }
}
```

Desde o Java 7, `try-with-resources` fecha automaticamente qualquer recurso que implemente a interface `AutoCloseable`:

```java
try (FileReader reader = new FileReader("dados.txt")) {
    // usar o reader
} catch (IOException e) {
    System.out.println("Erro: " + e.getMessage());
}
// reader.close() é chamado automaticamente ao sair do bloco, com ou sem exceção
```

É possível declarar múltiplos recursos no mesmo `try`, separados por `;` — eles são fechados na ordem inversa em que foram declarados:

```java
try (FileReader leitor = new FileReader("origem.txt");
     FileWriter escritor = new FileWriter("destino.txt")) {
    // usar leitor e escritor
} catch (IOException e) {
    System.out.println("Erro de I/O: " + e.getMessage());
}
```

## Boas práticas no tratamento de exceções

**1. Nunca capture exceções silenciosamente ("swallow exceptions")**

```java
// Prática ruim — o erro desaparece sem deixar rastro
try {
    processarPedido();
} catch (Exception e) {
    // vazio! ninguém nunca vai saber que isso falhou
}
```

```java
// Melhor — ao menos registrar o erro
try {
    processarPedido();
} catch (Exception e) {
    logger.error("Falha ao processar pedido", e);
}
```

**2. Não capture `Exception` genericamente quando um tipo específico resolve**

```java
// Evitar quando possível
try {
    Integer.parseInt(valor);
} catch (Exception e) { } // captura TUDO, inclusive erros inesperados

// Preferir
try {
    Integer.parseInt(valor);
} catch (NumberFormatException e) { } // captura exatamente o que se espera
```

**3. Preserve a causa raiz ao encapsular exceções**

```java
try {
    conectarBancoDeDados();
} catch (SQLException e) {
    // Passar 'e' como segundo argumento preserva o stack trace original
    throw new RuntimeException("Falha ao conectar ao banco de dados", e);
}
```

Sem passar a causa original, informações valiosas de depuração (onde o erro realmente começou) se perdem.

**4. Não use exceções para controle de fluxo normal**

Lançar e capturar exceções tem custo de performance (geração do stack trace) e prejudica a legibilidade quando usado para lógica que não é, de fato, excepcional — por exemplo, usar uma exceção para verificar se um elemento existe em uma lista, quando um simples `if` com `contains()` resolveria de forma mais clara e eficiente.

## Resumo do nível intermediário

- Exceções customizadas tornam erros de negócio explícitos; escolha checked para erros recuperáveis pelo chamador, unchecked para bugs/estados inválidos.
- Multi-catch (`catch (A | B e)`) evita duplicação de código quando o tratamento é idêntico para tipos diferentes de exceção.
- `try-with-resources` fecha automaticamente recursos `AutoCloseable`, eliminando blocos `finally` verbosos.
- Boas práticas incluem: nunca engolir exceções silenciosamente, evitar capturar `Exception` genérico desnecessariamente, preservar a causa raiz ao encapsular, e não usar exceções para fluxo de controle normal.

## Leituras complementares

- Baeldung — "Creating a Custom Exception in Java", "Try-With-Resources in Java", "Multi-Catch Statements in Java".
- Oracle Docs — *The try-with-resources Statement*.
- Livro: *Effective Java*, Joshua Bloch — capítulo dedicado a boas práticas com exceções.

---

