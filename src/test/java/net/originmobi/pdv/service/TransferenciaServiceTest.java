package net.originmobi.pdv.service;

import net.originmobi.pdv.model.Caixa;
import net.originmobi.pdv.model.Transferencia;
import net.originmobi.pdv.model.Usuario;
import net.originmobi.pdv.repository.TransferenciaRepository;
import net.originmobi.pdv.singleton.Aplicacao;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;

import java.sql.Timestamp;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(org.mockito.junit.MockitoJUnitRunner.class)
public class TransferenciaServiceTest {

    @InjectMocks
    private TransferenciaService transferenciaService;

    @Mock
    private TransferenciaRepository transferencias;

    @Mock
    private UsuarioService usuarios;

    @Mock
    private CaixaService caixas;

    @Before
    public void setUp() {
    }

    @Test
    public void testCadastrarComSucesso() {
        Double valor = 100.0;
        Long origemId = 1L;
        Long destinoId = 2L;

        Caixa origem = new Caixa();
        origem.setCodigo(origemId);
        origem.setDescricao("Caixa Origem");
        origem.setValor_total(200.0);
        origem.setData_fechamento(null);

        Caixa destino = new Caixa();
        destino.setCodigo(destinoId);
        destino.setDescricao("Caixa Destino");
        destino.setValor_total(300.0);
        destino.setData_fechamento(null);

        Aplicacao instancia = mock(Aplicacao.class);
        mockAplicacaoSingleton(instancia);

        when(caixas.busca(origemId)).thenReturn(Optional.of(origem));
        when(caixas.busca(destinoId)).thenReturn(Optional.of(destino));

        String resultado = transferenciaService.cadastrar(valor, origemId, destinoId, "Obs");

        assertEquals("Transferência realizada com sucesso", resultado);
        verify(transferencias).save(any(Transferencia.class));

        assertEquals(Double.valueOf(100.0), origem.getValor_total());
        assertEquals(Double.valueOf(400.0), destino.getValor_total());
    }

    @Test
    public void testTransferenciaComSaldoExato() {
        Double valor = 100.0;
        Long origemId = 1L;
        Long destinoId = 2L;

        Caixa origem = new Caixa();
        origem.setCodigo(origemId);
        origem.setValor_total(100.0);
        origem.setData_fechamento(null);

        Caixa destino = new Caixa();
        destino.setCodigo(destinoId);
        destino.setValor_total(50.0);
        destino.setData_fechamento(null);

        Aplicacao instancia = mock(Aplicacao.class);
        mockAplicacaoSingleton(instancia);

        when(caixas.busca(origemId)).thenReturn(Optional.of(origem));
        when(caixas.busca(destinoId)).thenReturn(Optional.of(destino));

        String resultado = transferenciaService.cadastrar(valor, origemId, destinoId, "Obs");

        assertEquals("Transferência realizada com sucesso", resultado);
        assertEquals(Double.valueOf(0.0), origem.getValor_total());
        assertEquals(Double.valueOf(150.0), destino.getValor_total());
    }

    @Test(expected = RuntimeException.class)
    public void testDestinoIgualOrigem() {
        Caixa caixa = new Caixa();
        when(caixas.busca(1L)).thenReturn(Optional.of(caixa));
        transferenciaService.cadastrar(100.0, 1L, 1L, "Obs");
    }

    @Test(expected = RuntimeException.class)
    public void testOrigemFechada() {
        Caixa origem = new Caixa();
        origem.setData_fechamento(new Timestamp(System.currentTimeMillis()));
        Caixa destino = new Caixa();

        when(caixas.busca(1L)).thenReturn(Optional.of(origem));
        when(caixas.busca(2L)).thenReturn(Optional.of(destino));

        transferenciaService.cadastrar(100.0, 1L, 2L, "Obs");
    }

    @Test(expected = RuntimeException.class)
    public void testDestinoFechado() {
        Caixa origem = new Caixa();
        origem.setValor_total(200.0);
        origem.setData_fechamento(null);

        Caixa destino = new Caixa();
        destino.setData_fechamento(new Timestamp(System.currentTimeMillis()));

        when(caixas.busca(1L)).thenReturn(Optional.of(origem));
        when(caixas.busca(2L)).thenReturn(Optional.of(destino));

        transferenciaService.cadastrar(50.0, 1L, 2L, "Obs");
    }

    @Test(expected = RuntimeException.class)
    public void testSaldoInsuficiente() {
        Caixa origem = new Caixa();
        origem.setValor_total(50.0);
        origem.setData_fechamento(null);

        Caixa destino = new Caixa();
        destino.setData_fechamento(null);

        when(caixas.busca(1L)).thenReturn(Optional.of(origem));
        when(caixas.busca(2L)).thenReturn(Optional.of(destino));

        transferenciaService.cadastrar(100.0, 1L, 2L, "Obs");
    }

    @Test(expected = RuntimeException.class)
    public void testErroNoSave() {
        Double valor = 100.0;
        Long origemId = 1L;
        Long destinoId = 2L;

        Caixa origem = new Caixa();
        origem.setValor_total(200.0);
        origem.setData_fechamento(null);

        Caixa destino = new Caixa();
        destino.setData_fechamento(null);

        Aplicacao instancia = mock(Aplicacao.class);
        mockAplicacaoSingleton(instancia);

        when(caixas.busca(origemId)).thenReturn(Optional.of(origem));
        when(caixas.busca(destinoId)).thenReturn(Optional.of(destino));

        doThrow(new RuntimeException("Erro no save")).when(transferencias).save(any(Transferencia.class));

        transferenciaService.cadastrar(valor, origemId, destinoId, "Obs");
    }

    @Test(expected = RuntimeException.class)
    public void testCaixaOrigemInexistente() {
        when(caixas.busca(1L)).thenReturn(Optional.empty());
        transferenciaService.cadastrar(50.0, 1L, 2L, "Obs");
    }

    @Test(expected = RuntimeException.class)
    public void testCaixaDestinoInexistente() {
        Caixa origem = new Caixa();
        origem.setValor_total(200.0);
        origem.setData_fechamento(null);

        when(caixas.busca(1L)).thenReturn(Optional.of(origem));
        when(caixas.busca(2L)).thenReturn(Optional.empty());

        transferenciaService.cadastrar(50.0, 1L, 2L, "Obs");
    }

    @Test(expected = RuntimeException.class)
    public void testTransferenciaComValorZero() {
        transferenciaService.cadastrar(0.0, 1L, 2L, "Obs");
    }

    @Test(expected = RuntimeException.class)
    public void testTransferenciaComValorNegativo() {
        transferenciaService.cadastrar(-50.0, 1L, 2L, "Obs");
    }

    @Test
    public void testTransferenciaComObservacaoNula() {
        Double valor = 50.0;
        Long origemId = 1L;
        Long destinoId = 2L;

        Caixa origem = new Caixa();
        origem.setCodigo(origemId);
        origem.setValor_total(200.0);
        origem.setData_fechamento(null);

        Caixa destino = new Caixa();
        destino.setCodigo(destinoId);
        destino.setValor_total(100.0);
        destino.setData_fechamento(null);

        Aplicacao instancia = mock(Aplicacao.class);
        mockAplicacaoSingleton(instancia);

        when(caixas.busca(origemId)).thenReturn(Optional.of(origem));
        when(caixas.busca(destinoId)).thenReturn(Optional.of(destino));

        String resultado = transferenciaService.cadastrar(valor, origemId, destinoId, null);

        assertEquals("Transferência realizada com sucesso", resultado);
        assertEquals(Double.valueOf(150.0), destino.getValor_total());
        assertEquals(Double.valueOf(150.0), origem.getValor_total());
    }

    @Test
    public void testTransferenciaComValorDecimal() {
        Double valor = 0.3333333333333;
        Long origemId = 1L;
        Long destinoId = 2L;

        Caixa origem = new Caixa();
        origem.setCodigo(origemId);
        origem.setValor_total(1.0);
        origem.setData_fechamento(null);

        Caixa destino = new Caixa();
        destino.setCodigo(destinoId);
        destino.setValor_total(0.0);
        destino.setData_fechamento(null);

        Aplicacao instancia = mock(Aplicacao.class);
        mockAplicacaoSingleton(instancia);

        when(caixas.busca(origemId)).thenReturn(Optional.of(origem));
        when(caixas.busca(destinoId)).thenReturn(Optional.of(destino));

        String resultado = transferenciaService.cadastrar(valor, origemId, destinoId, "Obs");

        assertEquals("Transferência realizada com sucesso", resultado);
        assertEquals(Double.valueOf(1.0 - valor), origem.getValor_total());
        assertEquals(Double.valueOf(0.0 + valor), destino.getValor_total());
    }

    private void mockAplicacaoSingleton(Aplicacao instanciaMock) {
        try {
            java.lang.reflect.Field instancia = Aplicacao.class.getDeclaredField("aplicacao");
            instancia.setAccessible(true);
            instancia.set(null, instanciaMock);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao simular singleton da Aplicacao", e);
        }
    }
}
