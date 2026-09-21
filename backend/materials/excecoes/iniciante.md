# Nível Iniciante

## Introdução

Uma **exceção** é um evento que ocorre durante a execução de um programa e que interrompe o fluxo normal de instruções — por exemplo, tentar dividir por zero, acessar uma posição inválida de um array, ou tentar abrir um arquivo que não existe. Sem tratamento adequado, uma exceção não capturada faz o programa **encerrar abruptamente**, exibindo um *stack trace* no console.

Java oferece um mecanismo estruturado para lidar com esses erros: os blocos `try`, `catch` e `finally`.

## A hierarquia de Throwable

Toda exceção em Java é uma subclasse de `Throwable`, que se divide em dois ramos principais:

- **`Error`**: problemas graves, geralmente fora do controle da aplicação (ex: `OutOfMemoryError`). Normalmente não são tratados pelo código da aplicação.
- **`Exception`**: problemas que a aplicação pode (e deve) tratar. Divide-se em:
  - **Checked exceptions**: verificadas em tempo de **compilação** — o compilador obriga a tratá-las ou declará-las. Exemplo: `IOException`.
  - **Unchecked exceptions** (`RuntimeException` e suas subclasses): não são verificadas em compilação. Exemplo: `NullPointerException`, `ArithmeticException`.

```
Throwable
├── Error (ex: OutOfMemoryError, StackOverflowError)
└── Exception
    ├── RuntimeException (unchecked) — NullPointerException, ArithmeticException, etc.
    └── outras Exception (checked) — IOException, SQLException, etc.
```

## try, catch e finally

```java
public class ExcecaoDemo {
    public static void main(String[] args) {
        try {
            int resultado = 10 / 0; // lança ArithmeticException
            System.out.println(resultado); // nunca executa
        } catch (ArithmeticException e) {
            System.out.println("Erro: não é possível dividir por zero.");
        } finally {
            System.out.println("Este bloco sempre executa, com ou sem erro.");
        }

        System.out.println("Programa continua normalmente.");
    }
}
```

Saída:
```
Erro: não é possível dividir por zero.
Este bloco sempre executa, com ou sem erro.
Programa continua normalmente.
```

- O bloco `try` contém o código que pode lançar uma exceção.
- O bloco `catch` captura e trata um tipo específico de exceção.
- O bloco `finally` executa **sempre**, tenha ocorrido exceção ou não — usado tipicamente para liberar recursos (fechar arquivos, conexões, etc.).

## Exceções comuns

```java
public class ExcecoesComuns {
    public static void main(String[] args) {
        // NullPointerException — tentar usar uma referência nula
        String texto = null;
        try {
            System.out.println(texto.length());
        } catch (NullPointerException e) {
            System.out.println("Erro: texto está nulo.");
        }

        // ArrayIndexOutOfBoundsException — índice fora dos limites
        int[] numeros = {1, 2, 3};
        try {
            System.out.println(numeros[10]);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Erro: índice inválido.");
        }

        // NumberFormatException — conversão inválida de String para número
        try {
            int valor = Integer.parseInt("abc");
        } catch (NumberFormatException e) {
            System.out.println("Erro: 'abc' não é um número válido.");
        }
    }
}
```

## Múltiplos blocos catch

É possível capturar diferentes tipos de exceção em blocos separados, sempre do mais específico para o mais genérico:

```java
public class MultiplosCatch {
    public static void main(String[] args) {
        try {
            int[] numeros = {1, 2, 3};
            System.out.println(numeros[5] / 0);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Índice inválido.");
        } catch (ArithmeticException e) {
            System.out.println("Erro aritmético.");
        } catch (Exception e) {
            System.out.println("Algum outro erro ocorreu: " + e.getMessage());
        }
    }
}
```

**Atenção**: o bloco `catch (Exception e)` genérico deve sempre vir **por último** — se viesse primeiro, capturaria tudo, e os blocos mais específicos abaixo dele nunca seriam alcançados (o que, inclusive, gera erro de compilação em Java).

## A palavra-chave throws

Quando um método pode lançar uma exceção **checked** mas não quer tratá-la internamente, ele declara isso na assinatura usando `throws`, repassando a responsabilidade para quem o chamar:

```java
import java.io.*;

public class LeituraArquivo {
    public static void main(String[] args) {
        try {
            lerArquivo("dados.txt");
        } catch (IOException e) {
            System.out.println("Não foi possível ler o arquivo: " + e.getMessage());
        }
    }

    static void lerArquivo(String caminho) throws IOException {
        FileReader reader = new FileReader(caminho); // pode lançar IOException
        reader.close();
    }
}
```

Como `IOException` é uma exceção *checked*, o compilador **exige** que `lerArquivo` a trate com `try/catch` ou a declare com `throws` — caso contrário, o código nem compila.

## Resumo do nível iniciante

- Exceções interrompem o fluxo normal; sem tratamento, encerram o programa com um stack trace.
- `Throwable` se divide em `Error` (geralmente não tratável) e `Exception` (checked e unchecked/`RuntimeException`).
- `try/catch/finally` estrutura o tratamento; `finally` sempre executa.
- Múltiplos blocos `catch` devem ir do mais específico para o mais genérico.
- `throws` na assinatura de um método repassa a obrigação de tratar uma exceção checked para quem o chama.

## Leituras complementares

- Oracle — *The Java Tutorials: Exceptions*.
- Baeldung — "Checked vs Unchecked Exceptions in Java", "Exception Handling in Java".
- Livro: *Java: How to Program*, Deitel & Deitel (capítulo sobre exceções).

---

