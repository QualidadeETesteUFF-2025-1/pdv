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
import static org.junit.Assert.assertTrue;

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
    private static final String SUCCESS_MESSAGE = "Lançamento realizado com sucesso";
    private static final String INSUFFICIENT_BALANCE_MESSAGE = "Saldo insuficiente para realizar esta operação";
    private static final String SUPRIMENTO_DEFAULT_OBSERVATION = "Suprimento de caixa";
    private static final String SANGRIA_DEFAULT_OBSERVATION = "Sangria de caixa";

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

    // ========== TESTES DE ENTRADA (SUPRIMENTO) ==========
    
    @Test
    public void shouldProcessSuprimentoWithValidPositiveValue() {
        // Given
        lancamento.setEstilo(EstiloLancamento.ENTRADA);
        lancamento.setTipo(TipoLancamento.SUPRIMENTO);
        lancamento.setValor(100.0);
        when(caixaLancamentoRepository.save(any(CaixaLancamento.class))).thenReturn(lancamento);
        
        // When
        String result = caixaLancamentoService.lancamento(lancamento);
        
        // Then
        assertEquals(SUCCESS_MESSAGE, result);
        verify(caixaLancamentoRepository).save(any(CaixaLancamento.class));
    }

    @Test
    public void shouldProcessSuprimentoWithZeroValue() {
        // Given
        lancamento.setEstilo(EstiloLancamento.ENTRADA);
        lancamento.setTipo(TipoLancamento.SUPRIMENTO);
        lancamento.setValor(0.0);
        when(caixaLancamentoRepository.save(any(CaixaLancamento.class))).thenReturn(lancamento);
        
        // When
        String result = caixaLancamentoService.lancamento(lancamento);
        
        // Then
        assertEquals(SUCCESS_MESSAGE, result);
        assertEquals(0.0, lancamento.getValor(), 0.001);
    }

    @Test
    public void shouldProcessSuprimentoWithNegativeValue() {
        // Given
        lancamento.setEstilo(EstiloLancamento.ENTRADA);
        lancamento.setTipo(TipoLancamento.SUPRIMENTO);
        lancamento.setValor(-100.0);
        when(caixaLancamentoRepository.save(any(CaixaLancamento.class))).thenReturn(lancamento);
        
        // When
        String result = caixaLancamentoService.lancamento(lancamento);
        
        // Then
        assertEquals(SUCCESS_MESSAGE, result);
        assertEquals(-100.0, lancamento.getValor(), 0.001);
    }

    @Test
    public void shouldSetDefaultObservationForSuprimentoWhenEmpty() {
        // Given
        lancamento.setEstilo(EstiloLancamento.ENTRADA);
        lancamento.setTipo(TipoLancamento.SUPRIMENTO);
        lancamento.setObservacao("");
        when(caixaLancamentoRepository.save(any(CaixaLancamento.class))).thenReturn(lancamento);
        
        // When
        caixaLancamentoService.lancamento(lancamento);
        
        // Then
        assertEquals(SUPRIMENTO_DEFAULT_OBSERVATION, lancamento.getObservacao());
    }

    @Test
    public void shouldKeepCustomObservationForSuprimento() {
        // Given
        String customObservation = "Custom suprimento observation";
        lancamento.setEstilo(EstiloLancamento.ENTRADA);
        lancamento.setTipo(TipoLancamento.SUPRIMENTO);
        lancamento.setObservacao(customObservation);
        when(caixaLancamentoRepository.save(any(CaixaLancamento.class))).thenReturn(lancamento);
        
        // When
        caixaLancamentoService.lancamento(lancamento);
        
        // Then
        assertEquals(customObservation, lancamento.getObservacao());
    }

    // ========== TESTES DE SAÍDA (SANGRIA) ==========
    
    @Test
    public void shouldProcessSangriaWithValidAmount() {
        // Given
        lancamento.setEstilo(EstiloLancamento.SAIDA);
        lancamento.setTipo(TipoLancamento.SANGRIA);
        lancamento.setValor(500.0);
        when(caixaLancamentoRepository.save(any(CaixaLancamento.class))).thenReturn(lancamento);
        
        // When
        String result = caixaLancamentoService.lancamento(lancamento);
        
        // Then
        assertEquals(SUCCESS_MESSAGE, result);
        assertEquals(-500.0, lancamento.getValor(), 0.001);
    }

    @Test
    public void shouldProcessSangriaWithAmountEqualToBalance() {
        // Given
        lancamento.setEstilo(EstiloLancamento.SAIDA);
        lancamento.setTipo(TipoLancamento.SANGRIA);
        lancamento.setValor(1000.0); // Equal to balance
        when(caixaLancamentoRepository.save(any(CaixaLancamento.class))).thenReturn(lancamento);
        
        // When
        String result = caixaLancamentoService.lancamento(lancamento);
        
        // Then
        assertEquals(SUCCESS_MESSAGE, result);
        assertEquals(-1000.0, lancamento.getValor(), 0.001);
    }

    @Test
    public void shouldRejectSangriaWithAmountExceedingBalance() {
        // Given
        lancamento.setEstilo(EstiloLancamento.SAIDA);
        lancamento.setTipo(TipoLancamento.SANGRIA);
        lancamento.setValor(1500.0); // Exceeds balance
        
        // When
        String result = caixaLancamentoService.lancamento(lancamento);
        
        // Then
        assertEquals(INSUFFICIENT_BALANCE_MESSAGE, result);
        verify(caixaLancamentoRepository, never()).save(any(CaixaLancamento.class));
    }

    @Test
    public void shouldKeepNegativeValueForSaidaWhenAlreadyNegative() {
        // Given
        lancamento.setEstilo(EstiloLancamento.SAIDA);
        lancamento.setTipo(TipoLancamento.SANGRIA);
        lancamento.setValor(-100.0);
        when(caixaLancamentoRepository.save(any(CaixaLancamento.class))).thenReturn(lancamento);
        
        // When
        caixaLancamentoService.lancamento(lancamento);
        
        // Then
        assertEquals(-100.0, lancamento.getValor(), 0.001);
    }

    @Test
    public void shouldSetDefaultObservationForSangriaWhenEmpty() {
        // Given
        lancamento.setEstilo(EstiloLancamento.SAIDA);
        lancamento.setTipo(TipoLancamento.SANGRIA);
        lancamento.setObservacao("");
        lancamento.setValor(100.0);
        when(caixaLancamentoRepository.save(any(CaixaLancamento.class))).thenReturn(lancamento);
        
        // When
        caixaLancamentoService.lancamento(lancamento);
        
        // Then
        assertEquals(SANGRIA_DEFAULT_OBSERVATION, lancamento.getObservacao());
    }

    @Test
    public void shouldKeepCustomObservationForSangria() {
        // Given
        String customObservation = "Custom sangria observation";
        lancamento.setEstilo(EstiloLancamento.SAIDA);
        lancamento.setTipo(TipoLancamento.SANGRIA);
        lancamento.setObservacao(customObservation);
        lancamento.setValor(100.0);
        when(caixaLancamentoRepository.save(any(CaixaLancamento.class))).thenReturn(lancamento);
        
        // When
        caixaLancamentoService.lancamento(lancamento);
        
        // Then
        assertEquals(customObservation, lancamento.getObservacao());
    }

    // ========== TESTES DE VALORES LIMITE ==========
    
    @Test
    public void shouldProcessSaidaWithMinimalPositiveValue() {
        // Given
        lancamento.setEstilo(EstiloLancamento.SAIDA);
        lancamento.setTipo(TipoLancamento.SANGRIA);
        lancamento.setValor(0.01);
        when(caixaLancamentoRepository.save(any(CaixaLancamento.class))).thenReturn(lancamento);
        
        // When
        String result = caixaLancamentoService.lancamento(lancamento);
        
        // Then
        assertEquals(SUCCESS_MESSAGE, result);
        assertEquals(-0.01, lancamento.getValor(), 0.001);
    }

    @Test
    public void shouldProcessSaidaWithZeroValue() {
        // Given
        lancamento.setEstilo(EstiloLancamento.SAIDA);
        lancamento.setTipo(TipoLancamento.SANGRIA);
        lancamento.setValor(0.0);
        when(caixaLancamentoRepository.save(any(CaixaLancamento.class))).thenReturn(lancamento);
        
        // When
        String result = caixaLancamentoService.lancamento(lancamento);
        
        // Then
        assertEquals(SUCCESS_MESSAGE, result);
        assertEquals(0.0, lancamento.getValor(), 0.001);
    }

    @Test
    public void shouldRejectSaidaWithValueJustAboveBalance() {
        // Given
        lancamento.setEstilo(EstiloLancamento.SAIDA);
        lancamento.setTipo(TipoLancamento.SANGRIA);
        lancamento.setValor(1000.01); // Just above balance
        
        // When
        String result = caixaLancamentoService.lancamento(lancamento);
        
        // Then
        assertEquals(INSUFFICIENT_BALANCE_MESSAGE, result);
    }

    // ========== TESTES DE CASOS EXCEPCIONAIS ==========
    
    @Test(expected = RuntimeException.class)
    public void shouldThrowExceptionWhenCaixaIsNull() {
        // Given
        lancamento.setCaixa(null);
        
        // When
        caixaLancamentoService.lancamento(lancamento);
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowExceptionWhenCaixaIsClosed() {
        // Given
        Caixa closedCaixa = new Caixa();
        closedCaixa.setData_fechamento(new Timestamp(System.currentTimeMillis()));
        closedCaixa.setValor_total(1000.0);
        lancamento.setCaixa(closedCaixa);
        
        // When
        caixaLancamentoService.lancamento(lancamento);
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowExceptionWhenRepositoryThrowsException() {
        // Given
        lancamento.setEstilo(EstiloLancamento.ENTRADA);
        lancamento.setTipo(TipoLancamento.SUPRIMENTO);
        when(caixaLancamentoRepository.save(any(CaixaLancamento.class)))
            .thenThrow(new RuntimeException("Database error"));
        
        // When
        caixaLancamentoService.lancamento(lancamento);
    }

    // ========== TESTES DE DIFERENTES TIPOS DE LANÇAMENTO ==========
    
    @Test
    public void shouldProcessRecebimentoLancamento() {
        // Given
        lancamento.setEstilo(EstiloLancamento.ENTRADA);
        lancamento.setTipo(TipoLancamento.RECEBIMENTO);
        lancamento.setObservacao("Recebimento de venda");
        when(caixaLancamentoRepository.save(any(CaixaLancamento.class))).thenReturn(lancamento);
        
        // When
        String result = caixaLancamentoService.lancamento(lancamento);
        
        // Then
        assertEquals(SUCCESS_MESSAGE, result);
        assertEquals("Recebimento de venda", lancamento.getObservacao());
    }

    @Test
    public void shouldProcessPagamentoLancamento() {
        // Given
        lancamento.setEstilo(EstiloLancamento.SAIDA);
        lancamento.setTipo(TipoLancamento.PAGAMENTO);
        lancamento.setObservacao("Pagamento de fornecedor");
        lancamento.setValor(300.0);
        when(caixaLancamentoRepository.save(any(CaixaLancamento.class))).thenReturn(lancamento);
        
        // When
        String result = caixaLancamentoService.lancamento(lancamento);
        
        // Then
        assertEquals(SUCCESS_MESSAGE, result);
        assertEquals(-300.0, lancamento.getValor(), 0.001);
        assertEquals("Pagamento de fornecedor", lancamento.getObservacao());
    }

    @Test
    public void shouldProcessSaldoInicialLancamento() {
        // Given
        lancamento.setEstilo(EstiloLancamento.ENTRADA);
        lancamento.setTipo(TipoLancamento.SALDOINICIAL);
        lancamento.setObservacao("Saldo inicial do caixa");
        when(caixaLancamentoRepository.save(any(CaixaLancamento.class))).thenReturn(lancamento);
        
        // When
        String result = caixaLancamentoService.lancamento(lancamento);
        
        // Then
        assertEquals(SUCCESS_MESSAGE, result);
        assertEquals("Saldo inicial do caixa", lancamento.getObservacao());
    }

    // ========== TESTES DE PARTIÇÕES DE EQUIVALÊNCIA PARA VALORES ==========
    
    @Test
    public void shouldProcessSmallPositiveValue() {
        // Given
        lancamento.setEstilo(EstiloLancamento.ENTRADA);
        lancamento.setTipo(TipoLancamento.SUPRIMENTO);
        lancamento.setValor(1.0);
        when(caixaLancamentoRepository.save(any(CaixaLancamento.class))).thenReturn(lancamento);
        
        // When
        String result = caixaLancamentoService.lancamento(lancamento);
        
        // Then
        assertEquals(SUCCESS_MESSAGE, result);
    }

    @Test
    public void shouldProcessLargePositiveValue() {
        // Given
        lancamento.setEstilo(EstiloLancamento.ENTRADA);
        lancamento.setTipo(TipoLancamento.SUPRIMENTO);
        lancamento.setValor(999999.99);
        when(caixaLancamentoRepository.save(any(CaixaLancamento.class))).thenReturn(lancamento);
        
        // When
        String result = caixaLancamentoService.lancamento(lancamento);
        
        // Then
        assertEquals(SUCCESS_MESSAGE, result);
    }

    @Test
    public void shouldProcessDecimalValues() {
        // Given
        lancamento.setEstilo(EstiloLancamento.ENTRADA);
        lancamento.setTipo(TipoLancamento.SUPRIMENTO);
        lancamento.setValor(123.45);
        when(caixaLancamentoRepository.save(any(CaixaLancamento.class))).thenReturn(lancamento);
        
        // When
        String result = caixaLancamentoService.lancamento(lancamento);
        
        // Then
        assertEquals(SUCCESS_MESSAGE, result);
        assertEquals(123.45, lancamento.getValor(), 0.001);
    }

    // ========== TESTES DE COMPORTAMENTO TEMPORAL ==========
    
    @Test
    public void shouldSetTimestampWhenLancamentoIsCreated() {
        // Given
        lancamento.setEstilo(EstiloLancamento.ENTRADA);
        lancamento.setTipo(TipoLancamento.SUPRIMENTO);
        when(caixaLancamentoRepository.save(any(CaixaLancamento.class))).thenReturn(lancamento);
        
        // When
        caixaLancamentoService.lancamento(lancamento);
        
        // Then
        assertNotNull(lancamento.getData_cadastro());
        assertTrue(lancamento.getData_cadastro().getTime() <= System.currentTimeMillis());
    }

    // ========== TESTES DO MÉTODO AUXILIAR ==========
    
    @Test
    public void shouldReturnAllLancamentosForGivenCaixa() {
        // Given
        List<CaixaLancamento> lancamentos = new ArrayList<>();
        lancamentos.add(lancamento);
        when(caixaLancamentoRepository.findByCaixaEquals(caixa)).thenReturn(lancamentos);
        
        // When
        List<CaixaLancamento> result = caixaLancamentoService.lancamentosDoCaixa(caixa);
        
        // Then
        assertEquals(1, result.size());
        assertEquals(lancamento, result.get(0));
    }
    
    @Test
    public void shouldReturnEmptyListWhenNoCaixaLancamentos() {
        // Given
        when(caixaLancamentoRepository.findByCaixaEquals(caixa)).thenReturn(new ArrayList<>());
        
        // When
        List<CaixaLancamento> result = caixaLancamentoService.lancamentosDoCaixa(caixa);
        
        // Then
        assertTrue(result.isEmpty());
    }

    // ========== TESTES DE INTEGRAÇÃO DE COMPORTAMENTOS ==========
    
    @Test
    public void shouldProcessCompleteSuprimentoWorkflow() {
        // Given
        lancamento.setEstilo(EstiloLancamento.ENTRADA);
        lancamento.setTipo(TipoLancamento.SUPRIMENTO);
        lancamento.setValor(250.0);
        lancamento.setObservacao("");
        when(caixaLancamentoRepository.save(any(CaixaLancamento.class))).thenReturn(lancamento);
        
        // When
        String result = caixaLancamentoService.lancamento(lancamento);
        
        // Then
        assertEquals(SUCCESS_MESSAGE, result);
        assertEquals(250.0, lancamento.getValor(), 0.001);
        assertEquals(SUPRIMENTO_DEFAULT_OBSERVATION, lancamento.getObservacao());
        assertNotNull(lancamento.getData_cadastro());
        verify(caixaLancamentoRepository).save(lancamento);
    }

    @Test
    public void shouldProcessCompleteSangriaWorkflow() {
        // Given
        lancamento.setEstilo(EstiloLancamento.SAIDA);
        lancamento.setTipo(TipoLancamento.SANGRIA);
        lancamento.setValor(150.0);
        lancamento.setObservacao("");
        when(caixaLancamentoRepository.save(any(CaixaLancamento.class))).thenReturn(lancamento);
        
        // When
        String result = caixaLancamentoService.lancamento(lancamento);
        
        // Then
        assertEquals(SUCCESS_MESSAGE, result);
        assertEquals(-150.0, lancamento.getValor(), 0.001);
        assertEquals(SANGRIA_DEFAULT_OBSERVATION, lancamento.getObservacao());
        assertNotNull(lancamento.getData_cadastro());
        verify(caixaLancamentoRepository).save(lancamento);
    }
}

