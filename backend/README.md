# Backend Java

Base inicial do backend do Java Studio, estruturada com Programacao Orientada a Objetos.

## POO aplicada

- `Nota` usa encapsulamento e valida os dados antes de alterar seu estado.
- `Material` e uma classe abstrata que define o contrato dos materiais teoricos.
- `MaterialJava` herda de `Material` e implementa `gerarConteudo`, demonstrando polimorfismo.
- `NotaRepository` define uma abstracao para persistencia.
- `NotaRepositoryMemoria` implementa o repositorio em memoria.
- `NotaService` concentra as regras de criacao, atualizacao e consulta de notas.

## Executar o exemplo

Na raiz do backend:

```bash
javac -d out $(Get-ChildItem -Recurse -Filter *.java | ForEach-Object FullName)
java -cp out com.javastudio.Application
```

O repositorio em memoria e uma base temporaria. A proxima etapa sera substituir sua implementacao por persistencia real e expor os endpoints para o frontend.