package fsu.jportal.it.test;

import org.hamcrest.core.CombinableMatcher;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.io.InputStream;

import static org.hamcrest.CoreMatchers.both;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * Created by chi on 29.10.15.
 * @author Huu Chi Vu
 */
public class ACLEditorITCase extends BaseITCase {
    @BeforeClass
    public static void setUp() throws Exception {
        InputStream resourceAsStream = Class.class.getResourceAsStream("/IT/ACLEditor/ACLRules.txt");
        assertNotNull(resourceAsStream);
    }

    @Before
    public void gotoACLEditor() {
        DRIVER.findElement(By.linkText("Admin")).click();
        DRIVER.findElement(By.linkText("ACL-Editor")).click();

        assertEquals("content does not match", "Editor für Zugriffsrechte",
                DRIVER.findElement(By.xpath("//div[@id='acle2-header']/span/label")).getText());
    }

    @Test
    public void createNewAccessWithNewRule() throws Exception {
        String accessID = "testObject";
        inputText(By.id("acle2-new-access-id"), accessID);
        inputText(By.id("acle2-new-access-pool"), "testCreate");

        // click on new access -> rule drop down menu
        click(By.xpath("//th[@class='acle2-new-access-rule']/div/a[@class='select2-choice']"));
        // select create new rule
        click(By.cssSelector(".select2-results > li:last-child"));

        //create new rule from object menu
        By ruleDesc = By.id("acle2-new-rule-desc");
        assertEquals("Description should be empty.", DRIVER.findElement(ruleDesc).getAttribute("value"), "");
        String ruleDescTxt = "testAllwaysFalse";
        inputText(ruleDesc, ruleDescTxt);

        By ruleText = By.className("acle2-new-rule-text");
        assertEquals("Description should be empty.", DRIVER.findElement(ruleText).getAttribute("value"), "");
        inputText(ruleText, "false");

        click(By.id("acle2-new-rule-add"));

        checkAlertPopup("Regel " + ruleDescTxt + " unter der ID SYSTEMRULE0000000002 erfolgreich hinzugefügt.");

        click(By.id("acle2-button-new-access"));

        checkAlertPopup("Regelzuweisung für " + accessID + " erfolgreich hinzugefügt.");

        checkSearchAccessRule(accessID);
    }

    private void checkAlertPopup(String expected) {
        WebElement alertArea = getElementWaitTillVissible(By.id("acle2-alert-area"));
        assertEquals("didn't save - content does not match", expected, alertArea.getText());

        By alertDiv = By.xpath("//div[@id='acle2-alert-area' and @class='alert fade alert-success in']");
        waiting((long) 5.1).until(ExpectedConditions.invisibilityOfElementLocated(alertDiv));
    }

    @Test
    public void changeObjID() throws Exception {
        String accessID = "changeObjID";
        checkSearchAccessRule(accessID);
        DRIVER.findElement(By.cssSelector(".acle2-access-id > i")).click();
        DRIVER.findElement(By.cssSelector(".acle2-access-id > input")).sendKeys("testObject2");
        DRIVER.findElement(By.cssSelector(".acle2-access-id > input")).sendKeys(Keys.ENTER);

        checkAlertPopup("Regelzuweisung erfolgreich geändert.");
    }

    private void checkSearchAccessRule(String accessID) {
        WebElement searchInput = DRIVER.findElement(By.id("acle2-access-filter-input-id"));
        waiting(2).until(ExpectedConditions.visibilityOf(searchInput));
        searchInput.click();
        searchInput.clear();
        searchInput.sendKeys(accessID);
        DRIVER.findElement(By.id("acle2-button-access-filter")).click();

        assertEquals("content does not match", accessID,
                DRIVER.findElement(By.cssSelector(".acle2-access-id")).getText());
    }

    private WebElement getAlertPopup() {
        By alertDiv = By.xpath("//div[@id='acle2-alert-area' and @class='alert fade alert-success in']");
        WebElement alertArea = DRIVER.findElement(alertDiv);
        waiting(2).until(ExpectedConditions.visibilityOf(alertArea));
        return alertArea;
    }

    private WebElement getElementWaitTillVissible(By element) {
        WebElement alertArea = DRIVER.findElement(element);
        waiting(2).until(ExpectedConditions.visibilityOf(alertArea));
        return alertArea;
    }

    private void click(By element) {
        waiting(2).until(ExpectedConditions.elementToBeClickable(element));
        DRIVER.findElement(element).click();
    }

    private void inputText(By element, String text) {
        waiting(2).until(ExpectedConditions.elementToBeClickable(element));
        DRIVER.findElement(element).sendKeys(text);
    }
}
