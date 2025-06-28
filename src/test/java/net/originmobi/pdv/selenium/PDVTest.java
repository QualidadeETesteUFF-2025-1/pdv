package net.originmobi.pdv.selenium;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;

import net.originmobi.pdv.selenium.pageobjects.CaixaPage;
import net.originmobi.pdv.selenium.pageobjects.DespesasPage;
import net.originmobi.pdv.selenium.pageobjects.LoginPage;

import io.github.bonigarcia.wdm.WebDriverManager;

public class PDVTest {
	
	protected WebDriver driver;
	
	@BeforeAll
	public static void configuraDriver() {
		WebDriverManager.chromedriver().setup();
	}
	
    @BeforeEach
    public void createDriver() {      
		System.out.println("Inicializando driver...");
    	driver = WebDriverManager.chromedriver().create();

        // Logging in
        LoginPage loginPage = new LoginPage(driver);
        loginPage.goTo();
        loginPage.Login();
        assertTrue(loginPage.isLoggedIn(), "Login should succeed before running the test");
    }

	@Test
    public void GerenciarPagamentoDeDespesas() throws InterruptedException {
        CaixaPage caixaPage = new CaixaPage(driver);
        caixaPage.clickCaixaMenu();
        caixaPage.createTesteCaixa();

        DespesasPage despesasPage = new DespesasPage(driver);
        despesasPage.clickDespesasMenu();
        despesasPage.createDespesa();
        assertTrue(despesasPage.verificarLinhaDespesa(), "A linha da despesa está correta");
        despesasPage.pagarDespesa();
    }

    
    @AfterEach
    public void quitDriver() {
       driver.quit();
    }	
}
