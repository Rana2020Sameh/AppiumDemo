package tests;

import org.testng.annotations.Test;
import pages.LoginPage;
import pages.SignupPage;

public class SignupTests extends BaseTests {

    @Test
    public void signUp() {
        // Step 1: Navigate from Login screen to Signup screen
        LoginPage loginPage = new LoginPage(driver);
        loginPage.tapSignup();

        // Step 2: Create fresh SignupPage AFTER navigation so PageFactory
        //         initializes elements on the Signup screen
        SignupPage signupPage = new SignupPage(driver);
        signupPage.register("rana", "r.h@gmail.com", "test@123");
    }
}
