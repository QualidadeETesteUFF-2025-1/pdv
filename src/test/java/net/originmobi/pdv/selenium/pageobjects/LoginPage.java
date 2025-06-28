package net.originmobi.pdv.selenium.pageobjects;

import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;

public class LoginPage {
    WebDriver driver;
    private static final String USERNAME = "gerente";
    private static final String PASSWORD = "123";

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    public void goTo() {
        driver.get("http://localhost:8080/");
    }

    @FindBy(id = "user")
    private WebElement usernameInput;
    @FindBy(id = "password")
    private WebElement passwordInput;
    @FindBy(id = "btn-login")
    private WebElement loginButton;

    public void Login() {
        usernameInput.sendKeys(USERNAME);
        passwordInput.sendKeys(PASSWORD);
        loginButton.click();
    }

    @FindBy(xpath = "//form[contains(@action, '/logout')]")
    private WebElement logoutForm;

    public boolean isLoggedIn() {
        return logoutForm.getText().contains("Usuário: " + USERNAME);
    }
}
