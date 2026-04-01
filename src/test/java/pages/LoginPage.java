package pages;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

public class LoginPage extends BasePage {

    @AndroidFindBy(xpath = "//android.widget.TextView[@text='Signup']")
    @iOSXCUITFindBy(accessibility = "Signup")
    private WebElement signupButton;

    @AndroidFindBy(xpath = "//android.widget.EditText[@hint='Enter your Email']")
    @iOSXCUITFindBy(accessibility = "Enter your Email")
    private WebElement usernameField;

    @AndroidFindBy(xpath = "//android.widget.EditText[@hint='Enter your Password']")
    @iOSXCUITFindBy(accessibility = "Enter your Password")
    private WebElement passwordField;

    @AndroidFindBy(xpath = "//android.widget.TextView[@text='Login']")
    @iOSXCUITFindBy(accessibility = "Login")
    private WebElement loginButton;

    public LoginPage(AppiumDriver driver) {
        super(driver);
    }

    public void enterUsername(String username) {
        usernameField.click();
        usernameField.clear();
        usernameField.sendKeys(username);
    }

    public void enterPassword(String password) {
        passwordField.click();
        passwordField.clear();
        passwordField.sendKeys(password);
    }

    public void tapSignup() {
        signupButton.click();
        // Wait for Signup screen — use accessibilityId which works on both iOS and Android
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.presenceOfElementLocated(
                        AppiumBy.accessibilityId("Enter your Full name")
                ));
    }

    public void tapLogin() {
        loginButton.click();
    }

    public void login(String username, String password) {
        enterUsername(username);
        enterPassword(password);
        tapLogin();
    }
}
