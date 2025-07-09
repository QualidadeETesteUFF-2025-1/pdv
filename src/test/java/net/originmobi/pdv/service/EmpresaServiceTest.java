package net.originmobi.pdv.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import net.originmobi.pdv.repository.EmpresaParametrosRepository;
import net.originmobi.pdv.repository.EmpresaRepository;
import net.originmobi.pdv.model.Empresa;
import net.originmobi.pdv.model.EmpresaParametro;
import net.originmobi.pdv.model.RegimeTributario;
import net.originmobi.pdv.dto.EmpresaDTO;
import net.originmobi.pdv.model.Cidade;
import net.originmobi.pdv.model.Endereco;

import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EmpresaServiceTest {

    @Mock
    private EmpresaRepository empresas;

    @Mock
    private EmpresaParametrosRepository parametros;

    @Mock
    private RegimeTributarioService regimes;

    @Mock
    private CidadeService cidades;

    @Mock
    private EnderecoService enderecos;

    @InjectMocks
    private EmpresaService empresaService;

    private Empresa empresaMock;

    @Before
    public void setup() {
        empresaMock = mock(Empresa.class);
    }

    @Test
    public void cadastro_DeveSalvarEmpresa() {
        when(empresas.save(any(Empresa.class))).thenReturn(empresaMock);

        empresaService.cadastro(empresaMock);

    }

    @Test
    public void cadastro_DeveTratarExcecaoSemLancar() {
        doThrow(new RuntimeException("Erro ao salvar")).when(empresas).save(empresaMock);

        empresaService.cadastro(empresaMock);
    }

    @Test
    public void verificaEmpresaCadastrada_RetornaEmpresaSeExiste() {
        when(empresas.buscaEmpresaCadastrada()).thenReturn(Optional.of(empresaMock));

        Optional<Empresa> resultado = empresaService.verificaEmpresaCadastrada();

        assertTrue(resultado.isPresent());
        assertEquals(empresaMock, resultado.get());
    }

    @Test
    public void verificaEmpresaCadastrada_RetornaVazioSeNaoExiste() {
        when(empresas.buscaEmpresaCadastrada()).thenReturn(Optional.empty());

        Optional<Empresa> resultado = empresaService.verificaEmpresaCadastrada();

        assertFalse(resultado.isPresent());
    }

    @Test
    public void merger_ComCodigo_AtualizaComSucesso() {
        EmpresaDTO empresaDTO = new EmpresaDTO();
        empresaDTO.setCodigo(1L);
        empresaDTO.setNome("Nome");
        empresaDTO.setNomeFantasia("Fantasia");
        empresaDTO.setCnpj("123456789");
        empresaDTO.setIe("123");
        empresaDTO.setSerie(1);
        empresaDTO.setAmbiente(2);
        empresaDTO.setCodRegime(3L);
        empresaDTO.setCodEndereco(4L);
        empresaDTO.setCodCidade(5L);
        empresaDTO.setRua("Rua X");
        empresaDTO.setBairro("Bairro Y");
        empresaDTO.setNumero("123");
        empresaDTO.setCep("00000-000");
        empresaDTO.setReferencia("Ref");
        empresaDTO.setAliqCalcCredito(10.0);

        doNothing().when(empresas).update(anyLong(), anyString(), anyString(), anyString(), anyString(), anyLong());
        doNothing().when(enderecos).update(anyLong(), anyLong(), anyString(), anyString(), anyString(), anyString(),
                anyString());

        String resultado = empresaService.merger(empresaDTO);

        assertEquals("Empresa salva com sucesso", resultado);
    }

    @Test
    public void merger_ComCodigo_QuandoEmpresasUpdateLancaException_DeveRetornarMensagemErro() {
        EmpresaDTO empresaDTO = new EmpresaDTO();
        empresaDTO.setCodigo(1L);
        empresaDTO.setNome("n");
        empresaDTO.setNomeFantasia("nf");
        empresaDTO.setCnpj("cnpj");
        empresaDTO.setIe("ie");
        empresaDTO.setSerie(1);
        empresaDTO.setAmbiente(1);
        empresaDTO.setCodRegime(1L);
        empresaDTO.setCodEndereco(1L);
        empresaDTO.setCodCidade(1L);
        empresaDTO.setRua("r");
        empresaDTO.setBairro("b");
        empresaDTO.setNumero("n");
        empresaDTO.setCep("c");
        empresaDTO.setReferencia("ref");
        empresaDTO.setAliqCalcCredito(1.0);

        doThrow(new RuntimeException("Erro update empresa")).when(empresas).update(anyLong(), anyString(), anyString(),
                anyString(), anyString(), anyLong());

        String resultado = empresaService.merger(empresaDTO);

        assertEquals("Erro ao salvar dados da empresa, chame o suporte", resultado);
    }

    @Test
    public void merger_SemCodigo_CaminhoSucesso() {
        EmpresaDTO empresaDTO = new EmpresaDTO();
        empresaDTO.setCodigo(null);
        empresaDTO.setNome("Empresa");
        empresaDTO.setNomeFantasia("Fantasia");
        empresaDTO.setCnpj("123");
        empresaDTO.setIe("456");
        empresaDTO.setSerie(1);
        empresaDTO.setAmbiente(2);
        empresaDTO.setCodRegime(10L);
        empresaDTO.setCodCidade(20L);
        empresaDTO.setRua("Rua Teste");
        empresaDTO.setBairro("Bairro Teste");
        empresaDTO.setNumero("10");
        empresaDTO.setCep("12345-678");
        empresaDTO.setReferencia("Referencia");
        empresaDTO.setAliqCalcCredito(15.5);

        EmpresaParametro parametroMock = new EmpresaParametro();
        parametroMock.setAmbiente(empresaDTO.getAmbiente());
        parametroMock.setSerie_nfe(empresaDTO.getSerie());
        parametroMock.setpCredSN(empresaDTO.getAliqCalcCredito());

        when(regimes.busca(empresaDTO.getCodRegime())).thenReturn(Optional.of(new RegimeTributario()));
        when(cidades.busca(empresaDTO.getCodCidade())).thenReturn(Optional.of(new Cidade()));
        when(parametros.save(any(EmpresaParametro.class))).thenReturn(new EmpresaParametro());
        when(enderecos.cadastrar(any(Endereco.class))).thenReturn(new Endereco());
        when(empresas.save(any(Empresa.class))).thenReturn(new Empresa());

        String resultado = empresaService.merger(empresaDTO);

        assertEquals("Empresa salva com sucesso", resultado);
    }

    @Test
    public void merger_SemCodigo_QuandoParametrosSaveLancaException_DeveRetornarMensagemErro() {
        EmpresaDTO empresaDTO = new EmpresaDTO();
        empresaDTO.setCodigo(null);
        empresaDTO.setNome("n");
        empresaDTO.setNomeFantasia("nf");
        empresaDTO.setCnpj("cnpj");
        empresaDTO.setIe("ie");
        empresaDTO.setSerie(1);
        empresaDTO.setAmbiente(2);
        empresaDTO.setCodRegime(1L);
        empresaDTO.setCodEndereco(1L);
        empresaDTO.setCodCidade(1L);
        empresaDTO.setRua("r");
        empresaDTO.setBairro("b");
        empresaDTO.setNumero("n");
        empresaDTO.setCep("c");
        empresaDTO.setReferencia("ref");
        empresaDTO.setAliqCalcCredito(10.0);

        doThrow(new RuntimeException("Erro save parametro")).when(parametros).save(any(EmpresaParametro.class));

        String resultado = empresaService.merger(empresaDTO);

        assertEquals("Erro ao salvar dados da empresa, chame o suporte", resultado);
    }

    // ========== TESTES FUNCIONAIS ==========

    // PARTICIONAMENTO DE EQUIVALÊNCIA - CNPJ
    @Test
    public void merger_CnpjValido_DeveProcessarCorretamente() {
        // Classe válida: CNPJ com 14 dígitos
        EmpresaDTO dto = criarEmpresaDTOValida();
        dto.setCnpj("12345678901234");

        configurarMocksParaSucesso(dto);

        String resultado = empresaService.merger(dto);

        assertEquals("Empresa salva com sucesso", resultado);
    }

    @Test
    public void merger_CnpjInvalido_DeveTratarCorretamente() {
        // Classe inválida: CNPJ com menos de 14 dígitos
        EmpresaDTO dto = criarEmpresaDTOValida();
        dto.setCnpj("123");

        configurarMocksParaSucesso(dto);

        String resultado = empresaService.merger(dto);

        // Sistema deve processar mesmo com CNPJ inválido (validação externa)
        assertEquals("Empresa salva com sucesso", resultado);
    }

    // ANÁLISE DE VALOR LIMITE - SÉRIE NFE
    @Test
    public void merger_SerieMinima_DeveProcessar() {
        // Valor limite inferior: série = 1
        EmpresaDTO dto = criarEmpresaDTOValida();
        dto.setSerie(1);

        configurarMocksParaSucesso(dto);

        String resultado = empresaService.merger(dto);

        assertEquals("Empresa salva com sucesso", resultado);
    }

    @Test
    public void merger_SerieMaxima_DeveProcessar() {
        // Valor limite superior: série = 999
        EmpresaDTO dto = criarEmpresaDTOValida();
        dto.setSerie(999);

        configurarMocksParaSucesso(dto);

        String resultado = empresaService.merger(dto);

        assertEquals("Empresa salva com sucesso", resultado);
    }

    @Test
    public void merger_SerieZero_DeveProcessar() {
        // Valor limite: série = 0
        EmpresaDTO dto = criarEmpresaDTOValida();
        dto.setSerie(0);

        configurarMocksParaSucesso(dto);

        String resultado = empresaService.merger(dto);

        assertEquals("Empresa salva com sucesso", resultado);
    }

    // ANÁLISE DE VALOR LIMITE - ALÍQUOTA CRÉDITO
    @Test
    public void merger_AliquotaZero_DeveProcessar() {
        // Valor limite: 0.0%
        EmpresaDTO dto = criarEmpresaDTOValida();
        dto.setAliqCalcCredito(0.0);

        configurarMocksParaSucesso(dto);

        String resultado = empresaService.merger(dto);

        assertEquals("Empresa salva com sucesso", resultado);
    }

    @Test
    public void merger_AliquotaMaxima_DeveProcessar() {
        // Valor limite: 100.0%
        EmpresaDTO dto = criarEmpresaDTOValida();
        dto.setAliqCalcCredito(100.0);

        configurarMocksParaSucesso(dto);

        String resultado = empresaService.merger(dto);

        assertEquals("Empresa salva com sucesso", resultado);
    }

    // TESTE BASEADO EM DECISÃO - FLUXO CRIAR vs ATUALIZAR
    @Test
    public void merger_CodigoNulo_DeveExecutarFluxoCriacao() {
        // Decisão: código == null → fluxo de criação
        EmpresaDTO dto = criarEmpresaDTOValida();
        dto.setCodigo(null);

        configurarMocksParaSucesso(dto);

        String resultado = empresaService.merger(dto);

        assertEquals("Empresa salva com sucesso", resultado);
        verify(empresas).save(any(Empresa.class));
        verify(empresas, never()).update(anyLong(), anyString(), anyString(), anyString(), anyString(), anyLong());
    }

    @Test
    public void merger_CodigoPreenchido_DeveExecutarFluxoAtualizacao() {
        // Decisão: código != null → fluxo de atualização
        EmpresaDTO dto = criarEmpresaDTOValida();
        dto.setCodigo(1L);

        doNothing().when(empresas).update(anyLong(), anyString(), anyString(), anyString(), anyString(), anyLong());
        doNothing().when(enderecos).update(anyLong(), anyLong(), anyString(), anyString(), anyString(), anyString(),
                anyString());

        String resultado = empresaService.merger(dto);

        assertEquals("Empresa salva com sucesso", resultado);
        verify(empresas, never()).save(any(Empresa.class));
    }

    // PARTICIONAMENTO DE EQUIVALÊNCIA - AMBIENTE NFE
    @Test
    public void merger_AmbienteProducao_DeveProcessar() {
        // Classe válida: ambiente = 1 (produção)
        EmpresaDTO dto = criarEmpresaDTOValida();
        dto.setAmbiente(1);

        configurarMocksParaSucesso(dto);

        String resultado = empresaService.merger(dto);

        assertEquals("Empresa salva com sucesso", resultado);
    }

    @Test
    public void merger_AmbienteHomologacao_DeveProcessar() {
        // Classe válida: ambiente = 2 (homologação)
        EmpresaDTO dto = criarEmpresaDTOValida();
        dto.setAmbiente(2);

        configurarMocksParaSucesso(dto);

        String resultado = empresaService.merger(dto);

        assertEquals("Empresa salva com sucesso", resultado);
    }

    // TESTE DE ROBUSTEZ - DADOS EXTREMOS
    @Test
    public void merger_NomeVazio_DeveProcessar() {
        EmpresaDTO dto = criarEmpresaDTOValida();
        dto.setNome("");

        configurarMocksParaSucesso(dto);

        String resultado = empresaService.merger(dto);

        assertEquals("Empresa salva com sucesso", resultado);
    }

    @Test
    public void merger_NomeMuitoLongo_DeveProcessar() {
        EmpresaDTO dto = criarEmpresaDTOValida();
        dto.setNome(String.join("", Collections.nCopies(255, "A")));

        configurarMocksParaSucesso(dto);

        String resultado = empresaService.merger(dto);

        assertEquals("Empresa salva com sucesso", resultado);
    }

    // MÉTODOS AUXILIARES
    private EmpresaDTO criarEmpresaDTOValida() {
        EmpresaDTO dto = new EmpresaDTO();
        dto.setNome("Empresa Teste");
        dto.setNomeFantasia("Fantasia Teste");
        dto.setCnpj("12345678901234");
        dto.setIe("123456789");
        dto.setSerie(1);
        dto.setAmbiente(2);
        dto.setCodRegime(1L);
        dto.setCodCidade(1L);
        dto.setCodEndereco(1L);
        dto.setRua("Rua Teste");
        dto.setBairro("Bairro Teste");
        dto.setNumero("123");
        dto.setCep("12345-678");
        dto.setReferencia("Referência");
        dto.setAliqCalcCredito(5.0);
        return dto;
    }

    private void configurarMocksParaSucesso(EmpresaDTO dto) {
        when(regimes.busca(dto.getCodRegime())).thenReturn(Optional.of(new RegimeTributario()));
        when(cidades.busca(dto.getCodCidade())).thenReturn(Optional.of(new Cidade()));
        when(parametros.save(any(EmpresaParametro.class))).thenReturn(new EmpresaParametro());
        when(enderecos.cadastrar(any(Endereco.class))).thenReturn(new Endereco());
        when(empresas.save(any(Empresa.class))).thenReturn(new Empresa());
    }
}
        dto.setAmbiente(2);
        dto.setCodRegime(1L);
        dto.setCodCidade(1L);
        dto.setCodEndereco(1L);
        dto.setRua("Rua Teste");
        dto.setBairro("Bairro Teste");
        dto.setNumero("123");
        dto.setCep("12345-678");
        dto.setReferencia("Referência");
        dto.setAliqCalcCredito(5.0);
        return dto;
    }
    
    private void configurarMocksParaSucesso(EmpresaDTO dto) {
        when(regimes.busca(dto.getCodRegime())).thenReturn(Optional.of(new RegimeTributario()));
        when(cidades.busca(dto.getCodCidade())).thenReturn(Optional.of(new Cidade()));
        when(parametros.save(any(EmpresaParametro.class))).thenReturn(new EmpresaParametro());
        when(enderecos.cadastrar(any(Endereco.class))).thenReturn(new Endereco());
        when(empresas.save(any(Empresa.class))).thenReturn(new Empresa());
    }
}