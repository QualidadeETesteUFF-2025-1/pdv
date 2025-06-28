package net.originmobi.pdv.selenium.pageobjects;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class CaixaPage {
    WebDriver driver;
    public static final String CAIXA_NAME = "Teste";
    private static final String CAIXA_VALOR = "100.00";

    public CaixaPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
        driver.manage().window().maximize();
    }

    @FindBy(xpath = "//a[img[@alt='Caixa']]")
    private WebElement caixaMenu;

    public void clickCaixaMenu() {
        caixaMenu.click();
    }

    @FindBy(css = "a[href='/caixa/form']")
    private WebElement abrirNovoButton;

    @FindBy(id = "descricao")
    private WebElement descricaoInput;

    @FindBy(id = "valorAbertura")
    private WebElement valorAberturaInput;

    @FindBy(css = "a.btn-abrir-caixa")
    private WebElement submitCaixaButton;

    public void createTesteCaixa() {
        abrirNovoButton.click();
        descricaoInput.sendKeys(CAIXA_NAME);
        valorAberturaInput.sendKeys(CAIXA_VALOR);
        submitCaixaButton.click();
    }
}
