package core;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.ios.options.XCUITestOptions;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.time.Duration;
import java.util.Properties;

public class DriverManager {

    private static Properties pro;

    static {
        try {
            // Works both locally and inside the Jenkins workspace
            String projectRoot = System.getProperty("user.dir");
            File proFile = new File(projectRoot + "/src/test/java/resources/config.properites");
            FileInputStream inputStream = new FileInputStream(proFile);
            pro = new Properties();
            pro.load(inputStream);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load config.properties: " + e.getMessage());
        }
    }

    /** Resolve a path that may be relative (to project root) or already absolute. */
    private static String resolveAppPath(String path) {
        File f = new File(path);
        if (f.isAbsolute()) return path;
        return System.getProperty("user.dir") + File.separator + path;
    }

    public static AppiumDriver initializeDriver(String platform) throws Exception {

        if (platform.equalsIgnoreCase("android")) {
            UiAutomator2Options options = new UiAutomator2Options();
            options.setPlatformName("Android");
            options.setAutomationName("UiAutomator2");
            options.setDeviceName(pro.getProperty("androidDeviceName"));
            options.setApp(resolveAppPath(pro.getProperty("androidApp")));

            return new AndroidDriver(
                    new URL(pro.getProperty("appiumUrl")),
                    options
            );

        } else if (platform.equalsIgnoreCase("ios")) {
            XCUITestOptions options = new XCUITestOptions();
            options.setPlatformName("iOS");
            options.setAutomationName("XCUITest");
            options.setDeviceName(pro.getProperty("iosDeviceName"));   // e.g. "iPhone 16 Pro"
            options.setUdid(pro.getProperty("iosUdid"));               // simulator UDID
            options.setApp(resolveAppPath(pro.getProperty("iosApp")));
            options.setNewCommandTimeout(Duration.ofSeconds(3600));
            // Always start from a clean app state — wipes app data on every session.
            // Ensures the Login screen is always the first screen regardless of previous runs.
            options.setFullReset(true);

            return new IOSDriver(
                    new URL(pro.getProperty("appiumUrl")),
                    options
            );

        } else {
            throw new IllegalArgumentException("Platform not supported: " + platform);
        }
    }
}
