package net.originmobi.pdv.service;

import net.originmobi.pdv.model.GrupoUsuario;
import net.originmobi.pdv.model.Usuario;
import net.originmobi.pdv.repository.GrupoUsuarioRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class GrupoUsuarioServiceTest {

    @InjectMocks    
    private GrupoUsuarioService service;

    @Mock
    private GrupoUsuarioRepository repository;

    @Mock
    private RedirectAttributes redirectAttributes;

    private GrupoUsuario grupo;

    private String mensagemErro = "mensagemErro";

    @Before
    public void setUp() {
        grupo = new GrupoUsuario();
        grupo.setCodigo(1L);
        grupo.setNome("Admin");
        grupo.setDescricao("Grupo administrativo");
    }

    @Test
    public void testBuscaGrupos() {
        Usuario usuario = new Usuario();
        when(repository.findByUsuarioIn(usuario)).thenReturn(Arrays.asList(grupo));

        List<GrupoUsuario> resultado = service.buscaGrupos(usuario);

        assertEquals(1, resultado.size());
        verify(repository).findByUsuarioIn(usuario);
    }

    @Test
    public void testLista() {
        when(repository.findAll()).thenReturn(Collections.singletonList(grupo));

        List<GrupoUsuario> resultado = service.lista();

        assertEquals(1, resultado.size());
        verify(repository).findAll();
    }

    @Test
    public void testBuscaGrupo() {
        when(repository.findByCodigoIn(1L)).thenReturn(grupo);

        GrupoUsuario resultado = service.buscaGrupo(1L);

        assertEquals("Admin", resultado.getNome());
        verify(repository).findByCodigoIn(1L);
    }

    @Test
    public void testMergeNovoGrupo() {
        GrupoUsuario novoGrupo = new GrupoUsuario(); // Código é null => novo grupo
        when(repository.save(novoGrupo)).thenReturn(novoGrupo);

        service.merge(novoGrupo, redirectAttributes);

        verify(redirectAttributes).addFlashAttribute(mensagemErro, "Grupo adicionado com sucesso");
        verify(repository).save(novoGrupo);
    }

    @Test
    public void testMergeGrupoExistente() {
        service.merge(grupo, redirectAttributes);

        verify(redirectAttributes).addFlashAttribute(mensagemErro, "Grupo atualizado com sucesso");
        verify(repository).update(grupo.getNome(), grupo.getDescricao(), grupo.getCodigo());
    }

    @Test
    public void testRemoveComUsuarioVinculado() {
        when(repository.grupoTemUsuaio(1L)).thenReturn(1);

        String resultado = service.remove(1L, redirectAttributes);

        verify(redirectAttributes).addFlashAttribute(mensagemErro,
                "Este grupo possue usuários vinculados a ele, verifique");
        assertEquals("redirect:/grupousuario/1", resultado);
    }

    @Test
    public void testRemoveSemUsuarioVinculado() {
        when(repository.grupoTemUsuaio(1L)).thenReturn(0);

        String resultado = service.remove(1L, redirectAttributes);

        verify(repository).deleteById(1L);
        assertEquals("redirect:/grupousuario", resultado);
    }

    @Test
    public void testAddPermissaoComPermissaoNova() {
        when(repository.grupoTemPermissao(1L, 2L)).thenReturn(0);

        String resultado = service.addPermissao(1L, 2L);

        verify(repository).addPermissao(1L, 2L);
        assertEquals("Permissao adicionada com sucesso", resultado);
    }

    @Test(expected = RuntimeException.class)
    public void testAddPermissaoJaExistente() {
        when(repository.grupoTemPermissao(1L, 2L)).thenReturn(1);

        service.addPermissao(1L, 2L);
    }

    @Test
    public void testRemovePermissao() {
        String resultado = service.removePermissao(2L, 1L);

        verify(repository).removePermissao(2L, 1L);
        assertEquals("Permissão removida com sucesso", resultado);
    }
}
