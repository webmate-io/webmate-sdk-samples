package examples;

import com.google.common.collect.ImmutableMap;
import com.testfabrik.webmate.javasdk.*;
import com.testfabrik.webmate.javasdk.devices.*;
import com.testfabrik.webmate.javasdk.packagemgmt.Package;
import examples.helpers.Helpers;
import examples.helpers.WebElementFunction;
import io.appium.java_client.MobileBy;
import io.appium.java_client.android.AndroidDriver;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import static com.google.common.io.Resources.toByteArray;
import static examples.MyCredentials.*;

/**
 * NOTE: it is crucial to first run deployAndroidDeviceAndInstallApp() and then perform test.
 * deployAndroidDeviceAndInstallApp() will deploy a device, upload and install the sample.apk app, which is then used by
 * performTest.
 */


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AppiumTestWithUpload extends Commons {
    private WebmateAPISession webmateSession;

    @Before
    public void setup() throws URISyntaxException {
        WebmateAuthInfo authInfo = new WebmateAuthInfo(MyCredentials.WEBMATE_USERNAME, MyCredentials.WEBMATE_APIKEY);
        webmateSession = new WebmateAPISession(
                authInfo,
                WebmateEnvironment.create(new URI(WEBMATE_API_URI)),
                WEBMATE_PROJECTID
        );
    }


    @Test
    public void deployAndroidDeviceAndInstallApp() throws IOException, InterruptedException {
        // request Android device
        DeviceDTO device = webmateSession.device.requestDeviceByRequirements(WEBMATE_PROJECTID,
                new DeviceRequest("Sample Device",
                        new DeviceRequirements(
                                ImmutableMap.of(
                                        DevicePropertyName.Model,  "Galaxy A52 5G",
                                        DevicePropertyName.AutomationAvailable, true))));
        // Wait until the device is ready. We are working on this issue to remove the sleep in the future.
        // Check if device has been deployed
        Thread.sleep(6000);

        byte[] apkData = toByteArray(this.getClass().getResource("sample.apk"));

        // Upload apk to webmate
        Package pkgInfo = webmateSession.packages.uploadApplicationPackage(WEBMATE_PROJECTID, apkData,
                "Material example app", "apk");
        // Wait until the package has been processed and is available. We are working on this issue to remove the sleep
        // in the future.
        Thread.sleep(3000);

        webmateSession.device.installAppOnDevice(device.getId(), pkgInfo.getId());
    }


    /**
     * Run deployAndroidDeviceAndInstallApp() before running this test!
     * @throws MalformedURLException
     * @throws URISyntaxException
     * @throws InterruptedException
     */
    @Test
    public void performTest() throws MalformedURLException, URISyntaxException, InterruptedException {
        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setCapability("browserName", "Appium");
        caps.setCapability("version", "1.21.0");
        caps.setCapability("platform", "Android_11");
        caps.setCapability("model", "Galaxy A52 5G");

        caps.setCapability("email", MyCredentials.WEBMATE_USERNAME);
        caps.setCapability("apikey", MyCredentials.WEBMATE_APIKEY);
        caps.setCapability("project", MyCredentials.WEBMATE_PROJECTID.toString());

        caps.setCapability("appPackage", "com.afollestad.materialdialogssample");
        caps.setCapability("appActivity", "com.afollestad.materialdialogssample.MainActivity");
        caps.setCapability("wm:video", true);
        caps.setCapability("wm:name", "Demo Appium Test");
        caps.setCapability("wm:tags", "Sprint=22, Feature=DemoApp");

        WebmateAuthInfo authInfo = new WebmateAuthInfo(MyCredentials.WEBMATE_USERNAME, MyCredentials.WEBMATE_APIKEY);
        WebmateAPISession webmateSession = new WebmateAPISession(authInfo, WebmateEnvironment.create(new URI(WEBMATE_API_URI)), WEBMATE_PROJECTID);

        Thread.sleep(3000);

        AndroidDriver driver = new AndroidDriver(new URL(MyCredentials.WEBMATE_SELENIUM_URL), caps);
        // Wait until the device is ready. We are working on this issue to remove the sleep in the future.
        // Check if device has been deployed
        Thread.sleep(3000);

        waitForElement(driver, "com.afollestad.materialdialogssample:id/basic_buttons")
                .click();

        driver.findElement(MobileBy.AndroidUIAutomator("new UiSelector().textContains(\"" + "AGREE" + "\")"))
                .click();

        //WebElementFunction elem = () -> driver.findElement(MobileBy.AndroidUIAutomator("new UiSelector().textContains(\"LIST + TITLE + CHECKBOX PROMPT + BUTTONS\")"));
        WebElementFunction elem = () -> driver.findElement(MobileBy.AndroidUIAutomator("new UiScrollable(new UiSelector().scrollable(true))" +
                ".scrollIntoView(new UiSelector().textContains(\"LIST + TITLE + CHECKBOX PROMPT + BUTTONS\"))"));
        System.out.println(elem.getElement());
        Helpers.scrollDownUntilElementIsInView(driver, elem);
        elem.getElement().click();

        WebElement checkbox = driver.findElementByAndroidUIAutomator("new UiSelector().text(\"I understand what this means\")");
        checkbox.click();

        WebElement btn = driver.findElementByAndroidUIAutomator("new UiSelector().text(\"AGREE\")");
        btn.click();
        driver.quit();
    }

}
