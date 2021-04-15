package examples;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import com.testfabrik.webmate.javasdk.*;
import com.testfabrik.webmate.javasdk.devices.DeviceDTO;
import com.testfabrik.webmate.javasdk.devices.DevicePropertyName;
import com.testfabrik.webmate.javasdk.devices.DeviceRequest;
import com.testfabrik.webmate.javasdk.devices.DeviceRequirements;
import com.testfabrik.webmate.javasdk.packagemgmt.Package;
import examples.helpers.Helpers;
import examples.helpers.WebElementFunction;
import io.appium.java_client.MobileBy;
import io.appium.java_client.android.AndroidDriver;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import static com.google.common.io.Resources.toByteArray;
import static examples.MyCredentials.MY_WEBMATE_PROJECTID;

public class AppiumTestWithUpload extends Commons {
    private WebmateAPISession webmateSession;

    @Before
    public void setup() {
        WebmateAuthInfo authInfo = new WebmateAuthInfo(MyCredentials.MY_WEBMATE_USERNAME, MyCredentials.MY_WEBMATE_APIKEY);
        webmateSession = new WebmateAPISession(
                authInfo,
                WebmateEnvironment.create(),
                MY_WEBMATE_PROJECTID
        );
    }

    @Test
    public void deployAndroidDeviceAndInstallApp() throws IOException {
        // request Pixel 3a device
        DeviceDTO device = webmateSession.device.requestDeviceByRequirements(MY_WEBMATE_PROJECTID,
                new DeviceRequest("Sample Device",
                        new DeviceRequirements(
                                ImmutableMap.of(
                                        DevicePropertyName.Model,  "Pixel 3a",
                                        DevicePropertyName.AutomationAvailable, true))));

        byte[] apkData = toByteArray(this.getClass().getResource("sample.apk"));

        // Upload apk to webmate
        Package pkgInfo = webmateSession.packages.uploadApplicationPackage(MY_WEBMATE_PROJECTID, apkData,
                "Material example app", "apk");

        webmateSession.device.installAppOnDevice(device.getId(), pkgInfo.getId());
    }


    @Test
    public void performTest() throws MalformedURLException {
        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setCapability("browserName", "APPIUM");
        caps.setCapability("version", "1.17.1");
        caps.setCapability("platform", "Android_10");
        caps.setCapability("model", "Pixel 3a");

        caps.setCapability("email", MyCredentials.MY_WEBMATE_USERNAME);
        caps.setCapability("apikey", MyCredentials.MY_WEBMATE_APIKEY);
        caps.setCapability("project", MyCredentials.MY_WEBMATE_PROJECTID.toString());

        caps.setCapability("appPackage", "com.afollestad.materialdialogssample");
        caps.setCapability("appActivity", "com.afollestad.materialdialogssample.MainActivity");
        caps.setCapability("wm:video", true);
        caps.setCapability("wm:name", "Demo Appium Test");
        caps.setCapability("wm:tags", "Sprint=22, Feature=DemoApp");

        AndroidDriver driver = new AndroidDriver(new URL(MyCredentials.WEBMATE_SELENIUM_URL), caps);

        WebmateAuthInfo authInfo = new WebmateAuthInfo(MyCredentials.MY_WEBMATE_USERNAME, MyCredentials.MY_WEBMATE_APIKEY);
        WebmateAPISession webmateSession = new WebmateAPISession(authInfo, WebmateEnvironment.create(), MY_WEBMATE_PROJECTID);

        waitForElement(driver, "com.afollestad.materialdialogssample:id/basic_checkbox_titled_buttons")
                .click();

        waitForElement(driver, "com.afollestad.materialdialogssample:id/md_checkbox_prompt")
                .click();

        driver.findElement(MobileBy.AndroidUIAutomator("new UiSelector().textContains(\"" + "AGREE" + "\")"))
                .click();

        WebElementFunction elem = () -> driver.findElementByAndroidUIAutomator("new UiSelector().text(\"LIST + TITLE + CHECKBOX PROMPT + BUTTONS\")");
        Helpers.scrollDownUntilElementIsInView(driver, elem);
        elem.getElement().click();

        WebElement checkbox = driver.findElementByAndroidUIAutomator("new UiSelector().text(\"I understand what this means\")");
        checkbox.click();

        WebElement btn = driver.findElementByAndroidUIAutomator("new UiSelector().text(\"AGREE\")");
        btn.click();
        driver.quit();
    }

}
