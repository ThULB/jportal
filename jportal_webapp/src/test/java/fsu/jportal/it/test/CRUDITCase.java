package fsu.jportal.it.test;

import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import fsu.jportal.it.BaseIntegrationTest;
import fsu.jportal.it.TestUtils;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class CRUDITCase extends BaseIntegrationTest {

    @Ignore
    @Test
    public void createPerson() throws Exception {
        WebDriverWait wait = new WebDriverWait(DRIVER, 2);
        By adminButton = By.xpath("//button[@class='btn btn-default fas fa-cog dropdown-toggle']");
        wait.until(ExpectedConditions.elementToBeClickable(adminButton));
        DRIVER.findElement(adminButton).click();

        DRIVER.findElement(By.name("Neue Person")).click();
        By waitForLoad = By.id("xeditor-title");
        wait.until(ExpectedConditions.elementToBeClickable(waitForLoad));
        assertEquals("content does not match", "Neue Person anlegen", DRIVER.findElement(waitForLoad).getText());

        createPersonSet();

        assertEquals("title does not match", "Goethe, Johann Wolfgang - JPortal", DRIVER.getTitle());
        assertEquals("header does not match", "Goethe, Johann Wolfgang von",
                DRIVER.findElement(By.id("jp-maintitle")).getText());

        TestUtils.deletObj(DRIVER, "");
    }

    private void createPersonSet() {
        WebDriverWait wait = new WebDriverWait(DRIVER, 2);
        // fill form
        By waitForLoad = By.name("/mycoreobject/metadata/def.heading/heading/lastName");
        wait.until(ExpectedConditions.elementToBeClickable(waitForLoad));
        DRIVER.findElement(waitForLoad).sendKeys("Goethe");
        DRIVER.findElement(By.name("/mycoreobject/metadata/def.heading/heading/firstName")).sendKeys("Johann Wolfgang");
        DRIVER.findElement(By.name("/mycoreobject/metadata/def.heading/heading/nameAffix")).sendKeys("von");
        DRIVER.findElement(By.name("/mycoreobject/metadata/def.alternative/alternative/lastName")).sendKeys("Goethe");
        DRIVER.findElement(By.name("/mycoreobject/metadata/def.alternative/alternative/firstName"))
                .sendKeys("Johann Wolfgang");
        DRIVER.findElement(By.name("/mycoreobject/metadata/def.alternative/alternative/collocation"))
                .sendKeys("Goethe_test");
        DRIVER.findElement(By.name("/mycoreobject/metadata/def.alternative/alternative/nameAffix")).sendKeys("von");
        DRIVER.findElement(By.name("/mycoreobject/metadata/def.alternative/alternative[2]/name"))
                .sendKeys("Goethe_test");
        DRIVER.findElement(By.name("/mycoreobject/metadata/def.alternative/alternative[2]/collocation"))
                .sendKeys("Goethe_test");
        DRIVER.findElement(By.name("/mycoreobject/metadata/def.alternative/alternative[2]/nameAffix"))
                .sendKeys("Goethe_test");
        DRIVER.findElement(By.name("/mycoreobject/metadata/def.peerage/peerage")).sendKeys("test");
        WebElement select = DRIVER.findElement(By.name("/mycoreobject/metadata/def.gender/gender/@categid"));
        select.findElement(By.xpath("option[@value='male']")).click();
        select = DRIVER.findElement(By.name("/mycoreobject/metadata/def.contact/contact/@type"));
        select.findElement(By.xpath("option[@value='email']")).click();
        DRIVER.findElement(By.name("/mycoreobject/metadata/def.contact/contact")).sendKeys("test");
        DRIVER.findElement(By.name("/mycoreobject/metadata/def.role/role"))
                .sendKeys("Publizist, Politiker, Jurist usw.");
        DRIVER.findElement(By.name("/mycoreobject/metadata/def.placeOfActivity/placeOfActivity")).sendKeys("test");
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
        select = DRIVER.findElement(By.name("/mycoreobject/metadata/def.note/note/@type"));
        select.findElement(By.xpath("option[@value='visible']")).click();
        DRIVER.findElement(By.name("/mycoreobject/metadata/def.note/note")).sendKeys("test");
        DRIVER.findElement(By.name("/mycoreobject/metadata/def.link/link/@xlink:href")).sendKeys("test.test");
        select = DRIVER.findElement(By.name("/mycoreobject/metadata/def.identifier/identifier/@type"));
        select.findElement(By.xpath("option[@value='gnd']")).click();
        DRIVER.findElement(By.name("/mycoreobject/metadata/def.identifier/identifier")).sendKeys("test");
        DRIVER.findElement(By.name("/mycoreobject/metadata/def.doubletOf/doubletOf")).sendKeys("test");
        TestUtils.saveForm(DRIVER);
    }

    @Ignore
    @Test
    public void createInstitution() throws Exception {
        WebDriverWait wait = new WebDriverWait(DRIVER, 2);
        By adminButton = By.xpath("//button[@class='btn btn-default fas fa-cog dropdown-toggle']");
        wait.until(ExpectedConditions.elementToBeClickable(adminButton));
        DRIVER.findElement(adminButton).click();

        DRIVER.findElement(By.name("Neue Institution")).click();
        assertEquals("language not changed - content does not match", "Neue Institution anlegen",
                DRIVER.findElement(By.id("xeditor-title")).getText());

        createInstSet();

        assertEquals("title does not match", "Thüringer Universitäts- und Landesbibliothek Jena - JPortal",
                DRIVER.getTitle());
        assertEquals("header does not match", "Thüringer Universitäts- und Landesbibliothek Jena",
                DRIVER.findElement(By.id("jp-maintitle")).getText());

        TestUtils.deletObj(DRIVER, "");
    }

    private void createInstSet() throws InterruptedException {
        WebDriverWait wait = new WebDriverWait(DRIVER, 2);
        // fill form
        By waitForLoad = By.name("/mycoreobject/metadata/names/name/fullname");
        wait.until(ExpectedConditions.elementToBeClickable(waitForLoad));
        DRIVER.findElement(waitForLoad).sendKeys("Thüringer Universitäts- und Landesbibliothek Jena");
        DRIVER.findElement(By.name("/mycoreobject/metadata/names/name/nickname")).sendKeys("ThULB");
        DRIVER.findElement(By.name("/mycoreobject/metadata/names/name/property")).sendKeys("test");
        DRIVER.findElement(By.name("/mycoreobject/metadata/alternatives/alternative/name")).sendKeys("ThULB");
        DRIVER.findElement(By.name("/mycoreobject/metadata/addresses/address/country")).sendKeys("Deutschland");
        DRIVER.findElement(By.name("/mycoreobject/metadata/addresses/address/zipcode")).sendKeys("07743");
        DRIVER.findElement(By.name("/mycoreobject/metadata/addresses/address/city")).sendKeys("Jena");
        DRIVER.findElement(By.name("/mycoreobject/metadata/addresses/address/street")).sendKeys("Bibliotheksplatz");
        DRIVER.findElement(By.name("/mycoreobject/metadata/addresses/address/number")).sendKeys("2");
        WebElement instSelectElement = DRIVER.findElement(By.name("/mycoreobject/metadata/phones/phone/@type"));
        instSelectElement.findElement(By.xpath("option[@value='Telefon']")).click();
        DRIVER.findElement(By.name("/mycoreobject/metadata/phones/phone")).sendKeys("+49 3641 9-40100");
        DRIVER.findElement(By.name("/mycoreobject/metadata/urls/url/@xlink:title")).sendKeys("ThULB");
        DRIVER.findElement(By.name("/mycoreobject/metadata/urls/url/@xlink:href"))
                .sendKeys("http://www.thulb.uni-jena.de");
        DRIVER.findElement(By.name("/mycoreobject/metadata/emails/email")).sendKeys("testEmail@test.de");
        DRIVER.findElement(By.name("/mycoreobject/metadata/notes/note")).sendKeys("Bibliothek");
        instSelectElement = DRIVER.findElement(By.name("/mycoreobject/metadata/identifiers/identifier/@type"));
        instSelectElement.findElement(By.xpath("option[@value='gnd']")).click();
        DRIVER.findElement(By.name("/mycoreobject/metadata/identifiers/identifier")).sendKeys("test");
        DRIVER.findElement(By.name("/mycoreobject/metadata/def.doubletOf/doubletOf")).sendKeys("test");

        DRIVER.findElement(By.id("thumbLogoPlain")).click();
        Thread.sleep(500);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#personSelect-modal-body > div > a")));
        assertNotNull(DRIVER.findElement(By.cssSelector("#personSelect-modal-body > .editor-logoSelect-container")));
        assertNotNull(DRIVER.findElement(By.cssSelector("#personSelect-modal-body > div > a:first-child > svg")));
        assertNotNull(
                DRIVER.findElement(By.cssSelector("#personSelect-modal-body > div > a:first-child > h5")).getText());
        assertEquals("Logo Auswahl aus Ordner: /logos/",
                DRIVER.findElement(By.id("personSelect-modal-title")).getText());

        DRIVER.findElement(By.className("glyphicon-folder-open")).click();
        waitForLoad = By.xpath("//div[@id='personSelect-modal-body']/div/a[1]");
        wait.until(ExpectedConditions.elementToBeClickable(waitForLoad));
        DRIVER.findElement(waitForLoad).click();
        DRIVER.findElement(By.id("personSelect-send")).click();

        By logoPic = By.cssSelector("#thumbLogoPlain > svg");
        wait.until(ExpectedConditions.presenceOfElementLocated(logoPic));
        assertNotNull(DRIVER.findElement(logoPic));

        TestUtils.saveForm(DRIVER);
    }

    @Ignore
    @Test
    public void createJournal() throws Exception {
        TestUtils.createMinPerson(DRIVER, "testPerson");

        TestUtils.home(DRIVER);
        TestUtils.clickCreatSelect(DRIVER, "Neue Zeitschrift");
        assertEquals("content does not match", "Neue Zeitschrift anlegen",
                DRIVER.findElement(By.id("xeditor-title")).getText());

        creatJournalSet();
        assertEquals("header does not match", "testJournal", DRIVER.findElement(By.id("jp-maintitle")).getText());

        TestUtils.deletObj(DRIVER, "testJournal");
        TestUtils.deletObj(DRIVER, "testPerson");
    }

    private void creatJournalSet() {
        WebDriverWait wait = new WebDriverWait(DRIVER, 2);
        By waitForLoad = By.name("/mycoreobject/metadata/maintitles/maintitle");
        wait.until(ExpectedConditions.elementToBeClickable(waitForLoad));
        DRIVER.findElement(waitForLoad).sendKeys("testJournal");
        WebElement participantSelect = DRIVER
                .findElement(By.name("/mycoreobject/metadata/participants/participant/@type"));
        participantSelect.findElement(By.xpath("option[@value='mainPublisher']")).click();

        DRIVER.findElement(By.xpath("//button[@class='btn btn-default jp-personSelect-person']")).click();
        By subselect = By.xpath("//div[@id='resultList']/div/div/div/a");
        wait.until(ExpectedConditions.elementToBeClickable(subselect));
        DRIVER.findElement(subselect).click();
        DRIVER.findElement(By.id("personSelect-send")).click();

        WebElement fromSelect = DRIVER.findElement(By.id("dateSelect"));
        fromSelect.findElement(By.xpath("option[@value='published_from']")).click();
        DRIVER.findElement(By.xpath("//div[@id='fromDateContainer']/input[@placeholder='Jahr']")).sendKeys("1795");
        fromSelect.findElement(By.xpath("//div[@id='untilDateContainer']/input[@placeholder='Jahr']")).sendKeys("1797");
        WebElement select = DRIVER.findElement(By.name("/mycoreobject/metadata/traditions/tradition/@type"));
        select.findElement(By.xpath("option[@value='otherLocation']")).click();
        DRIVER.findElement(By.name("/mycoreobject/metadata/traditions/tradition")).sendKeys("testJournal");
        select = DRIVER.findElement(By.name("/mycoreobject/metadata/identis/identi/@type"));
        select.findElement(By.xpath("option[@value='bvb']")).click();
        DRIVER.findElement(By.name("/mycoreobject/metadata/identis/identi")).sendKeys("testJournal");
        WebElement langSelect = DRIVER.findElement(By.name("/mycoreobject/metadata/languages/language/@categid"));
        langSelect.findElement(By.xpath("option[@value='de']")).click();
        DRIVER.findElement(By.name("/mycoreobject/metadata/rights/right")).sendKeys("testJournal");
        DRIVER.findElement(By.name("/mycoreobject/metadata/predeces/predece/@xlink:title")).sendKeys("testJournal");
        DRIVER.findElement(By.name("/mycoreobject/metadata/predeces/predece/@xlink:href")).sendKeys("testJournal");
        DRIVER.findElement(By.name("/mycoreobject/metadata/successors/successor/@xlink:title")).sendKeys("testJournal");
        DRIVER.findElement(By.name("/mycoreobject/metadata/successors/successor/@xlink:href")).sendKeys("testJournal");
        select = DRIVER.findElement(By.name("/mycoreobject/metadata/ddcs/ddc/@categid"));
        select.findElement(By.xpath("option[@value='001']")).click();
        DRIVER.findElement(By.name("/mycoreobject/metadata/abstracts/abstract")).sendKeys("testJournal");
        DRIVER.findElement(By.name("/mycoreobject/metadata/notes/note")).sendKeys("testJournal");
        // journal type
        select = DRIVER.findElement(By.name("/mycoreobject/metadata/contentClassis1/contentClassi1/@categid"));
        select.findElement(By.xpath("option[@value='historical']")).click();
        select = DRIVER.findElement(By.name("/mycoreobject/metadata/contentClassis2/contentClassi2/@categid"));
        select.findElement(By.xpath("option[@value='eventWeimarJena']")).click();
        select = DRIVER.findElement(By.name("/mycoreobject/metadata/contentClassis3/contentClassi3/@categid"));
        select.findElement(By.xpath("option[@value='countryArchiveRudolstadt']")).click();
        select = DRIVER.findElement(By.name("/mycoreobject/metadata/contentClassis4/contentClassi4/@categid"));
        select.findElement(By.xpath("option[@value='AVSL']")).click();
        select = DRIVER.findElement(By.name("/mycoreobject/metadata/contentClassis5/contentClassi5/@categid"));
        select.findElement(By.xpath("option[@value='100-199']")).click();
        select = DRIVER.findElement(By.name("/mycoreobject/metadata/contentClassis6/contentClassi6/@categid"));
        select.findElement(By.xpath("option[@value='a1']")).click();
        select = DRIVER.findElement(By.name("/mycoreobject/metadata/contentClassis7/contentClassi7/@categid"));
        select.findElement(By.xpath("option[@value='empty']")).click();

        DRIVER.findElement(By.name("/mycoreobject/metadata/maintitlesForSorting/maintitleForSorting"))
                .sendKeys("testJournal");
        // template
        select = DRIVER.findElement(By.name("/mycoreobject/metadata/hidden_templates/hidden_template"));
        select.findElement(By.xpath("option[@value='template_DynamicLayoutTemplates']")).click();

        DRIVER.findElement(By.name("/mycoreobject/metadata/hidden_genhiddenfields1/hidden_genhiddenfield1"))
                .sendKeys("testJournal");
        DRIVER.findElement(By.name("/mycoreobject/metadata/hidden_genhiddenfields2/hidden_genhiddenfield2"))
                .sendKeys("testJournal");
        DRIVER.findElement(By.name("/mycoreobject/metadata/hidden_genhiddenfields3/hidden_genhiddenfield3"))
                .sendKeys("testJournal");
        DRIVER.findElement(By.name("/mycoreobject/metadata/hidden_websitecontexts/hidden_websitecontext"))
                .sendKeys("testJournal");

        TestUtils.saveForm(DRIVER);
    }

    @Ignore
    @Test
    public void createVolume() throws Exception {
        TestUtils.creatMinJournal(DRIVER, "testJournal");

        TestUtils.clickCreatSelect(DRIVER, "Neuer Band");
        createVolumeSet();
        assertEquals("header does not match", "testVolume", DRIVER.findElement(By.id("jp-maintitle")).getText());

        TestUtils.deletObj(DRIVER, "testJournal");
    }

    private void createVolumeSet() {
        WebDriverWait wait = new WebDriverWait(DRIVER, 2);
        By waitForLoad = By.name("/mycoreobject/metadata/maintitles/maintitle");
        wait.until(ExpectedConditions.elementToBeClickable(waitForLoad));
        DRIVER.findElement(waitForLoad).sendKeys("testVolume");
        WebElement select = DRIVER.findElement(By.name("/mycoreobject/metadata/subtitles/subtitle/@type"));
        select.findElement(By.xpath("option[@value='title_beside']")).click();
        DRIVER.findElement(By.name("/mycoreobject/metadata/subtitles/subtitle")).sendKeys("testVolume");
        DRIVER.findElement(By.xpath("//div[@id='dateContainer']/input[@placeholder='Jahr']")).sendKeys("2015");
        select = DRIVER.findElement(By.name("/mycoreobject/metadata/traditions/tradition/@type"));
        select.findElement(By.xpath("option[@value='otherLocation']")).click();
        DRIVER.findElement(By.name("/mycoreobject/metadata/traditions/tradition")).sendKeys("testVolume");
        select = DRIVER.findElement(By.name("/mycoreobject/metadata/identis/identi/@type"));
        select.findElement(By.xpath("option[@value='misc']")).click();
        DRIVER.findElement(By.name("/mycoreobject/metadata/identis/identi")).sendKeys("testVolume");
        select = DRIVER.findElement(By.name("/mycoreobject/metadata/collationNotes/collationNote/@type"));
        select.findElement(By.xpath("option[@value='siteDetails']")).click();
        DRIVER.findElement(By.name("/mycoreobject/metadata/collationNotes/collationNote")).sendKeys("123");
        DRIVER.findElement(By.name("/mycoreobject/metadata/abstracts/abstract")).sendKeys("testVolume");
        DRIVER.findElement(By.name("/mycoreobject/metadata/notes/note")).sendKeys("testVolume");
        DRIVER.findElement(By.name("/mycoreobject/metadata/people/person")).sendKeys("testPerson");
        DRIVER.findElement(By.name("/mycoreobject/metadata/publicationNotes/publicationNote")).sendKeys("testVolume");
        DRIVER.findElement(By.name("/mycoreobject/metadata/footNotes/footNote")).sendKeys("testVolume");
        DRIVER.findElement(By.name("/mycoreobject/metadata/bibEvidences/bibEvidence")).sendKeys("testVolume");
        DRIVER.findElement(By.name("/mycoreobject/metadata/indexFields/indexField")).sendKeys("testVolume");
        DRIVER.findElement(By.name("/mycoreobject/metadata/hidden_positions/hidden_position")).sendKeys("testVolume");
        TestUtils.saveForm(DRIVER);
    }

    @Ignore
    @Test
    public void createArticle() throws Exception {
        TestUtils.creatMinJournal(DRIVER, "testJournal");
        TestUtils.creatMinVolume(DRIVER, "testBand");

        TestUtils.clickCreatSelect(DRIVER, "Neuer Artikel");
        createArticleSet();
        assertEquals("header does not match", "testArticle", DRIVER.findElement(By.id("jp-maintitle")).getText());

        TestUtils.deletObj(DRIVER, "testJournal");
    }

    private void createArticleSet() {
        WebDriverWait wait = new WebDriverWait(DRIVER, 2);
        By waitForLoad = By.name("/mycoreobject/metadata/maintitles/maintitle");
        wait.until(ExpectedConditions.elementToBeClickable(waitForLoad));
        DRIVER.findElement(waitForLoad).sendKeys("testArticle");
        WebElement select = DRIVER.findElement(By.name("/mycoreobject/metadata/subtitles/subtitle/@type"));
        select.findElement(By.xpath("option[@value='title_beside']")).click();
        DRIVER.findElement(By.name("/mycoreobject/metadata/subtitles/subtitle")).sendKeys("testArticle");
        DRIVER.findElement(By.xpath("//div[@id='dateContainer']/input[@placeholder='Jahr']")).sendKeys("2015");
        DRIVER.findElement(By.name("/mycoreobject/metadata/refs/ref")).sendKeys("test");
        select = DRIVER.findElement(By.name("/mycoreobject/metadata/identis/identi/@type"));
        select.findElement(By.xpath("option[@value='misc']")).click();
        DRIVER.findElement(By.name("/mycoreobject/metadata/identis/identi")).sendKeys("test");
        DRIVER.findElement(By.name("/mycoreobject/metadata/sizes/size")).sendKeys("1");
        DRIVER.findElement(By.name("/mycoreobject/metadata/keywords/keyword")).sendKeys("test");
        DRIVER.findElement(By.name("/mycoreobject/metadata/abstracts/abstract")).sendKeys("test");
        select = DRIVER.findElement(By.name("/mycoreobject/metadata/notes/note/@type"));
        select.findElement(By.xpath("option[@value='internalNote']")).click();
        DRIVER.findElement(By.name("/mycoreobject/metadata/notes/note")).sendKeys("test");
        TestUtils.saveForm(DRIVER);
    }

    @Ignore
    @Test
    public void articleVolumeSetting() throws Exception {
        TestUtils.clickCreatSelect(DRIVER, "Neue Zeitschrift");
        journalSettings();
        TestUtils.saveForm(DRIVER);

        TestUtils.clickCreatSelect(DRIVER, "Neuer Band");
        volumeSettings();
        TestUtils.saveForm(DRIVER);

        TestUtils.clickCreatSelect(DRIVER, "Neuer Artikel");
        articleSettings();
        TestUtils.saveForm(DRIVER);

        TestUtils.deletObj(DRIVER, "testJournal");
    }

    private void articleSettings() {
        WebDriverWait wait = new WebDriverWait(DRIVER, 2);
        By waitForLoad = By.name("/mycoreobject/metadata/maintitles/maintitle");
        wait.until(ExpectedConditions.elementToBeClickable(waitForLoad));
        DRIVER.findElement(waitForLoad).sendKeys("testArticle");

        WebElement select = DRIVER.findElement(By.name("/mycoreobject/metadata/types/type/@categid"));
        select.findElement(By.xpath("option[@value='00002000']")).click();
        select = DRIVER.findElement(By.name("/mycoreobject/metadata/rubrics/rubric/@categid"));
        select.findElement(By.xpath("option[@value='00003000']")).click();
        select = DRIVER.findElement(By.name("/mycoreobject/metadata/classispub/classipub/@categid"));
        select.findElement(By.xpath("option[@value='author']")).click();
        select = DRIVER.findElement(By.name("/mycoreobject/metadata/classispub2/classipub2/@categid"));
        select.findElement(By.xpath("option[@value='calender']")).click();
        select = DRIVER.findElement(By.name("/mycoreobject/metadata/classispub3/classipub3/@categid"));
        select.findElement(By.xpath("option[@value='published']")).click();
        select = DRIVER.findElement(By.name("/mycoreobject/metadata/classispub4/classipub4/@categid"));
        select.findElement(By.xpath("option[@value='siteDetails']")).click();
    }

    private void volumeSettings() {
        WebDriverWait wait = new WebDriverWait(DRIVER, 2);
        By waitForLoad = By.name("/mycoreobject/metadata/maintitles/maintitle");
        wait.until(ExpectedConditions.elementToBeClickable(waitForLoad));
        DRIVER.findElement(waitForLoad).sendKeys("testVolume");

        WebElement select = DRIVER
                .findElement(By.name("/mycoreobject/metadata/volContentClassis1/volContentClassi1/@categid"));
        select.findElement(By.xpath("option[@value='land-thueringen']")).click();
        select = DRIVER.findElement(By.name("/mycoreobject/metadata/volContentClassis2/volContentClassi2/@categid"));
        select.findElement(By.xpath("option[@value='00002000']")).click();
        select = DRIVER.findElement(By.name("/mycoreobject/metadata/volContentClassis3/volContentClassi3/@categid"));
        select.findElement(By.xpath("option[@value='report']")).click();
        select = DRIVER.findElement(By.name("/mycoreobject/metadata/volContentClassis4/volContentClassi4/@categid"));
        select.findElement(By.xpath("option[@value='article']")).click();
        select = DRIVER.findElement(By.name("/mycoreobject/metadata/volContentClassis5/volContentClassi5/@categid"));
        select.findElement(By.xpath("option[@value='otherLocation']")).click();
        select = DRIVER.findElement(By.name("/mycoreobject/metadata/volContentClassis6/volContentClassi6/@categid"));
        select.findElement(By.xpath("option[@value='internalNote']")).click();
    }

    private void journalSettings() {
        WebDriverWait wait = new WebDriverWait(DRIVER, 2);
        By waitForLoad = By.name("/mycoreobject/metadata/maintitles/maintitle");
        wait.until(ExpectedConditions.elementToBeClickable(waitForLoad));
        DRIVER.findElement(waitForLoad).sendKeys("testJournal");

        WebElement select = DRIVER.findElement(By.name("/mycoreobject/metadata/languages/language/@categid"));
        select.findElement(By.xpath("option[@value='de']")).click();
        select = DRIVER.findElement(By.name("/mycoreobject/metadata/hidden_templates/hidden_template"));
        select.findElement(By.xpath("option[@value='template_DynamicLayoutTemplates']")).click();

        //volume settings
        select = DRIVER.findElement(By.name("/mycoreobject/metadata/hidden_classiVol1/hidden_classiVol1"));
        select.findElement(By.xpath("option[@value='jportal_laws_territory']")).click();
        select = DRIVER.findElement(By.name("/mycoreobject/metadata/hidden_classiVol2/hidden_classiVol2"));
        select.findElement(By.xpath("option[@value='jportal_class_00000048']")).click();
        select = DRIVER.findElement(By.name("/mycoreobject/metadata/hidden_classiVol3/hidden_classiVol3"));
        select.findElement(By.xpath("option[@value='jportal_class_00000065']")).click();
        select = DRIVER.findElement(By.name("/mycoreobject/metadata/hidden_classiVol4/hidden_classiVol4"));
        select.findElement(By.xpath("option[@value='jportal_class_00000052']")).click();
        select = DRIVER.findElement(By.name("/mycoreobject/metadata/hidden_classiVol5/hidden_classiVol5"));
        select.findElement(By.xpath("option[@value='jportal_class_00000080']")).click();
        select = DRIVER.findElement(By.name("/mycoreobject/metadata/hidden_classiVol6/hidden_classiVol6"));
        select.findElement(By.xpath("option[@value='jportal_class_00000060']")).click();

        //article settings
        select = DRIVER.findElement(By.name("/mycoreobject/metadata/hidden_pubTypesID/hidden_pubTypeID"));
        select.findElement(By.xpath("option[@value='jportal_class_00000048']")).click();
        select = DRIVER.findElement(By.name("/mycoreobject/metadata/hidden_classispub/hidden_classipub"));
        select.findElement(By.xpath("option[@value='jportal_class_00000007']")).click();
        select = DRIVER.findElement(By.name("/mycoreobject/metadata/hidden_classispub2/hidden_classipub2"));
        select.findElement(By.xpath("option[@value='jportal_class_00000090']")).click();
        select = DRIVER.findElement(By.name("/mycoreobject/metadata/hidden_classispub3/hidden_classipub3"));
        select.findElement(By.xpath("option[@value='jportal_class_00000009']")).click();
        select = DRIVER.findElement(By.name("/mycoreobject/metadata/hidden_classispub4/hidden_classipub4"));
        select.findElement(By.xpath("option[@value='jportal_class_00000082']")).click();
        select = DRIVER.findElement(By.name("/mycoreobject/metadata/hidden_rubricsID/hidden_rubricID"));
        select.findElement(By.xpath("option[@value='jportal_class_00000044']")).click();
    }

    @Ignore
    @Test
    public void delete() throws Exception {
        WebDriverWait wait = new WebDriverWait(DRIVER, 2);
        // create
        By adminButton = By.xpath("//button[@class='btn btn-default fas fa-cog dropdown-toggle']");
        wait.until(ExpectedConditions.elementToBeClickable(adminButton));
        DRIVER.findElement(adminButton).click();

        DRIVER.findElement(By.name("Neue Institution")).click();
        DRIVER.findElement(By.name("/mycoreobject/metadata/names/name/fullname")).sendKeys("Uni Jena");
        TestUtils.saveForm(DRIVER);
        assertEquals("header does not match", "Uni Jena", DRIVER.findElement(By.id("jp-maintitle")).getText());

        // delete
        By deletButton = By.xpath("//button[@class='btn btn-default fas fa-cog dropdown-toggle']");
        wait.until(ExpectedConditions.elementToBeClickable(deletButton));
        DRIVER.findElement(deletButton).click();

        DRIVER.findElement(By.id("deleteDocButton")).click();

        By deletOk = By.id("delete-dialog-submit");
        wait.until(ExpectedConditions.elementToBeClickable(deletOk));
        DRIVER.findElement(deletOk).click();

        //waiting a bit until it deleted
        Thread.sleep(500);
        //check if ok
        assertEquals("delet failed", "Löschen erfolgreich!", DRIVER.findElement(By.id("delete-dialog-info")).getText());
        DRIVER.findElement(By.id("delete-dialog-close")).click();
    }

    @Ignore
    @Test
    public void importObj() throws Exception {
        WebDriverWait wait = new WebDriverWait(DRIVER, 2);
        TestUtils.clickCreatSelect(DRIVER, "Person/Institution importieren");

        //gnd for Bach, Wilhelm Friedemann
        DRIVER.findElement(By.id("inputField")).sendKeys("118505548");
        DRIVER.findElement(By.id("search")).click();

        By waitImport = By.id("doubletCheck");
        wait.until(ExpectedConditions.elementToBeClickable(waitImport));

        assertEquals("title does not match",
                "Datensatz kann importiert werden. Keine Dubletten (identische GND-ID) gefunden.",
                DRIVER.findElement(waitImport).getText());

        assertEquals("title does not match", "Bach, Wilhelm Friedemann",
                DRIVER.findElement(By.xpath("//div[@id='result']/div/dd[1]")).getText());

        DRIVER.findElement(By.linkText("Datensatz importieren")).click();

        Thread.sleep(600);

        assertThat(DRIVER.findElement(By.xpath("//div[@class='result']/p")).getText(),
                containsString("Datensatz erfolgreich importiert."));

        DRIVER.findElement(By.linkText("Link zum Objekt")).click();

        TestUtils.deletObj(DRIVER, "");
    }

    @Ignore
    @Test
    public void writeComments() throws Exception {
        WebDriverWait wait = new WebDriverWait(DRIVER, 2);
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
    }

    @Ignore
    @Test
    public void moveObjChild() throws Exception {
        WebDriverWait wait = new WebDriverWait(DRIVER, 30);
        TestUtils.creatMinJournal(DRIVER, "testJournalNoChild");
        String[] noChildJournal = DRIVER.getCurrentUrl().split("/");

        TestUtils.creatMinJournal(DRIVER, "testJournalWithChild");
        TestUtils.creatMinVolume(DRIVER, "testVolumeChild");

        DRIVER.findElement(By.linkText("testJournalWithChild")).click();

        TestUtils.clickCreatSelect(DRIVER, "Kinder verschieben");

        DRIVER.findElement(By.id("mom_checkbox_childlist_all")).click();
        DRIVER.findElement(By.id("mom_radio_search_filter2")).click();
        DRIVER.findElement(By.id("mom_search_button")).click();
        DRIVER.findElement(By.xpath("//div[@data-objid='" + noChildJournal[noChildJournal.length - 1] + "']/input"))
                .click();
        DRIVER.findElement(By.id("mom_button_move")).click();

        By waitForMove = By.id("jp-maintitle");
        wait.until(ExpectedConditions.elementToBeClickable(waitForMove));
        assertEquals("text does not match", "testJournalNoChild", DRIVER.findElement(waitForMove).getText());
        assertEquals("text does not match", "testVolumeChild",
                DRIVER.findElement(By.linkText("testVolumeChild")).getText());

        TestUtils.deletObj(DRIVER, "");
        TestUtils.goToObj(DRIVER, "testJournalWithChild");

        if (DRIVER.findElements(By.linkText("testVolumeChild")).size() > 0) {
            fail("Element still exist in the first parent journal.");
        }

        TestUtils.deletObj(DRIVER, "testJournalWithChild");
    }

    //edit journal, volumen, article
    @Ignore
    @Test
    public void editJournal() throws Exception {
        TestUtils.createMinPerson(DRIVER, "testPerson");
        TestUtils.creatMinJournal(DRIVER, "testJournalOriginal");
        TestUtils.creatMinVolume(DRIVER, "testVolumeOriginal");
        TestUtils.creatMinArticle(DRIVER, "testArticleOriginal");

        //****************** edit Article *********************************
        assertEquals("text does not match", "testArticleOriginal", DRIVER.findElement(By.id("jp-maintitle")).getText());
        TestUtils.clickCreatSelect(DRIVER, "Dokument bearbeiten");
        DRIVER.findElement(By.name("/mycoreobject/metadata/maintitles/maintitle")).clear();
        createArticleSet();
        assertEquals("text does not match", "testArticle", DRIVER.findElement(By.id("jp-maintitle")).getText());
        //*****************************************************************

        //****************** edit Volumen *********************************
        DRIVER.findElement(By.linkText("testVolumeOriginal")).click();
        assertEquals("text does not match", "testVolumeOriginal", DRIVER.findElement(By.id("jp-maintitle")).getText());
        TestUtils.clickCreatSelect(DRIVER, "Dokument bearbeiten");
        DRIVER.findElement(By.name("/mycoreobject/metadata/maintitles/maintitle")).clear();
        createVolumeSet();
        assertEquals("text does not match", "testVolume", DRIVER.findElement(By.id("jp-maintitle")).getText());
        //*****************************************************************

        //****************** edit Journal *********************************
        DRIVER.findElement(By.linkText("testJournalOriginal")).click();
        assertEquals("text does not match", "testJournalOriginal", DRIVER.findElement(By.id("jp-maintitle")).getText());
        TestUtils.clickCreatSelect(DRIVER, "Dokument bearbeiten");
        DRIVER.findElement(By.name("/mycoreobject/metadata/maintitles/maintitle")).clear();
        creatJournalSet();
        assertEquals("text does not match", "testJournal", DRIVER.findElement(By.id("jp-maintitle")).getText());
        //*****************************************************************

        TestUtils.deletObj(DRIVER, "testJournal");
        TestUtils.deletObj(DRIVER, "testPerson");
    }

    @Ignore
    @Test
    public void editPerson() throws Exception {
        WebDriverWait wait = new WebDriverWait(DRIVER, 2);
        TestUtils.createMinPerson(DRIVER, "testPerson");

        assertEquals("text does not match", "testPerson", DRIVER.findElement(By.id("jp-maintitle")).getText());
        TestUtils.clickCreatSelect(DRIVER, "Dokument bearbeiten");
        By waitForLoad = By.name("/mycoreobject/metadata/def.heading/heading/lastName");
        wait.until(ExpectedConditions.elementToBeClickable(waitForLoad));
        DRIVER.findElement(waitForLoad).clear();
        createPersonSet();
        assertEquals("header does not match", "Goethe, Johann Wolfgang von",
                DRIVER.findElement(By.id("jp-maintitle")).getText());

        TestUtils.deletObj(DRIVER, "");
    }

    @Ignore
    @Test
    public void editInst() throws Exception {
        TestUtils.creatMinInst(DRIVER, "testInst");

        assertEquals("header does not match", "testInst", DRIVER.findElement(By.id("jp-maintitle")).getText());
        TestUtils.clickCreatSelect(DRIVER, "Dokument bearbeiten");
        DRIVER.findElement(By.name("/mycoreobject/metadata/names/name/fullname")).clear();
        createInstSet();
        Thread.sleep(500);
        assertEquals("header does not match", "Thüringer Universitäts- und Landesbibliothek Jena",
                DRIVER.findElement(By.id("jp-maintitle")).getText());

        TestUtils.deletObj(DRIVER, "");
    }

    @Ignore
    @Test
    public void multipleParticipants() throws Exception {
        WebDriverWait wait = new WebDriverWait(DRIVER, 2);
        TestUtils.createMinPerson(DRIVER, "FirstPerson");
        TestUtils.createMinPerson(DRIVER, "SecondPerson");
        TestUtils.creatMinInst(DRIVER, "FirstInst");

        TestUtils.clickCreatSelect(DRIVER, "Neue Zeitschrift");

        By waitForLoad = By.name("/mycoreobject/metadata/maintitles/maintitle");
        wait.until(ExpectedConditions.elementToBeClickable(waitForLoad));
        DRIVER.findElement(waitForLoad).sendKeys("testJournal");
        WebElement langSelect = DRIVER.findElement(By.name("/mycoreobject/metadata/languages/language/@categid"));
        langSelect.findElement(By.xpath("option[@value='de']")).click();
        WebElement templateSelect = DRIVER
                .findElement(By.name("/mycoreobject/metadata/hidden_templates/hidden_template"));
        templateSelect.findElement(By.xpath("option[@value='template_DynamicLayoutTemplates']")).click();

        DRIVER.findElement(By.xpath("//div[@title='Formale Beschreibung']/div[5]/div[3]/button[1]")).click();

        By testButton = By.xpath("//div[@title='Formale Beschreibung']/div[6]/div[2]/div[3]/button[1]");
        wait.until(ExpectedConditions.presenceOfElementLocated(testButton));

        //ersten und zweiten select auf Herausgeber
        WebElement participantSelect = DRIVER
                .findElement(By.name("/mycoreobject/metadata/participants/participant/@type"));
        participantSelect.findElement(By.xpath("option[@value='mainPublisher']")).click();

        participantSelect = DRIVER.findElement(By.name("/mycoreobject/metadata/participants/participant[2]/@type"));
        participantSelect.findElement(By.xpath("option[@value='mainPublisher']")).click();

        DRIVER.findElement(By.xpath("//div[@title='Formale Beschreibung']/div[6]/div[2]/div[3]/button[1]")).click();
        By subselect = By.xpath("//a[@class='list-group-item']/h4[contains(.,'SecondPerson')]");
        wait.until(ExpectedConditions.elementToBeClickable(subselect));
        DRIVER.findElement(subselect).click();
        DRIVER.findElement(By.id("personSelect-send")).click();

        Thread.sleep(500);
        assertEquals("", DRIVER.findElement(
                By.xpath("//div[@title='Formale Beschreibung']/div[5]/div[2]/div[@class='jp-personSelect-name']/div"))
                .getText());
        assertTrue(
                DRIVER.findElement(By.xpath("//div[@title='Formale Beschreibung']/div[6]/div[2]/div[2]/div")).getText()
                        .contains("SecondPerson"));

        DRIVER.findElement(By.xpath("//div[@title='Formale Beschreibung']/div[5]/div[2]/div[3]/button[1]")).click();
        subselect = By.xpath("//a[@class='list-group-item']/h4[contains(.,'FirstPerson')]");
        wait.until(ExpectedConditions.elementToBeClickable(subselect));
        DRIVER.findElement(subselect).click();
        DRIVER.findElement(By.id("personSelect-send")).click();

        Thread.sleep(500);
        assertTrue(
                DRIVER.findElement(By.xpath("//div[@title='Formale Beschreibung']/div[5]/div[2]/div[2]/div")).getText()
                        .contains("FirstPerson"));
        assertTrue(
                DRIVER.findElement(By.xpath("//div[@title='Formale Beschreibung']/div[6]/div[2]/div[2]/div")).getText()
                        .contains("SecondPerson"));

        DRIVER.findElement(By.xpath("//div[@title='Formale Beschreibung']/div[6]/div[2]/div[3]/button[2]")).click();
        subselect = By.xpath("//a[@class='list-group-item']/h4[contains(.,'FirstInst')]");
        wait.until(ExpectedConditions.elementToBeClickable(subselect));
        DRIVER.findElement(subselect).click();
        DRIVER.findElement(By.id("personSelect-send")).click();

        Thread.sleep(500);
        assertTrue(
                DRIVER.findElement(By.xpath("//div[@title='Formale Beschreibung']/div[5]/div[2]/div[2]/div")).getText()
                        .contains("FirstPerson"));
        assertTrue(
                DRIVER.findElement(By.xpath("//div[@title='Formale Beschreibung']/div[6]/div[2]/div[2]/div")).getText()
                        .contains("FirstInst"));

        TestUtils.saveForm(DRIVER);

        TestUtils.deletObj(DRIVER, "");
        TestUtils.deletObj(DRIVER, "FirstPerson");
        TestUtils.deletObj(DRIVER, "SecondPerson");
        TestUtils.deletObj(DRIVER, "FirstInst");
    }

    @Ignore
    @Test
    public void impressum_partner_test() throws Exception {
        TestUtils.creatMinJournal(DRIVER, "testJournal");

        partnerImpressum("Impressum");
        TestUtils.goToObj(DRIVER, "testJournal");
        partnerImpressum("Partner");

        TestUtils.deletObj(DRIVER, "testJournal");
    }

    private void partnerImpressum(String name) throws Exception {
        WebDriverWait wait = new WebDriverWait(DRIVER, 2);
        TestUtils.clickCreatSelect(DRIVER, name + " auswählen");

        Thread.sleep(500);
        By waitForLoad = By.id("imprint-preview-title");
        wait.until(ExpectedConditions.presenceOfElementLocated(waitForLoad));
        assertEquals(name + " auswählen", DRIVER.findElement(waitForLoad).getText());
        createEntry("test" + name, "teste" + name + "Text", name);
        createEntry("testDelete", "testeDeleteText", name);

        DRIVER.findElement(By.id("imprint-delete-btn")).click();

        Thread.sleep(500);
        waitForLoad = By.id("imprint-alert-delete-btn");
        wait.until(ExpectedConditions.presenceOfElementLocated(waitForLoad));
        assertEquals("testDelete löschen?", DRIVER.findElement(By.id("imprint-alert-delete-title")).getText());
        DRIVER.findElement(waitForLoad).click();

        assertTrue(DRIVER.findElements(By.xpath("//div[contains(text(), 'testDelete')]")).size() == 0);

        DRIVER.findElement(By.id("imprint-preview-save")).click();

        DRIVER.navigate().refresh();

        waitForLoad = By.linkText(name);
        wait.until(ExpectedConditions.presenceOfElementLocated(waitForLoad));
        DRIVER.findElement(waitForLoad).click();
        assertEquals("teste" + name + "Text", DRIVER.findElement(By.xpath("//div[@id='main']/span/div")).getText());
    }

    private void createEntry(String name, String text, String containerName) throws InterruptedException {
        DRIVER.findElement(By.id("imprint-new-btn")).click();

        Thread.sleep(600);

        WebDriverWait wait = new WebDriverWait(DRIVER, 2);
        By waitForLoad = By.id("imprint-editor-input");
        wait.until(ExpectedConditions.presenceOfElementLocated(waitForLoad));

        assertEquals(containerName + " anlegen", DRIVER.findElement(By.id("imprint-new-title")).getText());

        DRIVER.findElement(waitForLoad).sendKeys(name);

        DRIVER.switchTo().frame(DRIVER.findElement(By.className("cke_wysiwyg_frame")));
        DRIVER.findElement(By.className("cke_editable")).sendKeys(text);

        DRIVER.switchTo().defaultContent();

        DRIVER.findElement(By.id("imprint-editor-save")).click();

        assertEquals(name, DRIVER.findElement(By.cssSelector(".list-group-item.active")).getText());
        assertEquals(text, DRIVER.findElement(By.id("imprint-preview")).getText());
    }
}
