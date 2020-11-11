package examples;

import com.testfabrik.webmate.javasdk.WebmateAPISession;
import com.testfabrik.webmate.javasdk.WebmateAuthInfo;
import com.testfabrik.webmate.javasdk.WebmateEnvironment;
import com.testfabrik.webmate.javasdk.devices.DeviceId;
import com.testfabrik.webmate.javasdk.devices.DevicePropertyName;
import com.testfabrik.webmate.javasdk.devices.DeviceRequest;
import com.testfabrik.webmate.javasdk.devices.DeviceRequirements;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static examples.MyCredentials.MY_WEBMATE_PROJECTID;

public class DeviceInteraction {
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
