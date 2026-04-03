package tests;

import org.testng.annotations.Test;
import pages.LoginPage;
import utils.LoginTestData;
import utils.TestDataReader;

public class LoginTest extends BaseTests {

    private static final LoginTestData data =
            TestDataReader.load("loginTestData", LoginTestData.class);

    @Test
    public void testSuccessfulLogin() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login(
                data.credentials.username,
                data.credentials.password
        );
    }
}
