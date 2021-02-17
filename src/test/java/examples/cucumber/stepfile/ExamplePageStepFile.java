package examples.cucumber.stepfile;

import com.testfabrik.webmate.javasdk.*;
import com.testfabrik.webmate.javasdk.testmgmt.TestRun;
import com.testfabrik.webmate.javasdk.testmgmt.TestRunEvaluationStatus;
import com.testfabrik.webmate.javasdk.testmgmt.TestSession;
import com.testfabrik.webmate.javasdk.testmgmt.spec.StoryCheckSpec;
import examples.MyCredentials;
import io.cucumber.java8.En;
import org.openqa.selenium.By;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import static examples.helpers.Helpers.waitForElement;
import static org.junit.Assert.assertEquals;

/**
 * This is example step file for the toy page: http://www.examplepage.org/form_interaction
 * The defined steps are referenced in the cucumber file example.feature.
 * The test executed on an iPhone XR in Safari. Make sure the device with the given specification is available in your
 * project. Otherwise customize the according capabilities.
 */
public class ExamplePageStepFile implements En {

    private static RemoteWebDriver driver;
    private static WebmateAPISession webmateSession;
    private static TestRun testRun;

    public ExamplePageStepFile() throws URISyntaxException {

        WebmateAuthInfo authInfo = new WebmateAuthInfo(MyCredentials.MY_WEBMATE_USERNAME, MyCredentials.MY_WEBMATE_APIKEY);
        webmateSession = new WebmateAPISession(authInfo, WebmateEnvironment.create(new URI("https://app.webmate.io/api/v1")),
                MyCredentials.MY_WEBMATE_PROJECTID);

        webmateSession.addTag(new Tag("GIT", "2020-09-02"));
        webmateSession.addTag(new Tag("Product", "Unfall"));
        webmateSession.addTag(new Tag("DBBackend", "red"));
        webmateSession.addTag(new Tag("Sprint", "21"));

        TestSession session = webmateSession.testMgmt.createTestSession("Example Test Session");
        webmateSession.addToTestSession(session.getId());

        DesiredCapabilities caps = new DesiredCapabilities();

        caps.setCapability("browserName", BrowserType.SAFARI);
        caps.setCapability("version", "14");
        caps.setCapability("platform", "iOS_14.2");
        caps.setCapability("model", "iPhone XR");

        caps.setCapability(WebmateCapabilityType.API_KEY, MyCredentials.MY_WEBMATE_APIKEY);
        caps.setCapability(WebmateCapabilityType.USERNAME, MyCredentials.MY_WEBMATE_USERNAME);
        caps.setCapability(WebmateCapabilityType.PROJECT, MyCredentials.MY_WEBMATE_PROJECTID.toString());

        caps.setCapability("wm:video", true);

        try {
            driver = new RemoteWebDriver(new URL(MyCredentials.WEBMATE_SELENIUM_URL), caps);
            webmateSession.addSeleniumSession(driver.getSessionId().toString());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        Given("the examplepage has been opened", () -> {
            testRun = webmateSession.testMgmt.startExecution(
                    StoryCheckSpec.StoryCheckBuilder.builder("testIfInteractionPageIsTestable"));

            driver.get("http://www.examplepage.org/form_interaction");
        });

        When("the user clicks on 'link click'", () -> {
            try {
                webmateSession.startAction("the user clicks on 'link click'");
                waitForElement(driver, By.id("lk")).click();
                assertEquals("Link Clicked!", waitForElement(driver, By.cssSelector(".success")).getText());
            } catch (Throwable e) {
                driver.quit();
                webmateSession.finishActionAsFailure(e.getMessage());
            } finally {
                webmateSession.finishAction();
            }
        });

        When("she clicks on 'button click'", () -> {
            try {
                webmateSession.startAction("she clicks on 'button click'");
                waitForElement(driver, By.id("bn")).click();
            } catch (Throwable e) {
                driver.quit();
            } finally {
                webmateSession.finishAction();
            }
        });

        When("she clicks on 'checkbox click'", () -> {
            try {
                webmateSession.startAction("she clicks on 'checkbox click'");
                waitForElement(driver, By.id("ck")).click();
            } catch (Throwable e) {
                driver.quit();
            } finally {
                webmateSession.finishAction();
            }
        });
        When("she enables the radio button", () -> {
            waitForElement(driver, By.id("rd")).click();
        });
        When("she activates 'hover me'", () -> {
            waitForElement(driver, By.id("mover")).click();
        });
        When("she enters input into the input field", () -> {
            waitForElement(driver, By.id("text-input")).click();
            waitForElement(driver, By.id("text-input")).sendKeys("hubba");
        });
        When("she enters input into the text area", () -> {
            waitForElement(driver, By.id("area")).click();
            waitForElement(driver, By.id("area")).sendKeys("hubba hub!");
        });
        Then("the test was successful", () -> {
            testRun.finish(TestRunEvaluationStatus.PASSED);
            driver.quit();
        });
    }
}
