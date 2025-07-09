import unittest
import time
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.ui import Select
from selenium.webdriver.common.action_chains import ActionChains
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.chrome.service import Service
from webdriver_manager.chrome import ChromeDriverManager

class GerenciarCaixaTest(unittest.TestCase):
    
    def setUp(self):        
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
        time.sleep(1)  
        password_input.send_keys("123")
        time.sleep(1)  
        login_button.click()
        time.sleep(1)  
        
        logout_form = self.wait.until(EC.presence_of_element_located((By.XPATH, "//form[contains(@action, '/logout')]")))
        self.assertIn("Usuário: gerente", logout_form.text, "Login deve ser realizado com sucesso")
    
    def navegar_para_caixa(self):
        caixa_menu = self.wait.until(EC.element_to_be_clickable((
            By.XPATH, "//img[@alt='Caixa']"
        )))
        caixa_menu.click()
        time.sleep(1)  
        time.sleep(2)
        
        page_title = self.wait.until(EC.presence_of_element_located((
            By.XPATH, "//h1[contains(text(), 'Caixa') or contains(text(), 'caixa')]"
        )))
        self.assertTrue(page_title.is_displayed(), "Deve navegar para a página de caixas")
    
    def abrir_novo_caixa(self):
        abrir_novo_button = self.wait.until(EC.element_to_be_clickable((
            By.XPATH, "//a[@href='/caixa/form' and contains(@class, 'btn-azul-padrao')]"
        )))
        abrir_novo_button.click()
        time.sleep(1)  
        
        self.wait.until(EC.url_contains("/caixa/form"))
        form_element = self.wait.until(EC.presence_of_element_located((
            By.XPATH, "//form | //input[@id='descricao']"
        )))
        self.assertTrue(form_element.is_displayed(), "Formulário de caixa deve estar carregado")
        
        
        self._limpar_e_preencher_input((By.ID, "descricao"), "Caixa Teste Selenium")
        
        tipo_select = Select(self.driver.find_element(By.ID, "caixatipo"))
        tipo_select.select_by_value("CAIXA")
        time.sleep(1)  
        
        
        self._limpar_e_preencher_input((By.ID, "valorAbertura"), "5000")
        
        abrir_button = self.wait.until(EC.element_to_be_clickable((
            By.XPATH, "//a[@href='/caixa' and contains(@class, 'btn-abrir-caixa')]"
        )))
        abrir_button.click()
        time.sleep(1)  
        
        time.sleep(2)
        self.wait.until(EC.url_contains("/caixa/gerenciar/"))
        
        gerenciar_title = self.wait.until(EC.presence_of_element_located((
            By.XPATH, "//h2[contains(text(), 'Gerenciar')]"
        )))
        self.assertTrue(gerenciar_title.is_displayed(), "Deve estar na página de gerenciar caixa")
    
    def verificar_saldo_inicial(self):
        saldo_text = self.get_valor_saldo()
        self.assertIn("5.000,00", saldo_text, "Saldo deve estar exibido em formato monetário")

    def get_valor_saldo(self):
        saldo_element_input = self.wait.until(EC.presence_of_element_located((
            By.XPATH, "//input[@id='valorTotal']"
        )))
        saldo_text = saldo_element_input.get_attribute("value")
        return saldo_text
    
    def aguardar_elemento_interagivel(self, locator):
        """Aguarda elemento estar presente, visível e interagível"""
        
        element = self.wait.until(EC.presence_of_element_located(locator))
        
        
        self.wait.until(EC.visibility_of_element_located(locator))
        
        
        self.wait.until(lambda driver: driver.find_element(*locator).is_enabled())
        
        
        return self.driver.find_element(*locator)
    
    def _limpar_e_preencher_input(self, locator, valor):
        """Limpa e preenche input de forma segura"""
        element = self.aguardar_elemento_interagivel(locator)
        
        
        self.wait.until(lambda driver: driver.find_element(*locator).is_displayed())
        
        
        try:
            
            element.clear()
            time.sleep(1)  
        except Exception:
            try:
                
                self.driver.execute_script("arguments[0].value = '';", element)
                time.sleep(1)  
            except Exception:
                try:
                    
                    element.send_keys(Keys.CONTROL + "a")
                    time.sleep(1)  
                    element.send_keys(Keys.DELETE)
                    time.sleep(1)  
                except Exception:
                    
                    ActionChains(self.driver).move_to_element(element).click().perform()
                    time.sleep(1)  
                    self.driver.execute_script("arguments[0].focus(); arguments[0].value = '';", element)
                    time.sleep(1)  
        
        
        element.send_keys(valor)
        time.sleep(1)  
        
        
        valor_atual = element.get_attribute("value")
        if valor_atual != valor:
            self.driver.execute_script("arguments[0].value = arguments[1];", element, valor)
            time.sleep(1)  
        
        return element
    
    def fazer_suprimento(self):
        suprimento_button = self.wait.until(EC.element_to_be_clickable((
            By.XPATH, "//button[@id='btnSuprimento']"
        )))
        suprimento_button.click()
        time.sleep(1)  
        
        
        self._limpar_e_preencher_input((By.XPATH, "//input[@id='idvalor']"), "100000")
        
        
        self._limpar_e_preencher_input((By.XPATH, "//input[@id='idObs']"), "Suprimento teste selenium")
        
        confirmar_button = self.wait.until(EC.element_to_be_clickable((
            By.XPATH, "//a[@href='/caixa/lancamento/suprimento' and contains(@class, 'btn-suprimento-caixa')]"
        )))
        confirmar_button.click()
        time.sleep(1)  
        
        alert = self.wait.until(EC.alert_is_present())
        alert_text = alert.text
        self.assertIn("Lançamento realizado com sucesso", alert_text, "Alert deve confirmar sucesso do suprimento")
        alert.accept()
        time.sleep(1)  
    
    def fazer_sangria(self):
        sangria_button = self.wait.until(EC.element_to_be_clickable((
            By.XPATH, "//button[@id='btnSangria']"
        )))
        sangria_button.click()
        time.sleep(1)  
        
        
        self._limpar_e_preencher_input((By.XPATH, "//input[@id='idvl']"), "50000")
        
        
        self._limpar_e_preencher_input((By.XPATH, "//input[@id='idobs']"), "Sangria teste selenium")
        
        confirmar_button = self.wait.until(EC.element_to_be_clickable((
            By.XPATH, "//a[@href='/caixa/lancamento/sangria' and contains(@class, 'btn-Sangria-Sangria')]"
        )))
        confirmar_button.click()
        time.sleep(1)  
        
        alert = self.wait.until(EC.alert_is_present())
        alert_text = alert.text
        self.assertIn("Lançamento realizado com sucesso", alert_text, "Alert deve confirmar sucesso da sangria")
        alert.accept()
        time.sleep(1)  
    
    def verificar_lancamentos(self):
        lancamentos_table = self.wait.until(EC.presence_of_element_located((
            By.XPATH, "//table[contains(@class, 'table')]"
        )))
        self.assertTrue(lancamentos_table.is_displayed(), "Tabela de lançamentos deve estar visível")
        
        
        try:
            suprimento_row = self.driver.find_element(By.XPATH, "//tr[td[contains(text(), 'Suprimento teste selenium')]]")
            self.assertTrue(suprimento_row.is_displayed(), "Lançamento de suprimento deve estar visível")
        except:
            self.fail("Lançamento de suprimento não encontrado na tabela")
        
        
        try:
            sangria_row = self.driver.find_element(By.XPATH, "//tr[td[contains(text(), 'Sangria teste selenium')]]")
            self.assertTrue(sangria_row.is_displayed(), "Lançamento de sangria deve estar visível")
        except:
            self.fail("Lançamento de sangria não encontrado na tabela")
    
    def verificar_saldo_final(self):
        saldo_text = self.get_valor_saldo()
        
        
        self.assertIn("5.500,00", saldo_text, "Saldo final deve ser R$ 5.500,00")
    
    def fechar_caixa(self):
        fechar_button = self.wait.until(EC.element_to_be_clickable((
            By.XPATH, "//button[@id='btnfechacaixa']"
        )))
        fechar_button.click()
        time.sleep(1)  
        
        
        self._limpar_e_preencher_input((By.ID, "admsenha"), "123")
        
        fechar_modal_button = self.wait.until(EC.element_to_be_clickable((
            By.XPATH, "//a[@class='btn btn-success btn-fechar-caixa' and @href='/caixa/fechar']"
        )))
        fechar_modal_button.click()
        time.sleep(1)  
        
        alert = self.wait.until(EC.alert_is_present())
        alert_text = alert.text
        self.assertIn("Caixa fechado com sucesso", alert_text, "Alert deve confirmar que o caixa foi fechado")
        alert.accept()
        time.sleep(1)  
    
    def verificar_caixa_fechado(self):
        status_element = self.driver.find_element(By.XPATH, "//span[contains(text(), 'FECHADO')]")
        self.assertTrue(status_element.is_displayed(), "Status fechado deve estar visível")
           
    def realizar_operacao_com_caixa_fechado(self):
        suprimento_button = self.wait.until(EC.presence_of_element_located((
            By.XPATH, "//button[@id='btnSuprimento']"
        )))
        suprimento_disabled = suprimento_button.get_attribute("disabled")
        self.assertTrue(suprimento_disabled, "Botão de suprimento deve estar desabilitado para caixa fechado")


        sangria_button = self.wait.until(EC.presence_of_element_located((
            By.XPATH, "//button[@id='btnSangria']"
        )))
        sangria_disabled = sangria_button.get_attribute("disabled")
        self.assertTrue(sangria_disabled, "Botão de sangria deve estar desabilitado para caixa fechado")
            
            
                
    
    
    def test_gerenciar_caixa_completo(self):
        """Teste completo de gerenciamento de caixa"""
        print("1. Navegando para caixa...")
        self.navegar_para_caixa()
        
        print("2. Abrindo novo caixa...")
        self.abrir_novo_caixa()
        
        print("3. Verificando saldo inicial...")
        self.verificar_saldo_inicial()
        
        print("4. Fazendo suprimento...")
        self.fazer_suprimento()
        
        print("5. Fazendo sangria...")
        self.fazer_sangria()
        
        print("6. Verificando lançamentos...")
        self.verificar_lancamentos()
        
        print("7. Verificando saldo final...")
        self.verificar_saldo_final()
        
        print("8. Fechando caixa...")
        self.fechar_caixa()
        
        print("9. Verificando caixa fechado...")
        self.verificar_caixa_fechado()
        
        print("10. Testando operação com caixa fechado...")
        self.realizar_operacao_com_caixa_fechado()
        
        print("Teste de gerenciamento de caixa concluído com sucesso!")
    
    def tearDown(self):
        self.driver.quit()


if __name__ == "__main__":
    unittest.main(verbosity=2) 