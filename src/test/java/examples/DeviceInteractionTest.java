package examples;

import com.google.common.collect.ImmutableMap;
import com.testfabrik.webmate.javasdk.*;
import com.testfabrik.webmate.javasdk.devices.*;
import com.testfabrik.webmate.javasdk.packagemgmt.ImageType;
import com.testfabrik.webmate.javasdk.packagemgmt.Package;
import org.apache.commons.io.IOUtils;
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
 * Note that the tests consequently releasing devices after requesting them to enable successive execution of the tests.
 * Otherwise, at some point there were no more available devices to deploy.
 */
public class DeviceInteractionTest {
    private WebmateAPISession webmateSession;

    private DeviceDTO device;

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
        if (device != null) {
            webmateSession.device.releaseDevice(device.getId());
        }
    }

    @Test
    public void deployAndroidDevice() {
        Platform platform = new Platform(PlatformType.ANDROID, "11");
        device = webmateSession.device.requestDeviceByRequirements(WEBMATE_PROJECTID,
                new DeviceRequest("Sample Device",
                        new DeviceRequirements(ImmutableMap.of(DevicePropertyName.Platform, platform.toString()))));

        webmateSession.device.waitForDevice(device.getId());

        System.out.println("Deployed Android device with id: " + device.getId());
    }

    @Test
    public void deviceTest() {
        // Request a Windows 11 device
        String deviceName = "Win 11";

        Map<DevicePropertyName, Object> deviceProperties = new HashMap<>();
        Platform platform = new Platform(PlatformType.WINDOWS, "11", "64");
        deviceProperties.put(DevicePropertyName.Platform, platform.toString());
        DeviceRequirements deviceRequirements = new DeviceRequirements(deviceProperties);
        DeviceRequest deviceRequest = new DeviceRequest(deviceName, deviceRequirements);
        device = webmateSession.device.requestDeviceByRequirements(MyCredentials.WEBMATE_PROJECTID, deviceRequest);

        // Check if device has been deployed
        webmateSession.device.waitForDevice(device.getId());
        System.out.println("Successfully deployed device with id: " + device.getId());
    }

    /**
     * This test uploads an APK into webmate, deploys a device in the project (check if a suitable slot is
     * available), and installs the uploaded app onto the device.
     */
    @Test
    public void installApp() throws IOException {
        // Read apk from classpath
        byte[] apkData = IOUtils.toByteArray(Objects.requireNonNull(this.getClass().getResource("sample.apk")));

        // Upload apk to webmate
        Package pkgInfo = webmateSession.packages.uploadApplicationPackage(WEBMATE_PROJECTID, apkData,
                "Example app", "apk");

        pkgInfo = webmateSession.packages.waitForPackage(pkgInfo.getId());

        System.out.println("App " + pkgInfo.getId() + " has been uploaded");

        // Deploy new device with Platform "Android_11"
        Platform platform = new Platform(PlatformType.ANDROID, "11");
        device = webmateSession.device.requestDeviceByRequirements(new DeviceRequest("Test Device",
                new DeviceRequirements(ImmutableMap.of(DevicePropertyName.Platform, platform.toString()))));


        device = webmateSession.device.waitForDeviceForAppInstallation(device.getId());
        System.out.println("Device " + device.getId() + " has been deployed");

        // Install app on device
        webmateSession.device.installAppOnDevice(device.getId(), pkgInfo.getId());
        System.out.println("App has been installed on device");
    }

    @Test
    public void uploadImagesAndUseForCameraSimulation() throws IOException {
        // Deploy new device with Platform "Android_11"
        Platform platform = new Platform(PlatformType.ANDROID, "11");
        device = webmateSession.device.requestDeviceByRequirements(new DeviceRequest("Test Device",
                new DeviceRequirements(ImmutableMap.of(DevicePropertyName.Platform, platform.toString()))));

        device = webmateSession.device.waitForDevice(device.getId());

        // Upload QR code image and push it to the device
        byte[] qrCode = IOUtils.toByteArray(Objects.requireNonNull(this.getClass().getResource("qrcode.png")));
        webmateSession.device.uploadImageToDeviceAndSetForCameraSimulation(WEBMATE_PROJECTID, qrCode, "MyQRCode",
                ImageType.PNG, device.getId());

    }

}
