package examples;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import java.util.ArrayList;
import java.util.Collections;

import static examples.MyCredentials.*;
import static examples.helpers.Helpers.waitForElement;

/**
 * Simple test showing how to perform an A11y test using webmate.
 */
@RunWith(JUnit4.class)
public class A11yTest {

    private WebmateAPISession webmateSession;

    @Before
    public void setup() throws URISyntaxException {
        WebmateAuthInfo authInfo = new WebmateAuthInfo(MyCredentials.WEBMATE_APIKEY);
        webmateSession = new WebmateAPISession(
                authInfo,
                WebmateEnvironment.create(new URI(WEBMATE_API_URI)),
                WEBMATE_PROJECTID);
    }

    @Test
    public void performTest() throws MalformedURLException {
        Platform platform = new Platform(PlatformType.WINDOWS, "11", "64");
        Browser browser = new Browser(BrowserType.CHROME, "135", platform);
        executeTestInBrowser(browser);
    }

    private DesiredCapabilities getCapabilities(Browser browser) {
        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setCapability("browserName", browser.getBrowserType().getValue());
        caps.setCapability("version", browser.getVersion());
        caps.setCapability("platform", browser.getPlatform().toString());
        caps.setCapability(WebmateCapabilityType.API_KEY, WEBMATE_APIKEY);
        caps.setCapability(WebmateCapabilityType.PROJECT, WEBMATE_PROJECTID.toString());
        caps.setCapability("wm:name", "A11y Test for Summit Community (DE)");
        caps.setCapability("wm:tags", "Tacon 2025 Demo");

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
            driver.get("https://summit-community.de/");
            browserSession.createState("start");

            browserSession.startAction("Decline cookies");
            System.out.println("Clicking on 'Decline cookies'");
            waitForElement(driver, ".ccm--decline-cookies").click();
            browserSession.finishAction("decline clicked");

            ObjectMapper om = new ObjectMapper();

            ObjectNode filter = om.createObjectNode();
            filter.put("mode", "complete");

            ObjectNode params = om.createObjectNode();
            params.set("filter", filter);
            params.put("addPassesScreenshots", false);
            params.put("language", "de");

            System.out.println("Starting A11y test'");
            browserSession.createState("start", new BrowserSessionStateExtractionConfig(null, null, null, null, null, null,
                    new ArrayList<>(Collections.singletonList(new FactRequest(FactType.fromArtifactType("Page.ContentAnalysis"), new FactParams(params)))), null, null));

            seleniumSession.finishTestRun(TestRunEvaluationStatus.PASSED, "TestRun completed successfully");
            System.out.println("A11y test completed");
        } catch(Throwable e) {
            seleniumSession.finishTestRun(TestRunEvaluationStatus.FAILED, "TestRun has failed");
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }
}
