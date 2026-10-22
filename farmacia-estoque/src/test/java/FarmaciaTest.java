
import exception.ValidacaoException;
import model.Fornecedor;
import model.Medicamento;
import repository.MedicamentoRepository;
import service.MedicamentoService;
import service.RelatorioService;
import util.CnpjUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FarmaciaTest {

    private static Fornecedor fornecedor() {
        return new Fornecedor("11.222.333/0001-81", "Distribuidora Saúde", "(62) 3200-1000",
            "contato@saude.com.br", "Goiânia", "GO");
    }

    private static Medicamento med(String codigo, LocalDate validade, int qtd, String preco, boolean controlado) {
        return new Medicamento(codigo, "Dipirona", "desc", "Dipirona sódica",
            validade, qtd, new BigDecimal(preco), controlado, fornecedor());
    }

    @Test
    void cnpjValidoEInvalido() {
        assertTrue(CnpjUtil.isValido("11.222.333/0001-81"));
        assertTrue(CnpjUtil.isValido("11222333000181"));
        assertFalse(CnpjUtil.isValido("11.222.333/0001-82"));
        assertFalse(CnpjUtil.isValido("00000000000000"));
        assertFalse(CnpjUtil.isValido("123"));
    }

    @Test
    void validacoesDeMedicamento() {
        LocalDate futuro = LocalDate.now().plusYears(1);
        assertDoesNotThrow(() -> util.Validador.validarMedicamento(med("MED0001", futuro, 10, "5.00", false)));

        assertThrows(ValidacaoException.class, () -> util.Validador.validarMedicamento(med("ABC", futuro, 10, "5.00", false)));
        assertThrows(ValidacaoException.class, () -> util.Validador.validarMedicamento(med("MED0001", LocalDate.now().minusDays(1), 10, "5.00", false)));
        assertThrows(ValidacaoException.class, () -> util.Validador.validarMedicamento(med("MED0001", futuro, -1, "5.00", false)));
        assertThrows(ValidacaoException.class, () -> util.Validador.validarMedicamento(med("MED0001", futuro, 10, "0", false)));
    }

    @Test
    void cadastrarPersisteEBloqueiaDuplicado(@TempDir Path pasta) throws Exception {
        Path csv = pasta.resolve("meds.csv");
        MedicamentoService servico = new MedicamentoService(new MedicamentoRepository(csv));
        LocalDate futuro = LocalDate.now().plusMonths(6);

        servico.cadastrar(med("MED0001", futuro, 10, "5.00", false));
        assertThrows(ValidacaoException.class, () -> servico.cadastrar(med("med0001", futuro, 1, "1.00", false)));

        // um novo serviço lendo o mesmo arquivo enxerga o dado salvo
        MedicamentoService outro = new MedicamentoService(new MedicamentoRepository(csv));
        outro.carregar();
        assertEquals(1, outro.listar().size());
        assertTrue(outro.buscarPorCodigo("med0001").isPresent());

        outro.excluir("MED0001");
        assertTrue(outro.listar().isEmpty());
        assertThrows(ValidacaoException.class, () -> outro.excluir("MED0001"));
    }

    @Test
    void relatoriosComStream() {
        LocalDate hoje = LocalDate.now();
        List<Medicamento> base = List.of(
            med("MED0001", hoje.plusDays(10), 2, "10.00", true),
            med("MED0002", hoje.plusDays(100), 50, "2.00", false),
            med("MED0003", hoje.minusDays(5), 5, "1.00", false));
        RelatorioService r = new RelatorioService();

        assertEquals(1, r.proximosAoVencimento(base, 30).size());
        assertEquals(1, r.estoqueBaixo(base, 5).size());
        assertEquals(new BigDecimal("125.00"), r.valorTotalGeral(base)); // 20 + 100 + 5

        Map<String, BigDecimal> porFornecedor = r.valorTotalPorFornecedor(base);
        assertEquals(new BigDecimal("125.00"), porFornecedor.get("Distribuidora Saúde"));

        Map<Boolean, List<Medicamento>> grupos = r.controladosVsNaoControlados(base);
        assertEquals(1, grupos.get(true).size());
        assertEquals(2, grupos.get(false).size());
    }
}
