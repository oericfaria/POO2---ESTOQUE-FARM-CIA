package ui;

import exception.PersistenciaException;
import exception.ValidacaoException;
import model.Fornecedor;
import model.Medicamento;
import repository.MedicamentoRepository;
import service.MedicamentoService;
import service.RelatorioService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ButtonType;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/** Controlador da tela principal: trata os eventos do formulário, da tabela e dos relatórios. */
public class MedicamentoController {

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final NumberFormat MOEDA = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    private static final String REL_VENCIMENTO = "Próximos ao vencimento (30 dias)";
    private static final String REL_ESTOQUE_BAIXO = "Estoque baixo (menos de 5 unidades)";
    private static final String REL_FORNECEDOR = "Valor total do estoque por fornecedor";
    private static final String REL_CONTROLADOS = "Controlados x não controlados";

    // ----- formulário -----
    @FXML private TextField txtCodigo;
    @FXML private TextField txtNome;
    @FXML private TextField txtDescricao;
    @FXML private TextField txtPrincipioAtivo;
    @FXML private DatePicker dpValidade;
    @FXML private TextField txtQuantidade;
    @FXML private TextField txtPreco;
    @FXML private CheckBox chkControlado;
    @FXML private TextField txtCnpj;
    @FXML private TextField txtRazaoSocial;
    @FXML private TextField txtTelefone;
    @FXML private TextField txtEmail;
    @FXML private TextField txtCidade;
    @FXML private TextField txtEstado;
    @FXML private TextField txtBusca;
    @FXML private Button btnCadastrar;
    @FXML private Button btnExcluir;
    @FXML private Label lblStatus;

    // ----- tabela -----
    @FXML private TableView<Medicamento> tabela;
    @FXML private TableColumn<Medicamento, String> colCodigo;
    @FXML private TableColumn<Medicamento, String> colNome;
    @FXML private TableColumn<Medicamento, String> colPrincipioAtivo;
    @FXML private TableColumn<Medicamento, String> colValidade;
    @FXML private TableColumn<Medicamento, String> colQuantidade;
    @FXML private TableColumn<Medicamento, String> colPreco;
    @FXML private TableColumn<Medicamento, String> colControlado;
    @FXML private TableColumn<Medicamento, String> colFornecedor;

    // ----- relatórios -----
    @FXML private ComboBox<String> cbRelatorio;
    @FXML private TextArea txtRelatorio;

    private final MedicamentoService servico =
        new MedicamentoService(new MedicamentoRepository(Path.of("data", "medicamentos.csv")));
    private final RelatorioService relatorios = new RelatorioService();
    private final ObservableList<Medicamento> dadosTabela = FXCollections.observableArrayList();

    /** Chamado automaticamente pelo FXMLLoader depois de injetar os campos @FXML. */
    @FXML
    private void initialize() {
        configurarColunas();
        tabela.setItems(dadosTabela);
        btnExcluir.disableProperty().bind(tabela.getSelectionModel().selectedItemProperty().isNull());

        cbRelatorio.setItems(FXCollections.observableArrayList(
            REL_VENCIMENTO, REL_ESTOQUE_BAIXO, REL_FORNECEDOR, REL_CONTROLADOS));
        cbRelatorio.getSelectionModel().selectFirst();

        try {
            servico.carregar();
            atualizarTabela(servico.listar());
            status(servico.listar().size() + " medicamento(s) carregado(s) do arquivo CSV.");
        } catch (PersistenciaException e) {
            erro("Erro ao carregar dados", e.getMessage());
            status("Falha ao carregar o arquivo CSV.");
        }
    }

    private void configurarColunas() {
        colCodigo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCodigo()));
        colNome.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNome()));
        colPrincipioAtivo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPrincipioAtivo()));
        colValidade.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDataValidade().format(DATA)));
        colQuantidade.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getQuantidadeEstoque())));
        colPreco.setCellValueFactory(c -> new SimpleStringProperty(MOEDA.format(c.getValue().getPreco())));
        colControlado.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isControlado() ? "Sim" : "Não"));
        colFornecedor.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFornecedor().getRazaoSocial()));
    }

    // ================= eventos =================

    @FXML
    private void aoCadastrar() {
        try {
            Medicamento m = lerFormulario();
            servico.cadastrar(m);
            atualizarTabela(servico.listar());
            limparFormulario();
            status("Medicamento " + m.getCodigo() + " cadastrado com sucesso.");
        } catch (ValidacaoException e) {
            aviso("Dados inválidos", e.getMessage());
        } catch (PersistenciaException e) {
            erro("Erro ao salvar", e.getMessage());
        }
    }

    @FXML
    private void aoLimpar() {
        limparFormulario();
        status("Formulário limpo.");
    }

    @FXML
    private void aoExcluir() {
        Medicamento selecionado = tabela.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            aviso("Exclusão", "Selecione um medicamento na tabela.");
            return;
        }
        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION,
            "Excluir o medicamento " + selecionado + "?", ButtonType.YES, ButtonType.NO);
        confirmacao.setHeaderText("Confirmar exclusão");
        Optional<ButtonType> resposta = confirmacao.showAndWait();
        if (resposta.isEmpty() || resposta.get() != ButtonType.YES) return;

        try {
            servico.excluir(selecionado.getCodigo());
            atualizarTabela(servico.listar());
            status("Medicamento " + selecionado.getCodigo() + " excluído.");
        } catch (ValidacaoException e) {
            aviso("Exclusão", e.getMessage());
        } catch (PersistenciaException e) {
            erro("Erro ao salvar", e.getMessage());
        }
    }

    @FXML
    private void aoConsultar() {
        String codigo = txtBusca.getText();
        if (codigo == null || codigo.isBlank()) {
            aviso("Consulta", "Informe o código do medicamento.");
            return;
        }
        Optional<Medicamento> encontrado = servico.buscarPorCodigo(codigo);
        if (encontrado.isPresent()) {
            atualizarTabela(List.of(encontrado.get()));
            tabela.getSelectionModel().selectFirst();
            status("Medicamento encontrado.");
        } else {
            atualizarTabela(List.of());
            status("Nenhum medicamento com o código " + codigo.trim().toUpperCase() + ".");
        }
    }

    @FXML
    private void aoListarTodos() {
        txtBusca.clear();
        atualizarTabela(servico.listar());
        status(dadosTabela.size() + " medicamento(s) listado(s).");
    }

    @FXML
    private void aoGerarRelatorio() {
        String escolhido = cbRelatorio.getValue();
        if (escolhido == null) {
            aviso("Relatórios", "Escolha um relatório.");
            return;
        }
        List<Medicamento> base = servico.listar();
        String texto = switch (escolhido) {
            case REL_VENCIMENTO -> relatorios.relatorioVencimento(base);
            case REL_ESTOQUE_BAIXO -> relatorios.relatorioEstoqueBaixo(base);
            case REL_FORNECEDOR -> relatorios.relatorioValorPorFornecedor(base);
            default -> relatorios.relatorioControlados(base);
        };
        txtRelatorio.setText(texto);
    }

    // ================= auxiliares =================

    /** Converte os campos de texto em objetos; erros de formato viram ValidacaoException. */
    private Medicamento lerFormulario() throws ValidacaoException {
        int quantidade;
        try {
            quantidade = Integer.parseInt(txtQuantidade.getText().trim());
        } catch (NumberFormatException e) {
            throw new ValidacaoException("Quantidade em estoque deve ser um número inteiro.");
        }

        BigDecimal preco;
        try {
            preco = new BigDecimal(txtPreco.getText().trim().replace(".", "").replace(",", "."));
        } catch (NumberFormatException e) {
            throw new ValidacaoException("Preço inválido. Use o formato 12,90.");
        }

        Fornecedor fornecedor = new Fornecedor(
            txtCnpj.getText(), txtRazaoSocial.getText(), txtTelefone.getText(),
            txtEmail.getText(), txtCidade.getText(), txtEstado.getText());

        return new Medicamento(
            txtCodigo.getText(), txtNome.getText(), txtDescricao.getText(),
            txtPrincipioAtivo.getText(), dpValidade.getValue(), quantidade, preco,
            chkControlado.isSelected(), fornecedor);
    }

    private void limparFormulario() {
        for (TextField t : List.of(txtCodigo, txtNome, txtDescricao, txtPrincipioAtivo, txtQuantidade,
                txtPreco, txtCnpj, txtRazaoSocial, txtTelefone, txtEmail, txtCidade, txtEstado)) {
            t.clear();
        }
        dpValidade.setValue(null);
        chkControlado.setSelected(false);
    }

    private void atualizarTabela(List<Medicamento> lista) {
        dadosTabela.setAll(lista);
    }

    private void status(String msg) { lblStatus.setText(msg); }

    private void aviso(String titulo, String msg) { alerta(Alert.AlertType.WARNING, titulo, msg); }

    private void erro(String titulo, String msg) { alerta(Alert.AlertType.ERROR, titulo, msg); }

    private void alerta(Alert.AlertType tipo, String titulo, String msg) {
        Alert a = new Alert(tipo, msg);
        a.setTitle(titulo);
        a.setHeaderText(titulo);
        a.showAndWait();
    }
}
