# Java Studio

Plataforma web e mobile para estudar a linguagem Java, revisar conceitos, criar anotações e testar conhecimentos por meio de quizzes.

## Status do projeto

O frontend está em desenvolvimento e já possui uma interface navegável com:

- Página inicial com conteúdo introdutório sobre Java
- Área de anotações
- Salvamento das anotações no backend com validação de título e conteúdo
- API Java para criar, editar, listar e apagar anotações
- Área de materiais com seleção de tópico e dificuldade
- Área de quiz reservada para perguntas teóricas
- Perfil do usuário
- Alternância entre modo claro e escuro
- Layout responsivo para desktop e dispositivos menores

O backend em Java e o aplicativo mobile em Java com layouts XML estão reservados para as próximas etapas.

## Estrutura

```text
Integrador202602/
├── frontend/   # Aplicação web em React + Vite
├── backend/    # Aplicação backend em Java
├── mobile/     # Aplicativo mobile em Java com layouts XML
└── README.md
```

## Tecnologias

### Frontend

- React
- Vite
- JavaScript
- CSS

### Backend

Reservado para Java.

### Mobile

Reservado para Java Android com layouts XML.

## Como executar o frontend

Entre na pasta do frontend e instale as dependências:

```bash
cd frontend
npm install
```

Inicie o servidor de desenvolvimento:

```bash
npm run dev
```

Depois, abra o endereço exibido pelo Vite, normalmente:

```text
http://127.0.0.1:5173/
```

## Como gerar o build

Para criar a versão de produção do frontend:

```bash
cd frontend
npm run build
```

## Próximas etapas

- Implementar a API do backend em Java
- Conectar o frontend à API
- Adicionar autenticação de usuários e associar anotações a cada conta
- Gerar materiais teóricos a partir do tópico e dificuldade escolhidos
- Permitir o download dos materiais em PDF
- Criar o banco de perguntas do quiz
- Implementar o aplicativo mobile
- Adicionar autenticação e progresso de estudos

## Repositório

[Projeto Integrador 2026.02](https://github.com/matheusleaomesquitarincon/ProjetoIntegrador-202602)
