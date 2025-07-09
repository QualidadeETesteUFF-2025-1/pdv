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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.*;
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

    private static class TestDataBuilder {

        static Function<Consumer<Fornecedor>, Fornecedor> fornecedorBuilder() {
            return customizer -> {
                Fornecedor fornecedor = new Fornecedor();
                fornecedor.setCodigo(1L);
                fornecedor.setNome("Fornecedor Teste");
                customizer.accept(fornecedor);
                return fornecedor;
            };
        }

        static Function<Consumer<PagarTipo>, PagarTipo> pagarTipoBuilder() {
            return customizer -> {
                PagarTipo pagarTipo = new PagarTipo();
                pagarTipo.setCodigo(1L);
                pagarTipo.setDescricao("Tipo Teste");
                customizer.accept(pagarTipo);
                return pagarTipo;
            };
        }

        static Function<Consumer<Caixa>, Caixa> caixaBuilder() {
            return customizer -> {
                Caixa caixa = new Caixa();
                caixa.setCodigo(1L);
                caixa.setValor_total(1000.0);
                customizer.accept(caixa);
                return caixa;
            };
        }

        static Function<Consumer<Usuario>, Usuario> usuarioBuilder() {
            return customizer -> {
                Usuario usuario = new Usuario();
                usuario.setCodigo(1L);
                customizer.accept(usuario);
                return usuario;
            };
        }

        static Function<Consumer<PagarParcela>, PagarParcela> parcelaBuilder() {
            return customizer -> {
                PagarParcela parcela = new PagarParcela();
                parcela.setCodigo(1L);
                parcela.setValor_restante(100.0);
                parcela.setValor_pago(0.0);
                parcela.setValor_desconto(0.0);
                parcela.setValor_acrescimo(0.0);
                parcela.setQuitado(0);
                customizer.accept(parcela);
                return parcela;
            };
        }
    }

    private static class TestScenarios {

        static Function<PagarService, String> cadastrarPagamentoComSucesso(
                Long fornecedorId, Double valor, String observacao, LocalDate data, PagarTipo tipo) {
            return service -> service.cadastrar(fornecedorId, valor, observacao, data, tipo);
        }

        static Function<PagarService, String> quitarPagamentoComSucesso(
                Long parcelaId, Double valorPago, Double valorDesconto, Double valorAcrescimo, Long caixaId) {
            return service -> service.quitar(parcelaId, valorPago, valorDesconto, valorAcrescimo, caixaId);
        }

        static Function<PagarService, List<Pagar>> listarPagamentos() {
            return PagarService::listar;
        }
    }

    private static class ValidationChains {

        static Function<String, String> validateSuccessMessage(String expectedMessage) {
            return result -> {
                assertThat(result, is(expectedMessage));
                return result;
            };
        }

        static Function<List<Pagar>, List<Pagar>> validateListSize(int expectedSize) {
            return list -> {
                assertThat(list, hasSize(expectedSize));
                return list;
            };
        }

        static Function<List<Pagar>, List<Pagar>> validateListNotEmpty() {
            return list -> {
                assertThat(list, not(empty()));
                return list;
            };
        }

        static Function<PagarParcela, PagarParcela> validateParcelaValues(
                Double expectedValorPago, Double expectedValorRestante, Integer expectedQuitado) {
            return parcela -> {
                assertThat(parcela.getValor_pago(), is(expectedValorPago));
                assertThat(parcela.getValor_restante(), is(expectedValorRestante));
                assertThat(parcela.getQuitado(), is(expectedQuitado));
                return parcela;
            };
        }

        static Function<PagarParcela, PagarParcela> validateParcelaQuitada() {
            return parcela -> {
                assertThat(parcela.getQuitado(), is(1));
                assertThat(parcela.getValor_restante(), is(0.0));
                return parcela;
            };
        }

        static Function<PagarParcela, PagarParcela> validateParcelaPendente() {
            return parcela -> {
                assertThat(parcela.getQuitado(), is(0));
                assertThat(parcela.getValor_restante(), greaterThan(0.0));
                return parcela;
            };
        }
    }

    private static class MockSetup {

        static <T> Consumer<T> withOptionalReturn(Function<T, Optional<T>> mockMethod) {
            return entity -> when(mockMethod.apply(entity)).thenReturn(Optional.of(entity));
        }

        static <T> Consumer<T> withSuccessReturn(Function<T, T> mockMethod) {
            return entity -> when(mockMethod.apply(entity)).thenReturn(entity);
        }

        static <T> Consumer<T> withExceptionThrown(Function<T, T> mockMethod, RuntimeException exception) {
            return entity -> when(mockMethod.apply(entity)).thenThrow(exception);
        }

        static Consumer<Runnable> executeScenario() {
            return Runnable::run;
        }
    }

    private static class TestPredicates {

        static Predicate<PagarParcela> isQuitada() {
            return parcela -> parcela.getQuitado() == 1 && parcela.getValor_restante() == 0.0;
        }

        static Predicate<PagarParcela> isPendente() {
            return parcela -> parcela.getQuitado() == 0 && parcela.getValor_restante() > 0.0;
        }

        static Predicate<PagarParcela> hasValorPago(Double valor) {
            return parcela -> parcela.getValor_pago().equals(valor);
        }

        static Predicate<PagarParcela> hasValorRestante(Double valor) {
            return parcela -> parcela.getValor_restante().equals(valor);
        }

        static Predicate<String> isSuccessMessage() {
            return message -> message.contains("sucesso");
        }
    }

    private static class TestUtils {

        static <T> Function<T, T> peek(Consumer<T> action) {
            return item -> {
                action.accept(item);
                return item;
            };
        }

        static <T> Function<T, T> validate(Predicate<T> predicate) {
            return item -> {
                assertThat(predicate.test(item), is(true));
                return item;
            };
        }

        static <T> Supplier<T> lazy(Supplier<T> supplier) {
            return supplier;
        }
    }

    @FunctionalInterface
    interface TestScenario {
        void execute();
    }

    private Fornecedor fornecedor;
    private PagarTipo pagarTipo;
    private Caixa caixa;
    private Usuario usuario;
    private PagarParcela parcela;

    @Before
    public void setUp() {
        fornecedor = TestDataBuilder.fornecedorBuilder().apply(f -> {
        });
        pagarTipo = TestDataBuilder.pagarTipoBuilder().apply(t -> {
        });
        caixa = TestDataBuilder.caixaBuilder().apply(c -> {
        });
        usuario = TestDataBuilder.usuarioBuilder().apply(u -> {
        });
        parcela = TestDataBuilder.parcelaBuilder().apply(p -> {
        });

        configureSecurityContext();
    }

    private void configureSecurityContext() {
        Optional.of(mock(Authentication.class))
                .ifPresent(auth -> {
                    when(auth.getName()).thenReturn("testuser");
                    SecurityContext securityContext = mock(SecurityContext.class);
                    when(securityContext.getAuthentication()).thenReturn(auth);
                    SecurityContextHolder.setContext(securityContext);
                });
    }

    @Test
    public void testListar() {
        List<Pagar> expectedPagamentos = new ArrayList<>();
        when(pagarRepository.findAll()).thenReturn(expectedPagamentos);

        Optional.of(pagarService)
                .map(TestScenarios.listarPagamentos())
                .map(ValidationChains.validateListSize(0))
                .ifPresent(result -> verify(pagarRepository, times(1)).findAll());
    }

    @Test
    public void testListarComDados() {
        Pagar pagamento = new Pagar();
        List<Pagar> expectedPagamentos = Arrays.asList(pagamento);
        when(pagarRepository.findAll()).thenReturn(expectedPagamentos);

        Optional.of(pagarService)
                .map(TestScenarios.listarPagamentos())
                .map(ValidationChains.validateListSize(1))
                .map(ValidationChains.validateListNotEmpty())
                .map(TestUtils.peek(list -> assertThat(list.get(0), is(pagamento))))
                .ifPresent(result -> verify(pagarRepository, times(1)).findAll());
    }

    @Test
    public void testCadastrarComObservacaoVaziaUsaDescricaoDoTipo() {
        setupCadastroSuccess();

        when(pagarRepository.save(any(Pagar.class))).thenAnswer(invocation -> {
            Pagar pagar = invocation.getArgument(0);
            assertThat(pagar.getObservacao(), is("Tipo Teste"));
            return pagar;
        });

        Optional.of(pagarService)
                .map(TestScenarios.cadastrarPagamentoComSucesso(1L, 100.0, "", LocalDate.now(), pagarTipo))
                .map(ValidationChains.validateSuccessMessage("Despesa lançada com sucesso"))
                .map(TestUtils.validate(TestPredicates.isSuccessMessage()))
                .orElseThrow(() -> new AssertionError("Teste falhou"));
    }

    @Test
    public void testCadastrarComSucesso() {
        setupCadastroSuccess();

        Optional.of(pagarService)
                .map(TestScenarios.cadastrarPagamentoComSucesso(1L, 100.0, "Teste", LocalDate.now(), pagarTipo))
                .map(ValidationChains.validateSuccessMessage("Despesa lançada com sucesso"))
                .ifPresent(result -> {
                    verify(pagarRepository, times(1)).save(any(Pagar.class));
                    verify(pagarParcelaService, times(1)).cadastrar(anyDouble(), anyDouble(), anyInt(),
                            any(Timestamp.class), any(LocalDate.class), any(Pagar.class));
                });
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
        doThrow(new RuntimeException()).when(pagarParcelaService)
                .cadastrar(anyDouble(), anyDouble(), anyInt(), any(Timestamp.class),
                        any(LocalDate.class), any(Pagar.class));

        pagarService.cadastrar(1L, 100.0, "Teste", LocalDate.now(), pagarTipo);
    }

    @Test
    public void testQuitarComSucesso() {
        setupQuitarSuccess();

        Optional.of(pagarService)
                .map(TestScenarios.quitarPagamentoComSucesso(1L, 50.0, 0.0, 0.0, 1L))
                .map(ValidationChains.validateSuccessMessage("Pagamento realizado com sucesso"))
                .ifPresent(result -> {
                    verify(pagarParcelaService, times(1)).merger(any(PagarParcela.class));
                    verify(caixaLancamentoService, times(1)).lancamento(any(CaixaLancamento.class));
                });
    }

    @Test(expected = RuntimeException.class)
    public void testQuitarValorPagoMaiorQueRestante() {
        when(pagarParcelaService.busca(1L)).thenReturn(Optional.of(parcela));

        pagarService.quitar(1L, 150.0, 0.0, 0.0, 1L);
    }

    @Test
    public void testQuitarComValorIgualAoValorRestante() {
        PagarParcela testParcela = TestDataBuilder.parcelaBuilder()
                .apply(p -> p.setValor_restante(100.0));

        setupQuitarMocks(testParcela);

        Optional.of(pagarService)
                .map(TestScenarios.quitarPagamentoComSucesso(1L, 100.0, 0.0, 0.0, 1L))
                .map(ValidationChains.validateSuccessMessage("Pagamento realizado com sucesso"))
                .ifPresent(result -> Optional.of(testParcela)
                        .map(ValidationChains.validateParcelaQuitada())
                        .orElseThrow(() -> new AssertionError("Parcela não foi quitada corretamente")));
    }

    @Test
    public void testQuitarComValorPagoAnteriorMaisNovoValor() {
        PagarParcela testParcela = TestDataBuilder.parcelaBuilder()
                .apply(p -> {
                    p.setValor_restante(200.0);
                    p.setValor_pago(20.0);
                });

        setupQuitarMocks(testParcela);

        pagarService.quitar(1L, 50.0, 0.0, 10.0, 1L);

        Optional.of(testParcela)
                .map(ValidationChains.validateParcelaValues(80.0, 150.0, 0))
                .map(TestUtils.validate(TestPredicates.hasValorPago(80.0)))
                .map(TestUtils.validate(TestPredicates.hasValorRestante(150.0)))
                .map(TestUtils.validate(TestPredicates.isPendente()))
                .orElseThrow(() -> new AssertionError("Validação da parcela falhou"));
    }

    @Test
    public void testQuitarComPagamentoExatoZeraRestanteSemForcar() {
        PagarParcela testParcela = TestDataBuilder.parcelaBuilder()
                .apply(p -> p.setValor_restante(100.0));

        setupQuitarMocks(testParcela);

        pagarService.quitar(1L, 90.0, 10.0, 0.0, 1L);

        Optional.of(testParcela)
                .map(ValidationChains.validateParcelaQuitada())
                .map(TestUtils.validate(TestPredicates.isQuitada()))
                .orElseThrow(() -> new AssertionError("Parcela deveria estar quitada"));
    }

    @Test
    public void testQuitarSomaDescontoAnteriorMaisNovo() {
        PagarParcela testParcela = TestDataBuilder.parcelaBuilder()
                .apply(p -> {
                    p.setValor_restante(100.0);
                    p.setValor_desconto(5.0);
                });

        setupQuitarMocks(testParcela);

        pagarService.quitar(1L, 90.0, 10.0, 0.0, 1L);

        Optional.of(testParcela)
                .map(TestUtils.peek(p -> assertThat(p.getValor_desconto(), is(15.0))))
                .map(ValidationChains.validateParcelaQuitada())
                .map(TestUtils.validate(TestPredicates.isQuitada()))
                .orElseThrow(() -> new AssertionError("Teste de desconto falhou"));
    }

    @Test(expected = RuntimeException.class)
    public void testQuitarSaldoInsuficiente() {
        when(pagarParcelaService.busca(1L)).thenReturn(Optional.of(parcela));
        Caixa caixaComSaldoInsuficiente = TestDataBuilder.caixaBuilder()
                .apply(c -> c.setValor_total(10.0));
        when(caixaService.busca(1L)).thenReturn(Optional.of(caixaComSaldoInsuficiente));

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

    private void setupCadastroSuccess() {
        when(fornecedorService.busca(1L)).thenReturn(Optional.of(fornecedor));
        when(pagarRepository.save(any(Pagar.class))).thenReturn(new Pagar());
        doNothing().when(pagarParcelaService).cadastrar(anyDouble(), anyDouble(), anyInt(),
                any(Timestamp.class), any(LocalDate.class), any(Pagar.class));
    }

    private void setupQuitarSuccess() {
        when(pagarParcelaService.busca(1L)).thenReturn(Optional.of(parcela));
        when(caixaService.busca(1L)).thenReturn(Optional.of(caixa));
        when(usuarioService.buscaUsuario(anyString())).thenReturn(usuario);
        when(pagarParcelaService.merger(any(PagarParcela.class))).thenReturn(parcela);
        when(caixaLancamentoService.lancamento(any(CaixaLancamento.class)))
                .thenReturn("Lançamento realizado com sucesso");
    }

    private void setupQuitarMocks(PagarParcela testParcela) {
        when(pagarParcelaService.busca(1L)).thenReturn(Optional.of(testParcela));
        when(caixaService.busca(1L)).thenReturn(Optional.of(caixa));
        when(usuarioService.buscaUsuario(anyString())).thenReturn(usuario);
        when(pagarParcelaService.merger(any(PagarParcela.class))).thenAnswer(i -> i.getArgument(0));
        when(caixaLancamentoService.lancamento(any())).thenReturn("Lançamento realizado com sucesso");
    }
}