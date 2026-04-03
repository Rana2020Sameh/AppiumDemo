package tests;

import core.DriverManager;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class BaseTests {
    protected AppiumDriver driver;

    @BeforeClass
    public void setUp() throws Exception {
        driver = DriverManager.initializeDriver("ios");
    }

/*    @AfterClass
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }*/


    @AfterMethod
    public void takeScreenshotOnFailure(ITestResult result) {
        if (result.getStatus() == ITestResult.FAILURE) {
            TakesScreenshot ts = (TakesScreenshot) driver;
            File src = ts.getScreenshotAs(OutputType.FILE);
            File dest = new File("screenshots/" + result.getName() + ".png");
            dest.getParentFile().mkdirs();
            try {
                Files.copy(src.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException("Failed to save screenshot: " + dest.getPath(), e);
            }
        }

    }

}
