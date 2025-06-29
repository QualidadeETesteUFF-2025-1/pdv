package net.originmobi.pdv.service;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.originmobi.pdv.dto.EmpresaDTO;
import net.originmobi.pdv.model.Cidade;
import net.originmobi.pdv.model.Empresa;
import net.originmobi.pdv.model.EmpresaParametro;
import net.originmobi.pdv.model.Endereco;
import net.originmobi.pdv.model.RegimeTributario;
import net.originmobi.pdv.repository.EmpresaParametrosRepository;
import net.originmobi.pdv.repository.EmpresaRepository;

@Service
public class EmpresaService {
	private static final Logger logger = LoggerFactory.getLogger(EmpresaService.class);

	@Autowired
	private EmpresaRepository empresas;

	@Autowired
	private EmpresaParametrosRepository parametros;

	@Autowired
	private RegimeTributarioService regimes;

	@Autowired
	private CidadeService cidades;

	@Autowired
	private EnderecoService enderecos;

	public void cadastro(Empresa empresa) {
		try {
			empresas.save(empresa);
		} catch (Exception e) {
			logger.error("Erro ao salvar empresa no cadastro inicial", e);
		}
	}

	public Optional<Empresa> verificaEmpresaCadastrada() {
		Optional<Empresa> empresa = empresas.buscaEmpresaCadastrada();

		if (empresa.isPresent())
			return empresa;

		return Optional.empty();
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public String merger(EmpresaDTO empresaDTO) {
		String erro = "Erro ao salvar dados da empresa, chame o suporte";

		if (empresaDTO.getCodigo() != null) {
			if (!atualizarEmpresa(empresaDTO) || !atualizarParametros(empresaDTO) || !atualizarEndereco(empresaDTO))
				return erro;
		} else {
			if (!salvarParametrosENovaEmpresa(empresaDTO))
				return erro;
		}

		return "Empresa salva com sucesso";
	}

	private boolean atualizarEmpresa(EmpresaDTO dto) {
		try {
			empresas.update(dto.getCodigo(), dto.getNome(), dto.getNomeFantasia(), dto.getCnpj(), dto.getIe(), dto.getCodRegime());
			return true;
		} catch (Exception e) {
			logger.error("Erro ao atualizar dados da empresa [codigo={}]", dto.getCodigo(), e);
			return false;
		}
	}

	private boolean atualizarParametros(EmpresaDTO dto) {
		try {
			parametros.update(dto.getSerie(), dto.getAmbiente(), dto.getAliqCalcCredito());
			return true;
		} catch (Exception e) {
			logger.error("Erro ao atualizar parâmetros da empresa", e);
			return false;
		}
	}

	private boolean atualizarEndereco(EmpresaDTO dto) {
		try {
			enderecos.update(dto.getCodEndereco(), dto.getCodCidade(), dto.getRua(), dto.getBairro(),
					dto.getNumero(), dto.getCep(), dto.getReferencia());
			return true;
		} catch (Exception e) {
			logger.error("Erro ao atualizar endereço da empresa [enderecoId={}]", dto.getCodEndereco(), e);
			return false;
		}
	}

	private boolean salvarParametrosENovaEmpresa(EmpresaDTO dto) {
		try {
			EmpresaParametro parametro = new EmpresaParametro();
			parametro.setAmbiente(dto.getAmbiente());
			parametro.setSerie_nfe(dto.getSerie());
			parametro.setpCredSN(dto.getAliqCalcCredito());
			parametros.save(parametro);

			Optional<RegimeTributario> regime = regimes.busca(dto.getCodRegime());
			Optional<Cidade> cidade = cidades.busca(dto.getCodCidade());

			Endereco endereco = new Endereco(dto.getRua(), dto.getBairro(), dto.getNumero(), dto.getCep(),
					dto.getReferencia(), Date.valueOf(LocalDate.now()), cidade.orElse(null));
			enderecos.cadastrar(endereco);

			Empresa empresa = new Empresa(dto.getNome(), dto.getNomeFantasia(), dto.getCnpj(), dto.getIe(),
					regime.orElse(null), endereco, parametro);
			empresas.save(empresa);

			return true;
		} catch (Exception e) {
			logger.error("Erro ao salvar nova empresa [cnpj={}]", dto.getCnpj(), e);
			return false;
		}
	}
}
