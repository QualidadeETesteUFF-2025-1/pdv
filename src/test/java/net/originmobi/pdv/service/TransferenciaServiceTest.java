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
        // Nada adicional necessário aqui
    }

@Test
public void testCadastrarComSucesso() {
    Double valor = 100.0;
    Long origemId = 1L;
    Long destinoId = 2L;

    Usuario usuario = new Usuario();
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
    when(instancia.getUsuarioAtual()).thenReturn("admin");
    mockAplicacaoSingleton(instancia);

    when(usuarios.buscaUsuario("admin")).thenReturn(usuario);
    when(caixas.busca(origemId)).thenReturn(Optional.of(origem));
    when(caixas.busca(destinoId)).thenReturn(Optional.of(destino));

    String resultado = transferenciaService.cadastrar(valor, origemId, destinoId, "Obs");

    assertEquals("Transferência realizada com sucesso", resultado);
    verify(transferencias).save(any(Transferencia.class));
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

        Usuario usuario = new Usuario();
        Caixa origem = new Caixa();
        origem.setValor_total(200.0);
        origem.setData_fechamento(null);

        Caixa destino = new Caixa();
        destino.setData_fechamento(null);

        Aplicacao instancia = mock(Aplicacao.class);
        when(instancia.getUsuarioAtual()).thenReturn("admin");
        mockAplicacaoSingleton(instancia);

        when(usuarios.buscaUsuario("admin")).thenReturn(usuario);
        when(caixas.busca(origemId)).thenReturn(Optional.of(origem));
        when(caixas.busca(destinoId)).thenReturn(Optional.of(destino));

        doThrow(new RuntimeException("erro banco")).when(transferencias).save(any(Transferencia.class));

        transferenciaService.cadastrar(valor, origemId, destinoId, "Obs");
    }

    // Workaround para simular o singleton da Aplicacao usando um "gancho estático"
    private void mockAplicacaoSingleton(Aplicacao instanciaMock) {
        try {
            java.lang.reflect.Field instancia = Aplicacao.class.getDeclaredField("instancia");
            instancia.setAccessible(true);
            instancia.set(null, instanciaMock);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao simular singleton da Aplicacao", e);
        }
    }
}
