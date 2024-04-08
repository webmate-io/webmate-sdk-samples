package examples;

import com.testfabrik.webmate.javasdk.*;
import com.testfabrik.webmate.javasdk.browsersession.BrowserSessionRef;

import com.testfabrik.webmate.javasdk.selenium.WebmateSeleniumSession;
import com.testfabrik.webmate.javasdk.testmgmt.TestRunEvaluationStatus;
import examples.helpers.DeviceScheduler;
import examples.helpers.Helpers;
import examples.helpers.WebElementFunction;
import io.appium.java_client.MobileBy;
import io.appium.java_client.android.AndroidDriver;
import org.joda.time.Duration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import static examples.MyCredentials.*;
import static examples.helpers.Helpers.waitForElement;
import static org.junit.Assert.assertEquals;


/**
 * Simple test that shows how to schedule Selenium tests with the webmate Java sdk.
 * <p>
 * __Disclaimer__: This approach will only work if everyone who tests in the corresponding project adheres to the following structure.
 * 1. use scheduleDevice to request and wait for a fresh device that fulfills the given requirements (Browser, BrowserVersion and Platform)
 * 2. use the slotId returned by scheduleDevice in your Selenium test capabilities to ensure that you run your test on the device
 * 3. run your test.
 * 4. make sure you release the device when your test is finished,or if it crashes.
 */
@RunWith(JUnit4.class)
public class SeleniumTestScheduling {

    private WebmateAPISession webmateSession;
    private DeviceScheduler deviceScheduler;

    @Before
    public void setup() throws URISyntaxException {
        WebmateAuthInfo authInfo = new WebmateAuthInfo(MyCredentials.WEBMATE_USERNAME, MyCredentials.WEBMATE_APIKEY);
        webmateSession = new WebmateAPISession(
                authInfo,
                WebmateEnvironment.create(new URI(WEBMATE_API_URI)),
                WEBMATE_PROJECTID);
        deviceScheduler = new DeviceScheduler(webmateSession);
    }

    private DesiredCapabilities getVMCapabilities(Browser browser) {
        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setCapability("browserName", browser.getBrowserType().getValue());
        caps.setCapability("browserVersion", browser.getVersion());
        caps.setCapability("platformName", browser.getPlatform().toString());
        caps.setCapability("wm:apikey", WEBMATE_APIKEY);
        caps.setCapability("wm:project", WEBMATE_PROJECTID.toString());
        return caps;
    }

    @Test
    public void performTestVM() {
        Platform platform = new Platform(PlatformType.WINDOWS, "11", "64");
        Browser browser = new Browser(BrowserType.CHROME, "121", platform);

        // 1-3. Schedule VM and run test as usual
        RemoteWebDriver driver = null;
        WebmateSeleniumSession seleniumSession = null;
        try {
            driver = (RemoteWebDriver) deviceScheduler.scheduleDevice("DeviceName", getVMCapabilities(browser), DeviceScheduler.DriverType.REMOTE, Duration.standardHours(2));
            seleniumSession = webmateSession.addSeleniumSession(driver.getSessionId().toString());
            BrowserSessionRef browserSession = webmateSession.browserSession
                    .getBrowserSessionForSeleniumSession(driver.getSessionId().toString());

            driver.get("http://www.examplepage.org/form_interaction");

            System.out.println("Click on link");
            waitForElement(driver, "lk").click();
            assertEquals("Link Clicked!", waitForElement(driver, ".success").getText());

            browserSession.startAction("Click on button");
            System.out.println("Clicking on Button");
            waitForElement(driver, "bn").click();
            browserSession.finishAction();

            browserSession.startAction("Click on Checkbox");
            System.out.println("Clicking on Checkbox");
            waitForElement(driver, "ck").click();
            browserSession.finishAction();

            browserSession.startAction("Click on Radiobutton");
            System.out.println("Clicking on RadioButton");
            waitForElement(driver, "rd").click();

            browserSession.createState("after radio button");
            browserSession.finishAction("was successful");

            System.out.println("Clicking on Element with a Hover Event");
            waitForElement(driver, "mover").click();

            System.out.println("Entering some Text...");
            waitForElement(driver, "text-input").click();
            waitForElement(driver, "text-input").sendKeys("Test test");

            System.out.println("Entering more text...");
            waitForElement(driver, "area").click();
            waitForElement(driver, "area").sendKeys("Here some more test");

            seleniumSession.finishTestRun(TestRunEvaluationStatus.PASSED, "TestRun completed successfully");
            System.out.println("Selenium expedition completed");
        } catch (Throwable e) {
            if (seleniumSession != null)
                seleniumSession.finishTestRun(TestRunEvaluationStatus.FAILED, "TestRun has failed");
            e.printStackTrace();
        } finally {
            if (driver != null)
                driver.quit();
            // 4. important -> release the device when the test is done or fails
            deviceScheduler.releaseDevice();

        }
    }



    private DesiredCapabilities getMobileCapabilites(String model) {
        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setCapability("browserName", "Appium");
        caps.setCapability("browserVersion", "1.22.3");
        caps.setCapability("wm:model", model);
        caps.setCapability("email", MyCredentials.WEBMATE_USERNAME);
        caps.setCapability("apikey", MyCredentials.WEBMATE_APIKEY);
        caps.setCapability("project", MyCredentials.WEBMATE_PROJECTID.toString());
        // For now, we can't ensure that a app is installed on the device using deviceRequirements.
        // You need to make sure the apps are installed using wm:installPackage in the capabilities
        caps.setCapability("wm:installPackage", "a3xxxxxx-xxxx-xxxx-xxxx-xxxxxxxx9d04");
        caps.setCapability("wm:appPackage", "com.afollestad.materialdialogssample");
        caps.setCapability("wm:appActivity", "com.afollestad.materialdialogssample.MainActivity");
        return caps;
    }

    @Test
    public void performTestMobile() throws MalformedURLException, InterruptedException {
        String model = "Galaxy A52 5G";
        AndroidDriver driver = null;
        try {
            driver = (AndroidDriver<?>) deviceScheduler.scheduleDevice("TestDevice", getMobileCapabilites(model), DeviceScheduler.DriverType.ANDROID, Duration.standardHours(2));
            waitForElement(driver, "com.afollestad.materialdialogssample:id/basic_buttons")
                    .click();

            driver.findElement(MobileBy.AndroidUIAutomator("new UiSelector().textContains(\"" + "AGREE" + "\")"))
                    .click();

            AndroidDriver finalDriver = driver;
            WebElementFunction elem = () -> finalDriver.findElement(MobileBy.AndroidUIAutomator("new UiScrollable(new UiSelector().scrollable(true))" +
                    ".scrollIntoView(new UiSelector().textContains(\"LIST + TITLE + CHECKBOX PROMPT + BUTTONS\"))"));
            System.out.println(elem.getElement());
            Helpers.scrollDownUntilElementIsInView(driver, elem);
            elem.getElement().click();

            WebElement checkbox = driver.findElementByAndroidUIAutomator("new UiSelector().text(\"I understand what this means\")");
            checkbox.click();

            WebElement btn = driver.findElementByAndroidUIAutomator("new UiSelector().text(\"AGREE\")");
            btn.click();
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            if (driver != null)
                driver.quit();
            deviceScheduler.releaseDevice();
        }
    }

}
