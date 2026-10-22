package service;

import exception.PersistenciaException;
import exception.ValidacaoException;
import model.Fornecedor;
import model.Medicamento;
import repository.MedicamentoRepository;
import util.CnpjUtil;
import util.Validador;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Regras de negócio do CRUD. Cada alteração é gravada imediatamente no CSV. */
public class MedicamentoService {

    private final MedicamentoRepository repositorio;
    private final Map<String, Medicamento> medicamentos = new LinkedHashMap<>();

    public MedicamentoService(MedicamentoRepository repositorio) {
        this.repositorio = repositorio;
    }

    /** Carrega os dados do arquivo (chamado ao iniciar o sistema). */
    public void carregar() throws PersistenciaException {
        medicamentos.clear();
        for (Medicamento m : repositorio.carregar()) {
            medicamentos.put(m.getCodigo().toUpperCase(), m);
        }
    }

    public void cadastrar(Medicamento m) throws ValidacaoException, PersistenciaException {
        Validador.validarMedicamento(m);
        normalizar(m);

        String chave = m.getCodigo().toUpperCase();
        if (medicamentos.containsKey(chave)) {
            throw new ValidacaoException("Já existe um medicamento com o código " + m.getCodigo() + ".");
        }
        medicamentos.put(chave, m);
        persistir(chave, null);
    }

    public void excluir(String codigo) throws ValidacaoException, PersistenciaException {
        String chave = codigo == null ? "" : codigo.trim().toUpperCase();
        Medicamento removido = medicamentos.remove(chave);
        if (removido == null) {
            throw new ValidacaoException("Medicamento não encontrado: " + codigo);
        }
        persistir(chave, removido);
    }

    public Optional<Medicamento> buscarPorCodigo(String codigo) {
        if (codigo == null) return Optional.empty();
        return Optional.ofNullable(medicamentos.get(codigo.trim().toUpperCase()));
    }

    public List<Medicamento> listar() {
        return new ArrayList<>(medicamentos.values());
    }

    // ---------- internos ----------

    private void normalizar(Medicamento m) {
        m.setCodigo(m.getCodigo().trim().toUpperCase());
        m.setNome(m.getNome().trim());
        m.setDescricao(m.getDescricao() == null ? "" : m.getDescricao().trim());
        m.setPrincipioAtivo(m.getPrincipioAtivo().trim());
        Fornecedor f = m.getFornecedor();
        f.setCnpj(CnpjUtil.formatar(f.getCnpj()));
        f.setRazaoSocial(f.getRazaoSocial().trim());
        f.setEstado(f.getEstado().trim().toUpperCase());
    }

    /** Grava no CSV; se falhar, desfaz a alteração em memória para manter tudo consistente. */
    private void persistir(String chave, Medicamento removido) throws PersistenciaException {
        try {
            repositorio.salvar(medicamentos.values());
        } catch (PersistenciaException e) {
            if (removido != null) medicamentos.put(chave, removido); else medicamentos.remove(chave);
            throw e;
        }
    }
}
