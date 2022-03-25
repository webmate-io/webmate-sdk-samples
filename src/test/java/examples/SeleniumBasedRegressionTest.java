package examples;

import com.google.common.collect.ImmutableList;
import com.testfabrik.webmate.javasdk.*;
import com.testfabrik.webmate.javasdk.browsersession.*;
import com.testfabrik.webmate.javasdk.testmgmt.TestRun;
import com.testfabrik.webmate.javasdk.testmgmt.TestRunInfo;
import com.testfabrik.webmate.javasdk.testmgmt.spec.*;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.UUID;

import static examples.MyCredentials.*;
import static org.junit.Assert.assertEquals;

/**
 * Simple test that shows how to compare two Selenium based browser sessions (aka "expeditions")
 * using webmate.
 * One of the most interesting use cases for this functionality is to compare
 * two versions of a web page or web application, e.g. a stable release deployed at the
 * beginning of a sprint and a nightly version.
 *
 * The following example provides two methods that may individually be executed as a JUnit test.
 *
 * The first method ("createReferenceExpedition") performs a Selenium test on a web page. After
 * the test finishes, the corresponding BrowserSessionId (an alternative / forthcoming terminology
 * for this is "ExpeditionId") is saved to the home directory of the executing user in the
 * file "webmate_referencesession_id.txt".
 *
 * The second method ("createExpeditionAndCompareWithReference") performs the same
 * Selenium test as above, returns its BrowserSessionId and starts a webmate TestRun of type
 * "ExpeditionComparison", which compares the layout of the latter Selenium test with
 * that whose Id is stored in "webmate_referencesession_id.txt".
 *
 * Usually, if both methods are executed in direct succession, webmate won't find any
 * differences because the web page did not change between those runs. If you want to
 * see differences, you may change the Selenium script for the second run to add synthetic
 * deviations.
 */
@RunWith(JUnit4.class)
public class SeleniumBasedRegressionTest extends Commons {

    private WebmateAPISession webmateSession;

    private static final String REFERENCE_FILENAME = "webmate_referencesession_id.txt";

    private static final BrowserType BROWSERTYPE = BrowserType.CHROME;
    private static final String BROWSERVERSION = "93";
    private static final Platform PLATFORM = new Platform(PlatformType.WINDOWS, "10", "64");

    @Before
    public void setup() throws URISyntaxException {
        WebmateAuthInfo authInfo = new WebmateAuthInfo(MyCredentials.WEBMATE_USERNAME, MyCredentials.WEBMATE_APIKEY);
        webmateSession = new WebmateAPISession(
                authInfo,
                WebmateEnvironment.create(new URI(WEBMATE_API_URI)),
                WEBMATE_PROJECTID);
    }

    private BrowserSessionId getReferenceExpeditionId() throws IOException {
        String home = System.getProperty("user.home");
        File file = new File(home + "/"  + REFERENCE_FILENAME);
        FileReader reader = new FileReader(file);
        BufferedReader bufferedReader = new BufferedReader(reader);
        String browserSessionIdStr = bufferedReader.readLine();
        return new BrowserSessionId(UUID.fromString(browserSessionIdStr));
    }

    private void saveReferenceSessionId(BrowserSessionId expeditionId) throws IOException {
        String home = System.getProperty("user.home");
        File file = new File(home + "/" + REFERENCE_FILENAME);
        FileWriter writer = new FileWriter(file);
        writer.write(expeditionId.getValueAsString());
        writer.close();
    }

    @Test
    @Ignore
    public void createReferenceExpedition() throws IOException {
        BrowserSessionId referenceExpedition = executeTest();
        saveReferenceSessionId(referenceExpedition);
    }

    @Test
    public void createExpeditionAndCompareWithReference() throws IOException {
        BrowserSessionId compareExpeditionId = executeTest();

        BrowserSessionId referenceExpeditionId = getReferenceExpeditionId();

        TestRun testRun = webmateSession.testMgmt.startExecutionWithBuilder(ExpeditionComparisonSpec.ExpeditionComparisonCheckBuilder.builder(
                "Example Regression Test",
                new OfflineExpeditionSpec(referenceExpeditionId),
                ImmutableList.of(new OfflineExpeditionSpec(compareExpeditionId))
        ).withTag("Selenium").withTag("Sprint", "22"));

        System.out.println("Starting layout analysis.");
        TestRunInfo testRunInfo = testRun.waitForCompletion();
        System.out.println("Comparison is finished: " + testRunInfo);
        System.out.println("The result is available at: https://app.webmate.io/#/projects/" +
                testRunInfo.getProjectId().toString() + "/testlab/testruns/" + testRunInfo.getTestRunId());
    }


    /**
     * Perform Selenium test.
     */
    public BrowserSessionId executeTest() throws MalformedURLException {

        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setCapability("browserName", BROWSERTYPE.getValue());
        caps.setCapability("version", BROWSERVERSION);
        caps.setCapability("platform", PLATFORM.toString());
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

//            System.out.println("Clicking on Checkbox");
//            waitForElement(driver, "ck").click();

            System.out.println("Clicking on RadioButton");
            waitForElement(driver, "rd").click();

            browserSession.createState("after radio button");

            System.out.println("Clicking on Element with a Hover Event");
            waitForElement(driver, "mover").click();

            System.out.println("Entering some Text...");
            waitForElement(driver, "text-input").click();
            waitForElement(driver, "text-input").sendKeys("Test test");

            System.out.println("Entering more Text...");
            waitForElement(driver, "area").click();
            waitForElement(driver, "area").sendKeys("Here some more test");

            System.out.println("Selenium expedition completed");
        } catch(Throwable e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }

        return browserSession.browserSessionId;
    }
}
