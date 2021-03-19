package examples;

import com.testfabrik.webmate.javasdk.*;
import com.testfabrik.webmate.javasdk.browsersession.*;
import com.testfabrik.webmate.javasdk.selenium.WebmateSeleniumSession;
import com.testfabrik.webmate.javasdk.testmgmt.*;
import com.testfabrik.webmate.javasdk.testmgmt.spec.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import static examples.MyCredentials.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Simple test showing how to perform a Selenium test using webmate.
 */
@RunWith(JUnit4.class)
public class SeleniumTest extends Commons {

    private WebmateAPISession webmateSession;

    @Before
    public void setup() {
        WebmateAuthInfo authInfo = new WebmateAuthInfo(MyCredentials.MY_WEBMATE_USERNAME, MyCredentials.MY_WEBMATE_APIKEY);
        webmateSession = new WebmateAPISession(
                authInfo,
                WebmateEnvironment.create(),
                MY_WEBMATE_PROJECTID);
    }

    @Test
    public void performTest() throws MalformedURLException {
        Platform platform = new Platform(PlatformType.WINDOWS, "10", "64");
        Browser browser = new Browser(BrowserType.CHROME, "83", platform);
        executeTestInBrowser(browser);
    }

    private DesiredCapabilities getCapabilities(Browser browser) {
        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setCapability("browserName", browser.getBrowserType().getValue());
        caps.setCapability("version", browser.getVersion());
        caps.setCapability("platform", browser.getPlatform().toString());
        caps.setCapability(WebmateCapabilityType.API_KEY, MY_WEBMATE_APIKEY);
        caps.setCapability(WebmateCapabilityType.USERNAME, MY_WEBMATE_USERNAME);
        caps.setCapability(WebmateCapabilityType.PROJECT, MY_WEBMATE_PROJECTID.toString());
        // See com.testfabrik.webmate.javasdk.WebmateCapabilityType for webmate specific capabilities
        caps.setCapability("wm:autoScreenshots", true);
        caps.setCapability("wm:name", "A sample selenium test");
        caps.setCapability("wm:tags", "Sprint=34, Hello World");

        return caps;
    }

    public BrowserSessionId executeTestInBrowser(Browser browser) throws MalformedURLException {
        System.out.println("Starting test for " + browser.getBrowserType() + " " + browser.getVersion() + " on " + browser.getPlatform());
        DesiredCapabilities caps = getCapabilities(browser);
        RemoteWebDriver driver = new RemoteWebDriver(new URL(WEBMATE_SELENIUM_URL), caps);
        WebmateSeleniumSession seleniumSession = webmateSession.addSeleniumSession(driver.getSessionId().toString());
        BrowserSessionRef browserSession = webmateSession.browserSession
                .getBrowserSessionForSeleniumSession(driver.getSessionId().toString());

        try {
            driver.get("http://www.examplepage.org/version/future");

            System.out.println("Selecting some elements....");
            WebDriverWait wait = new WebDriverWait(driver, 20);

            browserSession.createState("after click");

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

        return browserSession.browserSessionId;
    }
}
