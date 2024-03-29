package examples;

import com.testfabrik.webmate.javasdk.*;
import com.testfabrik.webmate.javasdk.browsersession.*;
import com.testfabrik.webmate.javasdk.selenium.WebmateSeleniumSession;
import com.testfabrik.webmate.javasdk.testmgmt.*;
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

import static examples.MyCredentials.*;
import static examples.helpers.Helpers.waitForElement;
import static org.junit.Assert.assertEquals;

/**
 * Simple test showing how to perform a Selenium test using webmate.
 */
@RunWith(JUnit4.class)
public class SeleniumTest {

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
        Platform platform = new Platform(PlatformType.WINDOWS, "11", "64");
        Browser browser = new Browser(BrowserType.FIREFOX, "106", platform);
        executeTestInBrowser(browser);
    }

    private DesiredCapabilities getCapabilities(Browser browser) {
        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setCapability("browserName", browser.getBrowserType().getValue());
        caps.setCapability("version", browser.getVersion());
        caps.setCapability("platform", browser.getPlatform().toString());
        caps.setCapability(WebmateCapabilityType.API_KEY, WEBMATE_APIKEY);
        caps.setCapability(WebmateCapabilityType.USERNAME, WEBMATE_USERNAME);
        caps.setCapability(WebmateCapabilityType.PROJECT, WEBMATE_PROJECTID.toString());
        // See com.testfabrik.webmate.javasdk.WebmateCapabilityType for webmate specific capabilities
        // caps.setCapability("wm:autoScreenshots", true);
        caps.setCapability("wm:name", "A sample selenium test");
        caps.setCapability("wm:tags", "Sprint=34, Hello World");

        return caps;
    }

    public void executeTestInBrowser(Browser browser) throws MalformedURLException {
        System.out.println("Starting test for " + browser.getBrowserType() + " " + browser.getVersion() + " on " + browser.getPlatform());
        DesiredCapabilities caps = getCapabilities(browser);
        RemoteWebDriver driver = new RemoteWebDriver(new URL(WEBMATE_SELENIUM_URL), caps);
        WebmateSeleniumSession seleniumSession = webmateSession.addSeleniumSession(driver.getSessionId().toString());
        BrowserSessionRef browserSession = webmateSession.browserSession
                .getBrowserSessionForSeleniumSession(driver.getSessionId().toString());

        try {
            driver.get("http://www.examplepage.org/version/future/");
            browserSession.createState("start");

            System.out.println("Clicking on something that will redirect us...");
            waitForElement(driver, "goto-examplepage").click();
            assertEquals("Cross Browser Issues Example", driver.getTitle());

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
