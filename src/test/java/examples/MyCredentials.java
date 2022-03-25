package examples;

import com.testfabrik.webmate.javasdk.ProjectId;

import java.util.UUID;

public class MyCredentials {

    /**
     * These are the credentials with which you can use the SDK with the demo instance of Webmate (https://demo.webmate.io).
     * Simply fill in the username, api key and project ID
     * Details on how to get your API Key and Project ID can be found in the official webmate documentation.
     *         https://docs.webmate.io/reference/generate-api-key/?searchTerm=api
     * There is no need to change the Selenium URL or the Webmate API URI
     */
    public static String WEBMATE_SELENIUM_URL = "https://selenium-demo.webmate.io/wd/hub";
    // fill in
    public static String WEBMATE_USERNAME = "xxxx@xxxxxxxxx.com";
    // fill in
    public static String WEBMATE_APIKEY = "e0bb0aed-9211-4f6d-acd4-38ab49bf2f34";
    // fill in
    public static ProjectId WEBMATE_PROJECTID = new ProjectId(UUID.fromString("93174ef1-6299-4690-b4c5-dfbaf11c4fa4"));
    public static String WEBMATE_API_URI = "https://demo.webmate.io/api/v1";

}
