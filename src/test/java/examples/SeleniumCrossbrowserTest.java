package examples;

import com.google.common.collect.ImmutableList;
import com.testfabrik.webmate.javasdk.ProjectId;
import com.testfabrik.webmate.javasdk.WebmateAPISession;
import com.testfabrik.webmate.javasdk.WebmateAuthInfo;
import com.testfabrik.webmate.javasdk.WebmateEnvironment;
import com.testfabrik.webmate.javasdk.browsersession.BrowserSessionId;
import com.testfabrik.webmate.javasdk.browsersession.BrowserSessionRef;
import com.testfabrik.webmate.javasdk.jobs.JobRunId;
import com.testfabrik.webmate.javasdk.jobs.jobconfigs.BrowserSessionCrossbrowserJobInput;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


/**
 * Simple test showing how to perform a Selenium based crossbrowser test using webmate.
 */
@RunWith(JUnit4.class)
public class SeleniumCrossbrowserTest {

    private WebmateAPISession webmateSession;

    private final static String MY_WEBMATE_USER = "xxxx@xxxxxxxxx.com";
    private final static String MY_WEBMATE_APIKEY = "xxxxxx-xxxxx-xxxx-ba43-da86b97734eb";
    private final static ProjectId MY_WEBMATE_PROJECTID = new ProjectId(UUID.fromString("xxxxxx-1b49-4eb0-bb3a-xxxxxxxxx"));

    /**
     * Helper class representing a Selenium browser.
     */
    private static class Browser {
        public final String browserName;
        public final String browserVersion;
        public final String browserPlatform;

        public Browser(String browserName, String browserVersion, String browserPlatform) {
            this.browserName = browserName;
            this.browserVersion = browserVersion;
            this.browserPlatform = browserPlatform;
        }

        @Override
        public String toString() {
            return "[" + browserName + ", " + browserVersion + ", " + browserPlatform + "]";
        }
    }

    private final static String WEBMATE_SELENIUM_URL = "https://app.webmate.io:44444/wd/hub";

    @Before
    public void setup() throws URISyntaxException {
        WebmateAuthInfo authInfo = new WebmateAuthInfo(MY_WEBMATE_USER, MY_WEBMATE_APIKEY);
        webmateSession = new WebmateAPISession(authInfo, WebmateEnvironment.create());
    }


    @After
    public void teardown() {
    }


    @Test
    public void multiBrowserTest() {

        Browser referenceBrowser = new Browser("firefox", "47", "WINDOWS_7_64");

        List<Browser> crossBrowsers = ImmutableList.of(
                new Browser("chrome", "59", "WINDOWS_7_64"),
                new Browser("ie", "11", "WINDOWS_7_64")
        );

        // perform test for reference browser
        BrowserSessionId referenceSession = performTest(referenceBrowser);

        // Perform tests for cross browsers and collect corresponding BrowserSessions.
        List<BrowserSessionId> crossbrowserSessions = new ArrayList<>();

        for (Browser crossbrowser : crossBrowsers) {
            BrowserSessionId browserSessionId = performTest(crossbrowser);
            crossbrowserSessions.add(browserSessionId);
        }

        // start crossbrowser layout comparison for browsersessions
        JobRunId jobRunId = webmateSession.jobEngine.startJob("SeleniumCrossbrowserTest-Example", new BrowserSessionCrossbrowserJobInput(referenceSession, crossbrowserSessions), MY_WEBMATE_PROJECTID);
        System.out.println("Started Layout-Comparison-Job, ID of the JobRun is " + jobRunId);
    }

    /**
     * Simple interaction with a web page.
     */
    public BrowserSessionId performTest(Browser browser) {

        System.out.println("Executing test with browser " + browser);

        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setCapability("browserName", browser.browserName);
        caps.setCapability("version", browser.browserVersion);
        caps.setCapability("platform", browser.browserPlatform);
        caps.setCapability("useproxy", true);
        caps.setCapability("email", MY_WEBMATE_USER);
        caps.setCapability("apikey", MY_WEBMATE_APIKEY);

        RemoteWebDriver driver;
        try {
            driver = new RemoteWebDriver(new URL(WEBMATE_SELENIUM_URL), caps);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        BrowserSessionRef browserSession = webmateSession.browserSession.getBrowserSessionForSeleniumSession(driver.getSessionId().toString());

        driver.get("http://www.examplepage.org/version/future");

        System.out.println("Selecting some elements....");
        WebDriverWait wait = new WebDriverWait(driver, 20);
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".container"))).click();

        browserSession.createState("after click");

        System.out.println("Clicking on something that will redirect us...");
        waitForElement(driver, "goto-examplepage").click();

        String titleOfPage = driver.getTitle();
        if (titleOfPage.equals("Cross Browser Issues Example")) {
            System.out.println("Redirect was successful and we verified that :-) Going to Form-Interaction Test:");
        } else {
            throw new RuntimeException("The title of the page is not \'Cross Browser Issues Example\'");
        }

        driver.get("http://www.examplepage.org/form_interaction");

        System.out.println("Click on link");
        waitForElement(driver, "lk").click();


        if (waitForElement(driver, ".success").getText().equals("Link Clicked!")) {
            System.out.println("Click was successful");
        } else {
            throw new IllegalStateException("Click failed. Text was not \'Link Clicked!\' ");
        }

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

        System.out.println("Test done");

        driver.quit();

        return browserSession.browserSessionId;
    }


    private static WebElement waitForElement(RemoteWebDriver driver, String element) {
        if (element.startsWith(".")) {

            WebDriverWait wait = new WebDriverWait(driver, 20);
            return wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(element)));
        } else {
            WebDriverWait wait = new WebDriverWait(driver, 20);
            return wait.until(ExpectedConditions.elementToBeClickable(By.id(element)));
        }
    }
}


