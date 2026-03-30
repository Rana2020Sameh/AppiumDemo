package core;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.ios.options.XCUITestOptions;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.Properties;

public class DriverManager {

    private static Properties pro;

    static {
        try {
            File proFile = new File("/Users/ranasameh/Documents/Study/Appium/AppiumDemo/src/test/java/resources/config.properites");
            FileInputStream inputStream = new FileInputStream(proFile);
            pro = new Properties();
            pro.load(inputStream);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load config.properties: " + e.getMessage());
        }
    }

    public static AppiumDriver initializeDriver(String platform) throws Exception {

        if (platform.equalsIgnoreCase("android")) {
            UiAutomator2Options options = new UiAutomator2Options();
            options.setPlatformName("Android");
            options.setAutomationName("UiAutomator2");
            options.setDeviceName(pro.getProperty("androidDeviceName"));
            options.setApp(pro.getProperty("androidApp"));

            return new AndroidDriver(
                    new URL(pro.getProperty("appiumUrl")),
                    options
            );
        }
        else if (platform.equalsIgnoreCase("ios")) {
            XCUITestOptions options = new XCUITestOptions();
            options.setPlatformName("iOS");
            options.setAutomationName("XCUITest");
            options.setUdid(pro.getProperty("iosUdid"));
            options.setApp(pro.getProperty("iosApp"));
            options.setNewCommandTimeout(java.time.Duration.ofSeconds(3600));

            return new IOSDriver(
                    new URL(pro.getProperty("appiumUrl")),
                    options
            );
        }
        else {
            throw new IllegalArgumentException("Platform not supported: " + platform);
        }
    }
}
