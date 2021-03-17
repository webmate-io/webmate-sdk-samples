Feature: Click through example page

  Scenario: Click through page
    Given the examplepage has been opened
    When the user clicks on 'link click'
    Then 'Link Clicked!' text box should be visible
    And she clicks on 'button click'
    And she clicks on 'checkbox click'
    And she enables the radio button
    And she activates 'hover me'
    And she enters input into the input field
    And she enters input into the text area
    Then the test was successful
