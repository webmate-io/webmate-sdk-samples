package examples;

import com.testfabrik.webmate.javasdk.ProjectId;

import java.util.UUID;

public class MyCredentials {

     /**
     * Simply fill in the username, api key and project ID
     * Details on how to get your API Key and Project ID can be found in the official webmate documentation.
     *         https://docs.webmate.io/reference/generate-api-key/?searchTerm=api
     * There is no need to change the Selenium URL or the webmate API URI
     **/
    // fill in
    public static String WEBMATE_USERNAME = System.getProperty("wm_sdk_sample_user","xxxx@xxxxxxxxx.com");
    // fill in
    public static String WEBMATE_APIKEY = System.getProperty("wm_sdk_sample_apikey","xxxxxxxx-xxxx-4f6d-xxxx-xxxxxxxxxxxx");
    // fill in
    public static ProjectId WEBMATE_PROJECTID = new ProjectId(UUID.fromString(System.getProperty("wm_sdk_sample_projectId","xxxxxxxx-xxxx-xxxx-b4c5-xxxxxxxxxxxx")));

    /**
     * These are the credentials with which you can use the SDK with app.webmate.io (https://app.webmate.io)
     */
    public static String WEBMATE_SELENIUM_URL = System.getProperty("wm_sdk_sample_selenium","https://selenium.webmate.io/wd/hub");
    public static String WEBMATE_API_URI = System.getProperty("wm_sdk_sample_api","https://app.webmate.io/api/v1");

}
