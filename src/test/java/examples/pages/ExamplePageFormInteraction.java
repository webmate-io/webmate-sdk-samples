package examples.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.remote.RemoteWebDriver;

import static examples.helpers.Helpers.waitForElement;

public class ExamplePageFormInteraction extends PageObject {

    public ExamplePageFormInteraction(RemoteWebDriver driver) {
        super(driver);
    }

    public void clickLink() {
        waitForElement(getDriver(), By.id("lk")).click();
    }

    public String getSuccessBoxText() {
        return waitForElement(getDriver(), By.cssSelector(".success")).getText();
    }

    public void clickButtonClick() {
        waitForElement(getDriver(), By.id("bn")).click();
    }

    public void clickCheckboxClick() {
        waitForElement(getDriver(), By.id("ck")).click();
    }

    public void clickRadioButton() {
        waitForElement(getDriver(), By.id("rd")).click();
    }

    public void clickHoverMe() {
        waitForElement(getDriver(), By.id("mover")).click();
    }

    public void enterTextIntoInput(String msg) {
        waitForElement(getDriver(), By.id("text-input")).click();
        waitForElement(getDriver(), By.id("text-input")).sendKeys(msg);
    }

    public void enterTextIntoTextArea(String msg) {
        waitForElement(getDriver(), By.id("area")).click();
        waitForElement(getDriver(), By.id("area")).sendKeys(msg);
    }

}
