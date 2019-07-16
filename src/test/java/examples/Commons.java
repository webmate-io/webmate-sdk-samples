package examples;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Helper class representing a Selenium browser.
 */
abstract class Commons {
    protected static WebElement waitForElement(RemoteWebDriver driver, String element) {
        if (element.startsWith(".")) {

            WebDriverWait wait = new WebDriverWait(driver, 20);
            return wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(element)));
        } else {
            WebDriverWait wait = new WebDriverWait(driver, 20);
            return wait.until(ExpectedConditions.elementToBeClickable(By.id(element)));
        }
    }
}
