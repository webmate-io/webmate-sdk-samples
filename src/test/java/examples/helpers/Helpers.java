package examples.helpers;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.TouchAction;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.function.Supplier;

import static io.appium.java_client.touch.offset.PointOption.point;

@SuppressWarnings("unused")
public class Helpers {
    public static WebElement waitForElement(WebDriver driver, By element) {
        WebDriverWait wait = new WebDriverWait(driver, 10);
        return wait.until(ExpectedConditions.elementToBeClickable(element));
    }

    public static WebElement waitForElement(RemoteWebDriver driver, String element) {
        WebDriverWait wait = new WebDriverWait(driver, 20);

        if (element.startsWith(".")) {
            return wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(element)));
        } else {
            return wait.until(ExpectedConditions.elementToBeClickable(By.id(element)));
        }
    }

    public static WebElement waitForElementToBeVisible(WebDriver driver, By element) {
        WebDriverWait wait = new WebDriverWait(driver, 10);
        return wait.until(ExpectedConditions.visibilityOfElementLocated(element));
    }

    public static WebElement waitForElementToExist(WebDriver driver, By element) {
        WebDriverWait wait = new WebDriverWait(driver, 10);
        return wait.until(ExpectedConditions.presenceOfElementLocated(element));
    }

    public static void waitUntilEquals(Supplier<Integer> query, int expected, long timeoutMillis) {
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < timeoutMillis) {
            try {
                if (query.get() == expected) {
                    return;
                }
                //noinspection BusyWait
                Thread.sleep(500);
            } catch (Exception ignored) { }
        }

        Assert.fail("Condition not fulfilled after timeout");
    }

    public static void waitUntilStable(Supplier<Integer> query, long interval) {
        int initialValue = query.get();
        while (true) {
            try {
                //noinspection BusyWait
                Thread.sleep(interval);
                int newValue = query.get();
                if (newValue == initialValue) {
                    return;
                }
                initialValue = newValue;
            } catch (Exception ignored) { }
        }
    }

    public static boolean isInView(WebElementFunction f) {
            try {
                WebElement e = f.getElement();
                return e != null;
            } catch (NoSuchElementException e) {
                return false;
            }
        }

    public static void scrollDownUntilElementIsInView(AppiumDriver<?> driver, WebElementFunction f) {
            int i = 0;
            while (!isInView(f) || !f.getElement().isDisplayed()) {
                new TouchAction<>(driver).longPress(point(20, 500)).moveTo(point(20, 300)).release().perform();
                i++;
                if (i > 50) {
                    throw new IllegalStateException("Element is probably not in view");
                }
            }
        }
}
