package examples;

import com.testfabrik.webmate.javasdk.WebmateAPISession;
import com.testfabrik.webmate.javasdk.WebmateAuthInfo;
import com.testfabrik.webmate.javasdk.WebmateEnvironment;
import com.testfabrik.webmate.javasdk.browsersession.BrowserSessionRef;
import com.testfabrik.webmate.javasdk.devices.DeviceOffer;
import com.testfabrik.webmate.javasdk.selenium.WebmateSeleniumSession;
import com.testfabrik.webmate.javasdk.testmgmt.TestRunEvaluationStatus;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static examples.MyCredentials.*;
import static examples.helpers.Helpers.waitForElement;
import static org.junit.Assert.assertEquals;

/**
 * Simple test showing how to query available mobile devices given some basic requirements (e.g. platform, platformversion, browser),
 * select one of those devices for a test and use it via slotId
 */
@RunWith(JUnit4.class)
public class QueryDeployablesTest {

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
    public void performTest() throws MalformedURLException {
        webmateSession.device.getDeviceIdsForProject(WEBMATE_PROJECTID);
        Set<DeviceOffer> deviceOffers = webmateSession.device.queryDeployablesByRequirements("android", "10", "chrome");
        executeTestInBrowser(deviceOffers);
    }

    public static <T> T getRandomElementFromSet(Set<T> set) {
        if (set.isEmpty()) return null;
        List<T> list = new ArrayList<>(set);
        Random random = new Random();
        return list.get(random.nextInt(list.size()));
    }

    public void executeTestInBrowser(Set<DeviceOffer> deviceOffers) throws MalformedURLException {
        String slot = getRandomElementFromSet(deviceOffers).getDeviceProperties().getSlotId();
        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setCapability("platformName", "android");
        caps.setCapability("email", MyCredentials.WEBMATE_USERNAME);
        caps.setCapability("apikey", MyCredentials.WEBMATE_APIKEY);
        caps.setCapability("project", WEBMATE_PROJECTID.toString());
        caps.setCapability("wm:slot", slot);
        RemoteWebDriver driver = new RemoteWebDriver(new URL(WEBMATE_SELENIUM_URL), caps);
        WebmateSeleniumSession seleniumSession = webmateSession.addSeleniumSession(driver.getSessionId().toString());
        BrowserSessionRef browserSession = webmateSession.browserSession
                .getBrowserSessionForSeleniumSession(driver.getSessionId().toString());

        try {
            driver.get("http://www.examplepage.org/form_interaction");

            System.out.println("Click on link");
            waitForElement(driver, "lk").click();
            assertEquals("Link Clicked!", waitForElement(driver, ".success").getText());

            browserSession.createState("after link");

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
        } catch(Throwable e) {
            seleniumSession.finishTestRun(TestRunEvaluationStatus.FAILED, "TestRun has failed");
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }
}
