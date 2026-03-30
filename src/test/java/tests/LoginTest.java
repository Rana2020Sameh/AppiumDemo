package tests;

import org.testng.annotations.Test;
import pages.LoginPage;

public class LoginTest extends BaseTests {

    @Test
    public void testSuccessfulLogin() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login("test@test.com", "test@123");
    }
}
