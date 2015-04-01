package fsu.jportal.it.test;

//import junit.framework.TestCase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import fsu.jportal.it.BaseIntegrationTest;
import fsu.jportal.it.TestUtils;

public class BaseITCase extends BaseIntegrationTest {
	WebDriverWait wait = new WebDriverWait(DRIVER, 5);
	
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
        assertEquals("language not changed - content does not match", "Welcome to journals@UrMEL!", DRIVER
            .findElement(By.xpath("//div[@class='jp-layout-index-intro']/h1")).getText());

        DRIVER.get(START_URL + "/content/below/index.xml");
        DRIVER.findElement(By.xpath("//li[@id='languageMenu']/a")).click();
        DRIVER.findElement(By.xpath("//ul[@id='languageList']/li[1]/a")).click();
        assertEquals("language not changed - content does not match", "Willkommen bei journals@UrMEL", DRIVER
            .findElement(By.xpath("//div[@class='jp-layout-index-intro']/h1")).getText());
    }

    @Test
    public void aToZ() throws Exception {
				TestUtils.home(DRIVER);
    		TestUtils.login(DRIVER);
    		TestUtils.creatMinJournal(DRIVER, "Der Spiegel");
    		Thread.sleep(500);
        DRIVER.get(START_URL + "/content/main/journalList.xml");
        
        try {
	        // click d
	        By d = By.xpath("//ul[@id='tabNav']/li[text()='D']");
	        wait.until(ExpectedConditions.elementToBeClickable(d));
	        DRIVER.findElement(d).click();
	        // test if spiegel occur
	        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Der Spiegel")));
	        // click
	        DRIVER.findElement(By.linkText("Der Spiegel")).click();
	        assertEquals("header does not match", "Der Spiegel", DRIVER.findElement(By.id("jp-maintitle"))
	            .getText());
        } catch (Exception e){
        	TestUtils.ERROR_MESSAGE = e.getMessage();
        }
        
        TestUtils.deletObj(DRIVER, "Der Spiegel");
        TestUtils.finishThis(DRIVER);
    }

    @Test
    public void navigate() throws Exception {
				TestUtils.home(DRIVER);
    		TestUtils.login(DRIVER);
    		TestUtils.creatMinJournal(DRIVER, "Der Spiegel");
    		TestUtils.creatMinVolume(DRIVER, "Erste Ausgabe (1947)");
    		TestUtils.creatMinArticle(DRIVER, "Schiller und die Räuber");
//        DRIVER.get(START_URL + "/receive/jportal_jpjournal_00000001");
    		try {
	    		TestUtils.goToObj(DRIVER, "Der Spiegel");
	        
	        while(!TestUtils.textEqualsLinkText(DRIVER, "Erste Ausgabe (1947)")){
	        	DRIVER.findElement(By.cssSelector("#resultPaginator > ul > li:last-child > a")).click();
	        }
	    		
	        DRIVER.findElement(By.linkText("Erste Ausgabe (1947)")).click();
	        
	        while(!TestUtils.textEqualsLinkText(DRIVER, "Schiller und die Räuber")){
	        	DRIVER.findElement(By.cssSelector("#resultPaginator > ul > li:last-child > a")).click();
	        }
	        
	        DRIVER.findElement(By.linkText("Schiller und die Räuber")).click();
	        assertEquals("header does not match", "Schiller und die Räuber", DRIVER.findElement(By.id("jp-maintitle"))
	            .getText());
	        
	        DRIVER.findElement(By.linkText("Der Spiegel")).click();
    		} catch (Exception e){
        	TestUtils.ERROR_MESSAGE = e.getMessage();
        }
    		
    		TestUtils.deletObj(DRIVER, "Der Spiegel");
    		TestUtils.finishThis(DRIVER);
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
    		
    		try {
	        //search article
	        DRIVER.findElement(By.id("inputField")).sendKeys("Schiller und die Räuber");
	        DRIVER.findElement(By.xpath("//span[@class='glyphicon glyphicon-search glyphSearchBar']")).submit();
	        DRIVER.findElement(By.linkText("Schiller und die Räuber")).click();
	
	        assertEquals("header does not match", "Schiller und die Räuber",
	            DRIVER.findElement(By.id("jp-maintitle")).getText());
	
	    		//search inst
	        TestUtils.home(DRIVER);
	        DRIVER.findElement(By.id("inputField")).sendKeys("Spiegel-Verlag");
	        DRIVER.findElement(By.xpath("//span[@class='glyphicon glyphicon-search glyphSearchBar']")).submit();
	        DRIVER.findElement(By.linkText("Spiegel-Verlag")).click();
	
	        assertEquals("header does not match", "Spiegel-Verlag", DRIVER.findElement(By.id("jp-maintitle"))
	            .getText());
    		} catch (Exception e){
        	TestUtils.ERROR_MESSAGE = e.getMessage();
        }
        try {
        	TestUtils.deletObj(DRIVER, "Spiegel-Verlag");
        } catch(Exception e) {}
        
        TestUtils.deletObj(DRIVER, "Der Spiegel");
        TestUtils.finishThis(DRIVER);
    }
    
		@Test
    public void advancedSearch() throws Exception {
				TestUtils.home(DRIVER);
				TestUtils.login(DRIVER);
				//create person
				TestUtils.createMinPerson(DRIVER, "Schiller");
				TestUtils.home(DRIVER);
				
				try {
					DRIVER.findElement(By.id("searchDropDownButton")).click();
					DRIVER.findElement(By.linkText("Erweiterte Suche")).click();
					
					//select search field
		      WebElement searchField = DRIVER.findElement(By.name("XSL.field1"));
		      searchField.findElement(By.xpath("option[@value='names_de']")).click();
		      //search value 
		      DRIVER.findElement(By.name("XSL.value1")).sendKeys("Schiller");
		      
		      DRIVER.findElement(By.id("submitButton")).click();
		      
		      assertEquals("text does not match", "Schiller", DRIVER.findElement(By.linkText("Schiller")).getText());
		      
					DRIVER.findElement(By.id("searchDropDownButton")).click();
					DRIVER.findElement(By.linkText("Erweiterte Suche bearbeiten")).click();
					
					searchField = DRIVER.findElement(By.name("XSL.field1"));
					assertEquals("text does not match", "Person/Institution", searchField.findElement(By.xpath("option[@value='names_de']")).getText());
					assertEquals("text does not match", "Schiller", DRIVER.findElement(By.name("XSL.value1")).getAttribute("value"));
				} catch (Exception e){
        	TestUtils.ERROR_MESSAGE = e.getMessage();
        }
				
				TestUtils.deletObj(DRIVER, "Schiller");
				TestUtils.finishThis(DRIVER);
		}
	
  @Test
  public void doublet() throws Exception {
      TestUtils.home(DRIVER);
      TestUtils.login(DRIVER);
      TestUtils.creatMinInst(DRIVER, "parentTestInst");
      
      String[] doubletID = DRIVER.getCurrentUrl().split("/");

      try {
	      TestUtils.clickCreatSelect(DRIVER, "Neue Institution");
	
	      // create inst with doublet
	      DRIVER.findElement(By.name("/mycoreobject/metadata/names/name/fullname")).sendKeys("testInst");
	      DRIVER.findElement(By.name("/mycoreobject/metadata/def.doubletOf/doubletOf")).sendKeys(doubletID[doubletID.length - 1]);
	
	      TestUtils.saveForm(DRIVER);
	      Thread.sleep(600);
	      
	      assertEquals("header does not match", "testInst", DRIVER.findElement(By.id("jp-maintitle")).getText());
	      
	      //go to doublet menu 
	      TestUtils.clickCreatSelect(DRIVER, "Dublettenfinder");
	      assertEquals("header does not match", "Dubletten-Check", DRIVER.findElement(By.cssSelector("#jportal_doublet_finder_module > div:nth-child(2) > span")).getText());
	      
	      //search the doublet
	      DRIVER.findElement(By.linkText("(Dubletten von Institutionen anzeigen...)")).click();
	      
	      assertEquals("text does not match", "testInst", DRIVER.findElement(By.linkText("testInst")).getText());
	
	      DRIVER.navigate().back();
	      
	      //del testInst
	      DRIVER.findElement(By.id("delDubButton")).click();
	
	      Thread.sleep(500);
	      assertEquals("didn't deleted", "0", DRIVER.findElement(By.xpath("//p[@id='jpinst_doublets']/span[@class='numDub']")).getText());
	
	      DRIVER.findElement(By.linkText("(Institutionen ohne GND anzeigen...)")).click();
	      DRIVER.findElement(By.linkText("parentTestInst")).click();
      } catch (Exception e){
      	TestUtils.ERROR_MESSAGE = e.getMessage();
      	TestUtils.deletObj(DRIVER, "testInst");
      }
      
      TestUtils.deletObj(DRIVER, "parentTestInst");
      TestUtils.finishThis(DRIVER);
  }
  
  @Test
  public void versionsInfo() throws Exception {
      TestUtils.home(DRIVER);
      TestUtils.login(DRIVER);
      TestUtils.createMinPerson(DRIVER, "testPerson");
   
      try{
      	TestUtils.clickCreatSelect(DRIVER, "Versionsgeschichte");
      	
      	assertEquals("Versionsgeschichte - JPortal", DRIVER.getTitle());
      	assertTrue("versionsinfo table should be displayed", DRIVER.findElement(By.xpath("//table[@class='table versioninfo']")).isDisplayed());
      	
      	DRIVER.findElement(By.linkText("Zurück...")).click();
      } catch (Exception e) {
      	TestUtils.ERROR_MESSAGE = e.getMessage();
      	TestUtils.deletObj(DRIVER, "testPerson");
      }
      
      TestUtils.deletObj(DRIVER, "");
      TestUtils.finishThis(DRIVER);
  }
}