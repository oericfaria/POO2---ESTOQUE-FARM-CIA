package util;

import exception.ValidacaoException;
import model.Fornecedor;
import model.Medicamento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.regex.Pattern;

/** Concentra todas as regras de validação do sistema. */
public final class Validador {

    public static final Pattern PADRAO_CODIGO = Pattern.compile("^[A-Za-z0-9]{7}$");
    private static final Pattern PADRAO_EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");
    private static final Set<String> UFS = Set.of(
        "AC","AL","AP","AM","BA","CE","DF","ES","GO","MA","MT","MS","MG",
        "PA","PB","PR","PE","PI","RJ","RN","RS","RO","RR","SC","SP","SE","TO");
    private static final int TAMANHO_MINIMO_NOME = 3;

    private Validador() { }

    public static void validarMedicamento(Medicamento m) throws ValidacaoException {
        if (m == null) throw new ValidacaoException("Medicamento não informado.");

        if (m.getCodigo() == null || !PADRAO_CODIGO.matcher(m.getCodigo()).matches()) {
            throw new ValidacaoException("Código inválido: use exatamente 7 caracteres alfanuméricos (ex.: MED0001).");
        }
        if (m.getNome() == null || m.getNome().isBlank()) {
            throw new ValidacaoException("O nome não pode ser vazio.");
        }
        if (m.getNome().trim().length() < TAMANHO_MINIMO_NOME) {
            throw new ValidacaoException("O nome deve ter pelo menos " + TAMANHO_MINIMO_NOME + " caracteres.");
        }
        if (m.getPrincipioAtivo() == null || m.getPrincipioAtivo().isBlank()) {
            throw new ValidacaoException("O princípio ativo não pode ser vazio.");
        }
        if (m.getDataValidade() == null) {
            throw new ValidacaoException("Informe a data de validade.");
        }
        if (m.getDataValidade().isBefore(LocalDate.now())) {
            throw new ValidacaoException("A data de validade não pode ser uma data passada.");
        }
        if (m.getQuantidadeEstoque() < 0) {
            throw new ValidacaoException("A quantidade em estoque não pode ser negativa.");
        }
        if (m.getPreco() == null || m.getPreco().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidacaoException("O preço deve ser um valor positivo.");
        }
        validarFornecedor(m.getFornecedor());
    }

    public static void validarFornecedor(Fornecedor f) throws ValidacaoException {
        if (f == null) throw new ValidacaoException("Fornecedor não informado.");

        if (!CnpjUtil.isValido(f.getCnpj())) {
            throw new ValidacaoException("CNPJ inválido (verifique o formato e os dígitos verificadores).");
        }
        if (f.getRazaoSocial() == null || f.getRazaoSocial().isBlank()) {
            throw new ValidacaoException("A razão social do fornecedor não pode ser vazia.");
        }
        if (f.getTelefone() == null || CnpjUtil.somenteDigitos(f.getTelefone()).length() < 10) {
            throw new ValidacaoException("Telefone inválido: informe DDD + número (mínimo 10 dígitos).");
        }
        if (f.getEmail() == null || !PADRAO_EMAIL.matcher(f.getEmail()).matches()) {
            throw new ValidacaoException("E-mail do fornecedor inválido.");
        }
        if (f.getCidade() == null || f.getCidade().isBlank()) {
            throw new ValidacaoException("A cidade do fornecedor não pode ser vazia.");
        }
        if (f.getEstado() == null || !UFS.contains(f.getEstado().toUpperCase())) {
            throw new ValidacaoException("Estado inválido: informe a sigla da UF (ex.: GO).");
        }
    }
}
