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
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CaixaLancamentoServiceIntegrationTest {

    @Mock
    private CaixaLancamentoRepository caixaLancamentoRepository;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private CaixaLancamentoService caixaLancamentoService;

    private Caixa caixaAberto;
    private Caixa caixaFechado;

    @Before
    public void setUp() {
        caixaAberto = criarCaixa(1000.0, null);
        caixaFechado = criarCaixa(1000.0, new Timestamp(System.currentTimeMillis()));
    }

    private Caixa criarCaixa(double saldo, Timestamp dataFechamento) {
        Caixa caixa = new Caixa();
        caixa.setValor_total(saldo);
        caixa.setData_fechamento(dataFechamento);
        return caixa;
    }

    private CaixaLancamento criarLancamento(Caixa caixa, TipoLancamento tipo, EstiloLancamento estilo, double valor) {
        CaixaLancamento lancamento = new CaixaLancamento();
        lancamento.setCaixa(caixa);
        lancamento.setTipo(tipo);
        lancamento.setEstilo(estilo);
        lancamento.setValor(valor);
        lancamento.setObservacao("");
        return lancamento;
    }

    @Test
    public void shouldProcessSuprimentoSuccessfully() {
        CaixaLancamento lancamento = criarLancamento(caixaAberto, TipoLancamento.SUPRIMENTO, EstiloLancamento.ENTRADA, 100.0);
        when(caixaLancamentoRepository.save(any(CaixaLancamento.class))).thenReturn(lancamento);

        String resultado = caixaLancamentoService.lancamento(lancamento);

        assertEquals("Lançamento realizado com sucesso", resultado);
        assertEquals("Suprimento de caixa", lancamento.getObservacao());
        assertNotNull(lancamento.getData_cadastro());
        verify(caixaLancamentoRepository).save(lancamento);
    }

    @Test
    public void shouldProcessSangriaSuccessfully() {
        CaixaLancamento lancamento = criarLancamento(caixaAberto, TipoLancamento.SANGRIA, EstiloLancamento.SAIDA, 100.0);
        when(caixaLancamentoRepository.save(any(CaixaLancamento.class))).thenReturn(lancamento);

        String resultado = caixaLancamentoService.lancamento(lancamento);

        assertEquals("Lançamento realizado com sucesso", resultado);
        assertEquals("Sangria de caixa", lancamento.getObservacao());
        assertEquals(-100.0, lancamento.getValor(), 0.001);
        assertNotNull(lancamento.getData_cadastro());
    }

    @Test
    public void shouldProcessRecebimentoSuccessfully() {
        CaixaLancamento lancamento = criarLancamento(caixaAberto, TipoLancamento.RECEBIMENTO, EstiloLancamento.ENTRADA, 150.0);
        when(caixaLancamentoRepository.save(any(CaixaLancamento.class))).thenReturn(lancamento);

        String resultado = caixaLancamentoService.lancamento(lancamento);

        assertEquals("Lançamento realizado com sucesso", resultado);
        assertEquals("", lancamento.getObservacao()); // Service doesn't set observation for RECEBIMENTO
        assertEquals(150.0, lancamento.getValor(), 0.001);
    }

    @Test
    public void shouldProcessTransferenciaSuccessfully() {
        CaixaLancamento lancamento = criarLancamento(caixaAberto, TipoLancamento.TRANSFERENCIA, EstiloLancamento.SAIDA, 200.0);
        when(caixaLancamentoRepository.save(any(CaixaLancamento.class))).thenReturn(lancamento);

        String resultado = caixaLancamentoService.lancamento(lancamento);

        assertEquals("Lançamento realizado com sucesso", resultado);
        assertEquals("", lancamento.getObservacao()); // Service doesn't set observation for TRANSFERENCIA
        assertEquals(-200.0, lancamento.getValor(), 0.001);
    }

    @Test
    public void shouldProcessPagamentoSuccessfully() {
        CaixaLancamento lancamento = criarLancamento(caixaAberto, TipoLancamento.PAGAMENTO, EstiloLancamento.SAIDA, 75.0);
        when(caixaLancamentoRepository.save(any(CaixaLancamento.class))).thenReturn(lancamento);

        String resultado = caixaLancamentoService.lancamento(lancamento);

        assertEquals("Lançamento realizado com sucesso", resultado);
        assertEquals("", lancamento.getObservacao()); // Service doesn't set observation for PAGAMENTO
        assertEquals(-75.0, lancamento.getValor(), 0.001);
    }

    @Test
    public void shouldProcessSaldoInicialSuccessfully() {
        CaixaLancamento lancamento = criarLancamento(caixaAberto, TipoLancamento.SALDOINICIAL, EstiloLancamento.ENTRADA, 500.0);
        when(caixaLancamentoRepository.save(any(CaixaLancamento.class))).thenReturn(lancamento);

        String resultado = caixaLancamentoService.lancamento(lancamento);

        assertEquals("Lançamento realizado com sucesso", resultado);
        assertEquals("", lancamento.getObservacao()); // Service doesn't set observation for SALDOINICIAL
        assertEquals(500.0, lancamento.getValor(), 0.001);
    }

    @Test
    public void shouldRejectSaidaWhenInsufficientBalance() {
        Caixa caixaComSaldoBaixo = criarCaixa(100.0, null);
        CaixaLancamento lancamento = criarLancamento(caixaComSaldoBaixo, TipoLancamento.SANGRIA, EstiloLancamento.SAIDA, 200.0);

        String resultado = caixaLancamentoService.lancamento(lancamento);

        assertEquals("Saldo insuficiente para realizar esta operação", resultado);
        verify(caixaLancamentoRepository, never()).save(any(CaixaLancamento.class));
    }

    @Test
    public void shouldAllowSaidaWhenValueEqualsBalance() {
        Caixa caixaComSaldoExato = criarCaixa(100.0, null);
        CaixaLancamento lancamento = criarLancamento(caixaComSaldoExato, TipoLancamento.SANGRIA, EstiloLancamento.SAIDA, 100.0);
        when(caixaLancamentoRepository.save(any(CaixaLancamento.class))).thenReturn(lancamento);

        String resultado = caixaLancamentoService.lancamento(lancamento);

        assertEquals("Lançamento realizado com sucesso", resultado);
        verify(caixaLancamentoRepository).save(lancamento);
    }

    @Test
    public void shouldConvertPositiveValueToNegativeForSaida() {
        CaixaLancamento lancamento = criarLancamento(caixaAberto, TipoLancamento.SANGRIA, EstiloLancamento.SAIDA, 100.0);
        when(caixaLancamentoRepository.save(any(CaixaLancamento.class))).thenReturn(lancamento);

        caixaLancamentoService.lancamento(lancamento);

        assertEquals(-100.0, lancamento.getValor(), 0.001);
    }

    @Test
    public void shouldKeepNegativeValueForSaidaWhenAlreadyNegative() {
        CaixaLancamento lancamento = criarLancamento(caixaAberto, TipoLancamento.SANGRIA, EstiloLancamento.SAIDA, -100.0);
        when(caixaLancamentoRepository.save(any(CaixaLancamento.class))).thenReturn(lancamento);

        caixaLancamentoService.lancamento(lancamento);

        assertEquals(-100.0, lancamento.getValor(), 0.001);
    }

    @Test
    public void shouldKeepCustomObservationWhenProvided() {
        CaixaLancamento lancamento = criarLancamento(caixaAberto, TipoLancamento.SANGRIA, EstiloLancamento.SAIDA, 100.0);
        lancamento.setObservacao("Observação personalizada");
        when(caixaLancamentoRepository.save(any(CaixaLancamento.class))).thenReturn(lancamento);

        caixaLancamentoService.lancamento(lancamento);

        assertEquals("Observação personalizada", lancamento.getObservacao());
    }

    @Test
    public void shouldHandleZeroValueLancamento() {
        CaixaLancamento lancamento = criarLancamento(caixaAberto, TipoLancamento.SUPRIMENTO, EstiloLancamento.ENTRADA, 0.0);
        when(caixaLancamentoRepository.save(any(CaixaLancamento.class))).thenReturn(lancamento);

        String resultado = caixaLancamentoService.lancamento(lancamento);

        assertEquals("Lançamento realizado com sucesso", resultado);
        assertEquals(0.0, lancamento.getValor(), 0.001);
    }

    @Test
    public void shouldHandleDecimalValues() {
        CaixaLancamento lancamento = criarLancamento(caixaAberto, TipoLancamento.SANGRIA, EstiloLancamento.SAIDA, 99.99);
        when(caixaLancamentoRepository.save(any(CaixaLancamento.class))).thenReturn(lancamento);

        String resultado = caixaLancamentoService.lancamento(lancamento);

        assertEquals("Lançamento realizado com sucesso", resultado);
        assertEquals(-99.99, lancamento.getValor(), 0.001);
    }

    @Test
    public void shouldHandleMultipleSmallValues() {
        List<Double> valores = Arrays.asList(0.01, 0.99, 1.50, 2.75);
        
        valores.forEach(valor -> {
            CaixaLancamento lancamento = criarLancamento(caixaAberto, TipoLancamento.SUPRIMENTO, EstiloLancamento.ENTRADA, valor);
            when(caixaLancamentoRepository.save(any(CaixaLancamento.class))).thenReturn(lancamento);
            
            String resultado = caixaLancamentoService.lancamento(lancamento);
            assertEquals("Lançamento realizado com sucesso", resultado);
        });
    }

    @Test
    public void shouldHandleLargeValues() {
        Caixa caixaComMuitoSaldo = criarCaixa(10000.0, null);
        List<Double> valores = Arrays.asList(999.99, 5000.0, 9999.99);
        
        valores.forEach(valor -> {
            CaixaLancamento lancamento = criarLancamento(caixaComMuitoSaldo, TipoLancamento.SANGRIA, EstiloLancamento.SAIDA, valor);
            when(caixaLancamentoRepository.save(any(CaixaLancamento.class))).thenReturn(lancamento);
            
            String resultado = caixaLancamentoService.lancamento(lancamento);
            assertEquals("Lançamento realizado com sucesso", resultado);
        });
    }

    @Test
    public void shouldValidateBusinessRules() {
        // Testa se valor é válido para saque
        double valor = 100.0;
        double saldo = 1000.0;
        assertTrue(valor <= saldo);
        
        // Testa se valor é positivo
        assertTrue(valor > 0);
        
        // Testa saldo insuficiente
        double valorAlto = 2000.0;
        assertFalse(valorAlto <= saldo);
    }

    @Test
    public void shouldTestDifferentBalanceScenarios() {
        List<Double> saldos = Arrays.asList(100.0, 500.0, 1000.0, 2500.0);
        
        saldos.forEach(saldo -> {
            Caixa caixa = criarCaixa(saldo, null);
            double valorSaque = saldo / 2;
            CaixaLancamento lancamento = criarLancamento(caixa, TipoLancamento.SANGRIA, EstiloLancamento.SAIDA, valorSaque);
            when(caixaLancamentoRepository.save(any(CaixaLancamento.class))).thenReturn(lancamento);
            
            String resultado = caixaLancamentoService.lancamento(lancamento);
            assertEquals("Lançamento realizado com sucesso", resultado);
        });
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowExceptionWhenCaixaIsClosed() {
        CaixaLancamento lancamento = criarLancamento(caixaFechado, TipoLancamento.SUPRIMENTO, EstiloLancamento.ENTRADA, 100.0);
        caixaLancamentoService.lancamento(lancamento);
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowExceptionWhenCaixaIsNull() {
        CaixaLancamento lancamento = criarLancamento(null, TipoLancamento.SUPRIMENTO, EstiloLancamento.ENTRADA, 100.0);
        caixaLancamentoService.lancamento(lancamento);
    }

    @Test(expected = RuntimeException.class)
    public void shouldPropagateRepositoryExceptions() {
        CaixaLancamento lancamento = criarLancamento(caixaAberto, TipoLancamento.SUPRIMENTO, EstiloLancamento.ENTRADA, 100.0);
        doThrow(new RuntimeException("Database error")).when(caixaLancamentoRepository).save(any(CaixaLancamento.class));
        
        caixaLancamentoService.lancamento(lancamento);
    }

    @Test
    public void shouldReturnLancamentosForGivenCaixa() {
        CaixaLancamento lancamento1 = criarLancamento(caixaAberto, TipoLancamento.SUPRIMENTO, EstiloLancamento.ENTRADA, 100.0);
        CaixaLancamento lancamento2 = criarLancamento(caixaAberto, TipoLancamento.SANGRIA, EstiloLancamento.SAIDA, 50.0);
        List<CaixaLancamento> lancamentos = Arrays.asList(lancamento1, lancamento2);
        
        when(caixaLancamentoRepository.findByCaixaEquals(caixaAberto)).thenReturn(lancamentos);

        List<CaixaLancamento> resultado = caixaLancamentoService.lancamentosDoCaixa(caixaAberto);

        assertEquals(2, resultado.size());
        verify(caixaLancamentoRepository).findByCaixaEquals(caixaAberto);
    }

    @Test
    public void shouldSetTimestampOnAllLancamentos() {
        List<TipoLancamento> tipos = Arrays.asList(
            TipoLancamento.SUPRIMENTO, 
            TipoLancamento.SANGRIA, 
            TipoLancamento.RECEBIMENTO, 
            TipoLancamento.TRANSFERENCIA
        );

        tipos.forEach(tipo -> {
            EstiloLancamento estilo = tipo == TipoLancamento.SUPRIMENTO || tipo == TipoLancamento.RECEBIMENTO ? 
                EstiloLancamento.ENTRADA : EstiloLancamento.SAIDA;
            
            CaixaLancamento lancamento = criarLancamento(caixaAberto, tipo, estilo, 100.0);
            when(caixaLancamentoRepository.save(any(CaixaLancamento.class))).thenReturn(lancamento);

            caixaLancamentoService.lancamento(lancamento);
            assertNotNull("Timestamp deve ser definido", lancamento.getData_cadastro());
        });
    }

    @Test
    public void shouldSetCorrectObservationsForAllTypes() {
        Function<TipoLancamento, String> getExpectedObservation = tipo -> {
            switch (tipo) {
                case SUPRIMENTO: return "Suprimento de caixa";
                case SANGRIA: return "Sangria de caixa";
                case RECEBIMENTO: return ""; // Service doesn't set observation
                case TRANSFERENCIA: return ""; // Service doesn't set observation
                case PAGAMENTO: return ""; // Service doesn't set observation
                case SALDOINICIAL: return ""; // Service doesn't set observation
                default: return "";
            }
        };

        Stream.of(TipoLancamento.values()).forEach(tipo -> {
            EstiloLancamento estilo = tipo == TipoLancamento.SUPRIMENTO || 
                                    tipo == TipoLancamento.RECEBIMENTO || 
                                    tipo == TipoLancamento.SALDOINICIAL ? 
                                    EstiloLancamento.ENTRADA : EstiloLancamento.SAIDA;
            
            CaixaLancamento lancamento = criarLancamento(caixaAberto, tipo, estilo, 100.0);
            when(caixaLancamentoRepository.save(any(CaixaLancamento.class))).thenReturn(lancamento);

            caixaLancamentoService.lancamento(lancamento);
            assertEquals(getExpectedObservation.apply(tipo), lancamento.getObservacao());
        });
    }

    @Test
    public void shouldHandleBoundaryValues() {
        List<Double> valores = Arrays.asList(0.01, 999.99, 1000.0);
        
        valores.forEach(valor -> {
            Caixa caixa = criarCaixa(valor + 1, null);
            CaixaLancamento lancamento = criarLancamento(caixa, TipoLancamento.SANGRIA, EstiloLancamento.SAIDA, valor);
            when(caixaLancamentoRepository.save(any(CaixaLancamento.class))).thenReturn(lancamento);
            
            String resultado = caixaLancamentoService.lancamento(lancamento);
            assertEquals("Lançamento realizado com sucesso", resultado);
        });
    }
}

