package examples;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.testfabrik.webmate.javasdk.*;
import com.testfabrik.webmate.javasdk.Browser;
import com.testfabrik.webmate.javasdk.devices.DeviceId;
import com.testfabrik.webmate.javasdk.devices.DevicePropertyName;
import com.testfabrik.webmate.javasdk.devices.DeviceRequest;
import com.testfabrik.webmate.javasdk.devices.DeviceRequirements;
import com.testfabrik.webmate.javasdk.testmgmt.*;
import com.testfabrik.webmate.javasdk.testmgmt.spec.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.fail;


/**
 * Simple test showing how to perform a cross browser test using webmate.
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
    public void crossBrowserTest() {
        // Specify the reference browser
        Browser referenceBrowser = new Browser(BrowserType.Firefox, "61", new Platform("windows", "10"));
        BrowserSpecification referenceBrowserSpecification = new BrowserSpecification(referenceBrowser);
        // Specify the browsers that should be compared to the reference browser
        List<BrowserSpecification> crossBrowserSpecifications = ImmutableList.of(
                new BrowserSpecification(new Browser(BrowserType.Chrome, "67", new Platform("windows", "10")))
        );

        // Specify the urls under test
        List<URI> urls = ImmutableList.of(URI.create("http://www.examplepage.org/version/future"));
        List<Tag> tags = ImmutableList.of(new Tag("SDK"));
        TestExecutionSpec testExecutionSpec = new ExpeditionComparisonSpec("CrossBrowser Test via SDK",
                tags, referenceBrowserSpecification, crossBrowserSpecifications, urls);
        CreateTestExecutionResponse createTestExecutionResponse = webmateSession.testMgmt.startExecution(testExecutionSpec, MyCredentials.MY_WEBMATE_PROJECTID);
        TestRunId testRunId = createTestExecutionResponse.optTestRunId.get();
        TestExecutionId testExecutionId = createTestExecutionResponse.executionId;
        TestExecutionSummary testExecutionSummary = webmateSession.testMgmt.getTestExecutionSummary(testExecutionId);
        try {
            Thread.sleep(60 * 1000);
            while (testExecutionSummary.getExecutionStatus() == TestExecutionExecutionStatus.Active) {
                Thread.sleep(20 * 1000);
                testExecutionSummary = webmateSession.testMgmt.getTestExecutionSummary(testExecutionId);
            }
        } catch (Exception e) {
            System.err.println("An error occurred while waiting for test results");
            e.printStackTrace();
            fail();
        }

        System.out.println("Test execution status is: " + testExecutionSummary.getExecutionStatus().getValue());

        Optional<List<TestResult>> testResults = webmateSession.testMgmt.getTestResults(testRunId);
        if (testResults.isPresent()) {
            System.out.println("Got " + testResults.get().size() + " test results");
            for (TestResult result : testResults.get()) {
                System.out.println(result);
            }
            System.out.println();
        }
    }


    @Test
    public void deviceTest() {
        // Count devices currently deployed
        List<DeviceId> existingDevices = new ArrayList<>(webmateSession.device.getDeviceIdsForProject(MyCredentials.MY_WEBMATE_PROJECTID));
        int baseNumberDevices = existingDevices.size();
        System.out.println("Found existing devices " + existingDevices);

        // Request a Windows 10 device
        String windows10Request = "Win10 Request";
        Map<DevicePropertyName, Object> deviceProperties = new HashMap<>();
        deviceProperties.put(DevicePropertyName.Platform, "WINDOWS_10_64");
        DeviceRequirements deviceRequirements = new DeviceRequirements(deviceProperties);
        DeviceRequest deviceRequest = new DeviceRequest(windows10Request, deviceRequirements);
        webmateSession.device.requestDeviceByRequirements(MyCredentials.MY_WEBMATE_PROJECTID, deviceRequest);

        // Check if device has been deployed
        Util.waitUntilEquals(() -> webmateSession.device.getDeviceIdsForProject(MyCredentials.MY_WEBMATE_PROJECTID).size(), baseNumberDevices + 1, 60000);

        // Find id of new device
        List<DeviceId> newDevices = new ArrayList<>(webmateSession.device.getDeviceIdsForProject(MyCredentials.MY_WEBMATE_PROJECTID));
        System.out.println("Currently deployed devices: " + newDevices);
        DeviceId newId = newDevices.stream().filter(id -> !existingDevices.contains(id)).findFirst().get();

        // Delete device
        System.out.println("Going to delete device " + newId);
        webmateSession.device.releaseDevice(newId);
        System.out.println("Deleting...");

        // Check if device has been deleted
        Util.waitUntilEquals(() -> webmateSession.device.getDeviceIdsForProject(MyCredentials.MY_WEBMATE_PROJECTID).size(), baseNumberDevices, 60000);
        System.out.println("Successfully deleted device");
    }

}


