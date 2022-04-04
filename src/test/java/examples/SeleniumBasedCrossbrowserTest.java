package examples;

import com.google.common.collect.ImmutableList;
import com.testfabrik.webmate.javasdk.*;
import com.testfabrik.webmate.javasdk.browsersession.BrowserSessionId;
import com.testfabrik.webmate.javasdk.browsersession.BrowserSessionRef;
import com.testfabrik.webmate.javasdk.browsersession.OfflineExpeditionSpec;
import com.testfabrik.webmate.javasdk.testmgmt.TestRun;
import com.testfabrik.webmate.javasdk.testmgmt.TestRunInfo;
import com.testfabrik.webmate.javasdk.testmgmt.spec.ExpeditionComparisonSpec;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import static examples.MyCredentials.*;
import static org.junit.Assert.assertEquals;

/**
 * Simple test showing how to perform a cross browser test using webmate.
 */
@RunWith(JUnit4.class)
public class SeleniumBasedCrossbrowserTest extends Commons {

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
        Platform platform = new Platform(PlatformType.WINDOWS, "10", "64");
        BrowserSessionId chromeSessionId = executeTestInBrowser(new Browser(BrowserType.CHROME, "93", platform));
        BrowserSessionId firefoxSessionId = executeTestInBrowser(new Browser(BrowserType.FIREFOX, "91", platform));

        TestRun testRun = webmateSession.testMgmt.startExecutionWithBuilder(ExpeditionComparisonSpec.ExpeditionComparisonCheckBuilder.builder(
                        "Example cross-browser comparison",
                        new OfflineExpeditionSpec(chromeSessionId),
                        ImmutableList.of(new OfflineExpeditionSpec(firefoxSessionId))
                ).withTag("Selenium")
                .withTag("Sprint", "22")
                .withCurrentDateAsTag());

        System.out.println("Starting layout analysis.");
        TestRunInfo testRunInfo = testRun.waitForCompletion();
        System.out.println("Comparison is finished: " + testRunInfo);

        System.out.println("Issue summary: " + testRunInfo.getIssueSummary().toPrettyString());

        System.out.println("The result is available at: https://app.webmate.io/#/projects/" +
                testRunInfo.getProjectId().toString() + "/testlab/testruns/" + testRunInfo.getTestRunId());
    }

    public BrowserSessionId executeTestInBrowser(Browser browser) throws MalformedURLException {

        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setCapability("browserName", browser.getBrowserType().getValue());
        caps.setCapability("version", browser.getVersion());
        caps.setCapability("platform", browser.getPlatform().toString());
        caps.setCapability(WebmateCapabilityType.API_KEY, WEBMATE_APIKEY);
        caps.setCapability(WebmateCapabilityType.USERNAME, WEBMATE_USERNAME);
        caps.setCapability(WebmateCapabilityType.PROJECT, WEBMATE_PROJECTID.toString());

        RemoteWebDriver driver = new RemoteWebDriver(new URL(WEBMATE_SELENIUM_URL), caps);

        BrowserSessionRef browserSession = webmateSession.browserSession
                .getBrowserSessionForSeleniumSession(driver.getSessionId().toString());

        try {
            driver.get("http://www.examplepage.org/version/future");

            System.out.println("Selecting some elements....");
            WebDriverWait wait = new WebDriverWait(driver, 20);
//        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".container"))).click();

            browserSession.createState("after click");

            System.out.println("Clicking on something that will redirect us...");
            waitForElement(driver, "goto-examplepage").click();
            assertEquals("Cross Browser Issues Example", driver.getTitle());

            driver.get("http://www.examplepage.org/form_interaction");

            System.out.println("Click on link");
            waitForElement(driver, "lk").click();
            assertEquals("Link Clicked!", waitForElement(driver, ".success").getText());

            browserSession.createState("after link");

            System.out.println("Clicking on Button");
            waitForElement(driver, "bn").click();

            System.out.println("Clicking on Checkbox");
            waitForElement(driver, "ck").click();

            System.out.println("Clicking on RadioButton");
            waitForElement(driver, "rd").click();

            browserSession.createState("after radio button");

            System.out.println("Clicking on Element with a Hover Event");
            waitForElement(driver, "mover").click();

            System.out.println("Entering some Text...");
            waitForElement(driver, "text-input").click();
            waitForElement(driver, "text-input").sendKeys("hubba");

            System.out.println("Entering more Text...");
            waitForElement(driver, "area").click();
            waitForElement(driver, "area").sendKeys("hubba hub!");

            System.out.println("Selenium expedition completed");
        } catch(Throwable e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }

        return browserSession.browserSessionId;
    }
}


