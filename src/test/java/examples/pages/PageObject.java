package examples.pages;

import org.openqa.selenium.remote.RemoteWebDriver;

public abstract class PageObject {

    private final RemoteWebDriver driver;

    public PageObject(RemoteWebDriver driver) {
        this.driver = driver;
    }

    public RemoteWebDriver getDriver() {
        return driver;
    }

}
