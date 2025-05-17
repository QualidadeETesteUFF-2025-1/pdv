package net.originmobi.pdv.service;

import net.originmobi.pdv.enumerado.caixa.EstiloLancamento;
import net.originmobi.pdv.enumerado.caixa.TipoLancamento;
import net.originmobi.pdv.model.Caixa;
import net.originmobi.pdv.model.CaixaLancamento;
import net.originmobi.pdv.repository.CaixaLancamentoRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CaixaLancamentoServiceTest {

    @Mock
    private CaixaLancamentoRepository caixaLancamentoRepository;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private CaixaLancamentoService caixaLancamentoService;

    private CaixaLancamento lancamento;
    private Caixa caixa;

    @Before
    public void setUp() {
        caixa = new Caixa();
        caixa.setValor_total(1000.0);
        caixa.setData_fechamento(null);

        lancamento = new CaixaLancamento();
        lancamento.setCaixa(caixa);
        lancamento.setValor(100.0);
        lancamento.setObservacao("");
    }

    @Test
    public void shouldReturnSuccessMessageWhenSuprimentoIsCreated() {
        lancamento.setEstilo(EstiloLancamento.ENTRADA);
        lancamento.setTipo(TipoLancamento.SUPRIMENTO);
        when(caixaLancamentoRepository.save(any(CaixaLancamento.class))).thenReturn(lancamento);
        
        String resultado = caixaLancamentoService.lancamento(lancamento);
        
        assertEquals("Lançamento realizado com sucesso", resultado);
    }
    
    @Test
    public void shouldAddTimestampWhenLancamentoIsCreated() {
        lancamento.setEstilo(EstiloLancamento.ENTRADA);
        lancamento.setTipo(TipoLancamento.SUPRIMENTO);
        when(caixaLancamentoRepository.save(any(CaixaLancamento.class))).thenReturn(lancamento);
        
        caixaLancamentoService.lancamento(lancamento);
        
        assertNotNull(lancamento.getData_cadastro());
    }
    
    @Test
    public void shouldSaveSuprimentoLancamentoToRepository() {
        lancamento.setEstilo(EstiloLancamento.ENTRADA);
        lancamento.setTipo(TipoLancamento.SUPRIMENTO);
        when(caixaLancamentoRepository.save(any(CaixaLancamento.class))).thenReturn(lancamento);
        
        caixaLancamentoService.lancamento(lancamento);
        
        verify(caixaLancamentoRepository, times(1)).save(any(CaixaLancamento.class));
    }
    
    @Test
    public void shouldSetDefaultObservationForSuprimentoWhenEmpty() {
        lancamento.setEstilo(EstiloLancamento.ENTRADA);
        lancamento.setTipo(TipoLancamento.SUPRIMENTO);
        when(caixaLancamentoRepository.save(any(CaixaLancamento.class))).thenReturn(lancamento);
        
        caixaLancamentoService.lancamento(lancamento);
        
        assertEquals("Suprimento de caixa", lancamento.getObservacao());
    }

    @Test
    public void shouldKeepCustomObservationForSangria() {
        lancamento.setEstilo(EstiloLancamento.SAIDA);
        lancamento.setTipo(TipoLancamento.SANGRIA);
        lancamento.setObservacao("Observação personalizada");
        when(caixaLancamentoRepository.save(any(CaixaLancamento.class))).thenReturn(lancamento);
        
        caixaLancamentoService.lancamento(lancamento);
        
        assertEquals("Observação personalizada", lancamento.getObservacao());
    }
    
    @Test
    public void shouldConvertPositiveValueToNegativeForSaida() {
        lancamento.setEstilo(EstiloLancamento.SAIDA);
        lancamento.setTipo(TipoLancamento.SANGRIA);
        lancamento.setObservacao("Observação personalizada");
        lancamento.setValor(100.0);
        when(caixaLancamentoRepository.save(any(CaixaLancamento.class))).thenReturn(lancamento);
        
        caixaLancamentoService.lancamento(lancamento);
        
        assertEquals(-100.0, lancamento.getValor(), 0.001);
    }

    @Test
    public void shouldSetDefaultObservationForSangriaWhenEmpty() {
        lancamento.setEstilo(EstiloLancamento.SAIDA);
        lancamento.setTipo(TipoLancamento.SANGRIA);
        when(caixaLancamentoRepository.save(any(CaixaLancamento.class))).thenReturn(lancamento);
        
        caixaLancamentoService.lancamento(lancamento);
        
        assertEquals("Sangria de caixa", lancamento.getObservacao());
    }

    @Test
    public void shouldKeepNegativeValueForSaidaWhenAlreadyNegative() {
        lancamento.setEstilo(EstiloLancamento.SAIDA);
        lancamento.setTipo(TipoLancamento.SANGRIA);
        lancamento.setValor(-100.0);
        when(caixaLancamentoRepository.save(any(CaixaLancamento.class))).thenReturn(lancamento);
        
        caixaLancamentoService.lancamento(lancamento);
        
        assertEquals(-100.0, lancamento.getValor(), 0.001);
    }

    @Test
    public void shouldReturnInsufficientBalanceMessageWhenSaidaValueExceedsBalance() {
        lancamento.setEstilo(EstiloLancamento.SAIDA);
        lancamento.setValor(2000.0);
        
        String resultado = caixaLancamentoService.lancamento(lancamento);
        
        assertEquals("Saldo insuficiente para realizar esta operação", resultado);
    }
    
    @Test
    public void shouldNotSaveLancamentoWhenBalanceIsInsufficient() {
        lancamento.setEstilo(EstiloLancamento.SAIDA);
        lancamento.setValor(2000.0);
        
        caixaLancamentoService.lancamento(lancamento);
        
        verify(caixaLancamentoRepository, never()).save(any(CaixaLancamento.class));
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowExceptionWhenCaixaIsClosed() {
        Caixa caixaFechado = new Caixa();
        caixaFechado.setData_fechamento(new Timestamp(System.currentTimeMillis()));
        lancamento.setCaixa(caixaFechado);
        
        caixaLancamentoService.lancamento(lancamento);
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowExceptionWhenCaixaIsNull() {
        lancamento.setCaixa(null);
        
        caixaLancamentoService.lancamento(lancamento);
    }

    @Test(expected = RuntimeException.class)
    public void shouldPropagateExceptionWhenRepositorySaveThrowsException() {
        lancamento.setEstilo(EstiloLancamento.ENTRADA);
        doThrow(new RuntimeException("Erro de banco")).when(caixaLancamentoRepository).save(any(CaixaLancamento.class));
        
        caixaLancamentoService.lancamento(lancamento);
    }

    @Test
    public void shouldReturnAllLancamentosForGivenCaixa() {
        List<CaixaLancamento> lancamentos = new ArrayList<>();
        lancamentos.add(lancamento);
        when(caixaLancamentoRepository.findByCaixaEquals(caixa)).thenReturn(lancamentos);
        
        List<CaixaLancamento> resultado = caixaLancamentoService.lancamentosDoCaixa(caixa);
        
        assertEquals(1, resultado.size());
    }
    
    @Test
    public void shouldCallRepositoryMethodToFindLancamentos() {
        List<CaixaLancamento> lancamentos = new ArrayList<>();
        lancamentos.add(lancamento);
        when(caixaLancamentoRepository.findByCaixaEquals(caixa)).thenReturn(lancamentos);
        
        caixaLancamentoService.lancamentosDoCaixa(caixa);
        
        verify(caixaLancamentoRepository, times(1)).findByCaixaEquals(caixa);
    }
} 