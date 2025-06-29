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

import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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

        verify(empresas, times(1)).save(empresaMock);
    }

    @Test
    public void cadastro_DeveTratarExcecaoSemLancar() {
        doThrow(new RuntimeException("Erro ao salvar")).when(empresas).save(empresaMock);

        empresaService.cadastro(empresaMock);

        verify(empresas, times(1)).save(empresaMock);
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
        doNothing().when(parametros).update(anyInt(), anyInt(), anyDouble());
        doNothing().when(enderecos).update(anyLong(), anyLong(), anyString(), anyString(), anyString(), anyString(), anyString());

        String resultado = empresaService.merger(empresaDTO);

        assertEquals("Empresa salva com sucesso", resultado);

        verify(empresas, times(1)).update(eq(empresaDTO.getCodigo()), eq(empresaDTO.getNome()), eq(empresaDTO.getNomeFantasia()), eq(empresaDTO.getCnpj()), eq(empresaDTO.getIe()), eq(empresaDTO.getCodRegime()));
        verify(parametros, times(1)).update(empresaDTO.getSerie(), empresaDTO.getAmbiente(), empresaDTO.getAliqCalcCredito());
        verify(enderecos, times(1)).update(empresaDTO.getCodEndereco(), empresaDTO.getCodCidade(), empresaDTO.getRua(), empresaDTO.getBairro(), empresaDTO.getNumero(), empresaDTO.getCep(), empresaDTO.getReferencia());
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

        doThrow(new RuntimeException("Erro update empresa")).when(empresas).update(anyLong(), anyString(), anyString(), anyString(), anyString(), anyLong());

        String resultado = empresaService.merger(empresaDTO);

        assertEquals("Erro ao salvar dados da empresa, chame o suporte", resultado);

        verify(empresas, times(1)).update(anyLong(), anyString(), anyString(), anyString(), anyString(), anyLong());
        verify(parametros, never()).update(anyInt(), anyInt(), anyDouble());
        verify(enderecos, never()).update(anyLong(), anyLong(), anyString(), anyString(), anyString(), anyString(), anyString());
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

        verify(parametros, times(1)).save(any(EmpresaParametro.class));
        verify(enderecos, times(1)).cadastrar(any(Endereco.class));
        verify(empresas, times(1)).save(any(Empresa.class));
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

        verify(parametros, times(1)).save(any(EmpresaParametro.class));
        verify(enderecos, never()).cadastrar(any(Endereco.class));
        verify(empresas, never()).save(any(Empresa.class));
    }
}
