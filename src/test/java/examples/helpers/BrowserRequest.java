package examples.helpers;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.testfabrik.webmate.javasdk.Browser;
import com.testfabrik.webmate.javasdk.BrowserType;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BrowserRequest {

    private BrowserType browserType;
    private String version;
    private String platform;
    private JsonNode properties;
    public BrowserRequest(Browser browser) {
        this.browserType = browser.getBrowserType();
        this.version = browser.getVersion();
        this.platform =  browser.getPlatform().getPlatformType() + "_" + browser.getPlatform().getPlatformVersion() + "_" + browser.getPlatform().getPlatformArchitecture();
        this.properties = browser.getProperties();
    }

    public String getPlatform() {
        return platform;
    }

    public BrowserType getBrowserType() {
        return browserType;
    }

    public String getVersion() {
        return version;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public JsonNode getProperties() {
        return properties;
    }
}
