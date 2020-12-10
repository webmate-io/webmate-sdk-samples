package examples;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.ByteStreams;
import com.google.common.io.Resources;
import com.testfabrik.webmate.javasdk.WebmateAPISession;
import com.testfabrik.webmate.javasdk.WebmateAuthInfo;
import com.testfabrik.webmate.javasdk.WebmateEnvironment;
import com.testfabrik.webmate.javasdk.devices.*;
import com.testfabrik.webmate.javasdk.packagemgmt.Package;
import com.testfabrik.webmate.javasdk.packagemgmt.PackageId;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import static examples.MyCredentials.MY_WEBMATE_PROJECTID;

public class DeviceInteraction {
    private WebmateAPISession webmateSession;

    @Before
    public void setup() throws URISyntaxException {
        WebmateAuthInfo authInfo = new WebmateAuthInfo(MyCredentials.MY_WEBMATE_USERNAME, MyCredentials.MY_WEBMATE_APIKEY);
        webmateSession = new WebmateAPISession(
                authInfo,
//                WebmateEnvironment.create(),
                WebmateEnvironment.create(URI.create("http://localhost:8876/v1")),
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

    @Test
    public void deployAndroidDevice() {
        webmateSession.device.requestDeviceByRequirements(MY_WEBMATE_PROJECTID,
                new DeviceRequest("Sample Device",
                        new DeviceRequirements(ImmutableMap.of(DevicePropertyName.Platform, "Android_9"))));
    }

    /**
     * This test uploads an APK into webmate, deploys a device in the project (check if a suitable slot is
     * available), and installs the uploaded app onto the device.
     */
    @Test
    public void installApp() throws IOException {

        // read apk from classpath
        byte[] apkData = Resources.toByteArray(this.getClass().getResource("sample.apk"));

        // upload apk to webmate
        Package pkgInfo = webmateSession.packages.uploadApplicationPackage(MY_WEBMATE_PROJECTID, apkData,
                "Example app", "apk");

        System.out.println("App " + pkgInfo.getId() + " has been uploaded");

        // deploy new device with Platform "Android_10"
        DeviceDTO newDevice = webmateSession.device.requestDeviceByRequirements(new DeviceRequest("Test Device",
                new DeviceRequirements(ImmutableMap.of(DevicePropertyName.Platform, "Android_10"))));

        System.out.println("Device " + newDevice.getId() + " has been deployed");

        // install app on device
        webmateSession.device.installAppOnDevice(newDevice.getId(), pkgInfo.getId());
        System.out.println("App has been installed on device");

    }
}
