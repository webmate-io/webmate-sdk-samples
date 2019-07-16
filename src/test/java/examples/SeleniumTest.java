package examples;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.testfabrik.webmate.javasdk.WebmateAPISession;
import com.testfabrik.webmate.javasdk.WebmateAuthInfo;
import com.testfabrik.webmate.javasdk.WebmateEnvironment;
import com.testfabrik.webmate.javasdk.browsersession.BrowserSessionId;
import com.testfabrik.webmate.javasdk.browsersession.BrowserSessionRef;
import com.testfabrik.webmate.javasdk.jobs.JobRunId;
import com.testfabrik.webmate.javasdk.jobs.JobRunSummary;
import com.testfabrik.webmate.javasdk.jobs.jobconfigs.BrowserSessionCrossbrowserJobInput;
import com.testfabrik.webmate.javasdk.testmgmt.TestResult;
import org.junit.After;
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
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


/**
 * Simple test showing how to perform a Selenium based crossbrowser test using webmate.
 */
@RunWith(JUnit4.class)
public class SeleniumTest extends Commons {

    private WebmateAPISession webmateSession;

    @Before
    public void setup() {
        WebmateAuthInfo authInfo = new WebmateAuthInfo(MyCredentials.MY_WEBMATE_USERNAME, MyCredentials.MY_WEBMATE_APIKEY);
        webmateSession = new WebmateAPISession(authInfo, WebmateEnvironment.create());
    }


    @After
    public void teardown() {
    }


    @Test
    public void performTest() {
        Browser browser = new Browser("FIREFOX", "65", "WINDOWS_10_64");

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

            System.out.println("Clicking on Button");
            waitForElement(driver, "bn").click();

            System.out.println("Clicking on Checkbox");
            waitForElement(driver, "ck").click();

            System.out.println("Clicking on RadioButton");
            waitForElement(driver, "rd").click();

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

        } catch (Exception e) {
            driver.quit();
            throw e;
        }
    }
}


