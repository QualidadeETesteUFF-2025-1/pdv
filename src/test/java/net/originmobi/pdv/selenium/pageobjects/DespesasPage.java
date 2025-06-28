package net.originmobi.pdv.selenium.pageobjects;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

public class DespesasPage {
    WebDriver driver;
    private static final String VALOR = "10,00";
    private static final String OBS = "Despesa de teste automatizado";

    public DespesasPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    @FindBy(xpath = "//a[img[@alt='Pagar']]")
    private WebElement despesasMenu;

    public void clickDespesasMenu() {
        WebDriverWait wait = new WebDriverWait(driver, 5);
        wait.until(ExpectedConditions.elementToBeClickable(despesasMenu));
        despesasMenu.click();
    }

    @FindBy(css = "a.btnAbreModal")
    private WebElement novoDespesaButton;
    
    @FindBy(id = "codFornecedor")
    private WebElement fornecedorSelect;

    @FindBy(id = "vltotalDespesa")
    private WebElement valorTotalDespesaInput;

    @FindBy(id = "dataVencimento")
    private WebElement dataVencimentoInput;

    @FindBy(id = "obs")
    private WebElement observacaoTextarea;

    @FindBy(css = "a.btn-despesa")
    private WebElement lancarButton;

    public void createDespesa() {
        novoDespesaButton.click();

        Select select = new Select(fornecedorSelect);
        String selectedText = select.getFirstSelectedOption().getText();
        assertTrue(selectedText.equals("Fornecedor Padrão"));

        valorTotalDespesaInput.sendKeys(VALOR);
        LocalDate dueDate = LocalDate.now().plusMonths(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        dataVencimentoInput.sendKeys(dueDate.format(formatter));
        observacaoTextarea.sendKeys(OBS);
        lancarButton.click();
        WebDriverWait wait = new WebDriverWait(driver, 2);
        Alert alert = wait.until(ExpectedConditions.alertIsPresent());
        alert.accept();
    }

    public boolean verificarLinhaDespesa() {
        WebDriverWait wait = new WebDriverWait(driver, 5);
        WebElement linha = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("table.tab-despesas tbody tr.success")));

        List<WebElement> colunas = linha.findElements(By.tagName("td"));
        String observacao = colunas.get(2).getText();
        String valorTotal = colunas.get(3).getText();
        String vencimento = colunas.get(6).getText();

        if (!observacao.equals(OBS)) return false;
        if (!valorTotal.equals("R$ " + VALOR)) return false;

        LocalDate dueDate = LocalDate.now().plusMonths(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        if (!vencimento.equals(dueDate.format(formatter))) return false;

        return true;
    }

    @FindBy(css = "a.btn-modal-paga")
    private WebElement pagarButton;

    @FindBy(css = "a.btn-pag-despesa")
    private WebElement pagarDespesaButton;

    public void pagarDespesa() {
        pagarButton.click();

        WebDriverWait wait = new WebDriverWait(driver, 5);
        WebElement caixaSelectElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("caixa")));

        Select select = new Select(caixaSelectElement);
        String selectedText = select.getFirstSelectedOption().getText();
        assertTrue(selectedText.equals(CaixaPage.CAIXA_NAME), "Caixa selecionado deve ser o " + CaixaPage.CAIXA_NAME);

        pagarDespesaButton.click();

        Alert alert = wait.until(ExpectedConditions.alertIsPresent());
        alert.accept();
    }
}
