package fsu.jportal.it.test;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Created by chi on 29.10.15.
 * @author Huu Chi Vu
 */
public class ACLEditorITCase extends BaseITCase {
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
        String accessPerm = "testCreate";
        inputText(By.id("acle2-new-access-id"), accessID);
        inputText(By.id("acle2-new-access-pool"), accessPerm);

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

        checkAlertPopup("Regel " + ruleDescTxt + " unter der ID SYSTEMRULE0000000007 erfolgreich hinzugefügt.");

        click(By.id("acle2-button-new-access"));

        checkAlertPopup("Regelzuweisung für " + accessID + " erfolgreich hinzugefügt.");

        checkSearchAccessRule(accessID, accessPerm);
    }

    @Test
    public void changeAccessID() throws Exception {
        String accessID = "changeAccessID";
        String accessPerm = "read";
        String accessIDchanged = "AccessIDchanged";
        WebElement[] accessRule = checkSearchAccessRule(accessID, accessPerm);
        WebElement accessIDElem = accessRule[0];
        //click edit button
        accessIDElem.findElement(By.xpath("./i")).click();
        inputText(By.cssSelector(".acle2-access-id > input"), accessIDchanged);
        DRIVER.findElement(By.cssSelector(".acle2-access-id > input")).sendKeys(Keys.ENTER);

        checkAlertPopup("Regelzuweisung erfolgreich geändert.");
        checkSearchAccessRule(accessIDchanged, accessPerm);
    }

    @Test
    public void changeAccessPermission() throws Exception {
        String accessID = "TestAccessID";
        String accessPerm = "changeAccessPermission";
        String accessPermchanged = "AccessPermissionChanged";
        WebElement[] accessRule = checkSearchAccessRule(accessID, accessPerm);
        WebElement accessPermElem = accessRule[1];
        //click edit button
        accessPermElem.findElement(By.xpath("./i")).click();
        inputText(By.cssSelector(".acle2-access-pool > input"), accessPermchanged);
        DRIVER.findElement(By.cssSelector(".acle2-access-pool > input")).sendKeys(Keys.ENTER);

        checkAlertPopup("Regelzuweisung erfolgreich geändert.");
        checkSearchAccessRule(accessID, accessPermchanged);
    }

    private void checkAlertPopup(String expected) {
        WebElement alertArea = getElementWaitTillVissible(By.id("acle2-alert-area"));
        assertEquals("didn't save - content does not match", expected, alertArea.getText());

        By alertDiv = By.xpath("//div[@id='acle2-alert-area' and @class='alert fade alert-success in']");
        waiting((long) 5.1).until(ExpectedConditions.invisibilityOfElementLocated(alertDiv));
    }

    private WebElement[] checkSearchAccessRule(String accessID, String accessPerm) {
        inputText(By.id("acle2-access-filter-input-id"), accessID);
        DRIVER.findElement(By.id("acle2-button-access-filter")).click();
        By elemAccessor = By.cssSelector(".acle2-table-access-entry");
        List<WebElement> elements = DRIVER.findElements(elemAccessor);
        for (WebElement element : elements) {
            if(element.isDisplayed()){
                WebElement accessIDElem = element.findElement(By.xpath(
                        "./td[@class = 'acle2-access-id acle2-table-access-entry-td']"));
                assertEquals("content does not match", accessID, accessIDElem.getText());
                WebElement accessPermElem = element.findElement(By.xpath(
                        "./td[@class = 'acle2-access-pool acle2-table-access-entry-td']"));
                assertEquals("content does not match", accessPerm, accessPermElem.getText());
                return new WebElement[] { accessIDElem, accessPermElem };
            }
        }
        fail("Could not found " + accessID + " # " + accessPerm);
        return new WebElement[0];
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
        WebElement inputField = DRIVER.findElement(element);
        inputField.click();
        inputField.clear();
        inputField.sendKeys(text);
    }
}
