package fsu.jportal.it;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import static org.junit.Assert.assertEquals;

public abstract class TestUtils {
  public static void home(WebDriver driver) {
    driver.get(BaseIntegrationTest.getHomeAddress());
    assertEquals("invald index page - title does not match", "journals@UrMEL - JPortal", driver.getTitle());
  }

  public static void login(WebDriver driver) {
    if (isLoggedIn(driver)) {
        return;
    }
    driver.findElement(By.xpath("//div[@id='navbar-collapse-globalHeader']/ul/li[2]/a")).click();
    WebElement uid = driver.findElement(By.xpath("//input[@name='uid']"));
    uid.sendKeys("administrator");
    driver.findElement(By.xpath("//input[@name='pwd']")).sendKeys("alleswirdgut");
    uid.submit();
  }

  public static void logout(WebDriver driver) {
    driver.findElement(By.xpath("//div[@id='navbar-collapse-globalHeader']/ul/li[5]/a")).click();
  }

  public static void saveForm(WebDriver driver) {
    driver.findElement(By.xpath("//input[@type='submit' and @value='Speichern']")).click();
  }

  public static boolean isLoggedIn(WebDriver driver) {
    try {
      String userName = driver.findElement(By.xpath("//div[@id='navbar-collapse-globalHeader']/ul/li[@class='userName jp-layout-mainHeader-SeperatorRight']/a")).getText();
      return "Superuser".equals(userName);
    } catch (Exception exc) {
        return false;
    }
  }
  
  public static boolean textEqualsLinkText(WebDriver driver, String myText) {
    try {
  		String test2 = driver.findElement(By.linkText(myText)).getText();
      return myText.equals(test2);
    } catch (Exception exc) {
      return false;
    }
  }
  
  public static void clickCreatSelect(WebDriver driver, String which) {
  	driver.findElement(By.xpath("//button[@class='btn btn-default fas fa-cog dropdown-toggle']")).click();
  	driver.findElement(By.name(which)).click();
  }
  
  public static void createMinPerson(WebDriver driver, String lastName) {
  	clickCreatSelect(driver, "Neue Person");
  	WebDriverWait wait = new WebDriverWait(driver, 2);
  	By waitForLoad = By.name("/mycoreobject/metadata/def.heading/heading/lastName");
		wait.until(ExpectedConditions.elementToBeClickable(waitForLoad));
  	driver.findElement(waitForLoad).sendKeys(lastName);
  	WebElement genderSelect = driver.findElement(By.name("/mycoreobject/metadata/def.gender/gender/@categid"));
    genderSelect.findElement(By.xpath("option[@value='male']")).click();
  	saveForm(driver);
  }
  
  public static void creatMinInst(WebDriver driver, String fullName) {
  	clickCreatSelect(driver, "Neue Institution");
  	WebDriverWait wait = new WebDriverWait(driver, 2);
  	By waitForLoad = By.name("/mycoreobject/metadata/names/name/fullname");
		wait.until(ExpectedConditions.elementToBeClickable(waitForLoad));
  	driver.findElement(waitForLoad).sendKeys(fullName);
  	saveForm(driver);
  }
  
  public static void creatMinJournal(WebDriver driver, String name) {
    home(driver);
    clickCreatSelect(driver, "Neue Zeitschrift");
    WebDriverWait wait = new WebDriverWait(driver, 2);
    By waitForLoad = By.name("/mycoreobject/metadata/maintitles/maintitle");
		wait.until(ExpectedConditions.elementToBeClickable(waitForLoad));
    driver.findElement(waitForLoad).sendKeys(name);
    WebElement langSelect = driver.findElement(By.name("/mycoreobject/metadata/languages/language/@categid"));
    langSelect.findElement(By.xpath("option[@value='de']")).click();
    WebElement templateSelect = driver.findElement(By
        .name("/mycoreobject/metadata/hidden_templates/hidden_template"));
    templateSelect.findElement(By.xpath("option[@value='template_DynamicLayoutTemplates']")).click();
    saveForm(driver);
  }
  
  public static void creatMinVolume(WebDriver driver, String name) {
  	clickCreatSelect(driver, "Neuer Band");
  	WebDriverWait wait = new WebDriverWait(driver, 2);
  	By waitForLoad = By.name("/mycoreobject/metadata/maintitles/maintitle");
		wait.until(ExpectedConditions.elementToBeClickable(waitForLoad));
  	driver.findElement(waitForLoad).sendKeys(name);
  	saveForm(driver);
  }
  
  public static void creatMinArticle(WebDriver driver, String name) {
  	clickCreatSelect(driver, "Neuer Artikel");
  	WebDriverWait wait = new WebDriverWait(driver, 2);
  	By waitForLoad = By.name("/mycoreobject/metadata/maintitles/maintitle");
		wait.until(ExpectedConditions.elementToBeClickable(waitForLoad));
  	driver.findElement(waitForLoad).sendKeys(name);
  	saveForm(driver);
  }
  
  public static void deletObj(WebDriver driver, String toDelete) throws Exception {
    //find
    if(!toDelete.equals("")) {
    	home(driver);
    	goToObj(driver, toDelete);
    }
    
    clickCreatSelect(driver, "Dokument löschen");
  	WebDriverWait wait = new WebDriverWait(driver, 2);
	  By deleteOk = By.id("delete-dialog-submit");
	  wait.until(ExpectedConditions.elementToBeClickable(deleteOk));
	  driver.findElement(deleteOk).click();
	  
	  deleteOk = By.xpath("//div[@id='delete-dialog-info'][text()='Löschen erfolgreich!']");
	  wait.until(ExpectedConditions.elementToBeClickable(deleteOk));
    assertEquals("testObj didn't deleted successfully", "Löschen erfolgreich!", driver.findElement(By.id("delete-dialog-info")).getText());
	  
  	driver.findElement(By.id("delete-dialog-close")).click();
  }
  
  public static void goToObj(WebDriver driver, String where) throws Exception {
		Thread.sleep(600);
  	driver.findElement(By.id("inputField")).sendKeys(where);
  	driver.findElement(By.xpath("//span[@class='glyphicon glyphicon-search glyphSearchBar']")).submit();
  	WebDriverWait wait = new WebDriverWait(driver, 2);
  	By waitForSearch = By.linkText(where);
  	wait.until(ExpectedConditions.elementToBeClickable(waitForSearch));
  	driver.findElement(waitForSearch).click();
  }
}
