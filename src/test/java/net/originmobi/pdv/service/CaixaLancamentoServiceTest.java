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
    public void testLancamentoComSucesso() {
        lancamento.setEstilo(EstiloLancamento.ENTRADA);
        lancamento.setTipo(TipoLancamento.SUPRIMENTO);

        when(caixaLancamentoRepository.save(any(CaixaLancamento.class))).thenReturn(lancamento);

        String resultado = caixaLancamentoService.lancamento(lancamento);

        assertEquals("Lançamento realizado com sucesso", resultado);
        verify(caixaLancamentoRepository, times(1)).save(any(CaixaLancamento.class));
        assertTrue(lancamento.getData_cadastro() != null);
        assertEquals("Suprimento de caixa", lancamento.getObservacao());
    }

    @Test
    public void testLancamentoSangriaComObservacao() {
        lancamento.setEstilo(EstiloLancamento.SAIDA);
        lancamento.setTipo(TipoLancamento.SANGRIA);
        lancamento.setObservacao("Observação personalizada");

        when(caixaLancamentoRepository.save(any(CaixaLancamento.class))).thenReturn(lancamento);

        String resultado = caixaLancamentoService.lancamento(lancamento);

        assertEquals("Lançamento realizado com sucesso", resultado);
        verify(caixaLancamentoRepository, times(1)).save(any(CaixaLancamento.class));
        assertEquals("Observação personalizada", lancamento.getObservacao());
        assertEquals(-100.0, lancamento.getValor(), 0.001);
    }

    @Test
    public void testLancamentoSangriaComObservacaoVazia() {
        lancamento.setEstilo(EstiloLancamento.SAIDA);
        lancamento.setTipo(TipoLancamento.SANGRIA);

        when(caixaLancamentoRepository.save(any(CaixaLancamento.class))).thenReturn(lancamento);

        String resultado = caixaLancamentoService.lancamento(lancamento);

        assertEquals("Lançamento realizado com sucesso", resultado);
        verify(caixaLancamentoRepository, times(1)).save(any(CaixaLancamento.class));
        assertEquals("Sangria de caixa", lancamento.getObservacao());
        assertEquals(-100.0, lancamento.getValor(), 0.001);
    }

    @Test
    public void testLancamentoSaidaValorNegativo() {
        lancamento.setEstilo(EstiloLancamento.SAIDA);
        lancamento.setTipo(TipoLancamento.SANGRIA);
        lancamento.setValor(-100.0);

        when(caixaLancamentoRepository.save(any(CaixaLancamento.class))).thenReturn(lancamento);

        String resultado = caixaLancamentoService.lancamento(lancamento);

        assertEquals("Lançamento realizado com sucesso", resultado);
        verify(caixaLancamentoRepository, times(1)).save(any(CaixaLancamento.class));
        assertEquals(-100.0, lancamento.getValor(), 0.001);
    }

    @Test
    public void testLancamentoSaldoInsuficiente() {
        lancamento.setEstilo(EstiloLancamento.SAIDA);
        lancamento.setValor(2000.0);

        String resultado = caixaLancamentoService.lancamento(lancamento);

        assertEquals("Saldo insuficiente para realizar esta operação", resultado);
        verify(caixaLancamentoRepository, never()).save(any(CaixaLancamento.class));
    }

    @Test(expected = RuntimeException.class)
    public void testLancamentoSemCaixaAberto() {
        Caixa caixaFechado = new Caixa();
        caixaFechado.setData_fechamento(new Timestamp(System.currentTimeMillis()));
        
        lancamento.setCaixa(caixaFechado);

        caixaLancamentoService.lancamento(lancamento);
    }



    @Test(expected = RuntimeException.class)
    public void testLancamentoSemCaixa() {
        lancamento.setCaixa(null);

        caixaLancamentoService.lancamento(lancamento);
    }

    @Test(expected = RuntimeException.class)
    public void testLancamentoErroAoSalvar() {
        lancamento.setEstilo(EstiloLancamento.ENTRADA);
        
        doThrow(new RuntimeException("Erro de banco")).when(caixaLancamentoRepository).save(any(CaixaLancamento.class));

        caixaLancamentoService.lancamento(lancamento);
    }

    @Test
    public void testLancamentosDoCaixa() {
        List<CaixaLancamento> lancamentos = new ArrayList<>();
        lancamentos.add(lancamento);
        
        when(caixaLancamentoRepository.findByCaixaEquals(caixa)).thenReturn(lancamentos);

        List<CaixaLancamento> resultado = caixaLancamentoService.lancamentosDoCaixa(caixa);

        assertEquals(1, resultado.size());
        verify(caixaLancamentoRepository, times(1)).findByCaixaEquals(caixa);
    }
} 