package examples;

import com.testfabrik.webmate.javasdk.*;
import com.testfabrik.webmate.javasdk.browsersession.*;
import com.testfabrik.webmate.javasdk.selenium.WebmateSeleniumSession;
import com.testfabrik.webmate.javasdk.testmgmt.*;
import org.junit.*;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.rules.TestWatcher;
import org.junit.runners.model.Statement;
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
 * This class executes the same test as class SeleniumTest
 * but uses a Junit Rule to start and stop actions using
 * the webmate SDK. If at least one test method fails, the
 * webmate session corresponding to the test run is also failed.
 * This class also demonstrates how to separate code that is
 * specific to webmate (i.e. code that interacts with the SDK)
 * from vanilla selenium code. To highlight this, all webmate specific
 * code has been moved to the end of the class.
 * This class has two test methods (which together test the same things
 * as the code in class SeleniumTest) that can be executed in any order.
 * If order matters, we can use the JUnit MethodSorters Annotation.
 * Larger test bases with more (and more complex) tests can use
 * the same approach based on JUnit rules as demonstrated in this class
 * with ClassRules and Test Suites.
 */
@RunWith(JUnit4.class)
public class SeleniumTestWithActionRule extends Commons {

    // Share the selenium driver between executions of tests
    private static RemoteWebDriver driver;

    @BeforeClass
    public static void setup() throws MalformedURLException, URISyntaxException {
        // create the selenium driver
        Platform platform = new Platform(PlatformType.WINDOWS, "10", "64");
        setupSeleniumSession(BrowserType.CHROME.toString(), "94", platform.toString());

        // setup the webmate session
        // if this call and the corresponding teardown call are removed, the selenium test executes just fine
        setupWebmateSession();
    }

    @AfterClass
    public static void tearDown() {
        try {
            // teardown the webmate session and report the result to webmate
            // if this call and the corresponding setup call are removed, the selenium test executes just fine
            teardownWebmateSession();
        } finally {
            driver.quit();
        }
    }

    /** Utility method to setup the selenium driver. It uses plain Selenium and sets
     *  some capabilities needed to connect to webmate.
     */
    private static void setupSeleniumSession(String browserName, String browserVersion,
                                             String browserPlatform) throws MalformedURLException {

        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setCapability("browserName", browserName);
        caps.setCapability("version", browserVersion);
        caps.setCapability("platform", browserPlatform);
        caps.setCapability(WebmateCapabilityType.API_KEY, WEBMATE_APIKEY);
        caps.setCapability(WebmateCapabilityType.USERNAME, WEBMATE_USERNAME);
        caps.setCapability(WebmateCapabilityType.PROJECT, WEBMATE_PROJECTID.toString());
        // See com.testfabrik.webmate.javasdk.WebmateCapabilityType for webmate specific capabilities
        caps.setCapability("wm:autoScreenshots", true);

        driver = new RemoteWebDriver(new URL(WEBMATE_SELENIUM_URL), caps);
    }

    @Test
    public void redirectTest() {
        driver.get("http://www.examplepage.org/version/future");

        System.out.println("Selecting some elements....");
        WebDriverWait wait = new WebDriverWait(driver, 20);
//        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".container"))).click();

        browserSession.createState("after click");

        System.out.println("Clicking on something that will redirect us...");
        waitForElement(driver, "goto-examplepage").click();
        assertEquals("Cross Browser Issues Example", driver.getTitle());

    }

    @Test
    public void formularTest() {
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

        System.out.println("Entering more Text...");
        waitForElement(driver, "area").click();
        waitForElement(driver, "area").sendKeys("Here some more test");

    }

    /** ============= Code that interacts with the webmate SDK =============== */

    private static WebmateAPISession webmateSession;

    private static WebmateSeleniumSession seleniumSession;

    private static BrowserSessionRef browserSession;

    private static boolean hasAtLeaseOneTestFailed = false;

    /** Authenticate with the webmate SDK and setup selenium and browsersessions */
    private static void setupWebmateSession() throws URISyntaxException {
        WebmateAuthInfo authInfo = new WebmateAuthInfo(MyCredentials.WEBMATE_USERNAME, MyCredentials.WEBMATE_APIKEY);
        webmateSession = new WebmateAPISession(
                authInfo,
                WebmateEnvironment.create(new URI(WEBMATE_API_URI)),
                WEBMATE_PROJECTID);

        seleniumSession = webmateSession.addSeleniumSession(driver.getSessionId().toString());

        browserSession = webmateSession.browserSession .getBrowserSessionForSeleniumSession(driver.getSessionId().toString());
    }

    /** Finish the webmate session and report the test run status to webmate. */
    private static void teardownWebmateSession() {
        System.out.println("Finishing test run");
        if (hasAtLeaseOneTestFailed) {
            seleniumSession.finishTestRun(TestRunEvaluationStatus.FAILED, "TestRun has failed");
        } else {
            seleniumSession.finishTestRun(TestRunEvaluationStatus.PASSED, "Successful.");
        }

    }

    /** This is a simple JUnit Test Rule that starts and finishes
     *  actions in the webmate session. It uses the JUnit display name
     *  for the action name. If a test fails, the corresponding action
     *  is also failed.
     *  Depending on how fine-grained the tests are this implementation
     *  could also create and finish Story-Checks instead of Actions.
     */
    @Rule
    public final TestRule actionRule = new TestWatcher() {
        @Override
        public Statement apply(Statement base, Description description) {
            return super.apply(base, description);
        }

        @Override
        protected void succeeded(Description description) {
            webmateSession.finishAction();
        }

        @Override
        protected void failed(Throwable e, Description description) {
            webmateSession.finishActionAsFailure(e.getClass().getName() + ": " + e.getMessage());
            hasAtLeaseOneTestFailed = true;
        }

        @Override
        protected void starting(Description description) {
            webmateSession.startAction(description.getDisplayName());
        }
    };

}
