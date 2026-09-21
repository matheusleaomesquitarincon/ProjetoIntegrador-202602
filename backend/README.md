# Backend Java

Base inicial do backend do Java Studio, estruturada com Programacao Orientada a Objetos.

## POO aplicada

- `Nota` usa encapsulamento e valida os dados antes de alterar seu estado.
- `Material` e uma classe abstrata que define o contrato dos materiais teoricos.
- `MaterialJava` herda de `Material` e implementa `gerarConteudo`, demonstrando polimorfismo.
- `NotaRepository` define uma abstracao para persistencia.
- `NotaRepositoryMemoria` implementa o repositorio em memoria.
- `NotaService` concentra as regras de criacao, atualizacao e consulta de notas.

## API de anotacoes

Com o servidor em execucao, a API oferece:

- `GET /api/notas` para listar anotacoes
- `POST /api/notas` para criar uma anotacao
- `PUT /api/notas/{id}` para editar uma anotacao
- `DELETE /api/notas/{id}` para apagar uma anotacao
- `GET /api/materiais/{topico}/{dificuldade}` para carregar um material Markdown
- `GET /api/materiais/{topico}/{dificuldade}/md` para baixar o material original em Markdown

## API de autenticacao

- `POST /api/auth/cadastro` para criar uma conta
- `POST /api/auth/login` para iniciar uma sessao por cookie
- `GET /api/auth/me` para verificar a sessao atual
- `POST /api/auth/logout` para invalidar a sessao

As rotas de anotacoes e materiais exigem uma sessao valida. As senhas sao armazenadas com hash PBKDF2 e os cookies usam `HttpOnly` e `SameSite=Lax`.

O frontend React funciona como cliente dessa API. Os dados ficam em `backend/data/notas.db`, que nao e versionado.

## Executar o backend

Na raiz do backend:

```bash
javac -d out $(Get-ChildItem -Recurse -Filter *.java | ForEach-Object FullName)
java -cp out com.javastudio.Application
```

O servidor ficara disponivel em `http://localhost:8080`. O download preserva o Markdown original para manter a formatacao de titulos, tabelas e blocos de codigo.

Os materiais ficam organizados em `backend/materials/{topico}/`, com um arquivo Markdown para cada dificuldade. Atualmente estao disponiveis os topicos `fundamentos`, `orientacao-a-objetos`, `collections`, `excecoes` e `spring`.