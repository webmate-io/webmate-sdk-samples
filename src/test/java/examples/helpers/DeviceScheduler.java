package examples.helpers;

import com.testfabrik.webmate.javasdk.*;
import com.testfabrik.webmate.javasdk.devices.*;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import org.joda.time.Duration;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.WebDriver;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static examples.MyCredentials.*;

public class DeviceScheduler {

    public enum DriverType {
        REMOTE,
        ANDROID,
        IOS
    }

    private WebmateAPISession webmateSession;
    private DeviceDTO deviceDTO;

    public DeviceScheduler(WebmateAPISession webmateSession) {
        this.webmateSession = webmateSession;
    }

    public WebDriver scheduleDevice(String deviceName, DesiredCapabilities capabilities, DriverType driverType, Duration maxRetryTime) throws InterruptedException, MalformedURLException {
        waitForRunningDevice(deviceName, capabilities, maxRetryTime);
        return selectDriver(deviceName, capabilities, driverType);
    }

    private void waitForRunningDevice(String deviceName, DesiredCapabilities capabilities, Duration maxRetryTime) throws InterruptedException {
        long endTime = System.currentTimeMillis() + maxRetryTime.getMillis();
        String deviceState = "";

        Map<DevicePropertyName, Object> requirements = buildDeviceRequirements(capabilities);
        DeviceRequest request = new DeviceRequest(deviceName, new DeviceRequirements(requirements));

        while ((deviceDTO == null || !deviceState.equals("running")) && System.currentTimeMillis() < endTime) {
            try {
                System.out.println("Sending Request for Device");
                deviceDTO = webmateSession.device.requestDeviceByRequirements(request);
                while (deviceDTO == null || !deviceState.equals("running")) {
                    TimeUnit.MINUTES.sleep(1); // Wait for a minute before retrying
                    if (deviceDTO != null) {
                        deviceState = webmateSession.device.getDeviceInfo(deviceDTO.getId()).getState();
                    }
                }
            } catch (Exception e) {
                System.out.println("Could not get device, will retry ...");
                TimeUnit.MINUTES.sleep(1); // Wait for a minute before retrying after an exception
            }
        }

        if (deviceDTO == null || !deviceState.equals("running")) {
            throw new IllegalStateException("Was not able to schedule Device for capabilities " + capabilities + ".\n" +
                    "This can have multiple reasons, for example there was no free slot in the Project or" +
                    " the Project can't satisfy your device requirements.");
        }
    }

    private WebDriver selectDriver(String deviceName, DesiredCapabilities capabilities, DriverType driverType) throws MalformedURLException {
        capabilities.setCapability("wm:slot", deviceDTO.getSlot().getValueAsString());

        switch (driverType) {
            case IOS:
                return new IOSDriver(new URL(WEBMATE_SELENIUM_URL), capabilities);
            case ANDROID:
                return new AndroidDriver(new URL(WEBMATE_SELENIUM_URL), capabilities);
            default:
                return new RemoteWebDriver(new URL(WEBMATE_SELENIUM_URL), capabilities);
        }
    }

    private Map<DevicePropertyName, Object> buildDeviceRequirements(DesiredCapabilities capabilities) {
        Map<DevicePropertyName, Object> requirements = new HashMap<>();
        requirements.put(DevicePropertyName.AutomationAvailable, true);

        String platform = (String) capabilities.getCapability("platformName");
        if (platform != null) {
            requirements.put(DevicePropertyName.Platform, platform);
        }

        String model = (String) capabilities.getCapability("model");
        if (model != null) {
            requirements.put(DevicePropertyName.Model, model);
        }

        String browserVersion = (String) capabilities.getCapability("browserVersion");
        String browserName = (String) capabilities.getCapability("browserName");
        if (browserVersion != null || browserName != null) {
            Map<String, Object> browserCapabilities = new HashMap<>();
            if (browserVersion != null)
                browserCapabilities.put("version", browserVersion);
            if (browserName != null)
                browserCapabilities.put("browserType", browserName);
            requirements.put(DevicePropertyName.Browsers, browserCapabilities);
        }
        return requirements;
    }

    public void releaseDevice() {
        webmateSession.device.releaseDevice(deviceDTO.getId());
    }
}