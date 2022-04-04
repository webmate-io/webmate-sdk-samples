package examples;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import com.testfabrik.webmate.javasdk.*;
import com.testfabrik.webmate.javasdk.devices.*;
import com.testfabrik.webmate.javasdk.packagemgmt.ImageType;
import com.testfabrik.webmate.javasdk.packagemgmt.Package;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import static examples.MyCredentials.WEBMATE_API_URI;
import static examples.MyCredentials.WEBMATE_PROJECTID;


/**
 * The device interaction test shows basic and advanced management of devices, such as deploying, releasing,
 * uploading apps, uploading images and configuring camera simulation.
 *
 * Note that the tests consequently releasing devices after requesting them to enable successive execution of the tests.
 * Otherwise at some point there were no more available devices to deploy.
 */
public class DeviceInteraction {
    private WebmateAPISession webmateSession;

    @Before
    public void setup() throws URISyntaxException {
        WebmateAuthInfo authInfo = new WebmateAuthInfo(MyCredentials.WEBMATE_USERNAME, MyCredentials.WEBMATE_APIKEY);
        webmateSession = new WebmateAPISession(
                authInfo,
                WebmateEnvironment.create(new URI(WEBMATE_API_URI)),
                WEBMATE_PROJECTID
        );

    }

    @After
    public void cleanup(){
        // release mobile devices - needs to be done, as only one device slot is available in demo session
        List<DeviceId> existingDevices = new ArrayList<>(webmateSession.device.getDeviceIdsForProject(MyCredentials.WEBMATE_PROJECTID));
        for (DeviceId id : existingDevices){
            if(webmateSession.device.getDeviceInfo(id).getProperties().containsKey("appium.port"))
                webmateSession.device.releaseDevice(id);
        }
    }

    @Test
    public void deployAndroidDevice() throws InterruptedException {
        // Count devices currently deployed
        List<DeviceId> existingDevices = new ArrayList<>(webmateSession.device.getDeviceIdsForProject(MyCredentials.WEBMATE_PROJECTID));
        int baseNumberDevices = existingDevices.size();
        Platform platform = new Platform(PlatformType.ANDROID, "11");
        DeviceDTO device = webmateSession.device.requestDeviceByRequirements(WEBMATE_PROJECTID,
                new DeviceRequest("Sample Device",
                        new DeviceRequirements(ImmutableMap.of(DevicePropertyName.Platform, platform.toString()))));
        // Check if device has been deployed
        Util.waitUntilEquals(() -> webmateSession.device.getDeviceIdsForProject(MyCredentials.WEBMATE_PROJECTID).size(), baseNumberDevices + 1, 60000);
        Thread.sleep(2000);
        System.out.println("Deployed Android device with id: " + device.getId());


    }

    @Test
    public void deviceTest() {
        // Count devices currently deployed
        List<DeviceId> existingDevices = new ArrayList<>(webmateSession.device.getDeviceIdsForProject(MyCredentials.WEBMATE_PROJECTID));
        int baseNumberDevices = existingDevices.size();
        System.out.println("Found existing devices " + existingDevices);

        // Request a Windows 11 device
        String windows10Request = "Win11 Request";
        Map<DevicePropertyName, Object> deviceProperties = new HashMap<>();
        Platform platform = new Platform(PlatformType.WINDOWS, "11", "64");
        deviceProperties.put(DevicePropertyName.Platform, platform.toString());
        DeviceRequirements deviceRequirements = new DeviceRequirements(deviceProperties);
        DeviceRequest deviceRequest = new DeviceRequest(windows10Request, deviceRequirements);
        webmateSession.device.requestDeviceByRequirements(MyCredentials.WEBMATE_PROJECTID, deviceRequest);

        // Check if device has been deployed
        Util.waitUntilEquals(() -> webmateSession.device.getDeviceIdsForProject(MyCredentials.WEBMATE_PROJECTID).size(), baseNumberDevices + 1, 60000);

        // Find id of new device
        List<DeviceId> newDevices = new ArrayList<>(webmateSession.device.getDeviceIdsForProject(MyCredentials.WEBMATE_PROJECTID));
        System.out.println("Successfully deployed device with id: " + newDevices);
        DeviceId newId = newDevices.stream().filter(id -> !existingDevices.contains(id)).findFirst().get();

        // Delete the new device again
        webmateSession.device.releaseDevice(newId);

        // Check if device has been deleted
        Util.waitUntilEquals(() -> webmateSession.device.getDeviceIdsForProject(MyCredentials.WEBMATE_PROJECTID).size(), baseNumberDevices, 60000);
        System.out.println("Successfully deleted device");
    }

    /**
     * This test uploads an APK into webmate, deploys a device in the project (check if a suitable slot is
     * available), and installs the uploaded app onto the device.
     */
    @Test
    public void installApp() throws IOException, InterruptedException {
        // Read apk from classpath
        byte[] apkData = Resources.toByteArray(this.getClass().getResource("sample.apk"));
        // Upload apk to webmate
        Package pkgInfo = webmateSession.packages.uploadApplicationPackage(WEBMATE_PROJECTID, apkData,
                "Example app", "apk");
        // Wait until the package has been processed and is available. We are working on this issue to remove the sleep
        // in the future.
        Thread.sleep(3000);

        System.out.println("App " + pkgInfo.getId() + " has been uploaded");

        // Deploy new device with Platform "Android_11"
        Platform platform = new Platform(PlatformType.ANDROID, "11");
        DeviceDTO newDevice = webmateSession.device.requestDeviceByRequirements(new DeviceRequest("Test Device",
                new DeviceRequirements(ImmutableMap.of(DevicePropertyName.Platform, platform.toString()))));

        System.out.println("Device " + newDevice.getId() + " has been deployed");

        // Wait until the device is ready. We are working on this issue to remove the sleep in the future.
        // Check if device has been deployed
        Thread.sleep(3000);

        // Install app on device
        webmateSession.device.installAppOnDevice(newDevice.getId(), pkgInfo.getId());
        System.out.println("App has been installed on device");

    }

    @Test
    public void uploadImagesAndUseForCameraSimulation() throws IOException, InterruptedException {
        // Deploy new device with Platform "Android_11"
        Platform platform = new Platform(PlatformType.ANDROID, "11");
        DeviceDTO newDevice = webmateSession.device.requestDeviceByRequirements(new DeviceRequest("Test Device",
                new DeviceRequirements(ImmutableMap.of(DevicePropertyName.Platform, platform.toString()))));
        Thread.sleep(3000);
        // Upload QR code image and push it to the device
        byte[] qrCode = Resources.toByteArray(this.getClass().getResource("qrcode.png"));
        webmateSession.device.uploadImageToDeviceAndSetForCameraSimulation(WEBMATE_PROJECTID, qrCode, "MyQRCode",
                ImageType.PNG, newDevice.getId());

    }

}
