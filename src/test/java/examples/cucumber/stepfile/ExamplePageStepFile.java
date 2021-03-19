package examples.cucumber.stepfile;

import com.testfabrik.webmate.javasdk.*;
import com.testfabrik.webmate.javasdk.browsersession.BrowserSessionRef;
import com.testfabrik.webmate.javasdk.testmgmt.TestRun;
import com.testfabrik.webmate.javasdk.testmgmt.TestRunEvaluationStatus;
import com.testfabrik.webmate.javasdk.testmgmt.TestSession;
import com.testfabrik.webmate.javasdk.testmgmt.spec.StoryCheckSpec;
import examples.MyCredentials;
import examples.pages.ExamplePageFormInteraction;
import io.cucumber.java8.En;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import static org.junit.Assert.assertEquals;

/**
 * This is example step file for the toy page: http://www.examplepage.org/form_interaction
 * The defined steps are referenced in the cucumber file example.feature.
 * The test executed on an iPhone XR in Safari. Make sure the device with the given specification is available in your
 * project. Otherwise customize the according capabilities.
 *
 * See the cucumber documentation for more information about cucumber hooks and keywords:
 * - https://cucumber.io/docs/gherkin/reference/
 * - https://cucumber.io/docs/cucumber/api/#hooks
 */
public class ExamplePageStepFile implements En {

    private static RemoteWebDriver driver;
    private static WebmateAPISession webmateSession;
    private static BrowserSessionRef browserSession;
    private static TestRun testRun;
    private static ExamplePageFormInteraction examplePage;

    private static void onFailure(Throwable e, boolean failedWebmateAction) throws Throwable {
        System.err.println("An error happened: " + e);
        e.printStackTrace();
        testRun.finish(TestRunEvaluationStatus.FAILED);
        if (failedWebmateAction) {
            webmateSession.finishActionAsFailure(e.toString());
        }
        throw e;
    }

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

        caps.setCapability(WebmateCapabilityType.ENABLE_VIDEO_RECORDING, true);

        Before(() -> {
            try {
                driver = new RemoteWebDriver(new URL(MyCredentials.WEBMATE_SELENIUM_URL), caps);
                examplePage = new ExamplePageFormInteraction(driver);
                webmateSession.addSeleniumSession(driver.getSessionId().toString());
                browserSession = webmateSession.browserSession
                        .getBrowserSessionForSeleniumSession(driver.getSessionId().toString());

                testRun = webmateSession.testMgmt.startExecutionWithBuilder(
                        StoryCheckSpec.StoryCheckBuilder.builder("testIfInteractionPageIsTestable"));
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        });

        Given("the examplepage has been opened", () -> {
            driver.get("http://www.examplepage.org/form_interaction");
        });

        When("the user clicks on 'link click'", () -> {
            try {
                webmateSession.startAction("the user clicks on 'link click'");
                examplePage.clickLink();
                webmateSession.finishAction();
            } catch (Throwable e) {
                onFailure(e, true);
            }
        });

        Then("{string} text box should be visible", (String msg) -> {
            String sucText = examplePage.getSuccessBoxText();
            assertEquals(msg, sucText);
        });

        When("she clicks on 'button click'", () -> {
            try {
                webmateSession.startAction("she clicks on 'button click'");
                examplePage.clickButtonClick();
                webmateSession.finishAction();
            } catch (Throwable e) {
                onFailure(e, true);
            }
        });

        When("she clicks on 'checkbox click'", () -> {
            try {
                webmateSession.startAction("she clicks on 'checkbox click'");
                examplePage.clickCheckboxClick();
                webmateSession.finishAction();
            } catch (Throwable e) {
                onFailure(e, true);
            }
        });

        When("she enables the radio button", () -> {
            try {
                examplePage.clickRadioButton();
            } catch (Throwable e) {
                onFailure(e, false);
            }
        });

        When("she activates 'hover me'", () -> {
            try {
                examplePage.clickHoverMe();
            } catch (Exception e) {
                onFailure(e, false);
            }
        });

        When("she enters input into the input field", () -> {
            try {
                examplePage.enterTextIntoInput("Test test");
            } catch (Exception e) {
                onFailure(e, false);
            }
        });

        When("she enters input into the text area", () -> {
            try {
                examplePage.enterTextIntoTextArea("Here some more test");
            } catch (Exception e) {
                onFailure(e, false);
            }
        });

        Then("the test was successful", () -> {
            testRun.finish(TestRunEvaluationStatus.PASSED);
        });

        After(() -> {
            driver.quit();
        });

    }
}
