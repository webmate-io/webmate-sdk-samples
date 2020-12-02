package examples;

import com.google.common.collect.ImmutableList;
import com.testfabrik.webmate.javasdk.*;
import com.testfabrik.webmate.javasdk.browsersession.*;
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
import java.net.URI;
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
    public void setup() throws URISyntaxException {
        WebmateAuthInfo authInfo = new WebmateAuthInfo(MyCredentials.MY_WEBMATE_USERNAME, MyCredentials.MY_WEBMATE_APIKEY);
        webmateSession = new WebmateAPISession(
                authInfo,
                WebmateEnvironment.create(),
                MY_WEBMATE_PROJECTID);
    }

    @Test
    public void performTest() throws MalformedURLException {
        executeTestInBrowser("CHROME", "83", "WINDOWS_10_64");
    }

    public BrowserSessionId executeTestInBrowser(String browserName, String browserVersion,
                                                 String browserPlatform) throws MalformedURLException {

        DesiredCapabilities caps = new DesiredCapabilities();
//        caps.setCapability("browserName", "CHROME");
        caps.setCapability("browserName", browserName);
        caps.setCapability("version", browserVersion);
        caps.setCapability("platform", browserPlatform);
        caps.setCapability("apikey", MY_WEBMATE_APIKEY_TFRED);
        caps.setCapability("email", MY_WEBMATE_USERNAME_TFRED);
        caps.setCapability("project", MY_WEBMATE_PROJECTID_TFRED.toString());
        caps.setCapability("wm:autoScreenshots", true);

        RemoteWebDriver driver = new RemoteWebDriver(new URL(WEBMATE_SELENIUM_URL), caps);
        webmateSession.addSeleniumSession(driver.getSessionId().toString());

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


