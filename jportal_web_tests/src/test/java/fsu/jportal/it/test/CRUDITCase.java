package fsu.jportal.it.test;

//import junit.framework.TestCase;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.hamcrest.CoreMatchers.containsString;
import fsu.jportal.it.BaseIntegrationTest;
import fsu.jportal.it.TestUtils;

public class CRUDITCase extends BaseIntegrationTest {
	WebDriverWait wait = new WebDriverWait(DRIVER, 10);

		@Test
    public void createPerson() throws Exception {
        TestUtils.home(DRIVER);
        TestUtils.login(DRIVER);
	      By adminButton = By.xpath("//button[@class='btn btn-default fa fa-gear dropdown-toggle']");
	      wait.until(ExpectedConditions.elementToBeClickable(adminButton));
	      DRIVER.findElement(adminButton).click();
        
        DRIVER.findElement(By.name("Neue Person")).click();
        assertEquals("language not changed - content does not match", "Neue Person anlegen", DRIVER
            .findElement(By.id("xeditor-title")).getText());
        
        // fill form
        DRIVER.findElement(By.name("/mycoreobject/metadata/def.heading/heading/lastName")).sendKeys("Goethe");
        DRIVER.findElement(By.name("/mycoreobject/metadata/def.heading/heading/firstName")).sendKeys("Johann Wolfgang");
        DRIVER.findElement(By.name("/mycoreobject/metadata/def.heading/heading/nameAffix")).sendKeys("von");
        WebElement select = DRIVER.findElement(By.name("/mycoreobject/metadata/def.gender/gender/@categid"));
        select.findElement(By.xpath("option[@value='male']")).click();
        DRIVER.findElement(By.name("/mycoreobject/metadata/def.role/role")).sendKeys("Publizist, Politiker, Jurist usw.");
//        DRIVER.findElement(By.name("/mycoreobject/metadata/def.dateOfBirth/dateOfBirth")).sendKeys("1749-08-28");
        DRIVER.findElement(By.xpath("//div[@id='dateOfBirthCon']/input[@placeholder='Jahr']")).sendKeys("1749");
        DRIVER.findElement(By.xpath("//div[@id='dateOfBirthCon']/input[@placeholder='Monat']")).sendKeys("08");
        DRIVER.findElement(By.xpath("//div[@id='dateOfBirthCon']/input[@placeholder='Tag']")).sendKeys("28");
        DRIVER.findElement(By.name("/mycoreobject/metadata/def.placeOfBirth/placeOfBirth"))
            .sendKeys("Frankurt am Main");
//        DRIVER.findElement(By.name("/mycoreobject/metadata/def.dateOfDeath/dateOfDeath")).sendKeys("1832-03-22");
        DRIVER.findElement(By.xpath("//div[@id='dateOfDeathCon']/input[@placeholder='Jahr']")).sendKeys("1832");
        DRIVER.findElement(By.xpath("//div[@id='dateOfDeathCon']/input[@placeholder='Monat']")).sendKeys("03");
        DRIVER.findElement(By.xpath("//div[@id='dateOfDeathCon']/input[@placeholder='Tag']")).sendKeys("22");
        DRIVER.findElement(By.name("/mycoreobject/metadata/def.placeOfDeath/placeOfDeath")).sendKeys("Weimar");
        TestUtils.saveForm(DRIVER);
        
        // test page
        assertEquals("title does not match", "Goethe, Johann Wolfgang - JPortal", DRIVER.getTitle());
        assertEquals("header does not match", "Goethe, Johann Wolfgang von",
            DRIVER.findElement(By.id("jp-maintitle")).getText());
        TestUtils.deletObj(DRIVER, "");
        TestUtils.home(DRIVER);
        TestUtils.logout(DRIVER);
    }

    @Test
    public void createInstitution() throws Exception {
        TestUtils.home(DRIVER);
        TestUtils.login(DRIVER);
        By adminButton = By.xpath("//button[@class='btn btn-default fa fa-gear dropdown-toggle']");
        wait.until(ExpectedConditions.elementToBeClickable(adminButton));
        DRIVER.findElement(adminButton).click();
        
        DRIVER.findElement(By.name("Neue Institution")).click();
        assertEquals("language not changed - content does not match", "Neue Institution anlegen", DRIVER
            .findElement(By.id("xeditor-title")).getText());

        // fill form
        DRIVER.findElement(By.name("/mycoreobject/metadata/names/name/fullname")).sendKeys(
            "Thüringer Universitäts- und Landesbibliothek Jena");
        DRIVER.findElement(By.name("/mycoreobject/metadata/names/name/nickname")).sendKeys("ThULB");
        DRIVER.findElement(By.name("/mycoreobject/metadata/addresses/address/country")).sendKeys("Deutschland");
        DRIVER.findElement(By.name("/mycoreobject/metadata/addresses/address/zipcode")).sendKeys("07743");
        DRIVER.findElement(By.name("/mycoreobject/metadata/addresses/address/city")).sendKeys("Jena");
        DRIVER.findElement(By.name("/mycoreobject/metadata/addresses/address/street")).sendKeys("Bibliotheksplatz");
        DRIVER.findElement(By.name("/mycoreobject/metadata/addresses/address/number")).sendKeys("2");
        DRIVER.findElement(By.name("/mycoreobject/metadata/urls/url/@xlink:title")).sendKeys("ThULB");
        DRIVER.findElement(By.name("/mycoreobject/metadata/urls/url/@xlink:href")).sendKeys(
            "http://www.thulb.uni-jena.de");
        DRIVER.findElement(By.name("/mycoreobject/metadata/emails/email")).sendKeys("testEmail@test.de");
        DRIVER.findElement(By.name("/mycoreobject/metadata/notes/note")).sendKeys("Bibliothek");
        TestUtils.saveForm(DRIVER);

        // test page
        assertEquals("title does not match", "Thüringer Universitäts- und Landesbibliothek Jena - JPortal",
            DRIVER.getTitle());
        assertEquals("header does not match", "Thüringer Universitäts- und Landesbibliothek Jena", DRIVER
            .findElement(By.id("jp-maintitle")).getText());
        TestUtils.deletObj(DRIVER, "");
        TestUtils.home(DRIVER);
        TestUtils.logout(DRIVER);
    }

    @Test
    public void createJournal() throws Exception {
        TestUtils.home(DRIVER);
        TestUtils.login(DRIVER);
	  		TestUtils.createMinPerson(DRIVER, "testPerson");
	  		TestUtils.home(DRIVER);

        // create journal
        By adminButton = By.xpath("//button[@class='btn btn-default fa fa-gear dropdown-toggle']");
        wait.until(ExpectedConditions.elementToBeClickable(adminButton));
        DRIVER.findElement(adminButton).click();
        
        DRIVER.findElement(By.name("Neue Zeitschrift")).click();
        assertEquals("language not changed - content does not match", "Neue Zeitschrift anlegen", DRIVER
            .findElement(By.id("xeditor-title")).getText());

        // bibl. beschreibung
        DRIVER.findElement(By.name("/mycoreobject/metadata/maintitles/maintitle")).sendKeys("Die Horen");
        // main publisher
        WebElement participantSelect = DRIVER.findElement(By.name("/mycoreobject/metadata/participants/participant/@type"));
        participantSelect.findElement(By.xpath("option[@value='mainPublisher']")).click();
        
        DRIVER.findElement(By.xpath("//button[@class='btn btn-default jp-personSelect-person']")).click();
        
        By subselect = By.xpath("//div[@id='resultList']/div/div/div/a");
        wait.until(ExpectedConditions.elementToBeClickable(subselect));
        DRIVER.findElement(subselect).click();
        
        DRIVER.findElement(By.id("personSelect-send")).click();
        // from
        WebElement fromSelect = DRIVER.findElement(By.id("dateSelect"));
        fromSelect.findElement(By.xpath("option[@value='published_from']")).click();
        DRIVER.findElement(By.xpath("//div[@id='fromDateContainer']/input[@placeholder='Jahr']")).sendKeys("1795");
        // until
        fromSelect.findElement(By.xpath("//div[@id='untilDateContainer']/input[@placeholder='Jahr']")).sendKeys("1797");
        // language
        WebElement langSelect = DRIVER.findElement(By.name("/mycoreobject/metadata/languages/language/@categid"));
        langSelect.findElement(By.xpath("option[@value='de']")).click();
        // journal type
        WebElement journalTypeSelect = DRIVER.findElement(By
            .name("/mycoreobject/metadata/contentClassis1/contentClassi1/@categid"));
        journalTypeSelect.findElement(By.xpath("option[@value='historical']")).click();
        // template
        WebElement templateSelect = DRIVER.findElement(By
            .name("/mycoreobject/metadata/hidden_templates/hidden_template"));
        templateSelect.findElement(By.xpath("option[@value='template_DynamicLayoutTemplates']")).click();

        TestUtils.saveForm(DRIVER);

        // Tests
        assertEquals("header does not match", "Die Horen", DRIVER.findElement(By.id("jp-maintitle")).getText());
        TestUtils.deletObj(DRIVER, "");
        TestUtils.deletObj(DRIVER, "testPerson");
        TestUtils.home(DRIVER);
        TestUtils.logout(DRIVER);
    }

    @Test
    public void createVolume() throws Exception {
        TestUtils.home(DRIVER);
        TestUtils.login(DRIVER);
        TestUtils.creatMinJournal(DRIVER, "testJournal");
        TestUtils.clickCreatSelect(DRIVER, "Neuer Band");
        // bibl. Beschreibung
        DRIVER.findElement(By.name("/mycoreobject/metadata/maintitles/maintitle")).sendKeys("Zweite Ausgabe");
        // Datum
        DRIVER.findElement(By.xpath("//div[@id='dateContainer']/input[@placeholder='Jahr']")).sendKeys("1947");

        TestUtils.saveForm(DRIVER);

        // Tests
        assertEquals("header does not match", "Zweite Ausgabe", DRIVER.findElement(By.id("jp-maintitle"))
            .getText());
        TestUtils.deletObj(DRIVER, "testJournal");
        TestUtils.home(DRIVER);
        TestUtils.logout(DRIVER);
    }

    @Test
    public void createArticle() throws Exception {
        TestUtils.home(DRIVER);
        TestUtils.login(DRIVER);
        TestUtils.creatMinJournal(DRIVER, "testJournal");
        TestUtils.creatMinVolume(DRIVER, "testBand");
        TestUtils.clickCreatSelect(DRIVER, "Neuer Artikel");

//        DRIVER.get(START_URL + "/editor/start.xed?type=jparticle&action=create&parent=jportal_jpvolume_00000001");
        
        // bibl. Beschreibung
        DRIVER.findElement(By.name("/mycoreobject/metadata/maintitles/maintitle")).sendKeys("Heim ins Frankreich");
        // Seitenbereich
        DRIVER.findElement(By.name("/mycoreobject/metadata/sizes/size")).sendKeys("1");

        TestUtils.saveForm(DRIVER);

        // Tests
        assertEquals("header does not match", "Heim ins Frankreich", DRIVER.findElement(By.id("jp-maintitle"))
            .getText());
        TestUtils.deletObj(DRIVER, "testJournal");
        TestUtils.home(DRIVER);
        TestUtils.logout(DRIVER);
    }

    @Test
    public void delete() throws Exception {
        // create
        TestUtils.home(DRIVER);
        TestUtils.login(DRIVER);
        By adminButton = By.xpath("//button[@class='btn btn-default fa fa-gear dropdown-toggle']");
        wait.until(ExpectedConditions.elementToBeClickable(adminButton));
        DRIVER.findElement(adminButton).click();
        
        DRIVER.findElement(By.name("Neue Institution")).click();
        DRIVER.findElement(By.name("/mycoreobject/metadata/names/name/fullname")).sendKeys("Uni Jena");
        TestUtils.saveForm(DRIVER);
        assertEquals("header does not match", "Uni Jena", DRIVER.findElement(By.id("jp-maintitle")).getText());

        // delete
        By deletButton = By.xpath("//button[@class='btn btn-default fa fa-gear dropdown-toggle']");
        wait.until(ExpectedConditions.elementToBeClickable(deletButton));
        DRIVER.findElement(deletButton).click();
        
        DRIVER.findElement(By.id("deleteDocButton")).click();
        
        By deletOk = By.id("delete-dialog-submit");
        wait.until(ExpectedConditions.elementToBeClickable(deletOk));
        DRIVER.findElement(deletOk).click();

        //wait a bit until it deleted
        Thread.sleep(500);
        //check if ok
        assertEquals("delet failed", "Löschen erfolgreich!",DRIVER
        		.findElement(By.id("delete-dialog-info")).getText());
        DRIVER.findElement(By.id("delete-dialog-close")).click();
        TestUtils.home(DRIVER);
        TestUtils.logout(DRIVER);
    }

    @Test
    public void importObj() throws Exception {
		  TestUtils.home(DRIVER);
		  TestUtils.login(DRIVER);
		  TestUtils.clickCreatSelect(DRIVER, "Person/Institution importieren");

		  //gnd for Bach, Wilhelm Friedemann
		  DRIVER.findElement(By.id("inputField")).sendKeys("118505548");
		  DRIVER.findElement(By.id("search")).click();
		  
		  By waitImport = By.id("doubletCheck");
	    wait.until(ExpectedConditions.elementToBeClickable(waitImport));
		  
	    assertEquals("title does not match", "Datensatz kann importiert werden. Keine Dubletten (identische GND-ID) gefunden.", DRIVER
	    		.findElement(waitImport).getText());
		  
	    assertEquals("title does not match", "Bach, Wilhelm Friedemann", DRIVER
	    		.findElement(By.xpath("//div[@id='result']/div/dd[1]")).getText());
	    
	    DRIVER.findElement(By.linkText("Datensatz importieren")).click();
	    
	    //wait for it to be loaded
	    Thread.sleep(600);
	    //check if ok
	    assertThat(DRIVER.findElement(By.xpath("//div[@class='result']/p")).getText(), containsString("Datensatz erfolgreich importiert."));
	    
	    DRIVER.findElement(By.linkText("Link zum Objekt")).click();
	    
	    TestUtils.deletObj(DRIVER, "");
	    
		  TestUtils.home(DRIVER);
		  TestUtils.logout(DRIVER);
    }
	
		@Test
		public void writeComments() throws Exception {
		  TestUtils.home(DRIVER);
		  TestUtils.login(DRIVER);
		  TestUtils.creatMinJournal(DRIVER, "TestJournal");
		  
		  TestUtils.clickCreatSelect(DRIVER, "Beschreibung bearbeiten");

		  By waitForFrame = By.className("cke_wysiwyg_frame");
		  wait.until(ExpectedConditions.elementToBeClickable(waitForFrame));
		  DRIVER.switchTo().frame(DRIVER.findElement(waitForFrame));
		  waitForFrame = By.cssSelector(".cke_editable_themed");
		  wait.until(ExpectedConditions.elementToBeClickable(waitForFrame));
		  DRIVER.findElement(waitForFrame).sendKeys("Dies soll ein Test sein!");
		  
		  DRIVER.switchTo().defaultContent();
		  
	    DRIVER.findElement(By.id("ckeditorSaveButton")).click();
		  
		  assertEquals("text does not match", "Dies soll ein Test sein!", DRIVER.findElement(By.id("intro")).getText());
		  
		  TestUtils.deletObj(DRIVER, "");
		  
		  TestUtils.home(DRIVER);
		  TestUtils.logout(DRIVER);
		}
		
    @Test
    public void moveObjChild() throws Exception {
		  TestUtils.home(DRIVER);
		  TestUtils.login(DRIVER);
		  
		  //creat test obj
		  TestUtils.creatMinJournal(DRIVER, "testJournalNoChild");
		  String[] noChildJournal = DRIVER.getCurrentUrl().split("/");
		  
		  TestUtils.creatMinJournal(DRIVER, "testJournalWithChild");
		  TestUtils.creatMinVolume(DRIVER, "testVolumeChild");
		  
		  DRIVER.findElement(By.linkText("testJournalWithChild")).click();
		  
		  TestUtils.clickCreatSelect(DRIVER, "Kinder verschieben");
		  
		  DRIVER.findElement(By.id("mom_checkbox_childlist_all")).click();
		  DRIVER.findElement(By.id("mom_radio_search_filter2")).click();
		  DRIVER.findElement(By.id("mom_search_button")).click();
		  DRIVER.findElement(By.xpath("//div[@data-objid='" + noChildJournal[noChildJournal.length - 1] + "']/input")).click();
		  DRIVER.findElement(By.id("mom_button_move")).click();
		  
		  By waitForMove = By.id("jp-maintitle");
		  wait.until(ExpectedConditions.elementToBeClickable(waitForMove));
		  assertEquals("text does not match", "testJournalNoChild", DRIVER.findElement(waitForMove).getText());
		  assertEquals("text does not match", "testVolumeChild", DRIVER.findElement(By.linkText("testVolumeChild")).getText());
		  
		  TestUtils.deletObj(DRIVER, "");
		  TestUtils.goToObj(DRIVER, "testJournalWithChild");
		  
		  if(DRIVER.findElements(By.linkText("testVolumeChild")).size() > 0) {
		  	fail("Element still exist in the first parent journal.");
		  }
		  
		  TestUtils.deletObj(DRIVER, "");
		  
		  TestUtils.home(DRIVER);
		  TestUtils.logout(DRIVER);
		}
}
