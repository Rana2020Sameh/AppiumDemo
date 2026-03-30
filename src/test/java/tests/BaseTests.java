package tests;

import core.DriverManager;
import io.appium.java_client.AppiumDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

public class BaseTests {
    protected AppiumDriver driver;

    @BeforeClass
    public void setUp() throws Exception {
        driver = DriverManager.initializeDriver("android");
    }

//    @AfterClass
//    public void tearDown() {
//        if (driver != null) {
//            driver.quit();
//        }
//    }
}
