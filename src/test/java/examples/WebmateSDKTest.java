package examples;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.testfabrik.webmate.javasdk.ProjectId;
import com.testfabrik.webmate.javasdk.WebmateAPISession;
import com.testfabrik.webmate.javasdk.WebmateAuthInfo;
import com.testfabrik.webmate.javasdk.WebmateEnvironment;
import com.testfabrik.webmate.javasdk.browsersession.BrowserSessionId;
import com.testfabrik.webmate.javasdk.browsersession.BrowserSessionRef;
import com.testfabrik.webmate.javasdk.devices.DeviceId;
import com.testfabrik.webmate.javasdk.devices.DeviceTemplate;
import com.testfabrik.webmate.javasdk.jobs.JobRunId;
import com.testfabrik.webmate.javasdk.jobs.JobRunSummary;
import com.testfabrik.webmate.javasdk.jobs.jobconfigs.BrowserSessionCrossbrowserJobInput;
import com.testfabrik.webmate.javasdk.mailtest.TestMail;
import com.testfabrik.webmate.javasdk.mailtest.TestMailAddress;
import com.testfabrik.webmate.javasdk.testmgmt.ArtifactInfo;
import com.testfabrik.webmate.javasdk.testmgmt.ArtifactType;
import com.testfabrik.webmate.javasdk.testmgmt.TestResult;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.openqa.selenium.By;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import static org.junit.Assert.fail;


/**
 * Simple test showing how to perform a Selenium based crossbrowser test using webmate.
 */
@RunWith(JUnit4.class)
public class WebmateSDKTest extends Commons {

    private WebmateAPISession webmateSession;

    @Before
    public void setup() {
        WebmateAuthInfo authInfo = new WebmateAuthInfo(MyCredentials.MY_WEBMATE_USERNAME, MyCredentials.MY_WEBMATE_APIKEY);
        webmateSession = new WebmateAPISession(authInfo, WebmateEnvironment.create());
    }

    @Test
    public void multiBrowserTest() {

        Browser referenceBrowser = new Browser("firefox", "61", "WINDOWS_10_64");

        List<Browser> crossBrowsers = ImmutableList.of(
                new Browser("chrome", "67", "WINDOWS_10_64"),
                new Browser("ie", "11", "WINDOWS_10_64")
        );

        // perform test for reference browser
        BrowserSessionId referenceSession = performTest(referenceBrowser);

        // Query all screenshot artifacts made during the test on the reference browser
        Set<ArtifactType> artifactsToQuery = new HashSet<>();
        artifactsToQuery.add(ArtifactType.fromString("Page.FullpageScreenshot"));

        List<ArtifactInfo> artifacts = webmateSession.artifact.queryArtifacts(MyCredentials.MY_WEBMATE_PROJECTID, referenceSession, artifactsToQuery);
        System.out.println("Found " + artifacts.size() + " screenshot artifacts for session " + referenceSession);
        for (ArtifactInfo i : artifacts) {
            System.out.println(i);
        }
        System.out.println();


        // Perform tests for cross browsers and collect corresponding BrowserSessions.
        List<BrowserSessionId> crossbrowserSessions = new ArrayList<>();

        for (Browser crossbrowser : crossBrowsers) {
            BrowserSessionId browserSessionId = performTest(crossbrowser);
            crossbrowserSessions.add(browserSessionId);
        }

        // start crossbrowser layout comparison for browsersessions
        JobRunId jobRunId = webmateSession.jobEngine.startJob("WebmateSDKTest-Example", new BrowserSessionCrossbrowserJobInput(referenceSession, crossbrowserSessions), MyCredentials.MY_WEBMATE_PROJECTID);
        System.out.println("Started Layout-Comparison-Job, ID of the JobRun is " + jobRunId + "\n");

        // retrieve test results
        JobRunSummary summary = webmateSession.jobEngine.getSummaryOfJobRun(jobRunId);

        try {
            // Wait a few seconds until the first test results are available
            Thread.sleep(3000);
            // Wait until the number of results doesn't change anymore. This is an indication that the test is finished
            Util.waitUntilStable(() -> webmateSession.testMgmt.getTestResults(summary.getOptTestRunInfo().getTestRunId()).or(new ArrayList<>()).size(), 500);
        } catch(Exception e) {
            System.err.println("An error occured while waiting for test results");
            e.printStackTrace();
            fail();
        }

        Optional<List<TestResult>> testResults = webmateSession.testMgmt.getTestResults(summary.getOptTestRunInfo().getTestRunId());

        if (testResults.isPresent()) {
            System.out.println("Got " + testResults.get().size() + " test results");
            for (TestResult result : testResults.get()) {
                System.out.println(result);
            }
            System.out.println();
        }

        // Create a test mail address in the current test run
        TestMailAddress address = webmateSession.mailTest.createTestMailAddress(MyCredentials.MY_WEBMATE_PROJECTID, summary.getOptTestRunInfo().getTestRunId());
        System.out.println("Generated test mail address: " + address);
        // Get all email received for the current test run. In this case there are no emails
        List<TestMail> mails = webmateSession.mailTest.getMailsInTestRun(MyCredentials.MY_WEBMATE_PROJECTID, summary.getOptTestRunInfo().getTestRunId());
        Assert.assertEquals(mails.size(), 0);
    }

    @Test
    public void deviceTest() {
        // count devices currently deployed
        List<DeviceId> existingDevices = new ArrayList<>(webmateSession.device.getDeviceIdsForProject(MyCredentials.MY_WEBMATE_PROJECTID));
        int baseNumberDevices = existingDevices.size();
        System.out.println("Found existing devices " + existingDevices);

        // get templates
        List<DeviceTemplate> templates = new ArrayList<>(webmateSession.device.getDeviceTemplatesForProject(MyCredentials.MY_WEBMATE_PROJECTID));
        System.out.println("Found " + templates.size() + " templates");

        // Select some win-10 template
        DeviceTemplate winTemplate = templates.stream().filter(t -> t.getName().contains("win-10")).findFirst().get();
        System.out.println("Will deploy template " + winTemplate.getName());

        // deploy new device
        webmateSession.device.requestDeviceByTemplate(MyCredentials.MY_WEBMATE_PROJECTID, winTemplate.getId());
        System.out.println("Deploying...");

        // check if device has been deployed
        Util.waitUntilEquals(() -> webmateSession.device.getDeviceIdsForProject(MyCredentials.MY_WEBMATE_PROJECTID).size(), baseNumberDevices + 1, 60000);

        // Find id of new device
        List<DeviceId> newDevices = new ArrayList<>(webmateSession.device.getDeviceIdsForProject(MyCredentials.MY_WEBMATE_PROJECTID));
        System.out.println("Currently deployed devices: " + newDevices);
        DeviceId newId = newDevices.stream().filter(id -> !existingDevices.contains(id)).findFirst().get();

        // delete device
        System.out.println("Going to delete device " + newId);
        webmateSession.device.releaseDevice(newId);
        System.out.println("Deleting...");

        // check if device has been deleted
        Util.waitUntilEquals(() -> webmateSession.device.getDeviceIdsForProject(MyCredentials.MY_WEBMATE_PROJECTID).size(), baseNumberDevices, 60000);
        System.out.println("Successfully deleted device");
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
        caps.setCapability("email", MyCredentials.MY_WEBMATE_USERNAME);
        caps.setCapability("apikey", MyCredentials.MY_WEBMATE_APIKEY);
        caps.setCapability("project", MyCredentials.MY_WEBMATE_PROJECTID.toString());

        RemoteWebDriver driver;
        try {
            driver = new RemoteWebDriver(new URL(MyCredentials.WEBMATE_SELENIUM_URL), caps);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        BrowserSessionRef browserSession = webmateSession.browserSession.getBrowserSessionForSeleniumSession(driver.getSessionId().toString());

        try {

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

            System.out.println("Test done\n");

            driver.quit();

        } catch (Exception e) {
            driver.quit();
            throw e;
        }

        return browserSession.browserSessionId;
    }
}


