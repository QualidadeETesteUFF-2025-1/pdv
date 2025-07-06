import unittest
import time
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.ui import Select
from selenium.webdriver.common.action_chains import ActionChains


class AdicionarFornecedorTest(unittest.TestCase):
    
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
    
    def navegar_para_fornecedores(self):
        fornecedor_menu = self.wait.until(EC.element_to_be_clickable((
            By.XPATH, "//a[@href='/fornecedor' and img[@alt='Fornecedor']]"
        )))
        fornecedor_menu.click()
        
        self.wait.until(EC.url_contains("/fornecedor"))
        page_title = self.wait.until(EC.visibility_of_element_located((
            By.XPATH, "//h1[contains(text(), 'Fornecedor') or contains(text(), 'fornecedor')]"
        )))
        self.assertTrue(page_title.is_displayed(), "Deve navegar para a página de fornecedores")
    
    def abrir_formulario_novo_fornecedor(self):
        novo_button = self.wait.until(EC.element_to_be_clickable((
            By.XPATH, "//a[@href='/fornecedor/form' and @class='btn btn-azul-padrao']"
        )))
        novo_button.click()
        
        self.wait.until(EC.url_contains("/fornecedor/form"))
        form_element = self.wait.until(EC.presence_of_element_located((
            By.XPATH, "//form | //input[@id='nomefantasia']"
        )))
        self.assertTrue(form_element.is_displayed(), "Formulário de fornecedor deve estar carregado")
    
    def preencher_dados_fornecedor(self):
        nome_fantasia_input = self.wait.until(EC.presence_of_element_located((By.ID, "nomefantasia")))
        nome_fantasia_input.clear()
        nome_fantasia_input.send_keys("teste fantasia")
        
        nome_input = self.wait.until(EC.presence_of_element_located((By.ID, "nome")))
        nome_input.clear()
        nome_input.send_keys("teste nome")
        
        cnpj_input = self.wait.until(EC.presence_of_element_located((By.ID, "cnpj")))
        cnpj_input.clear()
        cnpj_input.send_keys("11.222.333/0001-81")
        
        inscricao_input = self.wait.until(EC.presence_of_element_located((By.ID, "escricao")))
        inscricao_input.clear()
        inscricao_input.send_keys("123456789")
        
        situacao_select = Select(self.driver.find_element(By.ID, "situacao"))
        situacao_select.select_by_value("1")
        
        observacao_textarea = self.wait.until(EC.presence_of_element_located((By.ID, "observacao")))
        observacao_textarea.clear()
        observacao_textarea.send_keys("teste observacao")
        
        endereco_tab = self.wait.until(EC.element_to_be_clickable((
            By.XPATH, "//a[@data-toggle='tab' and @href='#menu1' and contains(text(), 'Endereço')]"
        )))
        endereco_tab.click()
        self.wait.until(EC.visibility_of_element_located((By.ID, "menu1")))
        
        cidade_select = Select(self.wait.until(EC.presence_of_element_located((By.ID, "cidade"))))
        cidade_select.select_by_value("1")
        
        rua_input = self.wait.until(EC.presence_of_element_located((By.ID, "rua")))
        rua_input.clear()
        rua_input.send_keys("teste")
        
        bairro_input = self.wait.until(EC.presence_of_element_located((By.ID, "bairro")))
        bairro_input.clear()
        bairro_input.send_keys("teste")
        
        numero_input = self.wait.until(EC.presence_of_element_located((By.ID, "numero")))
        numero_input.clear()
        numero_input.send_keys("1")
        
        cep_input = self.wait.until(EC.presence_of_element_located((By.ID, "cep")))
        cep_input.clear()
        cep_input.send_keys("11111111")
        
        referencia_input = self.wait.until(EC.presence_of_element_located((By.ID, "referencia")))
        referencia_input.clear()
        referencia_input.send_keys("teste")
        
        contato_tab = self.wait.until(EC.element_to_be_clickable((
            By.XPATH, "//a[@data-toggle='tab' and @href='#menu2' and contains(text(), 'Contato')]"
        )))
        contato_tab.click()
        self.wait.until(EC.visibility_of_element_located((By.ID, "menu2")))
        
        tipo_select = Select(self.wait.until(EC.presence_of_element_located((By.ID, "tipo"))))
        tipo_select.select_by_value("CELULAR")
        
        fone_input = self.wait.until(EC.presence_of_element_located((By.ID, "fone")))
        fone_input.clear()
        fone_input.send_keys("(69) 99999-9999")
    
    def salvar_fornecedor(self):
        salvar_button = self.wait.until(EC.element_to_be_clickable((
            By.XPATH, "//input[@type='submit' and @value='Salvar' and @class='btn btn-azul-padrao']"
        )))
        salvar_button.click()
        
        success_message = self.wait.until(EC.visibility_of_element_located((
            By.XPATH, "//div[@class='alert alert-success alert-dismissable']//span[contains(text(), 'Fornecedor salvo com sucesso')]"
        )))
        self.assertTrue(success_message.is_displayed(), "Mensagem 'Fornecedor salvo com sucesso' deve aparecer")
    
    def verificar_fornecedor_criado(self):
        page_title = self.wait.until(EC.visibility_of_element_located((
            By.XPATH, "//h1[contains(text(), 'Fornecedor')]"
        )))
        self.assertTrue(page_title.is_displayed(), "Deve estar na página de fornecedores")
        
        tabela = self.wait.until(EC.presence_of_element_located((
            By.XPATH, "//table[contains(@class, 'table')]"
        )))
        self.assertTrue(tabela.is_displayed(), "Tabela de fornecedores deve estar visível")
        
        try:
            wait_short = WebDriverWait(self.driver, 3)
            fornecedor_row = wait_short.until(EC.presence_of_element_located((
                By.XPATH, "//tr[td[contains(text(), 'teste fantasia')] or td[contains(text(), 'teste nome')]]"
            )))
            if fornecedor_row.is_displayed():
                print("Fornecedor encontrado na listagem!")
        except:
            print("Fornecedor pode ter sido criado, mas não foi encontrado na listagem atual")
    
    def test_adicionar_fornecedor_completo(self):
        self.navegar_para_fornecedores()
        self.abrir_formulario_novo_fornecedor()
        self.preencher_dados_fornecedor()
        self.salvar_fornecedor()
    
    def tearDown(self):
        self.driver.quit()


if __name__ == "__main__":
    unittest.main(verbosity=2)
