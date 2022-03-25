# webmate Java SDK Samples <img src="https://avatars.githubusercontent.com/u/13346605" alt="webmate logo" width="28"/>

This repository contains examples of how to use the [webmate Java SDK](https://github.com/webmate-io/webmate-sdk-java).

## Usage 

To run the examples, add your webmate credentials in `MyCredentials.java` and follow the instructions there. Then run `mvn test`.
If you don't already have webmate credentials, you can sign up for demo credentials here: https://demo.webmate.io/#/login

## Samples

<table border="1">
    <tr>
        <th>Test</th>
        <th>Frameworks</th>
    </tr>
    <tr>
        <td>
            <a href="./src/test/java/examples/cucumber/DemoCucumberTest.java">DemoCucumberTest</a>
        </td>
        <td>Cucumber, Selenium, JUnit</td>
    </tr>
    <tr>
        <td>
            <a href="./src/test/java/examples/DeviceInteraction.java">DeviceInteraction</a>
        </td>
        <td>JUnit</td>
    </tr>
    <tr>
        <td>
            <a href="./src/test/java/examples/SeleniumBasedCrossbrowserTest.java">SeleniumBasedCrossbrowserTest</a>
        </td>
        <td>Selenium, JUnit</td>
    </tr>
    <tr>
        <td>
            <a href="./src/test/java/examples/SeleniumBasedRegressionTest.java">SeleniumBasedRegressionTest</a>
        </td>
        <td>Selenium, JUnit</td>
    </tr>
    <tr>
        <td>
            <a href="./src/test/java/examples/SeleniumTest.java">SeleniumTest</a>
        </td>
        <td>Selenium, JUnit</td>
    </tr>
    <tr>
        <td>
            <a href="./src/test/java/examples/SeleniumTestWithActionRule.java">SeleniumTestWithActionRule</a>
        </td>
        <td>Selenium, JUnit</td>
    </tr>
    <tr>
        <td>
            <a href="./src/test/java/examples/SeleniumTestWithActionRuleAlternative.java">SeleniumTestWithActionRuleAlternative</a>
        </td>
        <td>Selenium, JUnit</td>
    </tr>
    <tr>
        <td>
            <a href="./src/test/java/examples/UrlBasedCrossbrowserTest.java">UrlBasedCrossbrowserTest</a>
        </td>
        <td>JUnit</td>
    </tr>
</table>

## webmate API

Although, the SDK provides a number of features and convenience wrappers it doesn't exhaust the full potential of the webmate API.
See the REST API [Swagger documentation](https://app.webmate.io/api/swagger) for a comprehensive summary of the webmate functionalities.
