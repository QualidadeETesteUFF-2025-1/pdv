package net.originmobi.pdv.service;

import net.originmobi.pdv.model.*;
import net.originmobi.pdv.repository.PagarRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.Silent.class)
public class PagarServiceTest {

    @Mock
    private PagarRepository pagarRepository;

    @Mock
    private PagarParcelaService pagarParcelaService;

    @Mock
    private FornecedorService fornecedorService;

    @Mock
    private CaixaService caixaService;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private CaixaLancamentoService caixaLancamentoService;

    @InjectMocks
    private PagarService pagarService;

    private Fornecedor fornecedor;
    private PagarTipo pagarTipo;
    private Caixa caixa;
    private Usuario usuario;
    private PagarParcela parcela;

    @Before
    public void setUp() {
        fornecedor = new Fornecedor();
        fornecedor.setCodigo(1L);
        fornecedor.setNome("Fornecedor Teste");

        pagarTipo = new PagarTipo();
        pagarTipo.setCodigo(1L);
        pagarTipo.setDescricao("Tipo Teste");

        caixa = new Caixa();
        caixa.setCodigo(1L);
        caixa.setValor_total(1000.0);

        usuario = new Usuario();
        usuario.setCodigo(1L);

        parcela = new PagarParcela();
        parcela.setCodigo(1L);
        parcela.setValor_restante(100.0);
        parcela.setValor_pago(0.0);
        parcela.setValor_desconto(0.0);
        parcela.setValor_acrescimo(0.0);
        parcela.setQuitado(0);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("testuser");
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    public void testListar() {
        List<Pagar> pagamentos = new ArrayList<>();
        when(pagarRepository.findAll()).thenReturn(pagamentos);

        List<Pagar> resultado = pagarService.listar();

        assertEquals(pagamentos, resultado);
        verify(pagarRepository, times(1)).findAll();
    }

    @Test
    public void testListarComDados() {
        Pagar pagamento = new Pagar();
        List<Pagar> pagamentos = new ArrayList<>();
        pagamentos.add(pagamento);

        when(pagarRepository.findAll()).thenReturn(pagamentos);

        List<Pagar> resultado = pagarService.listar();

        assertEquals(1, resultado.size());
        assertEquals(pagamento, resultado.get(0));
        verify(pagarRepository, times(1)).findAll();
    }

    @Test
    public void testCadastrarComObservacaoVaziaUsaDescricaoDoTipo() {
        when(fornecedorService.busca(1L)).thenReturn(Optional.of(fornecedor));
        when(pagarRepository.save(any(Pagar.class))).thenAnswer(invocation -> {
            Pagar pagar = invocation.getArgument(0);
            assertEquals("Tipo Teste", pagar.getObservacao());
            return pagar;
        });
        doNothing().when(pagarParcelaService).cadastrar(anyDouble(), anyDouble(), anyInt(),
                any(Timestamp.class), any(LocalDate.class), any(Pagar.class));

        String resultado = pagarService.cadastrar(1L, 100.0, "", LocalDate.now(), pagarTipo);

        assertEquals("Despesa lançada com sucesso", resultado);
    }


    @Test
    public void testCadastrarComSucesso() {
        when(fornecedorService.busca(1L)).thenReturn(Optional.of(fornecedor));
        when(pagarRepository.save(any(Pagar.class))).thenReturn(new Pagar());
        doNothing().when(pagarParcelaService).cadastrar(anyDouble(), anyDouble(), anyInt(), any(Timestamp.class),
                any(LocalDate.class), any(Pagar.class));

        String resultado = pagarService.cadastrar(1L, 100.0, "Teste", LocalDate.now(), pagarTipo);

        assertEquals("Despesa lançada com sucesso", resultado);
        verify(pagarRepository, times(1)).save(any(Pagar.class));
        verify(pagarParcelaService, times(1)).cadastrar(anyDouble(), anyDouble(), anyInt(), any(Timestamp.class),
                any(LocalDate.class), any(Pagar.class));
    }

    @Test(expected = RuntimeException.class)
    public void testCadastrarErroAoSalvar() {
        when(fornecedorService.busca(1L)).thenReturn(Optional.of(fornecedor));
        when(pagarRepository.save(any(Pagar.class))).thenThrow(new RuntimeException());

        pagarService.cadastrar(1L, 100.0, "Teste", LocalDate.now(), pagarTipo);
    }

    @Test(expected = RuntimeException.class)
    public void testCadastrarErroAoSalvarParcela() {
        when(fornecedorService.busca(1L)).thenReturn(Optional.of(fornecedor));
        when(pagarRepository.save(any(Pagar.class))).thenReturn(new Pagar());
        doThrow(new RuntimeException()).when(pagarParcelaService).cadastrar(anyDouble(), anyDouble(), anyInt(),
                any(Timestamp.class), any(LocalDate.class), any(Pagar.class));

        pagarService.cadastrar(1L, 100.0, "Teste", LocalDate.now(), pagarTipo);
    }

    @Test
    public void testQuitarComSucesso() {
        when(pagarParcelaService.busca(1L)).thenReturn(Optional.of(parcela));
        when(caixaService.busca(1L)).thenReturn(Optional.of(caixa));
        when(usuarioService.buscaUsuario(anyString())).thenReturn(usuario);
        when(pagarParcelaService.merger(any(PagarParcela.class))).thenReturn(parcela);
        when(caixaLancamentoService.lancamento(any(CaixaLancamento.class)))
                .thenReturn("Lançamento realizado com sucesso");

        String resultado = pagarService.quitar(1L, 50.0, 0.0, 0.0, 1L);

        assertEquals("Pagamento realizado com sucesso", resultado);
        verify(pagarParcelaService, times(1)).merger(any(PagarParcela.class));
        verify(caixaLancamentoService, times(1)).lancamento(any(CaixaLancamento.class));
    }

    @Test(expected = RuntimeException.class)
    public void testQuitarValorPagoMaiorQueRestante() {
        when(pagarParcelaService.busca(1L)).thenReturn(Optional.of(parcela));

        pagarService.quitar(1L, 150.0, 0.0, 0.0, 1L);
    }

    @Test
    public void testQuitarComValorIgualAoValorRestante() {
        PagarParcela mockParcela = mock(PagarParcela.class);
        
        when(mockParcela.getValor_restante()).thenReturn(100.0);
        when(mockParcela.getValor_pago()).thenReturn(0.0);
        when(mockParcela.getValor_desconto()).thenReturn(0.0);
        when(mockParcela.getValor_acrescimo()).thenReturn(0.0);
    
        when(pagarParcelaService.busca(1L)).thenReturn(Optional.of(mockParcela));
        when(caixaService.busca(1L)).thenReturn(Optional.of(caixa));
        when(usuarioService.buscaUsuario(anyString())).thenReturn(usuario);
        when(pagarParcelaService.merger(any(PagarParcela.class))).thenReturn(mockParcela);
        when(caixaLancamentoService.lancamento(any(CaixaLancamento.class)))
                .thenReturn("Lançamento realizado com sucesso");
    
        String resultado = pagarService.quitar(1L, 100.0, 0.0, 0.0, 1L);
    
        assertEquals("Pagamento realizado com sucesso", resultado);
    }

    @Test
    public void testQuitarComValorPagoAnteriorMaisNovoValor() {
        PagarParcela parcela = new PagarParcela();
        parcela.setValor_restante(200.0);
        parcela.setValor_pago(20.0); // já pago anteriormente
        parcela.setValor_desconto(0.0);
        parcela.setValor_acrescimo(0.0);
    
        when(pagarParcelaService.busca(1L)).thenReturn(Optional.of(parcela));
        when(caixaService.busca(1L)).thenReturn(Optional.of(caixa));
        when(usuarioService.buscaUsuario(anyString())).thenReturn(usuario);
    
        when(pagarParcelaService.merger(any(PagarParcela.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(caixaLancamentoService.lancamento(any(CaixaLancamento.class)))
                .thenReturn("Lançamento realizado com sucesso");
    
        pagarService.quitar(1L, 50.0, 0.0, 10.0, 1L); // 50 + 10 = 60 + 20 anteriores = 80
    
        assertEquals(80.0, parcela.getValor_pago(), 0.001);
        assertEquals(150.0, parcela.getValor_restante(), 0.001); // <- Corrigido aqui
        assertEquals(10.0, parcela.getValor_acrescimo(), 0.001);
    }
    

    @Test
    public void testQuitarComPagamentoExatoZeraRestanteSemForcar() {
        PagarParcela parcela = new PagarParcela();
        parcela.setValor_restante(100.0);
        parcela.setValor_pago(0.0);
        parcela.setValor_desconto(0.0);
        parcela.setValor_acrescimo(0.0);
    
        when(pagarParcelaService.busca(1L)).thenReturn(Optional.of(parcela));
        when(caixaService.busca(1L)).thenReturn(Optional.of(caixa));
        when(usuarioService.buscaUsuario(anyString())).thenReturn(usuario);
    
        when(pagarParcelaService.merger(any(PagarParcela.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(caixaLancamentoService.lancamento(any(CaixaLancamento.class)))
                .thenReturn("Lançamento realizado com sucesso");
    
        pagarService.quitar(1L, 90.0, 10.0, 0.0, 1L);
    
        assertEquals(0.0, parcela.getValor_restante(), 0.001);
        assertEquals(1, parcela.getQuitado());
    }

    @Test
    public void testQuitarSomaDescontoAnteriorMaisNovo() {
        PagarParcela parcela = new PagarParcela();
        parcela.setValor_restante(100.0);
        parcela.setValor_pago(0.0);
        parcela.setValor_desconto(5.0); // já tinha desconto
        parcela.setValor_acrescimo(0.0);

        when(pagarParcelaService.busca(1L)).thenReturn(Optional.of(parcela));
        when(caixaService.busca(1L)).thenReturn(Optional.of(caixa));
        when(usuarioService.buscaUsuario(anyString())).thenReturn(usuario);
        when(pagarParcelaService.merger(any(PagarParcela.class))).thenAnswer(i -> i.getArgument(0));
        when(caixaLancamentoService.lancamento(any())).thenReturn("ok");

        pagarService.quitar(1L, 90.0, 10.0, 0.0, 1L); // novo desconto = 10
        assertEquals(15.0, parcela.getValor_desconto(), 0.001);
        assertEquals(0.0, parcela.getValor_restante(), 0.001);
    }
 

    @Test(expected = RuntimeException.class)
    public void testQuitarSaldoInsuficiente() {
        when(pagarParcelaService.busca(1L)).thenReturn(Optional.of(parcela));
        caixa.setValor_total(10.0);
        when(caixaService.busca(1L)).thenReturn(Optional.of(caixa));

        pagarService.quitar(1L, 50.0, 0.0, 0.0, 1L);
    }

    @Test(expected = RuntimeException.class)
    public void testQuitarErroAoSalvarParcela() {
        when(pagarParcelaService.busca(1L)).thenReturn(Optional.of(parcela));
        when(caixaService.busca(1L)).thenReturn(Optional.of(caixa));
        when(usuarioService.buscaUsuario(anyString())).thenReturn(usuario);
        when(pagarParcelaService.merger(any(PagarParcela.class))).thenThrow(new RuntimeException());

        pagarService.quitar(1L, 50.0, 0.0, 0.0, 1L);
    }

    @Test(expected = RuntimeException.class)
    public void testQuitarErroAoSalvarLancamento() {
        when(pagarParcelaService.busca(1L)).thenReturn(Optional.of(parcela));
        when(caixaService.busca(1L)).thenReturn(Optional.of(caixa));
        when(usuarioService.buscaUsuario(anyString())).thenReturn(usuario);
        when(pagarParcelaService.merger(any(PagarParcela.class))).thenReturn(parcela);
        doThrow(new RuntimeException()).when(caixaLancamentoService).lancamento(any(CaixaLancamento.class));

        pagarService.quitar(1L, 50.0, 0.0, 0.0, 1L);
    }
}