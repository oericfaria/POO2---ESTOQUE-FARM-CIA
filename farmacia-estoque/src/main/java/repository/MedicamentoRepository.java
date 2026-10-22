package repository;

import exception.PersistenciaException;
import model.Fornecedor;
import model.Medicamento;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Persistência em arquivo CSV (UTF-8) com campos separados por ponto-e-vírgula.
 * Uma linha por medicamento; os dados do fornecedor ficam nas últimas colunas.
 */
public class MedicamentoRepository {

    private static final String SEPARADOR = ";";
    private static final String CABECALHO = String.join(SEPARADOR,
        "codigo", "nome", "descricao", "principioAtivo", "dataValidade", "quantidadeEstoque",
        "preco", "controlado", "cnpj", "razaoSocial", "telefone", "email", "cidade", "estado");
    private static final int TOTAL_COLUNAS = 14;

    private final Path arquivo;

    public MedicamentoRepository(Path arquivo) {
        this.arquivo = arquivo;
    }

    public Path getArquivo() { return arquivo; }

    /** Lê todos os medicamentos do arquivo. Se o arquivo não existir, devolve lista vazia. */
    public List<Medicamento> carregar() throws PersistenciaException {
        List<Medicamento> lista = new ArrayList<>();
        if (!Files.exists(arquivo)) return lista;

        try {
            List<String> linhas = Files.readAllLines(arquivo, StandardCharsets.UTF_8);
            for (int i = 1; i < linhas.size(); i++) { // pula o cabeçalho
                String linha = linhas.get(i);
                if (linha.isBlank()) continue;
                lista.add(fromCsv(linha, i + 1));
            }
        } catch (IOException e) {
            throw new PersistenciaException("Não foi possível ler o arquivo " + arquivo, e);
        }
        return lista;
    }

    /** Sobrescreve o arquivo com a coleção informada. */
    public void salvar(Collection<Medicamento> medicamentos) throws PersistenciaException {
        List<String> linhas = new ArrayList<>();
        linhas.add(CABECALHO);
        medicamentos.stream().map(this::toCsv).forEach(linhas::add);

        try {
            Path pasta = arquivo.toAbsolutePath().getParent();
            if (pasta != null) Files.createDirectories(pasta);
            Files.write(arquivo, linhas, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new PersistenciaException("Não foi possível gravar o arquivo " + arquivo, e);
        }
    }

    // ---------- conversão ----------

    private String toCsv(Medicamento m) {
        Fornecedor f = m.getFornecedor();
        return String.join(SEPARADOR,
            limpar(m.getCodigo()),
            limpar(m.getNome()),
            limpar(m.getDescricao()),
            limpar(m.getPrincipioAtivo()),
            m.getDataValidade().toString(),                 // ISO-8601: yyyy-MM-dd
            String.valueOf(m.getQuantidadeEstoque()),
            m.getPreco().toPlainString(),
            String.valueOf(m.isControlado()),
            limpar(f.getCnpj()),
            limpar(f.getRazaoSocial()),
            limpar(f.getTelefone()),
            limpar(f.getEmail()),
            limpar(f.getCidade()),
            limpar(f.getEstado()));
    }

    private Medicamento fromCsv(String linha, int numeroLinha) throws PersistenciaException {
        String[] c = linha.split(SEPARADOR, -1);
        if (c.length != TOTAL_COLUNAS) {
            throw new PersistenciaException("Linha " + numeroLinha + " do CSV inválida: esperado "
                + TOTAL_COLUNAS + " campos, encontrado " + c.length + ".", null);
        }
        try {
            Fornecedor f = new Fornecedor(c[8], c[9], c[10], c[11], c[12], c[13]);
            return new Medicamento(c[0], c[1], c[2], c[3], LocalDate.parse(c[4]),
                Integer.parseInt(c[5]), new BigDecimal(c[6]), Boolean.parseBoolean(c[7]), f);
        } catch (RuntimeException e) {
            throw new PersistenciaException("Linha " + numeroLinha + " do CSV com valor inválido: "
                + e.getMessage(), e);
        }
    }

    /** Impede que o texto digitado quebre o formato (remove ';' e quebras de linha). */
    private String limpar(String texto) {
        return texto == null ? "" : texto.replace(SEPARADOR, ",").replaceAll("[\\r\\n]+", " ").trim();
    }
}
