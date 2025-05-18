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
        Long codigo = 1L;
        String nome = "Nome";
        String nomeFantasia = "Fantasia";
        String cnpj = "123456789";
        String ie = "123";
        int serie = 1;
        int ambiente = 2;
        Long codRegime = 3L;
        Long codEndereco = 4L;
        Long codCidade = 5L;
        String rua = "Rua X";
        String bairro = "Bairro Y";
        String numero = "123";
        String cep = "00000-000";
        String referencia = "Ref";
        Double aliqCalcCredito = 10.0;

        doNothing().when(empresas).update(anyLong(), anyString(), anyString(), anyString(), anyString(), anyLong());
        doNothing().when(parametros).update(anyInt(), anyInt(), anyDouble());
        doNothing().when(enderecos).update(anyLong(), anyLong(), anyString(), anyString(), anyString(), anyString(), anyString());

        String resultado = empresaService.merger(codigo, nome, nomeFantasia, cnpj, ie, serie, ambiente, codRegime, codEndereco,
                codCidade, rua, bairro, numero, cep, referencia, aliqCalcCredito);

        assertEquals("Empresa salva com sucesso", resultado);

        verify(empresas, times(1)).update(eq(codigo), eq(nome), eq(nomeFantasia), eq(cnpj), eq(ie), eq(codRegime));
        verify(parametros, times(1)).update(serie, ambiente, aliqCalcCredito);
        verify(enderecos, times(1)).update(codEndereco, codCidade, rua, bairro, numero, cep, referencia);
    }

    @Test
    public void merger_ComCodigo_QuandoEmpresasUpdateLancaException_DeveRetornarMensagemErro() {
        Long codigo = 1L;

        doThrow(new RuntimeException("Erro update empresa")).when(empresas).update(anyLong(), anyString(), anyString(), anyString(), anyString(), anyLong());

        String resultado = empresaService.merger(codigo, "n", "nf", "cnpj", "ie", 1, 1, 1L, 1L, 1L, "r", "b", "n", "c", "ref", 1.0);

        assertEquals("Erro ao salvar dados da empresa, chame o suporte", resultado);

        verify(empresas, times(1)).update(anyLong(), anyString(), anyString(), anyString(), anyString(), anyLong());
        verify(parametros, never()).update(anyInt(), anyInt(), anyDouble());
        verify(enderecos, never()).update(anyLong(), anyLong(), anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void merger_SemCodigo_CaminhoSucesso() {
        Long codigo = null;
        String nome = "Empresa";
        String nomeFantasia = "Fantasia";
        String cnpj = "123";
        String ie = "456";
        int serie = 1;
        int ambiente = 2;
        Long codRegime = 10L;
        Long codcidade = 20L;
        String rua = "Rua Teste";
        String bairro = "Bairro Teste";
        String numero = "10";
        String cep = "12345-678";
        String referencia = "Referencia";
        Double aliqCalcCredito = 15.5;

        EmpresaParametro parametroMock = new EmpresaParametro();
        parametroMock.setAmbiente(ambiente);
        parametroMock.setSerie_nfe(serie);
        parametroMock.setpCredSN(aliqCalcCredito);

        when(regimes.busca(codRegime)).thenReturn(Optional.of(new RegimeTributario()));
        when(cidades.busca(codcidade)).thenReturn(Optional.of(new Cidade()));
        when(parametros.save(any(EmpresaParametro.class))).thenReturn(new EmpresaParametro());
        when(enderecos.cadastrar(any(Endereco.class))).thenReturn(new Endereco());
        when(empresas.save(any(Empresa.class))).thenReturn(new Empresa());

        String resultado = empresaService.merger(codigo, nome, nomeFantasia, cnpj, ie, serie, ambiente, codRegime, null,
                codcidade, rua, bairro, numero, cep, referencia, aliqCalcCredito);

        assertEquals("Empresa salva com sucesso", resultado);

        verify(parametros, times(1)).save(any(EmpresaParametro.class));
        verify(enderecos, times(1)).cadastrar(any(Endereco.class));
        verify(empresas, times(1)).save(any(Empresa.class));
    }

    @Test
    public void merger_SemCodigo_QuandoParametrosSaveLancaException_DeveRetornarMensagemErro() {
        Long codigo = null;
        int serie = 1;
        int ambiente = 2;
        Double aliqCalcCredito = 10.0;

        doThrow(new RuntimeException("Erro save parametro")).when(parametros).save(any(EmpresaParametro.class));

        String resultado = empresaService.merger(codigo, "n", "nf", "cnpj", "ie", serie, ambiente, 1L, 1L, 1L, "r", "b", "n", "c", "ref", aliqCalcCredito);

        assertEquals("Erro ao salvar dados da empresa, chame o suporte", resultado);

        verify(parametros, times(1)).save(any(EmpresaParametro.class));
        verify(enderecos, never()).cadastrar(any(Endereco.class));
        verify(empresas, never()).save(any(Empresa.class));
    }
}
