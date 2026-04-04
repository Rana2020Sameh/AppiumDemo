package pages;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;
import org.openqa.selenium.WebElement;

import java.util.Date;
import java.util.Map;

public class Purchasepage extends BasePage{
    public Purchasepage(AppiumDriver driver) {
        super(driver);
    }

    @iOSXCUITFindBy(accessibility = "Sauce Labs Backpack")
    private WebElement firstitem;


    @iOSXCUITFindBy(accessibility = "Add To Cart button")
    private WebElement addtocart;
    @iOSXCUITFindBy(accessibility = "tab bar option cart")
    private WebElement cart;
    //kkk

    @iOSXCUITFindBy (accessibility = "Proceed To Checkout button")
    private WebElement proceedtocheckoutbutton;
    @iOSXCUITFindBy(accessibility = "Username input field")
    private WebElement username;
    @iOSXCUITFindBy(accessibility = "Password input field")
    private WebElement password;
    @iOSXCUITFindBy(accessibility = "Login button")
    private WebElement loginButton;
  @iOSXCUITFindBy(accessibility = "Full Name* input field")
  private WebElement  name;
  @iOSXCUITFindBy(accessibility = "Address Line 1* input field")
  private WebElement address;
  @iOSXCUITFindBy(accessibility = "City* input field")
  private WebElement city;
  @iOSXCUITFindBy(accessibility = "Zip Code* input field")
  private WebElement zipCode;
  @iOSXCUITFindBy(accessibility = "Country* input field")
  private WebElement country;
  @iOSXCUITFindBy(accessibility = "To Payment button")
  private WebElement paymentButton;

  @iOSXCUITFindBy(accessibility = "Full Name* input field")
private  WebElement cardFullName;

    @iOSXCUITFindBy(accessibility = "Card Number* input field")
    private  WebElement cardNumber;


    @iOSXCUITFindBy(accessibility = "Expiration Date* input field")
    private  WebElement cardExpirationDate;

    @iOSXCUITFindBy(accessibility = "Security Code* input field")
    private  WebElement cardCSV;

    @iOSXCUITFindBy(accessibility = "checkbox for My billing address is the same as my shipping address.")
    private  WebElement checkMyBill;

    @iOSXCUITFindBy(accessibility = "Review Order button")
    private  WebElement reviewButton;



    public  void additems()
    {
        firstitem.click();
        addtocart.click();
    }

    public  void proceedpurch()
    {
        cart.click();
        proceedtocheckoutbutton.click();
    }
    public void insertcredintails(String userName,String pass)
    {
        username.click();
        username.clear();
        username.sendKeys(userName);
        password.click();
        password.clear();
        password.sendKeys(pass);
        loginButton.click();

    }

    public void checkoutscreen(String NAme,String Address,String City,String ZipCode,String Country)
    {
        name.click();
        name.clear();
        name.sendKeys(NAme);
        address.click();
        address.clear();
        address.sendKeys(Address);
        city.click();
        city.clear();
        city.sendKeys(City);
        zipCode.click();
        zipCode.clear();
        zipCode.sendKeys(ZipCode);
        country.click();
        country.clear();
        country.sendKeys(Country);
        driver.executeScript("mobile: tap", Map.of("x", 75.0, "y", 134.0));
        paymentButton.click();


    }

    public void checkoutToReviewOrder(String cardfullName, String cardnumber, String cardexpirationDate, String cardcSV)
    {
        cardFullName.click();
        cardFullName.clear();
        cardFullName.sendKeys(cardfullName);

        cardNumber.click();
        cardNumber.clear();
        cardNumber.sendKeys(cardnumber);

        // Expiration date field is a masked numeric input — it auto-inserts the "/".
        // Sending "06/30" fails because "/" cannot be typed on the number pad.
        // Strip the slash and send only the 4 digits; the field formats them to MM/YY.
        String expiryDigits = cardexpirationDate.replace("/", "");
        cardExpirationDate.click();
        cardExpirationDate.clear();
        cardExpirationDate.sendKeys(expiryDigits);

        // Security code is capped at 3 digits — truncate in case a longer value is passed.
        String csvValue = cardcSV.length() > 3 ? cardcSV.substring(0, 3) : cardcSV;
        cardCSV.click();
        cardCSV.clear();
        cardCSV.sendKeys(csvValue);

        checkMyBill.click();

        reviewButton.click();
    }

}
