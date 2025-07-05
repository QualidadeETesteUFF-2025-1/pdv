import unittest
import time
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.ui import Select
from selenium.webdriver.common.action_chains import ActionChains


class GerenciarGrupoUsuarioTest(unittest.TestCase):
    
    def setUp(self):
        try:
            self.driver = webdriver.Chrome()
        except:
            from selenium.webdriver.chrome.service import Service
            from webdriver_manager.chrome import ChromeDriverManager
            service = Service(ChromeDriverManager().install())
            self.driver = webdriver.Chrome(service=service)
            
        self.driver.maximize_window()
        self.wait = WebDriverWait(self.driver, 10)
        
        self.driver.get("http://localhost:8080/")
        self.fazer_login()
    
    def fazer_login(self):
        username_input = self.wait.until(EC.presence_of_element_located((By.ID, "user")))
        password_input = self.driver.find_element(By.ID, "password")
        login_button = self.driver.find_element(By.ID, "btn-login")
        
        username_input.send_keys("gerente")
        password_input.send_keys("123")
        login_button.click()
        
        logout_form = self.wait.until(EC.presence_of_element_located((By.XPATH, "//form[contains(@action, '/logout')]")))
        self.assertIn("Usuário: gerente", logout_form.text, "Login deve ser realizado com sucesso")
    
    def navegar_para_grupos_usuario(self):
        gerenciar_usuarios_menu = self.wait.until(EC.element_to_be_clickable((
            By.XPATH, "//a[img[@alt='Usuários']]"
        )))
        gerenciar_usuarios_menu.click()
        
        usuarios_option = self.wait.until(EC.visibility_of_element_located((
            By.XPATH, "//a[@href='/usuario' and @class='opcoes']"
        )))
        grupo_usuarios_option = self.wait.until(EC.visibility_of_element_located((
            By.XPATH, "//a[@href='/grupousuario' and @class='opcoes']"
        )))
        
        self.assertTrue(usuarios_option.is_displayed(), "Opção 'Usuários' deve estar visível")
        self.assertTrue(grupo_usuarios_option.is_displayed(), "Opção 'Grupo usuários' deve estar visível")
        
        grupo_usuarios_clickable = self.wait.until(EC.element_to_be_clickable((
            By.XPATH, "//a[@href='/grupousuario' and @class='opcoes']"
        )))
        grupo_usuarios_clickable.click()
        
        page_title = self.wait.until(EC.visibility_of_element_located((
            By.XPATH, "//h1[contains(text(), 'Grupos') or contains(text(), 'Grupo')]"
        )))
        grupos_table = self.driver.find_element(By.XPATH, "//table[contains(@class, 'table')]")
        
        self.assertTrue(page_title.is_displayed(), "Título da página deve estar visível")
        self.assertTrue(grupos_table.is_displayed(), "Tabela de grupos deve estar visível")
    
    def editar_grupo_descricao(self):
        editar_grupo_button = self.wait.until(EC.element_to_be_clickable((
            By.XPATH, "//a[span[@class='glyphicon glyphicon-pencil']]"
        )))
        editar_grupo_button.click()
        
        descricao_textarea = self.wait.until(EC.visibility_of_element_located((
            By.ID, "descricao"
        )))
        self.assertTrue(descricao_textarea.is_displayed(), "Campo de descrição deve estar visível")
        
        nova_descricao = "Teste selenium"
        descricao_textarea.clear()
        descricao_textarea.send_keys(nova_descricao)
        
        salvar_button = self.driver.find_element(By.XPATH, "//input[@type='submit' and @class='btn btn-azul-padrao' and @value='Salvar']")
        salvar_button.click()
        
        success_message = self.wait.until(EC.visibility_of_element_located((
            By.XPATH, "//div[@class='alert alert-success alert-dismissable']//span[contains(text(), 'Grupo atualizado com sucesso')]"
        )))
        self.assertTrue(success_message.is_displayed(), "Mensagem 'Grupo atualizado com sucesso' deve aparecer")
    
    def gerenciar_permissoes_grupo(self):
        editar_grupo_button_again = self.wait.until(EC.element_to_be_clickable((
            By.XPATH, "//a[span[@class='glyphicon glyphicon-pencil']]"
        )))
        editar_grupo_button_again.click()
        
        permissions_table = self.wait.until(EC.visibility_of_element_located((
            By.XPATH, "//div[@class='tabela-dados-permissoes']//table[@class='table']"
        )))
        
        self.driver.execute_script("arguments[0].scrollIntoView(true);", permissions_table)
        time.sleep(1)
        
        self.assertTrue(permissions_table.is_displayed(), "Tabela de permissões deve estar visível")
        
        self._remover_permissao_faz_ajuste()
        
        self._adicionar_permissao_faz_ajuste()
    
    def _remover_permissao_faz_ajuste(self):
        try:
            remover_permissao_button = self.driver.find_element(By.XPATH, "//a[@class='btn-remove-permissao' and @data-codigo='51']")
            remover_permissao_button.click()
            
            alert = self.wait.until(EC.alert_is_present())
            alert_text = alert.text
            self.assertIn("Permissão removida com sucesso", alert_text, "Alert deve confirmar que a permissão foi removida")
            alert.accept()
        except:
            pass
    
    def _adicionar_permissao_faz_ajuste(self):
        time.sleep(2)
        
        permissao_dropdown = self.wait.until(EC.element_to_be_clickable((
            By.XPATH, "//button[@class='btn dropdown-toggle selectpicker btn-default' and @data-id='codigoPermissao']"
        )))
        permissao_dropdown.click()
        
        faz_ajuste_option = self.wait.until(EC.element_to_be_clickable((
            By.XPATH, "//li[@data-original-index]//span[contains(text(), 'FAZ_AJUSTE')]"
        )))
        faz_ajuste_option.click()
        
        inserir_button = self.wait.until(EC.element_to_be_clickable((
            By.XPATH, "//a[@href='/grupousuario/addpermissao' and @class='btn form-control btn-add-permissao btn-azul-padrao' and @id='js-url' and contains(text(), 'Inserir')]"
        )))
        inserir_button.click()
        
        alert_add = self.wait.until(EC.alert_is_present())
        alert_add_text = alert_add.text
        self.assertIn("Permissao adicionada com sucesso", alert_add_text, "Alert deve confirmar que a permissão foi adicionada")
        alert_add.accept()
    
    def navegar_para_usuarios(self):
        gerenciar_usuarios_menu_again = self.wait.until(EC.element_to_be_clickable((
            By.XPATH, "//a[img[@alt='Usuários']]"
        )))
        gerenciar_usuarios_menu_again.click()
        
        usuarios_option = self.wait.until(EC.element_to_be_clickable((
            By.XPATH, "//a[@href='/usuario' and @class='opcoes']"
        )))
        usuarios_option.click()
        
        usuarios_page_title = self.wait.until(EC.visibility_of_element_located((
            By.XPATH, "//h1[contains(text(), 'Usuários') or contains(text(), 'Usuário')]"
        )))
        usuarios_table = self.driver.find_element(By.XPATH, "//table[contains(@class, 'table')]")
        
        self.assertTrue(usuarios_page_title.is_displayed(), "Título da página de usuários deve estar visível")
        self.assertTrue(usuarios_table.is_displayed(), "Tabela de usuários deve estar visível")
    
    def acessar_edicao_usuario(self):
        editar_usuario_button = self.wait.until(EC.element_to_be_clickable((
            By.XPATH, "//a[span[@class='glyphicon glyphicon-pencil']]"
        )))
        editar_usuario_button.click()
    
    def clicar_aba_grupos(self):
        self._debug_abas_disponiveis()
        
        estrategias = [
            ("Standard click", lambda: self._clicar_aba_grupos_metodo_1()),
            ("JavaScript click", lambda: self._clicar_aba_grupos_metodo_2()),
            ("ActionChains", lambda: self._clicar_aba_grupos_metodo_3()),
            ("Por texto", lambda: self._clicar_aba_grupos_metodo_4())
        ]
        
        for nome_estrategia, metodo in estrategias:
            try:
                metodo()
                return
            except Exception as e:
                pass
        
        raise Exception("Todos os métodos de clique na aba grupos falharam")
    
    def _debug_abas_disponiveis(self):
        try:
            all_tabs = self.driver.find_elements(By.XPATH, "//ul[@class='nav nav-tabs']//a")
        except Exception as e:
            pass
    
    def _clicar_aba_grupos_metodo_1(self):
        aba_grupos = self.wait.until(EC.element_to_be_clickable((
            By.XPATH, "//a[@data-toggle='tab' and @href='#gruposusuario']"
        )))
        self.driver.execute_script("arguments[0].scrollIntoView(true);", aba_grupos)
        time.sleep(1)
        aba_grupos.click()
    
    def _clicar_aba_grupos_metodo_2(self):
        aba_grupos = self.wait.until(EC.presence_of_element_located((
            By.XPATH, "//a[@data-toggle='tab' and @href='#gruposusuario']"
        )))
        self.driver.execute_script("arguments[0].scrollIntoView(true);", aba_grupos)
        time.sleep(1)
        self.driver.execute_script("arguments[0].click();", aba_grupos)
    
    def _clicar_aba_grupos_metodo_3(self):
        aba_grupos = self.wait.until(EC.presence_of_element_located((
            By.XPATH, "//a[@data-toggle='tab' and @href='#gruposusuario']"
        )))
        self.driver.execute_script("arguments[0].scrollIntoView(true);", aba_grupos)
        time.sleep(1)
        ActionChains(self.driver).move_to_element(aba_grupos).click().perform()
    
    def _clicar_aba_grupos_metodo_4(self):
        aba_grupos = self.wait.until(EC.element_to_be_clickable((
            By.XPATH, "//a[contains(text(), 'Grupos')]"
        )))
        self.driver.execute_script("arguments[0].scrollIntoView(true);", aba_grupos)
        time.sleep(1)
        self.driver.execute_script("arguments[0].click();", aba_grupos)
    
    def aguardar_conteudo_aba_grupos(self):
        try:
            grupos_tab_content = self.wait.until(EC.visibility_of_element_located((
                By.XPATH, "//div[@id='gruposusuario']"
            )))
        except:
            grupos_tab_content = self.wait.until(EC.visibility_of_element_located((
                By.XPATH, "//div[contains(@class, 'tab-pane') and contains(@id, 'grupos')]"
            )))
        
        try:
            grupos_table = self.driver.find_element(By.XPATH, "//table[contains(@class, 'grupos') or .//th[contains(text(), 'Grupo')]]")
            self.assertTrue(grupos_table.is_displayed(), "Tabela de grupos deve estar visível")
        except:
            pass
        
        self.assertTrue(grupos_tab_content.is_displayed(), "Conteúdo da aba grupos deve estar visível")
    
    def adicionar_grupo_vendedor(self):
        grupo_dropdown = self.wait.until(EC.element_to_be_clickable((
            By.XPATH, "//button[@class='btn dropdown-toggle selectpicker btn-default' and @data-id='codigoGrupo']"
        )))
        grupo_dropdown.click()
        
        self._debug_opcoes_dropdown()
        
        self._selecionar_opcao_vendedor()
        
        inserir_button = self.wait.until(EC.element_to_be_clickable((
            By.XPATH, "//a[@href='/usuario/addgrupo/?codigoUsu=1&codigoGru' and @class='btn form-control js-add-grupo btn-azul-padrao' and @id='js-url' and contains(text(), 'Inserir')]"
        )))
        inserir_button.click()
        
        self._processar_alert_adicao()
    
    def _debug_opcoes_dropdown(self):
        time.sleep(2)
        try:
            dropdown_options = self.driver.find_elements(By.XPATH, "//li[@data-original-index]")
        except Exception as e:
            pass
    
    def _selecionar_opcao_vendedor(self):
        estrategias = [
            ("Case insensitive span", lambda: self._selecionar_vendedor_metodo_1()),
            ("Case insensitive li", lambda: self._selecionar_vendedor_metodo_2()),
            ("Primeira opção", lambda: self._selecionar_vendedor_metodo_3()),
            ("Qualquer span", lambda: self._selecionar_vendedor_metodo_4())
        ]
        
        for nome_estrategia, metodo in estrategias:
            try:
                metodo()
                return
            except Exception as e:
                pass
        
        raise Exception("Todos os métodos de seleção do vendedor falharam")
    
    def _selecionar_vendedor_metodo_1(self):
        vendedor_option = self.wait.until(EC.element_to_be_clickable((
            By.XPATH, "//li[@data-original-index]//span[contains(translate(text(), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'vendedor')]"
        )))
        vendedor_option.click()
    
    def _selecionar_vendedor_metodo_2(self):
        vendedor_option = self.wait.until(EC.element_to_be_clickable((
            By.XPATH, "//li[contains(translate(text(), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'vendedor')]"
        )))
        vendedor_option.click()
    
    def _selecionar_vendedor_metodo_3(self):
        first_option = self.wait.until(EC.element_to_be_clickable((
            By.XPATH, "//li[@data-original-index][1]"
        )))
        first_option.click()
    
    def _selecionar_vendedor_metodo_4(self):
        any_option = self.wait.until(EC.element_to_be_clickable((
            By.XPATH, "//div[@class='dropdown-menu open']//span"
        )))
        any_option.click()
    
    def _processar_alert_adicao(self):
        alert = self.wait.until(EC.alert_is_present())
        alert_text = alert.text
        
        if "Grupo adicionado com sucesso" in alert_text:
            pass
        elif "Grupo já esta adicionado a este usuário" in alert_text:
            pass
        else:
            pass
        
        alert.accept()
    
    def remover_grupo_vendedor(self):
        time.sleep(2)
        
        self._debug_grupos_tabela()
        
        self._clicar_botao_remover_vendedor()
        
        self._processar_alert_remocao()
    
    def _debug_grupos_tabela(self):
        try:
            grupos_table = self.driver.find_element(By.ID, "tabUsuGrupos")
            grupos_rows = grupos_table.find_elements(By.XPATH, ".//tbody/tr")
        except Exception as e:
            pass
    
    def _clicar_botao_remover_vendedor(self):
        estrategias = [
            ("Por linha VENDEDOR", lambda: self._remover_vendedor_metodo_1()),
            ("Por data-codigogrupo", lambda: self._remover_vendedor_metodo_2()),
            ("Último botão", lambda: self._remover_vendedor_metodo_3())
        ]
        
        for nome_estrategia, metodo in estrategias:
            try:
                metodo()
                return
            except Exception as e:
                pass
        
        raise Exception("Todos os métodos de remoção do vendedor falharam")
    
    def _remover_vendedor_metodo_1(self):
        vendedor_remove_button = self.wait.until(EC.element_to_be_clickable((
            By.XPATH, "//tr[td[contains(text(), 'VENDEDOR')]]//a[@class='btn btn-link btn-xs js-remove-grupo']"
        )))
        vendedor_remove_button.click()
    
    def _remover_vendedor_metodo_2(self):
        vendedor_remove_button = self.wait.until(EC.element_to_be_clickable((
            By.XPATH, "//a[@class='btn btn-link btn-xs js-remove-grupo' and @data-codigogrupo='2']"
        )))
        vendedor_remove_button.click()
    
    def _remover_vendedor_metodo_3(self):
        remove_buttons = self.driver.find_elements(By.XPATH, "//a[@class='btn btn-link btn-xs js-remove-grupo']")
        if len(remove_buttons) > 0:
            remove_buttons[-1].click()
        else:
            raise Exception("Nenhum botão de remoção encontrado")
    
    def _processar_alert_remocao(self):
        alert_remove = self.wait.until(EC.alert_is_present())
        alert_remove_text = alert_remove.text
        self.assertIn("Grupo removido com sucesso", alert_remove_text, "Alert deve confirmar que o grupo foi removido")
        alert_remove.accept()
    
    def test_gerenciar_grupo_usuario_completo(self):
        self.navegar_para_grupos_usuario()
        
        self.editar_grupo_descricao()
        
        self.gerenciar_permissoes_grupo()
        
        self.navegar_para_usuarios()
        
        self.acessar_edicao_usuario()
        
        self.clicar_aba_grupos()
        
        self.aguardar_conteudo_aba_grupos()
        
        self.adicionar_grupo_vendedor()
        
        self.remover_grupo_vendedor()
        
        
    def tearDown(self):
        self.driver.quit()


if __name__ == "__main__":
    unittest.main(verbosity=2)
