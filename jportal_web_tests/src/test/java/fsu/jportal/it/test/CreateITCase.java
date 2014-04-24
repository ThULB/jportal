package fsu.jportal.it.test;

import junit.framework.TestCase;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import fsu.jportal.it.BaseIntegrationTest;
import fsu.jportal.it.TestUtils;

public class CreateITCase extends BaseIntegrationTest {

    @Test
    public void createPerson() {
        TestUtils.home(DRIVER);
        TestUtils.login(DRIVER);
        DRIVER.findElement(By.xpath("//a[@path='create/person']")).click();
        TestCase.assertEquals("title does not match", "Person anlegen - JPortal", DRIVER.getTitle());

        // fill form
        DRIVER.findElement(By.name("/mycoreobject/metadata/def.heading/heading/lastName")).sendKeys("Goethe");
        DRIVER.findElement(By.name("/mycoreobject/metadata/def.heading/heading/firstName")).sendKeys("Johann Wolfgang");
        DRIVER.findElement(By.name("/mycoreobject/metadata/def.heading/heading/nameAffix")).sendKeys("von");
        DRIVER.findElements(By.name("/mycoreobject/metadata/def.gender/gender/@categid")).get(0).click();
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
    }

    @Test
    public void createInstitution() {
        TestUtils.home(DRIVER);
        TestUtils.login(DRIVER);
        DRIVER.findElement(By.xpath("//a[@path='create/jpinst']")).click();
        TestCase.assertEquals("title does not match", "Institution anlegen - JPortal", DRIVER.getTitle());

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
    }

    @Test
    public void createJournal() throws Exception {
        TestUtils.home(DRIVER);
        TestUtils.login(DRIVER);

        // create journal
        DRIVER.findElement(By.xpath("//a[@path='create/jpjournal']")).click();
        TestCase.assertEquals("title does not match", "Zeitschrift anlegen - JPortal", DRIVER.getTitle());

        // bibl. beschreibung
        DRIVER.findElement(By.name("/mycoreobject/metadata/maintitles/maintitle")).sendKeys("Die Horen");
        // main publisher
        DRIVER.findElement(By.name("_s-sub.select1-/mycoreobject/metadata/participants/participant")).click();
        DRIVER.findElement(By.linkText("Schiller, Friedrich von")).click();
        WebElement participantSelect = DRIVER.findElement(By
            .name("/mycoreobject/metadata/participants/participant/@type"));
        participantSelect.findElement(By.xpath("option[@value='mainPublisher']")).click();
        // from
        WebElement fromSelect = DRIVER.findElement(By.name("/mycoreobject/metadata/dates/date/@type"));
        fromSelect.findElement(By.xpath("option[@value='published_from']")).click();
        DRIVER.findElement(By.name("/mycoreobject/metadata/dates/date")).sendKeys("1795");
        // until
        DRIVER.findElement(By.name("_p-/mycoreobject/metadata/dates/date-1")).click();
        WebElement toSelect = DRIVER.findElement(By.name("/mycoreobject/metadata/dates/date[2]/@type"));
        toSelect.findElement(By.xpath("option[@value='published_until']")).click();
        DRIVER.findElement(By.name("/mycoreobject/metadata/dates/date[2]")).sendKeys("1797");
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
    }

    @Test
    public void createVolume() throws Exception {
        TestUtils.home(DRIVER);
        TestUtils.login(DRIVER);
        DRIVER.get(START_URL + "/rsc/editor/jportal_jpjournal_00000001/create/jpvolume");

        // bibl. Beschreibung
        DRIVER.findElement(By.name("/mycoreobject/metadata/maintitles/maintitle")).sendKeys("Erstausgabe");
        // Datum
        DRIVER.findElement(By.name("/mycoreobject/metadata/dates/date")).sendKeys("1947");

        TestUtils.saveForm(DRIVER);

        // Tests
        TestCase.assertEquals("header does not match", "Erstausgabe", DRIVER.findElement(By.id("jp-maintitle"))
            .getText());
    }

    @Test
    public void createArticle() throws Exception {
        TestUtils.home(DRIVER);
        TestUtils.login(DRIVER);
        DRIVER.get(START_URL + "/rsc/editor/jportal_jpvolume_00000001/create/jparticle");

        // bibl. Beschreibung
        DRIVER.findElement(By.name("/mycoreobject/metadata/maintitles/maintitle")).sendKeys("Heim ins Frankreich");
        // Seitenbereich
        DRIVER.findElement(By.name("/mycoreobject/metadata/sizes/size")).sendKeys("1");

        TestUtils.saveForm(DRIVER);

        // Tests
        TestCase.assertEquals("header does not match", "Heim ins Frankreich", DRIVER.findElement(By.id("jp-maintitle"))
            .getText());
    }

}
