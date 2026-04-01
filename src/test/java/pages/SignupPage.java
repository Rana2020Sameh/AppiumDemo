package pages;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class SignupPage extends BasePage {

    @AndroidFindBy(xpath = "//android.widget.TextView[@text='Signup']")
    @iOSXCUITFindBy(accessibility = "Signup")
    private WebElement signupButton;

    @AndroidFindBy(xpath = "//android.widget.EditText[@hint='Enter your Full name']")
    @iOSXCUITFindBy(accessibility = "Enter your Full name")
    private WebElement fullName;

    @AndroidFindBy(xpath = "//android.widget.EditText[@hint='Enter your Email']")
    @iOSXCUITFindBy(accessibility = "Enter your Email")
    private WebElement emailField;

    @AndroidFindBy(xpath = "//android.widget.EditText[@hint='Enter your Password']")
    @iOSXCUITFindBy(accessibility = "Enter your Password")
    private WebElement passwordField;

    @AndroidFindBy(xpath = "//android.widget.TextView[@text='Login']")
    @iOSXCUITFindBy(accessibility = "Login")
    private WebElement loginLink;

    public SignupPage(AppiumDriver driver) {
        super(driver);
    }

    public void tapSignup() {
        signupButton.click();
    }

    /**
     * Dismisses the keyboard — uses hide-keyboard for Android,
     * and coordinate tap for iOS.
     */
    private void dismissKeyboard() {
        String platform = driver.getCapabilities().getPlatformName().toString();
        if (platform.equalsIgnoreCase("android")) {
            driver.executeScript("mobile: hideKeyboard");
        } else {
            Map<String, Object> args = new HashMap<>();
            args.put("x", 207);
            args.put("y", 150);
            driver.executeScript("mobile: tap", args);
        }
    }

    public void register(String fullNameText, String email, String password) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        // Full Name — accessibilityId works on both iOS (XCUITest) and Android
        wait.until(ExpectedConditions.elementToBeClickable(
                        AppiumBy.accessibilityId("Enter your Full name")))
                .sendKeys(fullNameText);

        // Email
        wait.until(ExpectedConditions.elementToBeClickable(
                        AppiumBy.accessibilityId("Enter your Email")))
                .sendKeys(email);

        // Password
        wait.until(ExpectedConditions.elementToBeClickable(
                        AppiumBy.accessibilityId("Enter your Password")))
                .sendKeys(password);

        // Dismiss keyboard
        dismissKeyboard();

        // Tap Signup button
        wait.until(ExpectedConditions.elementToBeClickable(
                        AppiumBy.accessibilityId("Signup")))
                .click();
    }

    public void tapLogin() {
        loginLink.click();
    }
}
