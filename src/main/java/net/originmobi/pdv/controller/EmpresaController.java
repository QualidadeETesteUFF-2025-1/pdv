package net.originmobi.pdv.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import net.originmobi.pdv.dto.EmpresaDTO;
import net.originmobi.pdv.model.Cidade;
import net.originmobi.pdv.model.RegimeTributario;
import net.originmobi.pdv.model.TipoAmbiente;
import net.originmobi.pdv.service.CidadeService;
import net.originmobi.pdv.service.EmpresaService;
import net.originmobi.pdv.service.RegimeTributarioService;
import net.originmobi.pdv.service.TipoAmbienteServer;

@Controller
@RequestMapping("/empresa")
public class EmpresaController {

	private static final String EMPRESA_FORM = "empresa/form";

	@Autowired
	private EmpresaService empresas;

	@Autowired
	private RegimeTributarioService regimesTributarios;

	@Autowired
	private CidadeService cidades;

	@Autowired
	private TipoAmbienteServer ambientes;

	@GetMapping
	public ModelAndView form() {
		ModelAndView mv = new ModelAndView(EMPRESA_FORM);
		mv.addObject("empresa", empresas.verificaEmpresaCadastrada());
		return mv;
	}

	@RequestMapping(method = RequestMethod.POST)
	public String cadastra(@RequestParam Map<String, String> request, RedirectAttributes attributes) {
		EmpresaDTO empresaDTO = new EmpresaDTO();

		String strCodigo = request.get("codigo");
		empresaDTO.setNome(request.get("nome"));
		empresaDTO.setNomeFantasia(request.get("nome_fantasia"));
		empresaDTO.setCnpj(request.get("cnpj"));
		empresaDTO.setIe(request.get("ie"));
		String vlSerie = request.get("parametro.serie_nfe");
		empresaDTO.setCodRegime(Long.decode(request.get("regime_tributario")));

		empresaDTO.setCodCidade(Long.decode(request.get("endereco.cidade")));
		String strCodEnde = request.get("endereco.codigo");
		empresaDTO.setRua(request.get("endereco.rua"));
		empresaDTO.setBairro(request.get("endereco.bairro"));
		empresaDTO.setNumero(request.get("endereco.numero"));
		empresaDTO.setCep(request.get("endereco.cep"));
		empresaDTO.setReferencia(request.get("endereco.referencia"));
		String tipoAmbiente = request.get("parametro.ambiente");
		String aliqCred = request.get("parametro.pCredSN").replace(",", ".");

		empresaDTO.setAmbiente(tipoAmbiente.isEmpty() ? null : Integer.parseInt(tipoAmbiente));
		empresaDTO.setSerie(vlSerie.isEmpty() ? 0 : Integer.parseInt(vlSerie));
		empresaDTO.setCodigo(strCodigo.isEmpty() ? null : Long.decode(strCodigo));
		empresaDTO.setCodEndereco(strCodEnde.isEmpty() ? null : Long.decode(strCodEnde));
		empresaDTO.setAliqCalcCredito(aliqCred.isEmpty() ? 0.0 : Double.valueOf(aliqCred)); 

		String mensagem = empresas.merger(empresaDTO);
		attributes.addFlashAttribute("mensagem", mensagem);

		return "redirect:/empresa";
	}

	@ModelAttribute("regimeTributario")
	private List<RegimeTributario> regimeTributario() {
		return regimesTributarios.lista();
	}

	@ModelAttribute("cidades")
	private List<Cidade> cidades() {
		return cidades.lista();
	}

	@ModelAttribute("ambientes")
	private List<TipoAmbiente> ambientes() {
		return ambientes.lista();
	}

}
