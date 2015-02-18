package fsu.jportal.it.test;

//import junit.framework.TestCase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.Keys;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.both;

import fsu.jportal.it.BaseIntegrationTest;
import fsu.jportal.it.TestUtils;

public class AdminITCase extends BaseIntegrationTest {
	WebDriverWait wait = new WebDriverWait(DRIVER, 2);
	
	@Test
	public void aclEditor() throws Exception {
//		test ACL-Editor 
		TestUtils.home(DRIVER);
		TestUtils.login(DRIVER);
		DRIVER.findElement(By.linkText("Admin")).click();
		DRIVER.findElement(By.linkText("ACL-Editor")).click();
		
	  assertEquals("content does not match", "Editor für Zugriffsrechte", DRIVER
	  		.findElement(By.xpath("//div[@id='acle2-header']/span/label")).getText());
		
		//create new object
		By waitForLoad = By.id("acle2-new-access-id");
	  wait.until(ExpectedConditions.elementToBeClickable(waitForLoad));
	  DRIVER.findElement(waitForLoad).sendKeys("testObject");
		DRIVER.findElement(By.id("acle2-new-access-pool")).sendKeys("testCreate");
		DRIVER.findElement(By.xpath("//th[@class='acle2-new-access-rule']/div/a[@class='select2-choice']")).click();
		
		By newRule = By.cssSelector(".select2-results > li:last-child");
	  wait.until(ExpectedConditions.elementToBeClickable(newRule));
	  DRIVER.findElement(newRule).click();
		
		//create new rule from object menu
	  By rule = By.id("acle2-new-rule-desc");
	  wait.until(ExpectedConditions.elementToBeClickable(rule));
	  DRIVER.findElement(rule).sendKeys("testAllwaysFalse");
	  
	  DRIVER.findElement(By.cssSelector(".acle2-new-rule-text")).sendKeys("false");
	  DRIVER.findElement(By.id("acle2-new-rule-add")).click();

	  assertThat(DRIVER.findElement(By.id("acle2-alert-area")).getText(), both(containsString("testAllwaysFalse")).and(containsString("erfolgreich hinzugefügt")));

	  DRIVER.findElement(By.id("acle2-button-new-access")).click();
	  
	  assertEquals("didn't save - content does not match", "Regelzuweisung für testObject erfolgreich hinzugefügt.", DRIVER
	  		.findElement(By.id("acle2-alert-area")).getText());
	  
	  //search for it 
	  DRIVER.findElement(By.id("acle2-access-filter-input-id")).sendKeys("testObject");
	  DRIVER.findElement(By.id("acle2-button-access-filter")).click();
	  
	  assertEquals("content does not match", "testObject", DRIVER
	  		.findElement(By.cssSelector(".acle2-access-id")).getText());
	  
	  //change objID
	  DRIVER.findElement(By.cssSelector(".acle2-access-id > i")).click();
	  DRIVER.findElement(By.cssSelector(".acle2-access-id > input")).sendKeys("testObject2");
	  DRIVER.findElement(By.cssSelector(".acle2-access-id > input")).sendKeys(Keys.ENTER);
	  
	  assertEquals("didn't save - content does not match", "Regelzuweisung erfolgreich geändert.", DRIVER
	  		.findElement(By.id("acle2-alert-area")).getText());
	  
		//change access right
	  DRIVER.findElement(By.cssSelector(".acle2-access-pool > i")).click();
	  DRIVER.findElement(By.cssSelector(".acle2-access-pool > input")).sendKeys("testCreate2");
	  DRIVER.findElement(By.cssSelector(".acle2-access-pool > input")).sendKeys(Keys.ENTER);
	  
	  assertEquals("didn't save - content does not match", "Regelzuweisung erfolgreich geändert.", DRIVER
	  		.findElement(By.id("acle2-alert-area")).getText());
	  
	  //change rule
	  DRIVER.findElement(By.xpath("//td[@class='acle2-access-rule-parent']/div/a[@class='select2-choice']")).click();
	  DRIVER.findElement(By.cssSelector(".select2-results > li:first-child")).click();
	  
	  assertEquals("didn't save - content does not match", "Regelzuweisung erfolgreich geändert.", DRIVER
	  		.findElement(By.id("acle2-alert-area")).getText());
	  
	  //create second obj
	  DRIVER.findElement(By.id("acle2-new-access-id")).sendKeys("testObject");
		DRIVER.findElement(By.id("acle2-new-access-pool")).sendKeys("testCreate");
		DRIVER.findElement(By.xpath("//th[@class='acle2-new-access-rule']/div/a[@class='select2-choice']")).click();
		By secObj = By.cssSelector(".select2-results > li:nth-child(2)");
	  wait.until(ExpectedConditions.elementToBeClickable(secObj));
	  DRIVER.findElement(secObj).click();
		DRIVER.findElement(By.id("acle2-button-new-access")).click();
		
		//change multiple obj rule
		DRIVER.findElement(By.id("acle2-button-select-multi-access")).click();
		DRIVER.findElement(By.id("acle2-button-edit-multi-access")).click();
		
	  By multiAccess = By.xpath("//h4[@id='acle2-lightbox-multi-edit-label']/label");
	  wait.until(ExpectedConditions.elementToBeClickable(multiAccess));
		
		assertEquals("content does not match", "Mehrere Regelzuweisungen bearbeiten", DRIVER
	  		.findElement(multiAccess).getText());
		
		DRIVER.findElement(By.xpath("//div[@id='acle2-lightbox-multi-edit-select']/div/a[@class='select2-choice']")).click();
		DRIVER.findElement(By.cssSelector(".select2-results > li:nth-child(2)")).click();
		DRIVER.findElement(By.id("acle2-lightbox-multi-edit-edit")).click();
		
		assertEquals("didn't save - content does not match", "Regelzuweisung erfolgreich geändert.", DRIVER
	  		.findElement(By.id("acle2-alert-area")).getText());
		
	  //delet obj
	  DRIVER.findElement(By.id("acle2-button-select-multi-access")).click();
	  DRIVER.findElement(By.id("acle2-button-select-multi-access")).click();
	  DRIVER.findElement(By.id("acle2-button-remove-multi-access")).click();
	  
	  By deletButton = By.id("acle2-lightbox-multi-delete-delete");
	  wait.until(ExpectedConditions.elementToBeClickable(deletButton));
	  DRIVER.findElement(deletButton).click();
		
	  assertEquals("didn't save - content does not match", "Makierte Regelzuweisungen erfolgreich gelöscht.", DRIVER
	  		.findElement(By.id("acle2-alert-area")).getText());
	  
	  //go to rule section and creat here new rule
	  DRIVER.findElement(By.id("acle2-rules-tab")).click();
	  DRIVER.findElement(By.cssSelector("#acle2-rule-list > li:first-child")).click();
	  DRIVER.findElement(By.id("acle2-rule-detail-ruleDesc")).sendKeys("testRule");
	  DRIVER.findElement(By.cssSelector(".acle2-rule-detail-table > dd > .acle2-rule-detail-ruleText")).sendKeys("false");
	  DRIVER.findElement(By.id("acle2-button-save-rule")).click();
	  
	  assertThat(DRIVER.findElement(By.id("acle2-alert-area")).getText(), both(containsString("testRule")).and(containsString("erfolgreich hinzugefügt")));
	  
	  //delet test rules
	  DRIVER.findElement(By.xpath("//li[@ruledesc='testAllwaysFalse']")).click();
	  DRIVER.findElement(By.id("acle2-button-delete-rule")).click();
	  
	  assertThat(DRIVER.findElement(By.id("acle2-alert-area")).getText(), containsString("erfolgreich gelöscht"));

	  DRIVER.findElement(By.xpath("//li[@ruledesc='testRule']")).click();
	  DRIVER.findElement(By.id("acle2-button-delete-rule")).click();
	  
	  assertThat(DRIVER.findElement(By.id("acle2-alert-area")).getText(), containsString("erfolgreich gelöscht"));
	  
	  //acl test finished
	  TestUtils.home(DRIVER);
		TestUtils.logout(DRIVER);
	}
	
	@Test
	public void classificationEditor() throws Exception {
		TestUtils.home(DRIVER);
		TestUtils.login(DRIVER);
		DRIVER.findElement(By.linkText("Admin")).click();
		DRIVER.findElement(By.linkText("Klassifikations Editor")).click();
		
		By loadClassEdt = By.xpath("//div[@id='dijit__TreeNode_0']/div/span[1]");
	  wait.until(ExpectedConditions.elementToBeClickable(loadClassEdt));
	  DRIVER.findElement(loadClassEdt).click();
	  
		DRIVER.findElement(By.id("dijit_form_Button_12")).click();
		DRIVER.findElement(By.xpath("//div[@id='dijit__TreeNode_0']/div/span[1]")).click();
		DRIVER.findElement(By.xpath("//div[@id='dijit__TreeNode_0']/div/span[1]")).click();
		
		loadClassEdt = By.cssSelector(".dijitTreeNodeContainer > div:first-child > div > .dijitTreeContent");
	  wait.until(ExpectedConditions.elementToBeClickable(loadClassEdt));
	  DRIVER.findElement(loadClassEdt).click();
	  
	  assertEquals("content does not match", "", DRIVER.findElement(By.id("dijit_form_TextBox_3")).getText());
	  DRIVER.findElement(By.id("dijit_form_TextBox_3")).sendKeys("testClassification");
	  assertEquals("content does not match", "", DRIVER.findElement(By.id("dijit_form_TextBox_4")).getText());
	  DRIVER.findElement(By.id("dijit_form_TextBox_4")).sendKeys("testClassification");
	  DRIVER.findElement(By.id("dijit_form_ValidationTextBox_1")).clear();
	  DRIVER.findElement(By.id("dijit_form_ValidationTextBox_1")).sendKeys("testClassification");
	
	  //save and accept alert dialog
	  DRIVER.findElement(By.id("dijit_form_Button_5")).click();
	  Thread.sleep(500);
	  Alert myAlert = DRIVER.switchTo().alert();
		assertEquals("save failed", "Speichern erfolgreich", myAlert.getText());
		myAlert.accept();
		
		DRIVER.navigate().refresh();
		//delete group
		loadClassEdt = By.cssSelector(".dijitTreeNodeContainer > div:last-child > div > .dijitTreeContent > .dijitTreeLabel");
	  wait.until(ExpectedConditions.elementToBeClickable(loadClassEdt));
		assertThat(DRIVER.findElement(loadClassEdt).getText(), containsString("testClassification"));
		DRIVER.findElement(By.cssSelector(".dijitTreeNodeContainer > div:last-child > div > .dijitTreeContent")).click();
		DRIVER.findElement(By.id("dijit_form_Button_13")).click();
		
	  DRIVER.findElement(By.id("dijit_form_Button_5")).click();
	  Thread.sleep(500);
	  myAlert = DRIVER.switchTo().alert();
		assertEquals("save failed", "Speichern erfolgreich", myAlert.getText());
		myAlert.accept();
		
	  TestUtils.home(DRIVER);
		TestUtils.logout(DRIVER);
	}
	
	@Test
	public void user() throws Exception {
		//test user 
		TestUtils.home(DRIVER);
		TestUtils.login(DRIVER);
		DRIVER.findElement(By.linkText("Admin")).click();
		DRIVER.findElement(By.linkText("Nutzer anlegen")).click();
		
	  assertEquals("content does not match", "Neuen Nutzer anlegen", DRIVER
	  		.findElement(By.xpath("//h3[@class='panel-title']")).getText());
		
	  DRIVER.findElement(By.id("userName")).sendKeys("testUser");
	  DRIVER.findElement(By.id("password")).sendKeys("test");
	  DRIVER.findElement(By.id("password2")).sendKeys("test");
	  DRIVER.findElement(By.id("hint")).sendKeys("test");
	  DRIVER.findElement(By.id("realName")).sendKeys("reallyTest");
	  DRIVER.findElement(By.id("email")).sendKeys("test@test.test");
	  DRIVER.findElement(By.id("validUntil")).sendKeys("2020-10-10");
	  
	  //owner select
	  DRIVER.findElement(By.cssSelector(".panel-body > div:nth-last-child(2) .btn-primary")).click();
	  DRIVER.findElement(By.linkText("administrator")).click();
	  
	  //role selectcd
	  DRIVER.findElement(By.cssSelector(".panel-body > div:last-child .btn-primary")).click();
	  DRIVER.findElement(By.linkText("The superuser role")).click();
	  
	  //save
	  DRIVER.findElement(By.name("_xed_submit_servlet:MCRUserServlet")).click();
	  
	  assertEquals("didn't save - content does not match", "Nutzerdaten anzeigen:testUser", DRIVER
		.findElement(By.xpath("//div[@class='user-details']/h2")).getText());
	  
	  //search user
	  TestUtils.home(DRIVER);
		DRIVER.findElement(By.linkText("Admin")).click();
		DRIVER.findElement(By.linkText("Suchen und verwalten")).click();
		DRIVER.findElement(By.xpath("//input[@name='search']")).sendKeys("testUser");
		DRIVER.findElement(By.xpath("//input[@name='search']")).sendKeys(Keys.ENTER);
		DRIVER.findElement(By.linkText("testUser")).click();
	  
	  //change data
		DRIVER.findElement(By.linkText("Daten ändern")).click();
		DRIVER.findElement(By.id("realName")).clear();
		DRIVER.findElement(By.id("realName")).sendKeys("changeTest");
		DRIVER.findElement(By.name("_xed_submit_servlet:MCRUserServlet")).click();
		
	  assertEquals("didn't save - content does not match", "changeTest", DRIVER
		.findElement(By.xpath("//table[@class='user table']/tbody/tr[5]/td")).getText());
	  
	  //change pw
	  DRIVER.findElement(By.linkText("Passwort ändern")).click();
	  
	  assertEquals("content does not match", "Passwort ändern", DRIVER
		.findElement(By.xpath("//h3[@class='panel-title']")).getText());
	  
	  DRIVER.findElement(By.id("password")).sendKeys("teste");
	  DRIVER.findElement(By.id("password2")).sendKeys("teste");
	  DRIVER.findElement(By.name("_xed_submit_servlet:MCRUserServlet")).click();
	  
	  assertEquals("didn't save - content does not match", "Das Passwort dieser Nutzerkennung wurde erfolgreich geändert.", DRIVER
		.findElement(By.cssSelector(".user-details > .alert > p > strong")).getText());
	  
	  //delete user
	  DRIVER.findElement(By.linkText("Nutzer löschen")).click();
	  
	  assertEquals("content does not match", "Sind Sie sicher, dass Sie diesen Nutzer löschen wollen?", DRIVER
		.findElement(By.cssSelector(".user-details > .alert > p > strong")).getText());
	  
	  DRIVER.findElement(By.xpath("//input[@value='Ja, löschen!']")).click();
	  
	  assertEquals("didn't delete - content does not match", "Die Nutzerkennung wurde mitsamt allen Rollenzugehörigkeiten gelöscht.", DRIVER
		.findElement(By.cssSelector(".user-details > .alert > p > strong")).getText());
	  
	  //user test finished
	  TestUtils.home(DRIVER);
		TestUtils.logout(DRIVER);
	}
	
	@Test
	public void userGroups() throws Exception {
		TestUtils.home(DRIVER);
		TestUtils.login(DRIVER);
		DRIVER.findElement(By.linkText("Admin")).click();
		DRIVER.findElement(By.linkText("Gruppen verwalten")).click();
		
		//create group test
		By loadGroups = By.xpath("//div[@id='dijit__TreeNode_0']/div/span[1]");
	  wait.until(ExpectedConditions.elementToBeClickable(loadGroups));
	  DRIVER.findElement(loadGroups).click();

		DRIVER.findElement(By.id("dijit_form_Button_12")).click();
		DRIVER.findElement(By.xpath("//div[@id='dijit__TreeNode_0']/div/span[1]")).click();
		DRIVER.findElement(By.xpath("//div[@id='dijit__TreeNode_0']/div/span[1]")).click();
		
		loadGroups = By.xpath("//div[@id='dijit__TreeNode_2']/div/span[1]");
	  wait.until(ExpectedConditions.elementToBeClickable(loadGroups));
	  DRIVER.findElement(loadGroups).click();
		
	  assertEquals("content does not match", "", DRIVER.findElement(By.id("dijit_form_TextBox_3")).getText());
	  DRIVER.findElement(By.id("dijit_form_TextBox_3")).sendKeys("testGroup");
	  assertEquals("content does not match", "", DRIVER.findElement(By.id("dijit_form_TextBox_4")).getText());
	  DRIVER.findElement(By.id("dijit_form_TextBox_4")).sendKeys("testGroup");
	  DRIVER.findElement(By.id("dijit_form_ValidationTextBox_2")).clear();
	  DRIVER.findElement(By.id("dijit_form_ValidationTextBox_2")).sendKeys("testGroup");

	  //save and accept alert dialog
	  DRIVER.findElement(By.id("dijit_form_Button_5")).click();
	  Thread.sleep(500);
	  Alert myAlert = DRIVER.switchTo().alert();
		assertEquals("save failed", "Speichern erfolgreich", myAlert.getText());
		myAlert.accept();

		DRIVER.navigate().refresh();
		
		//delete group
		loadGroups = By.xpath("//div[@id='dijit__TreeNode_1']/div/span[@class='dijitTreeContent']/span[@class='dijitTreeLabel']");
	  wait.until(ExpectedConditions.elementToBeClickable(loadGroups));
		assertThat(DRIVER.findElement(loadGroups).getText(), containsString("testGroup"));
		DRIVER.findElement(By.xpath("//div[@id='dijit__TreeNode_1']/div/span[@class='dijitTreeContent']")).click();
		DRIVER.findElement(By.id("dijit_form_Button_13")).click();
	  DRIVER.findElement(By.id("dijit_form_Button_5")).click();
	  Thread.sleep(500);
	  myAlert = DRIVER.switchTo().alert();
		assertEquals("save failed", "Speichern erfolgreich", myAlert.getText());
		myAlert.accept();
	  
	  TestUtils.home(DRIVER);
		TestUtils.logout(DRIVER);
	}
	
	@Test
	public void globalMsg() throws Exception {
		//test global msg editor
		TestUtils.home(DRIVER);
		TestUtils.login(DRIVER);
		DRIVER.findElement(By.linkText("Admin")).click();
		DRIVER.findElement(By.linkText("Globale Nachricht Bearbeiten")).click();
		
	  assertEquals("content does not match", "Globale Nachricht bearbeiten", DRIVER
	  		.findElement(By.xpath("//div[@id='jp-layout-globalmessage-editor']/div/div/h1[@class='panel-title']")).getText());

	  WebElement visibility = DRIVER.findElement(By.id("visibility"));
	  visibility.findElement(By.xpath("option[@value='visible']")).click();
	  
	  DRIVER.findElement(By.id("submit")).click();
	  
	  assertEquals("save failed", "Speichern erfolgreich", DRIVER
	  		.findElement(By.cssSelector("#jp-layout-globalmessage-editor > div:last-child")).getText());
	  
	  DRIVER.navigate().refresh();
	  
	  assertEquals("save failed", "Serverupdate", DRIVER
	  		.findElement(By.cssSelector(".jp-layout-message > strong")).getText());
	  
	  visibility = DRIVER.findElement(By.id("visibility"));
	  visibility.findElement(By.xpath("option[@value='hidden']")).click();
	  
	  DRIVER.findElement(By.id("submit")).click();
	  
	  assertEquals("save failed", "Speichern erfolgreich", DRIVER
	  		.findElement(By.cssSelector("#jp-layout-globalmessage-editor > div:last-child")).getText());
	  
	  TestUtils.home(DRIVER);
		TestUtils.logout(DRIVER);
	}
}