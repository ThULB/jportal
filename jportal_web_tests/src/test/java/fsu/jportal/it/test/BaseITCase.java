package fsu.jportal.it.test;

import junit.framework.TestCase;

//import static org.junit.Assert.*;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import fsu.jportal.it.BaseIntegrationTest;
import fsu.jportal.it.TestUtils;

public class BaseITCase extends BaseIntegrationTest {

    @Test
    public void loginAndLogout() throws Exception {
        TestUtils.home(DRIVER);
        if (TestUtils.isLoggedIn(DRIVER)) {
            TestUtils.logout(DRIVER);
        }
        TestUtils.login(DRIVER);
        TestUtils.logout(DRIVER);
    }

    @Test
    public void language() throws Exception {
        TestUtils.home(DRIVER);
        DRIVER.findElement(By.xpath("//li[@id='languageMenu']/a")).click();
        DRIVER.findElement(By.xpath("//ul[@id='languageList']/li[1]/a")).click();
        TestCase.assertEquals("language not changed - content does not match", "Welcome to journals@UrMEL!", DRIVER
            .findElement(By.xpath("//div[@class='jp-layout-index-intro']/h1")).getText());

        DRIVER.get(START_URL + "/content/below/index.xml");
        DRIVER.findElement(By.xpath("//li[@id='languageMenu']/a")).click();
        DRIVER.findElement(By.xpath("//ul[@id='languageList']/li[1]/a")).click();
        TestCase.assertEquals("language not changed - content does not match", "Willkommen bei journals@UrMEL", DRIVER
            .findElement(By.xpath("//div[@class='jp-layout-index-intro']/h1")).getText());
    }

    @Test
    public void aToZ() throws Exception {
				TestUtils.home(DRIVER);
    		TestUtils.login(DRIVER);
    		TestUtils.creatMinJournal(DRIVER, "Der Spiegel");
        DRIVER.get(START_URL + "/content/main/journalList.xml");
        WebDriverWait wait = new WebDriverWait(DRIVER, 5);
        // click d
        By d = By.xpath("//ul[@id='tabNav']/li[text()='D']");
        wait.until(ExpectedConditions.elementToBeClickable(d));
        DRIVER.findElement(d).click();
        // test if spiegel occur
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Der Spiegel")));
        // click
        DRIVER.findElement(By.linkText("Der Spiegel")).click();
        TestCase.assertEquals("header does not match", "Der Spiegel", DRIVER.findElement(By.id("jp-maintitle"))
            .getText());
        TestUtils.deletObj(DRIVER, "Der Spiegel");
        TestUtils.logout(DRIVER);
    }

    @Test
    public void navigate() throws Exception {
				TestUtils.home(DRIVER);
    		TestUtils.login(DRIVER);
    		TestUtils.creatMinJournal(DRIVER, "Der Spiegel");
    		TestUtils.creatMinVolume(DRIVER, "Erste Ausgabe (1947)");
    		TestUtils.creatMinArticle(DRIVER, "Schiller und die Räuber");
//        DRIVER.get(START_URL + "/receive/jportal_jpjournal_00000001");
    		TestUtils.goToObj(DRIVER, "Der Spiegel");
        
        while(!TestUtils.textEqualsLinkText(DRIVER, "Erste Ausgabe (1947)")){
        	DRIVER.findElement(By.cssSelector("#resultPaginator > ul > li:last-child > a")).click();
        }
    		
        DRIVER.findElement(By.linkText("Erste Ausgabe (1947)")).click();
        
        while(!TestUtils.textEqualsLinkText(DRIVER, "Schiller und die Räuber")){
        	DRIVER.findElement(By.cssSelector("#resultPaginator > ul > li:last-child > a")).click();
        }
        
        DRIVER.findElement(By.linkText("Schiller und die Räuber")).click();
        TestUtils.deletObj(DRIVER, "Der Spiegel");
        TestUtils.logout(DRIVER);
    }

    @Test
    public void search() throws Exception {
    		TestUtils.home(DRIVER);
    		TestUtils.login(DRIVER);
    		//create article and inst
    		TestUtils.creatMinJournal(DRIVER, "Der Spiegel");
    		TestUtils.creatMinVolume(DRIVER, "Erste Ausgabe (1947)");
    		TestUtils.creatMinArticle(DRIVER, "Schiller und die Räuber");
    		TestUtils.creatMinInst(DRIVER, "Spiegel-Verlag");
    		
        //search article
        DRIVER.findElement(By.id("inputField")).sendKeys("Schiller und die Räuber");
        DRIVER.findElement(By.xpath("//span[@class='glyphicon glyphicon-search glyphSearchBar']")).submit();
        DRIVER.findElement(By.linkText("Schiller und die Räuber")).click();

        TestCase.assertEquals("header does not match", "Schiller und die Räuber",
            DRIVER.findElement(By.id("jp-maintitle")).getText());

    		//search inst
        TestUtils.home(DRIVER);
        DRIVER.findElement(By.id("inputField")).sendKeys("Spiegel-Verlag");
        DRIVER.findElement(By.xpath("//span[@class='glyphicon glyphicon-search glyphSearchBar']")).submit();
        DRIVER.findElement(By.linkText("Spiegel-Verlag")).click();

        TestCase.assertEquals("header does not match", "Spiegel-Verlag", DRIVER.findElement(By.id("jp-maintitle"))
            .getText());
        
        TestUtils.deletObj(DRIVER, "Der Spiegel");
        TestUtils.deletObj(DRIVER, "Spiegel-Verlag");
        TestUtils.logout(DRIVER);
    }
}