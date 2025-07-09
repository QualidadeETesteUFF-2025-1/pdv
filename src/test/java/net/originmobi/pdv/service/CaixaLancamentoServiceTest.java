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

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes de Caixa Preta para CaixaLancamentoService
 * Técnicas aplicadas: Particionamento de Equivalência e Análise de Valor Limite
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class CaixaLancamentoServiceTest {

    @Mock
    private CaixaLancamentoRepository caixaLancamentoRepository;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private CaixaLancamentoService caixaLancamentoService;

    private CaixaLancamento lancamento;
    private Caixa caixaAberto;
    
    // Constantes para mensagens esperadas
    private static final String SUCCESS_MESSAGE = "Lançamento realizado com sucesso";
    private static final String INSUFFICIENT_BALANCE_MESSAGE = "Saldo insuficiente para realizar esta operação";
    
    // Constantes para valores de teste
    private static final Double SALDO_INICIAL = 1000.0;
    private static final Double VALOR_ZERO = 0.0;
    private static final Double VALOR_MINIMO = 0.01;
    private static final Double VALOR_MEDIO = 500.0;
    private static final Double VALOR_GRANDE = 999.99;
    private static final Double VALOR_IGUAL_SALDO = 1000.0;
    private static final Double VALOR_MAIOR_SALDO = 1000.01;
    private static final Double VALOR_MUITO_MAIOR = 5000.0;

    @Before
    public void setUp() {
        setupCaixaAberto();
        setupLancamentoBase();
    }

    private void setupCaixaAberto() {
        caixaAberto = new Caixa();
        caixaAberto.setValor_total(SALDO_INICIAL);
        caixaAberto.setData_fechamento(null);
    }

    private void setupLancamentoBase() {
        lancamento = new CaixaLancamento();
        lancamento.setCaixa(caixaAberto);
        lancamento.setValor(VALOR_MEDIO);
        lancamento.setObservacao("");
    }

    // ===============================================================
    // PARTICIONAMENTO DE EQUIVALÊNCIA - CLASSES VÁLIDAS E INVÁLIDAS
    // ===============================================================

    // Classe 1: Lançamentos de ENTRADA com valores válidos
    @Test
    public void deveProcessarEntradaComValorPositivo() {
        lancamento.setEstilo(EstiloLancamento.ENTRADA);
        lancamento.setTipo(TipoLancamento.SUPRIMENTO);
        lancamento.setValor(VALOR_MEDIO);
        when(caixaLancamentoRepository.save(any(CaixaLancamento.class))).thenReturn(lancamento);
        
        String resultado = caixaLancamentoService.lancamento(lancamento);
        
        assertEquals(SUCCESS_MESSAGE, resultado);
        assertEquals(VALOR_MEDIO, lancamento.getValor(), 0.001);
        verify(caixaLancamentoRepository).save(any(CaixaLancamento.class));
    }

    @Test
    public void deveProcessarEntradaComValorZero() {
        lancamento.setEstilo(EstiloLancamento.ENTRADA);
        lancamento.setTipo(TipoLancamento.RECEBIMENTO);
        lancamento.setValor(VALOR_ZERO);
        when(caixaLancamentoRepository.save(any(CaixaLancamento.class))).thenReturn(lancamento);
        
        String resultado = caixaLancamentoService.lancamento(lancamento);
        
        assertEquals(SUCCESS_MESSAGE, resultado);
        assertEquals(VALOR_ZERO, lancamento.getValor(), 0.001);
    }

    @Test
    public void deveProcessarEntradaComValorNegativo() {
        lancamento.setEstilo(EstiloLancamento.ENTRADA);
        lancamento.setTipo(TipoLancamento.SALDOINICIAL);
        lancamento.setValor(-VALOR_MEDIO);
        when(caixaLancamentoRepository.save(any(CaixaLancamento.class))).thenReturn(lancamento);
        
        String resultado = caixaLancamentoService.lancamento(lancamento);
        
        assertEquals(SUCCESS_MESSAGE, resultado);
        assertEquals(-VALOR_MEDIO, lancamento.getValor(), 0.001);
    }

    // Classe 2: Lançamentos de SAIDA com saldo suficiente
    @Test
    public void deveProcessarSaidaComSaldoSuficiente() {
        lancamento.setEstilo(EstiloLancamento.SAIDA);
        lancamento.setTipo(TipoLancamento.SANGRIA);
        lancamento.setValor(VALOR_MEDIO);
        when(caixaLancamentoRepository.save(any(CaixaLancamento.class))).thenReturn(lancamento);
        
        String resultado = caixaLancamentoService.lancamento(lancamento);
        
        assertEquals(SUCCESS_MESSAGE, resultado);
        assertEquals(-VALOR_MEDIO, lancamento.getValor(), 0.001);
    }

    @Test
    public void deveProcessarSaidaComValorIgualAoSaldo() {
        lancamento.setEstilo(EstiloLancamento.SAIDA);
        lancamento.setTipo(TipoLancamento.PAGAMENTO);
        lancamento.setValor(VALOR_IGUAL_SALDO);
        when(caixaLancamentoRepository.save(any(CaixaLancamento.class))).thenReturn(lancamento);
        
        String resultado = caixaLancamentoService.lancamento(lancamento);
        
        assertEquals(SUCCESS_MESSAGE, resultado);
        assertEquals(-VALOR_IGUAL_SALDO, lancamento.getValor(), 0.001);
    }

    // Classe 3: Lançamentos de SAIDA com saldo insuficiente
    @Test
    public void deveRejeitarSaidaComSaldoInsuficiente() {
        lancamento.setEstilo(EstiloLancamento.SAIDA);
        lancamento.setTipo(TipoLancamento.SANGRIA);
        lancamento.setValor(VALOR_MAIOR_SALDO);
        
        String resultado = caixaLancamentoService.lancamento(lancamento);
        
        assertEquals(INSUFFICIENT_BALANCE_MESSAGE, resultado);
        verify(caixaLancamentoRepository, never()).save(any(CaixaLancamento.class));
    }

    @Test
    public void deveRejeitarSaidaComValorMuitoMaior() {
        lancamento.setEstilo(EstiloLancamento.SAIDA);
        lancamento.setTipo(TipoLancamento.SANGRIA);
        lancamento.setValor(VALOR_MUITO_MAIOR);
        
        String resultado = caixaLancamentoService.lancamento(lancamento);
        
        assertEquals(INSUFFICIENT_BALANCE_MESSAGE, resultado);
        verify(caixaLancamentoRepository, never()).save(any(CaixaLancamento.class));
    }

    // ===============================================================
    // ANÁLISE DE VALOR LIMITE
    // ===============================================================

    @Test
    public void deveProcessarEntradaComValorMinimoPositivo() {
        lancamento.setEstilo(EstiloLancamento.ENTRADA);
        lancamento.setTipo(TipoLancamento.SUPRIMENTO);
        lancamento.setValor(VALOR_MINIMO);
        when(caixaLancamentoRepository.save(any(CaixaLancamento.class))).thenReturn(lancamento);
        
        String resultado = caixaLancamentoService.lancamento(lancamento);
        
        assertEquals(SUCCESS_MESSAGE, resultado);
        assertEquals(VALOR_MINIMO, lancamento.getValor(), 0.001);
    }

    @Test
    public void deveProcessarSaidaComValorMinimoPositivo() {
        lancamento.setEstilo(EstiloLancamento.SAIDA);
        lancamento.setTipo(TipoLancamento.SANGRIA);
        lancamento.setValor(VALOR_MINIMO);
        when(caixaLancamentoRepository.save(any(CaixaLancamento.class))).thenReturn(lancamento);
        
        String resultado = caixaLancamentoService.lancamento(lancamento);
        
        assertEquals(SUCCESS_MESSAGE, resultado);
        assertEquals(-VALOR_MINIMO, lancamento.getValor(), 0.001);
    }

    @Test
    public void deveProcessarSaidaComValorLimiteSuperior() {
        lancamento.setEstilo(EstiloLancamento.SAIDA);
        lancamento.setTipo(TipoLancamento.SANGRIA);
        lancamento.setValor(VALOR_GRANDE);
        when(caixaLancamentoRepository.save(any(CaixaLancamento.class))).thenReturn(lancamento);
        
        String resultado = caixaLancamentoService.lancamento(lancamento);
        
        assertEquals(SUCCESS_MESSAGE, resultado);
        assertEquals(-VALOR_GRANDE, lancamento.getValor(), 0.001);
    }

    @Test
    public void deveRejeitarSaidaComValorAcimaDoLimite() {
        lancamento.setEstilo(EstiloLancamento.SAIDA);
        lancamento.setTipo(TipoLancamento.SANGRIA);
        lancamento.setValor(SALDO_INICIAL + VALOR_MINIMO);
        
        String resultado = caixaLancamentoService.lancamento(lancamento);
        
        assertEquals(INSUFFICIENT_BALANCE_MESSAGE, resultado);
    }

    // ===============================================================
    // TESTE DO MÉTODO DE CONSULTA
    // ===============================================================

    @Test
    public void deveRetornarListaVaziaQuandoNaoHaLancamentos() {
        when(caixaLancamentoRepository.findByCaixaEquals(caixaAberto)).thenReturn(new ArrayList<>());
        
        List<CaixaLancamento> resultado = caixaLancamentoService.lancamentosDoCaixa(caixaAberto);
        
        assertTrue(resultado.isEmpty());
    }

    @Test
    public void deveRetornarLancamentosExistentes() {
        List<CaixaLancamento> lancamentosEsperados = new ArrayList<>();
        lancamentosEsperados.add(lancamento);
        when(caixaLancamentoRepository.findByCaixaEquals(caixaAberto)).thenReturn(lancamentosEsperados);
        
        List<CaixaLancamento> resultado = caixaLancamentoService.lancamentosDoCaixa(caixaAberto);
        
        assertEquals(1, resultado.size());
        assertEquals(lancamento, resultado.get(0));
    }
}

