# Estoque de Medicamentos — Farmácia

Sistema desktop em **Java + JavaFX** para gerenciar o estoque de medicamentos de uma farmácia.
Trabalho P1 — Programação Orientada a Objetos III (UNIFAN).

## Funcionalidades

- Cadastrar medicamento (com os dados do fornecedor)
- Excluir medicamento (com confirmação)
- Consultar medicamento por código
- Listar todos os medicamentos em tabela
- Persistência em arquivo CSV separado por `;` (carregado ao iniciar e salvo após cada operação)
- Relatórios com Stream API: vencimento em 30 dias, estoque baixo (< 5), valor por fornecedor, controlados x não controlados
- Validação de dados: código, nome, validade, quantidade, preço e CNPJ (com dígitos verificadores)

## Requisitos

- **JDK 17 ou superior**
- **Maven 3.8+** (baixa o JavaFX automaticamente)

## Como executar

```bash
git clone <URL-DO-SEU-REPOSITORIO>
cd farmacia-estoque
mvn clean javafx:run
```

Para rodar os testes automatizados:

```bash
mvn test
```

> Na primeira execução o Maven baixa as dependências, então é necessária conexão com a internet.
> Na IDE (IntelliJ/Eclipse/NetBeans) importe como projeto Maven e execute a classe `ui.MainApp`
> (ou `Launcher`, caso a IDE reclame de "JavaFX runtime components are missing").

## Arquivo de dados

O arquivo fica em `data/medicamentos.csv` (criado automaticamente se não existir). Já vem com 6 medicamentos de exemplo.

```
codigo;nome;descricao;principioAtivo;dataValidade;quantidadeEstoque;preco;controlado;cnpj;razaoSocial;telefone;email;cidade;estado
MED0001;Dipirona 500mg;Analgésico;Dipirona sódica;2027-08-15;120;8.50;false;11.222.333/0001-81;Distribuidora Saúde Ltda;(62) 3200-1000;contato@saude.com.br;Goiânia;GO
```

Datas no formato `yyyy-MM-dd`; ponto-e-vírgula digitado em campos de texto é trocado por vírgula ao gravar.

## Regras de validação

| Campo | Regra |
|---|---|
| Código | exatamente 7 caracteres alfanuméricos (ex.: `MED0001`), único |
| Nome | não vazio, mínimo 3 caracteres |
| Validade | não pode ser data passada |
| Quantidade | inteiro não negativo |
| Preço | maior que zero (aceita `12,90`) |
| CNPJ | 14 dígitos + verificação dos dois dígitos verificadores |
| Fornecedor | razão social, telefone (DDD + número), e-mail, cidade e UF válida |

## Estrutura do projeto

```
src/main/java/
├── model/        Fornecedor, Medicamento
├── util/         CnpjUtil, Validador
├── exception/    ValidacaoException, PersistenciaException
├── repository/   MedicamentoRepository (CSV)
├── service/      MedicamentoService (CRUD), RelatorioService (Streams)
└── ui/           MainApp, Launcher, MedicamentoController
src/main/resources/view/main.fxml
src/test/java/FarmaciaTest.java
```
