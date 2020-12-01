package examples;

import com.google.common.collect.ImmutableList;
import com.testfabrik.webmate.javasdk.*;
import com.testfabrik.webmate.javasdk.Browser;
import com.testfabrik.webmate.javasdk.browsersession.ExpeditionSpecFactory;
import com.testfabrik.webmate.javasdk.testmgmt.*;
import com.testfabrik.webmate.javasdk.testmgmt.spec.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Collectors;

import static examples.MyCredentials.*;


/**
 * Simple test showing how to perform a cross browser test using webmate.
 */
@RunWith(JUnit4.class)
public class UrlBasedCrossbrowserTest extends Commons {

    private WebmateAPISession webmateSession;

    @Before
    public void setup() throws URISyntaxException {
        WebmateAuthInfo authInfo = new WebmateAuthInfo(MyCredentials.MY_WEBMATE_USERNAME, MyCredentials.MY_WEBMATE_APIKEY);
        webmateSession = new WebmateAPISession(
                authInfo,
                WebmateEnvironment.create(),
                MY_WEBMATE_PROJECTID
        );
    }

    @Test
    public void crossBrowserTest() throws InterruptedException {
        // Specify the reference browser
        Browser referenceBrowser = new Browser(BrowserType.Firefox, "81", new Platform(PlatformType.WINDOWS, "10", "64"));

        // Specify the browsers that should be compared to the reference browser
        List<Browser> crossBrowsers = ImmutableList.of(
                new Browser(BrowserType.Chrome, "86", new Platform(PlatformType.WINDOWS, "10", "64")),
                new Browser(BrowserType.InternetExplorer, "11", new Platform(PlatformType.WINDOWS, "10", "64"))
        );

        // TODO: do something with results

        // Specify the urls under test
        List<URI> urls = ImmutableList.of(
                URI.create("http://www.examplepage.org/version/future"),
                URI.create("http://www.examplepage.org")
        );

        TestRun testRun = webmateSession.testMgmt.startExecution(
                ExpeditionComparisonSpec.ExpeditionComparisonCheckBuilder.builder(
                        "CrossBrowser Test via SDK",
                        ExpeditionSpecFactory.makeUrlListExpeditionSpec(urls, referenceBrowser),
                        crossBrowsers.stream()
                                .map(browser -> ExpeditionSpecFactory.makeUrlListExpeditionSpec(urls, browser))
                                .collect(Collectors.toList())
                ).withTag("SDK").withTag("Release", "2020-11")
        );

        TestRunInfo info = testRun.waitForCompletion();

        System.out.println("Finished waiting for TestRun: " + info.toString());

        System.out.println("The result is available at: https://app.webmate.io/#/projects/" +
                info.getProjectId().toString() + "/testlab/testruns/" + info.getTestRunId());
    }
}


