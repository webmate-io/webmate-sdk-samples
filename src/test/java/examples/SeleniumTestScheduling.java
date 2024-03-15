package examples;

import com.testfabrik.webmate.javasdk.*;
import com.testfabrik.webmate.javasdk.browsersession.BrowserSessionId;
import com.testfabrik.webmate.javasdk.browsersession.BrowserSessionRef;
import com.testfabrik.webmate.javasdk.devices.*;
import com.testfabrik.webmate.javasdk.selenium.WebmateSeleniumSession;
import com.testfabrik.webmate.javasdk.testmgmt.TestRunEvaluationStatus;
import examples.helpers.BrowserRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static examples.MyCredentials.*;
import static examples.helpers.Helpers.waitForElement;
import static org.junit.Assert.assertEquals;


/**
 * Simple test that shows how to schedule Selenium tests with the webmate Java sdk.
 *
 * __Disclaimer__: This approach will only work if everyone who tests in the corresponding project adheres to the following structure.
 * 1. use scheduleDevice to request and wait for a fresh device that fulfills the given requirements (Browser, BrowserVersion and Platform)
 * 2. use the slotId returned by scheduleDevice in your Selenium test capabilities to ensure that you run your test on the device
 * 3. run your test.
 * 4. make sure you release the device when your test is finished, otherwise it will crash.
 */
@RunWith(JUnit4.class)
public class SeleniumTestScheduling  {

    private WebmateAPISession webmateSession;

    @Before
    public void setup() throws URISyntaxException {
        WebmateAuthInfo authInfo = new WebmateAuthInfo(MyCredentials.WEBMATE_USERNAME, MyCredentials.WEBMATE_APIKEY);
        webmateSession = new WebmateAPISession(
                authInfo,
                WebmateEnvironment.create(new URI(WEBMATE_API_URI)),
                WEBMATE_PROJECTID);
    }

    @Test
    public void performTest() throws MalformedURLException, InterruptedException {
        Platform platform = new Platform(PlatformType.MACOS, "MONTEREY","64");
        Browser browser = new Browser(BrowserType.SAFARI, "17", platform);
        executeTest(browser);
    }

    // Helper functions
    private DesiredCapabilities getCapabilities(Browser browser) {
        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setCapability("browserName", browser.getBrowserType().getValue());
        caps.setCapability("browserVersion", browser.getVersion());
        caps.setCapability("platformName", browser.getPlatform().toString());
        caps.setCapability("wm:apikey", WEBMATE_APIKEY);
        caps.setCapability("wm:project", WEBMATE_PROJECTID.toString());
        return caps;
    }

    public DeviceDTO scheduleDevice(String deviceName, Browser browser, int maxRetries) throws InterruptedException{
        DeviceDTO deviceDTO = null;
        String deviceState = "";
        Map requirements = new HashMap();
        BrowserRequest browserRequest = new BrowserRequest(browser);
        requirements.put(DevicePropertyName.Platform, browserRequest.getPlatform());
        requirements.put(DevicePropertyName.AutomationAvailable, true);
        requirements.put(DevicePropertyName.Browsers, browserRequest);
        DeviceRequest request = new DeviceRequest(deviceName, new DeviceRequirements(requirements));

        int retries = 0;

        do {
            try {
                System.out.println("Sending Request for Device");
                deviceDTO = webmateSession.device.requestDeviceByRequirements(request);
                while (deviceDTO == null || !deviceState.equals("running")) {
                    TimeUnit.MINUTES.sleep(1); // Wait for a minute before retrying
                    if (deviceDTO != null) {
                        deviceState = webmateSession.device.getDeviceInfo(deviceDTO.getId()).getState();
                    }
                }
            } catch (Exception e) {
                System.out.println("Could not get device, will retry ...");
                TimeUnit.MINUTES.sleep(1); // Wait for a minute before retrying after an exception
                retries++;
            }
        } while ((deviceDTO == null || !deviceState.equals("running")) && retries < maxRetries);

        return deviceDTO;
    }

    public void executeTest(Browser browser) throws MalformedURLException, InterruptedException {
        // 1. Schedule device for test
        System.out.println("Scheduling test and wait for free device slot");

        DeviceDTO device = scheduleDevice("TestDevice", browser,5);
        if (device != null) {
            // 2. adding slotId to capabilities ensures that the freshly deployed device is used by the test
            DesiredCapabilities caps = getCapabilities(browser);
            caps.setCapability("wm:slot", device.getSlot().getValueAsString());

            // 3. run test as usual
            RemoteWebDriver driver = new RemoteWebDriver(new URL(WEBMATE_SELENIUM_URL), caps);
            WebmateSeleniumSession seleniumSession = webmateSession.addSeleniumSession(driver.getSessionId().toString());
            BrowserSessionRef browserSession = webmateSession.browserSession
                    .getBrowserSessionForSeleniumSession(driver.getSessionId().toString());
            try {
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
                seleniumSession.finishTestRun(TestRunEvaluationStatus.FAILED, "TestRun has failed");
                e.printStackTrace();
            } finally {
                driver.quit();
                // 4. important -> release the device when the test is done or fails
                webmateSession.device.releaseDevice(device.getId());
            }
        }

    }
}
