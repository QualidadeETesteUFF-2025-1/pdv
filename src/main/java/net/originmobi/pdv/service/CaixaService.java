package net.originmobi.pdv.service;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import net.originmobi.pdv.enumerado.caixa.CaixaTipo;
import net.originmobi.pdv.enumerado.caixa.EstiloLancamento;
import net.originmobi.pdv.enumerado.caixa.TipoLancamento;
import net.originmobi.pdv.filter.BancoFilter;
import net.originmobi.pdv.filter.CaixaFilter;
import net.originmobi.pdv.model.Caixa;
import net.originmobi.pdv.model.CaixaLancamento;
import net.originmobi.pdv.model.Usuario;
import net.originmobi.pdv.repository.CaixaRepository;
import net.originmobi.pdv.singleton.Aplicacao;

@Service
public class CaixaService {

	private String descricao;
	private Usuario usuario;

	@Autowired
	private CaixaRepository caixas;

	@Autowired
	private UsuarioService usuarios;

	@Autowired
	private CaixaLancamentoService lancamentos;

	private void validarCaixaAberto(Caixa caixa) {
		if (caixa.getTipo().equals(CaixaTipo.CAIXA) && caixaIsAberto()) {
			throw new RuntimeException("Existe caixa de dias anteriores em aberto, favor verifique");
		}
	}
	
	private void ajustarValorAbertura(Caixa caixa) {
		Double vlbertura = caixa.getValor_abertura() == null ? 0.0 : caixa.getValor_abertura();
		caixa.setValor_abertura(vlbertura);
	}
	
	private void validarValorAbertura(Caixa caixa) {
		if (caixa.getValor_abertura() < 0) {
			throw new RuntimeException("Valor informado é inválido");
		}
	}
	
	private String definirDescricao(Caixa caixa) {
		if (!caixa.getDescricao().isEmpty())
			return caixa.getDescricao();
	
		switch (caixa.getTipo()) {
			case CAIXA:
				return "Caixa diário";
			case COFRE:
				return "Cofre";
			case BANCO:
				return "Banco";
			default:
				return "Descrição indefinida";
		}
	}
	
	private void tratarDadosBancarios(Caixa caixa) {
		if (caixa.getTipo().equals(CaixaTipo.BANCO)) {
			caixa.setAgencia(caixa.getAgencia().replaceAll("\\D", ""));
			caixa.setConta(caixa.getConta().replaceAll("\\D", ""));
		}
	}
	
	private void salvarCaixa(Caixa caixa) {
		try {
			caixas.save(caixa);
		} catch (Exception e) {
			throw new RuntimeException("Erro no processo de abertura, chame o suporte técnico", e);
		}
	}
	
	private void registrarLancamentoInicial(Caixa caixa) {
		try {
			String observacao;
			switch (caixa.getTipo()) {
				case CAIXA:
					observacao = "Abertura de caixa";
					break;
				case COFRE:
					observacao = "Abertura de cofre";
					break;
				case BANCO:
					observacao = "Abertura de banco";
					break;
				default:
					observacao = "Abertura";
					break;
			}
	
			CaixaLancamento lancamento = new CaixaLancamento(
				observacao,
				caixa.getValor_abertura(),
				TipoLancamento.SALDOINICIAL,
				EstiloLancamento.ENTRADA,
				caixa,
				usuario
			);
	
			lancamentos.lancamento(lancamento);
		} catch (Exception e) {
			throw new RuntimeException("Erro no processo, chame o suporte", e);
		}
	}
	
	

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public Long cadastro(Caixa caixa) {
		validarCaixaAberto(caixa);
		ajustarValorAbertura(caixa);
		validarValorAbertura(caixa);
	
		usuario = usuarios.buscaUsuario(Aplicacao.getInstancia().getUsuarioAtual());
	
		caixa.setDescricao(definirDescricao(caixa));
		caixa.setUsuario(usuario);
		caixa.setData_cadastro(java.sql.Date.valueOf(LocalDate.now()));
	
		tratarDadosBancarios(caixa);
	
		salvarCaixa(caixa);
	
		if (caixa.getValor_abertura() > 0) {
			registrarLancamentoInicial(caixa);
		} else {
			caixa.setValor_total(0.0);
		}
	
		return caixa.getCodigo();
	}
	

	public String fechaCaixa(Long caixa, String senha) {

		Aplicacao aplicacao = Aplicacao.getInstancia();
		Usuario user = usuarios.buscaUsuario(aplicacao.getUsuarioAtual());

		BCryptPasswordEncoder decode = new BCryptPasswordEncoder();

		if (senha.equals(""))
			return "Favor, informe a senha";

		if (decode.matches(senha, user.getSenha())) {

			// busca caixa atual
			Optional<Caixa> caixaAtual = caixas.findById(caixa);

			if (caixaAtual.map(Caixa::getData_fechamento).isPresent())
				throw new RuntimeException("Caixa já esta fechado");

			Double valorTotal = caixaAtual.map(Caixa::getValor_total).orElse(0.0);


			Timestamp dataHoraAtual = new Timestamp(System.currentTimeMillis());
			caixaAtual.ifPresent(c -> c.setData_fechamento(dataHoraAtual));
			caixaAtual.ifPresent(c -> c.setValor_fechamento(valorTotal));

			try {
				caixaAtual.ifPresent(c -> caixas.save(c));
			} catch (Exception e) {
				throw new RuntimeException("Ocorreu um erro ao fechar o caixa, chame o suporte");
			}

			return "Caixa fechado com sucesso";

		} else {
			return "Senha incorreta, favor verifique";
		}
	}

	public boolean caixaIsAberto() {
		return caixas.caixaAberto().isPresent();
	}

	public List<Caixa> listaTodos() {
		return caixas.findByCodigoOrdenado();
	}

	public List<Caixa> listarCaixas(CaixaFilter filter) {
		if (filter.getData_cadastro() != null) {
			if (!filter.getData_cadastro().equals("")) {
				filter.setData_cadastro(filter.getData_cadastro().replace("/", "-"));
				return caixas.buscaCaixasPorDataAbertura(Date.valueOf(filter.getData_cadastro()));
			}
		}
		
		return caixas.listaCaixasAbertos();
	}

	public Optional<Caixa> caixaAberto() {
		return caixas.caixaAberto();
	}

	public List<Caixa> caixasAbertos() {
		return caixas.caixasAbertos();
	}

	public Optional<Caixa> busca(Long codigo) {
		return caixas.findById(codigo);
	}

	// pega o caixa aberto do usuário informado
	public Optional<Caixa> buscaCaixaUsuario(String usuario) {
		Usuario usu = usuarios.buscaUsuario(usuario);
		return Optional.ofNullable(caixas.findByCaixaAbertoUsuario(usu.getCodigo()));
	}

	public List<Caixa> listaBancos() {
		return caixas.buscaBancos(CaixaTipo.BANCO);
	}

	public List<Caixa> listaCaixasAbertosTipo(CaixaTipo tipo) {
		return caixas.buscaCaixaTipo(tipo);
	}

	public List<Caixa> listaBancosAbertosTipoFilterBanco(CaixaTipo tipo, BancoFilter filter) {
		if (filter.getData_cadastro() != null && !filter.getData_cadastro().equals("")) {
			filter.setData_cadastro(filter.getData_cadastro().replace("/", "-"));
			return caixas.buscaCaixaTipoData(tipo, Date.valueOf(filter.getData_cadastro()));
		}
		return caixas.buscaCaixaTipo(CaixaTipo.BANCO);
	}

}