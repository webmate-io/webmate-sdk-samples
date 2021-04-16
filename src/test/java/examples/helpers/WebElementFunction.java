package examples.helpers;

import org.openqa.selenium.WebElement;

@FunctionalInterface
public interface WebElementFunction {
    WebElement getElement();
}
