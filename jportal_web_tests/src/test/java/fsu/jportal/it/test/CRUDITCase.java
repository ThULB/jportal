package fsu.jportal.it.test;

import junit.framework.TestCase;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

import fsu.jportal.it.BaseIntegrationTest;
import fsu.jportal.it.TestUtils;

public class CRUDITCase extends BaseIntegrationTest {
	WebDriverWait wait = new WebDriverWait(DRIVER, 10);
	
    @Test
    public void createPerson() {
        TestUtils.home(DRIVER);
        TestUtils.login(DRIVER);
	      By adminButton = By.xpath("//button[@class='btn btn-default fa fa-gear dropdown-toggle']");
	      wait.until(ExpectedConditions.elementToBeClickable(adminButton));
	      DRIVER.findElement(adminButton).click();
        
        DRIVER.findElement(By.name("Neue Person")).click();
        TestCase.assertEquals("language not changed - content does not match", "Neue Person anlegen", DRIVER
            .findElement(By.id("xeditor-title")).getText());
        
        // fill form
        DRIVER.findElement(By.name("/mycoreobject/metadata/def.heading/heading/lastName")).sendKeys("Goethe");
        DRIVER.findElement(By.name("/mycoreobject/metadata/def.heading/heading/firstName")).sendKeys("Johann Wolfgang");
        DRIVER.findElement(By.name("/mycoreobject/metadata/def.heading/heading/nameAffix")).sendKeys("von");
        WebElement genderSelect = DRIVER.findElement(By.name("/mycoreobject/metadata/def.gender/gender/@categid"));
        genderSelect.findElement(By.xpath("option[@value='male']")).click();
        DRIVER.findElement(By.name("/mycoreobject/metadata/def.dateOfBirth/dateOfBirth")).sendKeys("1749-08-28");
        DRIVER.findElement(By.name("/mycoreobject/metadata/def.placeOfBirth/placeOfBirth"))
            .sendKeys("Frankurt am Main");
        DRIVER.findElement(By.name("/mycoreobject/metadata/def.dateOfDeath/dateOfDeath")).sendKeys("1832-03-22");
        DRIVER.findElement(By.name("/mycoreobject/metadata/def.placeOfDeath/placeOfDeath")).sendKeys("Weimar");
        TestUtils.saveForm(DRIVER);

        // test page
        TestCase.assertEquals("title does not match", "Goethe, Johann Wolfgang - JPortal", DRIVER.getTitle());
        TestCase.assertEquals("header does not match", "Goethe, Johann Wolfgang von",
            DRIVER.findElement(By.id("jp-maintitle")).getText());
        TestUtils.deletObj(DRIVER, "Goethe, Johann Wolfgang von");
        TestUtils.logout(DRIVER);
    }

    @Test
    public void createInstitution() {
        TestUtils.home(DRIVER);
        TestUtils.login(DRIVER);
        By adminButton = By.xpath("//button[@class='btn btn-default fa fa-gear dropdown-toggle']");
        wait.until(ExpectedConditions.elementToBeClickable(adminButton));
        DRIVER.findElement(adminButton).click();
        
        DRIVER.findElement(By.name("Neue Institution")).click();
        TestCase.assertEquals("language not changed - content does not match", "Neue Institution anlegen", DRIVER
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
        TestUtils.saveForm(DRIVER);

        // test page
        TestCase.assertEquals("title does not match", "Thüringer Universitäts- und Landesbibliothek Jena - JPortal",
            DRIVER.getTitle());
        TestCase.assertEquals("header does not match", "Thüringer Universitäts- und Landesbibliothek Jena", DRIVER
            .findElement(By.id("jp-maintitle")).getText());
        TestUtils.deletObj(DRIVER, "Thüringer Universitäts- und Landesbibliothek Jena");
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
        TestCase.assertEquals("language not changed - content does not match", "Neue Zeitschrift anlegen", DRIVER
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
        TestCase
            .assertEquals("header does not match", "Die Horen", DRIVER.findElement(By.id("jp-maintitle")).getText());
        TestUtils.deletObj(DRIVER, "Die Horen");
        TestUtils.deletObj(DRIVER, "testPerson");
        TestUtils.logout(DRIVER);
    }

    @Test
    public void createVolume() throws Exception {
        TestUtils.home(DRIVER);
        TestUtils.login(DRIVER);
//        DRIVER.get(START_URL + "/editor/start.xed?type=jpvolume&action=create&parent=jportal_jpjournal_00000001");
        TestUtils.creatMinJournal(DRIVER, "testJournal");
        TestUtils.clickCreatSelect(DRIVER, "Neuer Band");
        // bibl. Beschreibung
        DRIVER.findElement(By.name("/mycoreobject/metadata/maintitles/maintitle")).sendKeys("Zweite Ausgabe");
        // Datum
        DRIVER.findElement(By.xpath("//div[@id='dateContainer']/input[@placeholder='Jahr']")).sendKeys("1947");

        TestUtils.saveForm(DRIVER);

        // Tests
        TestCase.assertEquals("header does not match", "Zweite Ausgabe", DRIVER.findElement(By.id("jp-maintitle"))
            .getText());
        TestUtils.deletObj(DRIVER, "testJournal");
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
        TestCase.assertEquals("header does not match", "Heim ins Frankreich", DRIVER.findElement(By.id("jp-maintitle"))
            .getText());
        TestUtils.deletObj(DRIVER, "testJournal");
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
        TestCase.assertEquals("header does not match", "Uni Jena", DRIVER.findElement(By.id("jp-maintitle")).getText());

        // delete
        By deletButton = By.xpath("//button[@class='btn btn-default fa fa-gear dropdown-toggle']");
        wait.until(ExpectedConditions.elementToBeClickable(deletButton));
        DRIVER.findElement(deletButton).click();
        
        DRIVER.findElement(By.id("deleteDocButton")).click();
        
        By deletOk = By.id("delete-dialog-submit");
        wait.until(ExpectedConditions.elementToBeClickable(deletOk));
        DRIVER.findElement(deletOk).click();

        TestCase.assertEquals("title does not match", "Das Löschen der Daten war erfolgreich. - JPortal",DRIVER
        		.findElement(By.xpath("//div[@id='delete-dialog-info']/p")).getText());
        DRIVER.findElement(By.id("delete-dialog-close")).click();
        TestUtils.logout(DRIVER);
    }
}
