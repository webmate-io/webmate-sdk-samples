package examples;

import com.google.common.collect.ImmutableList;
import com.testfabrik.webmate.javasdk.*;
import com.testfabrik.webmate.javasdk.browsersession.BrowserSessionId;
import com.testfabrik.webmate.javasdk.browsersession.BrowserSessionRef;
import com.testfabrik.webmate.javasdk.browsersession.OfflineExpeditionSpec;
import com.testfabrik.webmate.javasdk.testmgmt.TestRun;
import com.testfabrik.webmate.javasdk.testmgmt.TestRunInfo;
import com.testfabrik.webmate.javasdk.testmgmt.TestSession;
import com.testfabrik.webmate.javasdk.testmgmt.spec.ExpeditionComparisonSpec;
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
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static examples.MyCredentials.*;

/**
 * Simple test showing how to perform a layout comparison between two web pages (with different URLs) using webmate.
 */
@RunWith(JUnit4.class)
public class UrlBasedLayoutComparisonTest extends Commons {

    private WebmateAPISession webmateSession;
    private TestSession testSession;

    private static List<String> referenceUrls = Arrays.asList(
            "http://examplepage.org/index.html",
            "http://examplepage.org/version/current"
    );

    private static List<String> compareUrls = Arrays.asList(
            "http://examplepage.org/index_alternative.html",
            "http://examplepage.org/version/future"
    );

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

        testSession = webmateSession.testMgmt.createTestSession("Regression Test");
        webmateSession.addToTestSession(testSession.getId());

        Platform platform = new Platform(PlatformType.WINDOWS, "10", "64");

        BrowserSessionId chromeSessionId1 = executeTestInBrowser(referenceUrls, new Browser(BrowserType.CHROME, "93", platform));
        BrowserSessionId chromeSessionId2 = executeTestInBrowser(compareUrls, new Browser(BrowserType.CHROME, "93", platform));

        TestRun testRun = webmateSession.testMgmt.startExecutionWithBuilder(ExpeditionComparisonSpec.ExpeditionComparisonCheckBuilder.builder(
                        "Layout comparison",
                        new OfflineExpeditionSpec(chromeSessionId1),
                        ImmutableList.of(new OfflineExpeditionSpec(chromeSessionId2))
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

    /**
     * Execute selenium session and collect layout data.
     */
    public BrowserSessionId executeTestInBrowser(List<String> urls, Browser browser) throws MalformedURLException {

        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setCapability("browserName", browser.getBrowserType().getValue());
        caps.setCapability("version", browser.getVersion());
        caps.setCapability("platform", browser.getPlatform().toString());
        caps.setCapability(WebmateCapabilityType.API_KEY, WEBMATE_APIKEY);
        caps.setCapability(WebmateCapabilityType.USERNAME, WEBMATE_USERNAME);
        caps.setCapability(WebmateCapabilityType.PROJECT, WEBMATE_PROJECTID.toString());
        caps.setCapability("wm:sessions", this.testSession.getId().toString());
        caps.setCapability("wm:name", "Regression Test");


        RemoteWebDriver driver = new RemoteWebDriver(new URL(WEBMATE_SELENIUM_URL), caps);

        BrowserSessionRef browserSession = webmateSession.browserSession
                .getBrowserSessionForSeleniumSession(driver.getSessionId().toString());

        try {
            IntStream.range(0, urls.size())
                    .forEach(i -> {
                        driver.get(urls.get(i));
                        browserSession.createState("Page " + i);
                    });
        } catch(Throwable e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }

        return browserSession.browserSessionId;
    }
}
