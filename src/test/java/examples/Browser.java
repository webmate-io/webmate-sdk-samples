package examples;

/**
 * Helper class representing a Selenium browser.
 */
class Browser {
    public final String browserName;
    public final String browserVersion;
    public final String browserPlatform;

    public Browser(String browserName, String browserVersion, String browserPlatform) {
        this.browserName = browserName;
        this.browserVersion = browserVersion;
        this.browserPlatform = browserPlatform;
    }

    @Override
    public String toString() {
        return "[" + browserName + ", " + browserVersion + ", " + browserPlatform + "]";
    }
}
