package examples;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import com.testfabrik.webmate.javasdk.*;
import com.testfabrik.webmate.javasdk.devices.*;
import com.testfabrik.webmate.javasdk.packagemgmt.ImageType;
import com.testfabrik.webmate.javasdk.packagemgmt.Package;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

import static examples.MyCredentials.MY_WEBMATE_PROJECTID;

public class DeviceInteraction {
    private WebmateAPISession webmateSession;

    @Before
    public void setup() {
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

    @Test
    public void deployAndroidDevice() {
        Platform platform = new Platform(PlatformType.ANDROID, "9");
        webmateSession.device.requestDeviceByRequirements(MY_WEBMATE_PROJECTID,
                new DeviceRequest("Sample Device",
                        new DeviceRequirements(ImmutableMap.of(DevicePropertyName.Platform, platform.toString()))));
    }

    /**
     * This test uploads an APK into webmate, deploys a device in the project (check if a suitable slot is
     * available), and installs the uploaded app onto the device.
     */
    @Test
    public void installApp() throws IOException {
        // Read apk from classpath
        byte[] apkData = Resources.toByteArray(this.getClass().getResource("sample.apk"));

        // Upload apk to webmate
        Package pkgInfo = webmateSession.packages.uploadApplicationPackage(MY_WEBMATE_PROJECTID, apkData,
                "Example app", "apk");

        System.out.println("App " + pkgInfo.getId() + " has been uploaded");

        // Deploy new device with Platform "Android_10"
        Platform platform = new Platform(PlatformType.ANDROID, "10");
        DeviceDTO newDevice = webmateSession.device.requestDeviceByRequirements(new DeviceRequest("Test Device",
                new DeviceRequirements(ImmutableMap.of(DevicePropertyName.Platform, platform.toString()))));

        System.out.println("Device " + newDevice.getId() + " has been deployed");

        // Install app on device
        webmateSession.device.installAppOnDevice(newDevice.getId(), pkgInfo.getId());
        System.out.println("App has been installed on device");
    }

    @Test
    public void uploadImagesAndUseForCameraSimulation() throws IOException {
        // Deploy new device with Platform "Android_10"
        Platform platform = new Platform(PlatformType.ANDROID, "10");
        DeviceDTO newDevice = webmateSession.device.requestDeviceByRequirements(new DeviceRequest("Test Device",
                new DeviceRequirements(ImmutableMap.of(DevicePropertyName.Platform, platform.toString()))));

        // Upload QR code image and push it to the device
        byte[] qrCode = Resources.toByteArray(this.getClass().getResource("qrcode.png"));
        webmateSession.device.uploadImageToDeviceAndSetForCameraSimulation(MY_WEBMATE_PROJECTID, qrCode, "MyQRCode",
                ImageType.PNG, newDevice.getId());
    }

}
