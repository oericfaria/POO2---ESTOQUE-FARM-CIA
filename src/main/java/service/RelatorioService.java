package service;

import model.Medicamento;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/** Relatórios gerenciais implementados com a API Stream (filter, map, reduce, collect). */
public class RelatorioService {

    public static final int DIAS_VENCIMENTO = 30;
    public static final int LIMITE_ESTOQUE_BAIXO = 5;

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final NumberFormat MOEDA = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    /** filter: vence entre hoje e hoje + dias (ainda não vencidos), ordenados pela validade. */
    public List<Medicamento> proximosAoVencimento(Collection<Medicamento> base, int dias) {
        LocalDate hoje = LocalDate.now();
        LocalDate limite = hoje.plusDays(dias);
        return base.stream()
            .filter(m -> !m.getDataValidade().isBefore(hoje) && !m.getDataValidade().isAfter(limite))
            .sorted(Comparator.comparing(Medicamento::getDataValidade))
            .collect(Collectors.toList());
    }

    /** filter: estoque menor que o limite, do menor para o maior. */
    public List<Medicamento> estoqueBaixo(Collection<Medicamento> base, int limite) {
        return base.stream()
            .filter(m -> m.getQuantidadeEstoque() < limite)
            .sorted(Comparator.comparingInt(Medicamento::getQuantidadeEstoque))
            .collect(Collectors.toList());
    }

    /** groupingBy + map + reduce: soma de (preço x quantidade) por fornecedor. */
    public Map<String, BigDecimal> valorTotalPorFornecedor(Collection<Medicamento> base) {
        return base.stream().collect(Collectors.groupingBy(
            m -> m.getFornecedor().getRazaoSocial(),
            TreeMap::new,
            Collectors.mapping(Medicamento::getValorTotalEstoque,
                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))));
    }

    /** map + reduce: valor total de todo o estoque. */
    public BigDecimal valorTotalGeral(Collection<Medicamento> base) {
        return base.stream()
            .map(Medicamento::getValorTotalEstoque)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** partitioningBy: true = controlados, false = não controlados. */
    public Map<Boolean, List<Medicamento>> controladosVsNaoControlados(Collection<Medicamento> base) {
        return base.stream().collect(Collectors.partitioningBy(Medicamento::isControlado));
    }

    // ---------- textos prontos para exibir na tela ----------

    public String relatorioVencimento(Collection<Medicamento> base) {
        List<Medicamento> lista = proximosAoVencimento(base, DIAS_VENCIMENTO);
        StringBuilder sb = new StringBuilder("MEDICAMENTOS PRÓXIMOS AO VENCIMENTO (próximos "
            + DIAS_VENCIMENTO + " dias)\n\n");
        if (lista.isEmpty()) return sb.append("Nenhum medicamento nessa condição.").toString();
        lista.forEach(m -> sb.append(String.format("%s | %-25s | vence em %s | %d un.%n",
            m.getCodigo(), m.getNome(), m.getDataValidade().format(DATA), m.getQuantidadeEstoque())));
        return sb.append("\nTotal: ").append(lista.size()).toString();
    }

    public String relatorioEstoqueBaixo(Collection<Medicamento> base) {
        List<Medicamento> lista = estoqueBaixo(base, LIMITE_ESTOQUE_BAIXO);
        StringBuilder sb = new StringBuilder("MEDICAMENTOS COM ESTOQUE BAIXO (menos de "
            + LIMITE_ESTOQUE_BAIXO + " unidades)\n\n");
        if (lista.isEmpty()) return sb.append("Nenhum medicamento nessa condição.").toString();
        lista.forEach(m -> sb.append(String.format("%s | %-25s | %d un. | Fornecedor: %s%n",
            m.getCodigo(), m.getNome(), m.getQuantidadeEstoque(), m.getFornecedor().getRazaoSocial())));
        return sb.append("\nTotal: ").append(lista.size()).toString();
    }

    public String relatorioValorPorFornecedor(Collection<Medicamento> base) {
        StringBuilder sb = new StringBuilder("VALOR TOTAL DO ESTOQUE POR FORNECEDOR\n\n");
        Map<String, BigDecimal> mapa = valorTotalPorFornecedor(base);
        if (mapa.isEmpty()) return sb.append("Nenhum medicamento cadastrado.").toString();
        mapa.forEach((fornecedor, total) ->
            sb.append(String.format("%-35s %s%n", fornecedor, MOEDA.format(total))));
        return sb.append("\nTOTAL GERAL: ").append(MOEDA.format(valorTotalGeral(base))).toString();
    }

    public String relatorioControlados(Collection<Medicamento> base) {
        Map<Boolean, List<Medicamento>> grupos = controladosVsNaoControlados(base);
        StringBuilder sb = new StringBuilder("MEDICAMENTOS CONTROLADOS x NÃO CONTROLADOS\n");
        anexarGrupo(sb, "CONTROLADOS", grupos.get(true));
        anexarGrupo(sb, "NÃO CONTROLADOS", grupos.get(false));
        return sb.toString();
    }

    private void anexarGrupo(StringBuilder sb, String titulo, List<Medicamento> lista) {
        sb.append("\n== ").append(titulo).append(" (").append(lista.size()).append(") ==\n");
        lista.forEach(m -> sb.append(String.format("%s | %-25s | %d un.%n",
            m.getCodigo(), m.getNome(), m.getQuantidadeEstoque())));
    }
}
