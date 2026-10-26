# Atividades de POO em Java: Exceções e JDBC (MySQL)

Lista de atividades da disciplina de **Programação Orientada a Objetos**, Engenharia de Software, Unicesumar Londrina (Prof. Cesar A. Malvezi). Entrega em 26/10/2026.

**Aluna:** Yasmin Fernanda de Carvalho

## Atenção: este repositório contém DUAS atividades

As duas **Atividades Extras** da lista estão no **mesmo projeto**, cada uma em seu próprio pacote, com seu próprio banco de dados, seu próprio `Main` e suas próprias classes. Elas **não dependem uma da outra** e podem ser executadas separadamente.

| Atividade | Pacote | Banco de dados | Script SQL | Classe principal |
|---|---|---|---|---|
| **Atividade 1:** Sistema de Biblioteca | `br.edu.biblioteca` | `biblioteca` | `biblioteca.sql` | `br.edu.biblioteca.Main` |
| **Atividade 2:** Controle de Estoque e Vendas | `br.edu.loja` | `loja` | `loja.sql` | `br.edu.loja.Main` |

> A **Parte A** da lista (exercícios 1.1 a 3.2, feitos sobre o projeto `crud-alunos` da aula) **não faz parte deste repositório**.

Os dois sistemas usam as mesmas regras exigidas na lista: acesso ao banco com `PreparedStatement` e try-with-resources, nenhum `catch` vazio, exceções personalizadas e organização em pacotes `model`, `dao`, `conexao` e `exception`.

## Estrutura do projeto

```
AtividadeJava/
├── lib/
│   └── mysql-connector-j-9.1.0.jar
├── src/br/edu/
│   ├── biblioteca/                  (Atividade 1)
│   │   ├── Main.java
│   │   ├── conexao/ConnectionFactory.java
│   │   ├── model/Livro.java, Emprestimo.java
│   │   ├── dao/LivroDAO.java, EmprestimoDAO.java
│   │   └── exception/DAOException.java, LivroNaoEncontradoException.java,
│   │                 LivroIndisponivelException.java
│   └── loja/                        (Atividade 2)
│       ├── Main.java
│       ├── conexao/ConnectionFactory.java
│       ├── model/Produto.java, Venda.java
│       ├── dao/ProdutoDAO.java, VendaDAO.java
│       ├── util/LogErros.java
│       └── exception/DAOException.java, ProdutoNaoEncontradoException.java,
│                     EstoqueInsuficienteException.java, QuantidadeInvalidaException.java
├── biblioteca.sql
├── loja.sql
└── README.md
```

Os dois pacotes têm classes com o mesmo nome (`Main`, `ConnectionFactory`, `DAOException`). Isso não gera conflito, porque cada uma pertence ao seu pacote.

## Como executar

**Requisitos:** JDK 17 ou superior, VS Code com o *Extension Pack for Java*, e MySQL (XAMPP).

1. Inicie o **MySQL** no XAMPP (e o Apache, se quiser usar o phpMyAdmin).
2. Crie os bancos executando os scripts (phpMyAdmin > aba **SQL**, ou o cliente MySQL):
   - `biblioteca.sql` para a Atividade 1;
   - `loja.sql` para a Atividade 2.
3. Confira o usuário e a senha na classe `ConnectionFactory` de cada pacote. O padrão é usuário `root` e senha vazia (padrão do XAMPP).
4. Abra o projeto no VS Code. O driver `mysql-connector-j-9.1.0.jar` está em `lib/` e deve aparecer em *Java Projects > Referenced Libraries*.
5. Execute **o `Main` da atividade desejada** (abra o arquivo e clique em **Run**):
   - Atividade 1: `src/br/edu/biblioteca/Main.java`
   - Atividade 2: `src/br/edu/loja/Main.java`

Ambas funcionam em modo console, com menu numérico. Entradas inválidas (letras no lugar de números) não encerram o programa.

---

## Atividade 1: Sistema de Biblioteca

Controle de livros e empréstimos.

**Funcionalidades**
- CRUD completo de livros (cadastrar, listar, buscar por ID, atualizar e excluir);
- busca de livros por parte do título (`LIKE`);
- registro de empréstimo (ID do livro e nome do leitor);
- registro de devolução;
- listagem de empréstimos em aberto (sem data de devolução).

**Tratamento de exceções**

| Exceção | Tipo | Quando é lançada |
|---|---|---|
| `LivroNaoEncontradoException` | Checked | Buscar, atualizar, excluir ou emprestar um ID inexistente |
| `LivroIndisponivelException` | Checked | Emprestar um livro que já está emprestado |
| `DAOException` | Unchecked | Embrulha qualquer `SQLException` da camada DAO |

**Outras regras implementadas**
- Validações na classe `Livro` (`IllegalArgumentException`): título e autor obrigatórios, ano entre 1450 e o ano atual, ISBN não vazio.
- ISBN duplicado tratado com mensagem amigável.
- Exclusão de livro com empréstimos bloqueada, com tratamento da violação de chave estrangeira.
- Empréstimo e devolução executados em **transação** (`commit` e `rollback`).

---

## Atividade 2: Controle de Estoque e Vendas

Controle de produtos e vendas de uma pequena loja. Cada venda reduz o estoque automaticamente e o sistema impede vendas sem estoque suficiente.

**Funcionalidades**
- CRUD completo de produtos;
- registro de venda (ID do produto e quantidade), com o valor total calculado pelo sistema;
- reposição de estoque;
- relatório de produtos com estoque baixo (quantidade menor que 5);
- relatório de vendas do dia, com o valor total vendido (`SUM`).

**Tratamento de exceções**

| Exceção | Tipo | Quando é lançada |
|---|---|---|
| `ProdutoNaoEncontradoException` | Checked | Operar sobre um ID de produto inexistente |
| `EstoqueInsuficienteException` | Checked | Venda maior que o estoque; a mensagem informa o estoque atual |
| `QuantidadeInvalidaException` | Unchecked | Quantidade de venda ou reposição menor ou igual a zero |
| `DAOException` | Unchecked | Embrulha qualquer `SQLException` |

**Outras regras implementadas**
- A venda é uma **transação**: insere em `venda` e atualiza `produto.quantidade`; qualquer falha executa `rollback`.
- `BigDecimal` para valores monetários e validação de preço maior que zero na classe `Produto`.
- Código de produto duplicado tratado com mensagem amigável.
- Classe `LogErros`: toda exceção capturada no `Main` é gravada no arquivo `erros.log` (data/hora, tipo da exceção e mensagem), usando try-with-resources com `BufferedWriter`/`FileWriter` e tratamento da `IOException`.
- O programa não encerra por exceção não tratada.

Exemplo de linha do `erros.log`:

```
[2026-10-26 19:40:12] EstoqueInsuficienteException - Estoque insuficiente para "Mouse USB". Disponível: 3, solicitado: 10
```