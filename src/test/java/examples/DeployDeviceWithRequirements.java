package examples;

import com.google.common.collect.ImmutableMap;
import com.testfabrik.webmate.javasdk.*;
import com.testfabrik.webmate.javasdk.devices.*;
import examples.helpers.Helpers;
import examples.helpers.WebElementFunction;
import io.appium.java_client.MobileBy;
import io.appium.java_client.android.AndroidDriver;
import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;

import static examples.MyCredentials.WEBMATE_API_URI;
import static examples.MyCredentials.WEBMATE_PROJECTID;

@RunWith(JUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DeployDeviceWithRequirements {
    private static WebmateAPISession webmateSession;
    private static DeviceDTO device;

    @Before
    public void setup() throws URISyntaxException {
        WebmateAuthInfo authInfo = new WebmateAuthInfo(MyCredentials.WEBMATE_USERNAME, MyCredentials.WEBMATE_APIKEY);
        webmateSession = new WebmateAPISession(
                authInfo,
                WebmateEnvironment.create(new URI(WEBMATE_API_URI)),
                WEBMATE_PROJECTID
        );
    }

    /**
     * This will deploy a device with the specified Operating System version.
     * For iOS, the full version including minor level is required. For Windows and macOS, specifying _64 is required.
     * Note that with macOS you also need to provide the name instead of the numeric version.
    **/
    @Test
    public void deployDeviceWithPlatformRequirement() throws IOException {
        device = webmateSession.device.requestDeviceByRequirements(WEBMATE_PROJECTID,
                new DeviceRequest("Sample Device",
                        new DeviceRequirements(
                                ImmutableMap.of(
                                        DevicePropertyName.Platform,  "Android_13",
                                        // DevicePropertyName.Platform,  "iOS_16.1.1",
                                        // DevicePropertyName.Platform,  "WINDOWS_11_64",
                                        // DevicePropertyName.Platform,  "MACOS_MONTEREY_64",
                                        DevicePropertyName.AutomationAvailable, true))));
    }

    /**
     * This will deploy a specific device model. Only available for iOS and Android.
     * To deploy a version specific macOS or Windows device, use the platform requirement above.
    **/
    @Test
    public void deployDeviceWithModelRequirement() throws IOException {
        device = webmateSession.device.requestDeviceByRequirements(WEBMATE_PROJECTID,
                new DeviceRequest("Sample Device",
                        new DeviceRequirements(
                                ImmutableMap.of(
                                        DevicePropertyName.Model,  "iPhone 11",
                                        // DevicePropertyName.Language,  "Galaxy Tab A8",
                                        DevicePropertyName.AutomationAvailable, true))));
    }


    /**
     * This will deploy a device with a specific browser version.
    **/
    @Test
    public void deployDeviceWithBrowserRequirement() throws IOException {
        device = webmateSession.device.requestDeviceByRequirements(WEBMATE_PROJECTID,
                new DeviceRequest("Sample Device",
                        new DeviceRequirements(
                                ImmutableMap.of(
                                        DevicePropertyName.Browsers,  ImmutableMap.of(
                                                "browserType", "safari",
                                                "version", "16"
                                        ),
                                        // DevicePropertyName.Browsers,  ImmutableMap.of(
                                        //        "browserType", "chrome",
                                        //        "version", "116"
                                        // ),
                                        DevicePropertyName.AutomationAvailable, true))));
    }

    /**
     * This will deploy a device with a specific language.
     * At this time, only Windows and macOS are supported.
    **/
    @Test
    public void deployDeviceWithLanguageRequirement() throws IOException {
        device = webmateSession.device.requestDeviceByRequirements(WEBMATE_PROJECTID,
                new DeviceRequest("Sample Device",
                        new DeviceRequirements(
                                ImmutableMap.of(
                                        DevicePropertyName.Language,  "en",
                                        // DevicePropertyName.Language,  "de",
                                        DevicePropertyName.AutomationAvailable, true))));
    }

    /**
     * Of course, it is possible to mix and match all the above requirements to get a device that fits your needs.
     * The following example will deploy a German Windows 11 device with Chrome 116.
    **/
    @Test
    public void deploySpecificDesktop() throws IOException {
        device = webmateSession.device.requestDeviceByRequirements(WEBMATE_PROJECTID,
                new DeviceRequest("Sample Device",
                        new DeviceRequirements(
                                ImmutableMap.of(
                                        DevicePropertyName.Language,  "de",
                                        DevicePropertyName.Browsers,  ImmutableMap.of(
                                                "browserType", "chrome",
                                                "version", "116"
                                        ),
                                        DevicePropertyName.Platform,  "WINDOWS_11_64",
                                        DevicePropertyName.AutomationAvailable, true))));
    }

    /**
     * You can also mix and match for mobile devices as well.
     * The following example will specifically deploy a Galaxy Tab A8 device with Chrome 120 and Android 12.
    **/
    @Test
    public void deploySpecificMobile() throws IOException {
        device = webmateSession.device.requestDeviceByRequirements(WEBMATE_PROJECTID,
                new DeviceRequest("Sample Device",
                        new DeviceRequirements(
                                ImmutableMap.of(
                                        DevicePropertyName.Model,  "Galaxy Tab A8",
                                        DevicePropertyName.Browsers,  ImmutableMap.of(
                                                "browserType", "chrome",
                                                "version", "120"
                                        ),
                                        DevicePropertyName.Platform,  "Android_12",
                                        DevicePropertyName.AutomationAvailable, true))));
    }
}

