# Relatório — Sistema de Estoque de Medicamentos

**Disciplina:** Programação Orientada a Objetos II  **Professor:** Daniel Corrêa da Silva
**Aluno:** Eric Faria dos Santos  **Curso:** Engenharia de Software - ESN4

## 1. Funcionalidades implementadas

- **CRUD:** cadastro, exclusão (com confirmação), consulta por código e listagem em `TableView`.
- **Persistência:** CSV com `;`, UTF-8. Os dados são lidos ao iniciar e o arquivo é regravado após cada cadastro ou exclusão.
- **Validações:** código (7 alfanuméricos, único), nome (mín. 3), validade (não passada), quantidade (≥ 0), preço (> 0), CNPJ (formato e dígitos verificadores), além de e-mail, telefone e UF do fornecedor.
- **Relatórios (4, todos com Stream API):**
  1. Próximos ao vencimento (30 dias) — `filter` + `sorted`
  2. Estoque baixo (< 5) — `filter` + `sorted`
  3. Valor total por fornecedor — `groupingBy` + `mapping` + `reducing`; total geral com `map` + `reduce`
  4. Controlados x não controlados — `partitioningBy`
- **Interface JavaFX (FXML):** aba de medicamentos (formulário, botões e tabela) e aba de relatórios.

## 2. Decisões de design

- **Camadas separadas:** `model` (entidades), `repository` (CSV), `service` (regras de negócio e relatórios), `ui` (controlador JavaFX). O controlador só trata eventos e delega o trabalho aos serviços.
- **Composição:** `Medicamento` contém um `Fornecedor`. Os dados do fornecedor ficam nas últimas colunas da mesma linha do CSV, mantendo um único arquivo, como pede o enunciado.
- **Validação centralizada** na classe `Validador`, executada no serviço (não no construtor), para que dados antigos do CSV, já vencidos, ainda possam ser carregados e consultados.
- **Exceções próprias:** `ValidacaoException` (erro do usuário, exibido como aviso) e `PersistenciaException` (erro de arquivo, exibido como erro). Se a gravação falhar, a alteração em memória é desfeita.
- **`BigDecimal` para dinheiro** e `LocalDate` para datas, evitando erros de arredondamento.
- **Chave do código sem diferenciar maiúsculas/minúsculas:** `med0001` e `MED0001` são o mesmo medicamento.
- **Testes unitários (JUnit 5)** para CNPJ, validações, persistência e relatórios.

## 3. Limitações

- Não há edição de medicamento (não exigida no enunciado).
- O CSV não usa aspas; por isso `;` e quebras de linha digitados pelo usuário são substituídos ao gravar.
