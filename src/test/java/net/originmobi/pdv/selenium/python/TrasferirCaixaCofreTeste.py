import unittest
import time
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.ui import Select


class TrasferirCaixaCofreTest(unittest.TestCase):
    
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
    
    def navegar_para_caixa_cofre(self):
        caixa_menu = self.wait.until(EC.element_to_be_clickable((
            By.XPATH, "//img[@alt='Caixa']"
        )))
        caixa_menu.click()
        time.sleep(2)
    
    def abrir_caixa(self):
        abrir_novo_button = self.wait.until(EC.element_to_be_clickable((
            By.XPATH, "//a[@href='/caixa/form' and contains(@class, 'btn-azul-padrao')]"
        )))
        abrir_novo_button.click()
        
        descricao_input = self.wait.until(EC.presence_of_element_located((By.ID, "descricao")))
        descricao_input.clear()
        descricao_input.send_keys("teste caixa 1")
        
        tipo_select = Select(self.driver.find_element(By.ID, "caixatipo"))
        tipo_select.select_by_value("CAIXA")
        
        valor_input = self.driver.find_element(By.ID, "valorAbertura")
        valor_input.clear()
        valor_input.send_keys("1000000")
        
        abrir_button = self.wait.until(EC.element_to_be_clickable((
            By.XPATH, "//a[@href='/caixa' and contains(@class, 'btn-abrir-caixa')]"
        )))
        abrir_button.click()
        
        time.sleep(2)
        
        caixa_menu_volta = self.wait.until(EC.element_to_be_clickable((
            By.XPATH, "//img[@alt='Caixa']"
        )))
        caixa_menu_volta.click()
        
        time.sleep(2)
    
    def abrir_cofre(self):
        abrir_novo_button = self.wait.until(EC.element_to_be_clickable((
            By.XPATH, "//a[@href='/caixa/form' and contains(@class, 'btn-azul-padrao')]"
        )))
        abrir_novo_button.click()
        
        descricao_input = self.wait.until(EC.presence_of_element_located((By.ID, "descricao")))
        descricao_input.clear()
        descricao_input.send_keys("teste cofre 1")
        
        tipo_select = Select(self.driver.find_element(By.ID, "caixatipo"))
        tipo_select.select_by_value("COFRE")
        
        valor_input = self.driver.find_element(By.ID, "valorAbertura")
        valor_input.clear()
        valor_input.send_keys("1000000")
        
        abrir_button = self.wait.until(EC.element_to_be_clickable((
            By.XPATH, "//a[@href='/caixa' and contains(@class, 'btn-abrir-caixa')]"
        )))
        abrir_button.click()
        
        time.sleep(2)
        
        caixa_menu_volta = self.wait.until(EC.element_to_be_clickable((
            By.XPATH, "//img[@alt='Caixa']"
        )))
        caixa_menu_volta.click()
        
        time.sleep(2)
    
    def realizar_transferencia_caixa_cofre(self):
        caixa_link = self.wait.until(EC.element_to_be_clickable((
            By.XPATH, "//a[contains(@href, '/caixa/gerenciar/')]//img[@src='/icons/glyphicons-459-money.png']"
        )))
        caixa_link.click()
        
        transferencia_button = self.wait.until(EC.element_to_be_clickable((
            By.ID, "btnTransferencia"
        )))
        transferencia_button.click()
        
        valor_input = self.wait.until(EC.element_to_be_clickable((By.ID, "vltotal")))
        time.sleep(1)
        valor_input.clear()
        valor_input.send_keys("100000")
        
        destino_select = Select(self.driver.find_element(By.ID, "iddestino"))
        
        try:
            for option in destino_select.options:
                if "cofre" in option.text.lower():
                    destino_select.select_by_visible_text(option.text)
                    break
        except:
            try:
                destino_select.select_by_index(1)
            except:
                destino_select.select_by_value("23")
        
        observacao_textarea = self.driver.find_element(By.ID, "idobservacao")
        observacao_textarea.clear()
        observacao_textarea.send_keys("teste de transferencia")
        
        confirmar_button = self.wait.until(EC.element_to_be_clickable((
            By.XPATH, "//a[@href='/transferencia' and contains(@class, 'btn-transferencia')]"
        )))
        confirmar_button.click()
        
        alert = self.wait.until(EC.alert_is_present())
        alert_text = alert.text
        self.assertIn("Transferência realizada com sucesso", alert_text, "Alert deve confirmar sucesso da transferência")
        alert.accept()
    
    def verificar_saldo_caixa(self):
        caixa_row = self.wait.until(EC.presence_of_element_located((
            By.XPATH, "//tr[td[contains(text(), 'teste caixa 1')]]"
        )))
        
        valor_cell = caixa_row.find_element(By.XPATH, "./td[3]")
        saldo_texto = valor_cell.text
        
        self.assertIsNotNone(saldo_texto, "Saldo do caixa deve estar visível")
        self.assertIn("R$", saldo_texto, "Valor deve estar formatado em Real")
        
        return saldo_texto
    
    def verificar_saldo_cofre(self):
        cofre_row = self.wait.until(EC.presence_of_element_located((
            By.XPATH, "//tr[td[contains(text(), 'teste cofre 1')]]"
        )))
        
        valor_cell = cofre_row.find_element(By.XPATH, "./td[3]")
        saldo_texto = valor_cell.text
        
        self.assertIsNotNone(saldo_texto, "Saldo do cofre deve estar visível")
        self.assertIn("R$", saldo_texto, "Valor deve estar formatado em Real")
        
        return saldo_texto
    
    def converter_valor_monetario(self, valor_texto):
        valor_limpo = valor_texto.replace('R$', '').replace(' ', '').replace('.', '').replace(',', '.')
        return float(valor_limpo)
    
    def verificar_saldos_apos_transferencia(self):
        try:
            caixa_menu = self.wait.until(EC.element_to_be_clickable((
                By.XPATH, "//img[@alt='Caixa']"
            )))
            caixa_menu.click()
            time.sleep(2)
        except:
            pass
        
        saldo_caixa_texto = self.verificar_saldo_caixa()
        saldo_cofre_texto = self.verificar_saldo_cofre()
        
        saldo_caixa_atual = self.converter_valor_monetario(saldo_caixa_texto)
        saldo_cofre_atual = self.converter_valor_monetario(saldo_cofre_texto)
        
        valor_inicial = 10000.00
        valor_transferido = 1000.00
        
        saldo_caixa_esperado = valor_inicial - valor_transferido
        saldo_cofre_esperado = valor_inicial + valor_transferido
        
        self.assertAlmostEqual(saldo_caixa_atual, saldo_caixa_esperado, places=2, 
                              msg=f"Saldo do caixa deveria ser R$ {saldo_caixa_esperado:.2f}, mas é R$ {saldo_caixa_atual:.2f}")
        self.assertAlmostEqual(saldo_cofre_atual, saldo_cofre_esperado, places=2, 
                              msg=f"Saldo do cofre deveria ser R$ {saldo_cofre_esperado:.2f}, mas é R$ {saldo_cofre_atual:.2f}")
    
    def fechar_caixa(self):
        caixa_menu = self.wait.until(EC.element_to_be_clickable((
            By.XPATH, "//img[@alt='Caixa']"
        )))
        caixa_menu.click()
        time.sleep(2)
        
        caixa_link = self.wait.until(EC.element_to_be_clickable((
            By.XPATH, "//tr[td[contains(text(), 'teste caixa 1')]]//a[contains(@href, '/caixa/gerenciar/')]"
        )))
        caixa_link.click()
        
        fechar_button = self.wait.until(EC.element_to_be_clickable((
            By.XPATH, "//button[@class='btn btn-azul-menu' and @data-toggle='modal' and @data-target='.modalFechaCaixa' and @id='btnfechacaixa']"
        )))
        fechar_button.click()
        
        senha_input = self.wait.until(EC.element_to_be_clickable((By.ID, "admsenha")))
        senha_input.clear()
        senha_input.send_keys("123")
        
        fechar_modal_button = self.wait.until(EC.element_to_be_clickable((
            By.XPATH, "//a[@class='btn btn-success btn-fechar-caixa' and @href='/caixa/fechar']"
        )))
        fechar_modal_button.click()
        
        alert = self.wait.until(EC.alert_is_present())
        alert_text = alert.text
        self.assertIn("Caixa fechado com sucesso", alert_text, "Alert deve confirmar que o caixa foi fechado")
        alert.accept()
    
    def fechar_cofre(self):
        caixa_menu = self.wait.until(EC.element_to_be_clickable((
            By.XPATH, "//img[@alt='Caixa']"
        )))
        caixa_menu.click()
        time.sleep(2)
        
        cofre_link = self.wait.until(EC.element_to_be_clickable((
            By.XPATH, "//tr[td[contains(text(), 'teste cofre 1')]]//a[contains(@href, '/caixa/gerenciar/')]"
        )))
        cofre_link.click()
        
        fechar_button = self.wait.until(EC.element_to_be_clickable((
            By.XPATH, "//button[@class='btn btn-azul-menu' and @data-toggle='modal' and @data-target='.modalFechaCaixa' and @id='btnfechacaixa']"
        )))
        fechar_button.click()
        
        senha_input = self.wait.until(EC.element_to_be_clickable((By.ID, "admsenha")))
        senha_input.clear()
        senha_input.send_keys("123")
        
        fechar_modal_button = self.wait.until(EC.element_to_be_clickable((
            By.XPATH, "//a[@class='btn btn-success btn-fechar-caixa' and @href='/caixa/fechar']"
        )))
        fechar_modal_button.click()
        
        alert = self.wait.until(EC.alert_is_present())
        alert_text = alert.text
        self.assertIn("Caixa fechado com sucesso", alert_text, "Alert deve confirmar que o cofre foi fechado")
        alert.accept()
    
    def test_transferir_caixa_cofre_completo(self):
        self.navegar_para_caixa_cofre()
        self.abrir_caixa()
        self.abrir_cofre()
        
        saldo_caixa_inicial = self.verificar_saldo_caixa()
        saldo_cofre_inicial = self.verificar_saldo_cofre()
        
        self.realizar_transferencia_caixa_cofre()
        self.verificar_saldos_apos_transferencia()
        self.fechar_caixa()
        self.fechar_cofre()
        
    def tearDown(self):
        self.driver.quit()


if __name__ == "__main__":
    unittest.main(verbosity=2)
