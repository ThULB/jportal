package fsu.jportal.it.test;

import static org.junit.Assert.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Function;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import fsu.jportal.it.BaseIntegrationTest;

import javax.imageio.ImageIO;

public class DerivateBrowserTest extends BaseIntegrationTest {

    private static final Logger LOGGER = LogManager.getLogger(DerivateBrowserTest.class);

    WebDriverWait WAIT = new WebDriverWait(DRIVER, 10);

    static JavascriptExecutor JS = (JavascriptExecutor) DRIVER;

    Actions BUILDER = new Actions(DRIVER);

    Color IMAGECOLOR = Color.magenta;

    int COLORMARGIN = 10;

    int POPOVERPIXELCOUNT = 24000;

    int LARGEVIEWMIDPIXELCOUNT = 212112;

    int LARGEVIEWLARGEPIXELCOUNT = 495000;

    @Ignore
    @Test
    public void testCreateJournal() {
        DRIVER.manage().window().setSize(new Dimension(1440, 872));
        DRIVER.get(BaseIntegrationTest.getStartUrl() + "/rsc/derivatebrowser/start");
        assertEquals("invald index page - title does not match", "Jportal Derivate Browser", DRIVER.getTitle());

        //create documents
        WebElement testJournal = createJournal("TestZeitschrift");
        String testJournalName = getDocName(testJournal);

        WebElement testVolume = createVolume(testJournal, "TestBand");
        String testVolumeName = getDocName(testVolume);

        WebElement testArticle = createArticle(testVolume, "TestArtikel");

        //upload derivate
        String testImgName = "test.png";
        WebElement testDerivate = createDerivate(testArticle, testImgName);
        String derivateID = getDocName(testDerivate);

        //check if uploaded image is right
        checkUploadedImage(DRIVER.findElement(By.className("popover-file")));

        //test derivate functions
        DRIVER.findElement(By.id("btn-urnAll")).click();
        WAIT.until(ExpectedConditions.invisibilityOfElementLocated(By.id("btn-urnAll")));

        DRIVER.findElement(By.id("btn-hide")).click();
        WAIT.until(ExpectedConditions.visibilityOfElementLocated(By.id("derivate-hidden")));
        DRIVER.findElement(By.id("btn-hide")).click();
        WAIT.until(ExpectedConditions.invisibilityOfElementLocated(By.id("derivate-hidden")));

        DRIVER.findElement(By.id("btn-viewer")).click();
        WAIT.until(ExpectedConditions
                .textToBePresentInElementLocated(By.cssSelector("ol.chapterTreeDesktop > li > a"), derivateID));
        DRIVER.navigate().back();
        assertEquals("invald index page - title does not match", "Jportal Derivate Browser", DRIVER.getTitle());
        WAIT.until(ExpectedConditions
                .textToBePresentInElementLocated(By.cssSelector("li.derivat > div.folder-name"), derivateID));

        DRIVER.findElement(By.id("btn-tileDeri")).click();
        WAIT.until(ExpectedConditions.textToBePresentInElementLocated(By.id("alert-area"), "Derivat wird gekachelt."));

        //upload second image
        String testImgSecName = "test2.png";
        Map<String, String> testImgMap = new HashMap<>();
        testImgMap.put(testImgSecName, "");
        uploadImagesToDerivate(getDocFromName(derivateID), testImgMap);
        checkUploadedImage(DRIVER.findElements(By.className("popover-file")).get(1));

        //filter table
        DRIVER.findElement(By.id("btn-filter-table")).click();
        WAIT.until(ExpectedConditions.visibilityOfElementLocated(By.id("btn-filter-table-input")));
        DRIVER.findElement(By.cssSelector("#btn-filter-table-input > input")).sendKeys(testImgName);
        DRIVER.findElement(By.cssSelector("#btn-filter-table-input > input")).sendKeys(Keys.RETURN);
        assertTrue(testImgSecName + " not filtered", (boolean) JS.executeScript(
                "return ($('tr.browser-table-file > td.browser-table-file-name:contains(" + testImgSecName
                        + "):visible').length == 0)"));
        assertTrue(testImgName + " filtered", (boolean) JS.executeScript(
                "return ($('tr.browser-table-file > td.browser-table-file-name:contains(" + testImgName
                        + "):visible').length != 0)"));
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        DRIVER.findElement(By.id("btn-filter-table-input-remove")).click();

        //get URN
        BUILDER.moveToElement(DRIVER.findElement(By.className("no-urn"))).build().perform();
        DRIVER.findElement(By.className("btn-new-urn")).click();

        //rename
        renameDerivateFile("test3.png");
        assertTrue("invald startfile name after change name",
                DRIVER.findElement(By.id("derivat-panel-startfile-label")).getText().endsWith("test3.png"));

        //select all
        DRIVER.findElement(By.className("btn-check-all")).click();
        //noinspection UnnecessaryBoxing
        assertEquals("not all selected", Long.valueOf(2),
                (JS.executeScript("return $('tr.browser-table-file.checked').length")));
        DRIVER.findElement(By.className("btn-check-all")).click();
        //noinspection UnnecessaryBoxing
        assertEquals("not all selected", Long.valueOf(0),
                (JS.executeScript("return $('tr.browser-table-file.checked').length")));

        //delete single file
        BUILDER.moveToElement(DRIVER.findElement(By.className("btns"))).build().perform();
        DRIVER.findElement(By.className("btn-delete")).click();
        WAIT.until(ExpectedConditions
                .textToBePresentInElementLocated(By.id("alert-area"), "Die Startdatei kann nicht gelöscht werden."));
        deleteDerivateFile(1);

        //create Folder
        String folderName = "TestFolder";
        DRIVER.findElement(By.className("btn-add")).click();
        DRIVER.findElement(By.className("input-new")).sendKeys(folderName);
        DRIVER.findElement(By.cssSelector("td.browser-table-file-name > input")).sendKeys(Keys.RETURN);
        WAIT.until(
                ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("td.browser-table-file-name > input")));
        assertEquals("invald name after create folder", folderName,
                DRIVER.findElement(By.cssSelector("tr.browser-table-folder  > td.browser-table-file-name")).getText());

        //go in folder
        DRIVER.findElement(By.className("btn-folder")).click();
        assertTrue("new folder is not empty", !DRIVER.findElements(By.id("browser-table-alert")).isEmpty());

        //upload 2 more images
        testImgMap = new HashMap<>();
        testImgMap.put("test.png", "");
        testImgMap.put("test2.png", "");
        uploadImagesToDerivate(getDocFromName(folderName), testImgMap);
        checkUploadedImage(DRIVER.findElements(By.className("popover-file")).get(0));
        checkUploadedImage(DRIVER.findElements(By.className("popover-file")).get(1));

        //rename file
        renameDerivateFile("test4.png");
        deleteDerivateFile(1);

        //move file
        String moveName = DRIVER.findElement(By.className("browser-table-file-name")).getText();
        BUILDER.moveToElement(DRIVER.findElement(By.className("btns"))).perform();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        DRIVER.findElement(By.className("btn-check")).click();
        DRIVER.findElement(By.className("btn-move-all")).click();
        WAIT.until(ExpectedConditions.visibilityOfElementLocated(By.id("lightbox-multi-move-confirm")));
        WebElement moveTarget = (WebElement) JS
                .executeScript("return $('li.target-folder-entry > div.folder-name:contains(" + derivateID + ")')[0]");
        moveTarget.click();
        DRIVER.findElement(By.id("lightbox-multi-move-confirm")).click();
        assertTrue("after move, folder is not empty", !DRIVER.findElements(By.id("browser-table-alert")).isEmpty());
        DRIVER.findElement(By.className("derivate-browser-breadcrumb-entry")).click();
        assertTrue("moved file not found", (boolean) JS
                .executeScript("return ($('td.browser-table-file-name:contains(" + moveName + ")').length > 0)"));

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //delete folder
        BUILDER.moveToElement(DRIVER.findElement(By.cssSelector("tr.browser-table-folder div.btns"))).build().perform();
        DRIVER.findElement(By.cssSelector("tr.browser-table-folder div.btns > span.btn-delete")).click();
        WAIT.until(ExpectedConditions.visibilityOfElementLocated(By.id("lightbox-alert-delete-confirm")));
        DRIVER.findElement(By.id("lightbox-alert-delete-confirm")).click();
        assertTrue("folder not deleted", (boolean) JS
                .executeScript("return ($('td.browser-table-folder:contains(" + folderName + ")').length == 0)"));

        //go to large view
        DRIVER.findElement(By.id("btn-large-view")).click();
        assertTrue("large view not opened", !DRIVER.findElements(By.id("view-large-normal")).isEmpty());
        WAIT.until(ExpectedConditions.invisibilityOfElementLocated(By.id("alert-area")));
        assertTrue("Image in large view displayed wrong", displayedImageCorrect(LARGEVIEWMIDPIXELCOUNT, IMAGECOLOR));
        DRIVER.findElement(By.id("view-large-normal")).click();
        assertTrue("Image in large view displayed wrong", displayedImageCorrect(LARGEVIEWLARGEPIXELCOUNT, IMAGECOLOR));
        DRIVER.findElement(By.id("view-large-large")).click();

        //select in large view
        DRIVER.findElement(By.className("btn-check-large")).click();
        assertTrue("file in large view not selected",
                DRIVER.findElement(By.id("view-large-panel-collapse")).getAttribute("class").contains("checked"));
        DRIVER.findElement(By.id("button-view-large-close")).click();
        //noinspection UnnecessaryBoxing
        assertEquals("file in normal view not selected", Long.valueOf(1),
                (JS.executeScript("return $('tr.browser-table-file.checked').length")));
        DRIVER.findElement(By.className("btn-check")).click();
        DRIVER.findElement(By.id("btn-large-view")).click();
        assertTrue("file in large view selected",
                !DRIVER.findElement(By.id("view-large-panel-collapse")).getAttribute("class").contains("checked"));

        //rename in large view
        String newName = "test5.png";
        DRIVER.findElement(By.className("btn-edit-large")).click();
        DRIVER.findElement(By.id("view-large-panel-input")).clear();
        DRIVER.findElement(By.id("view-large-panel-input")).sendKeys(newName);
        DRIVER.findElement(By.id("view-large-panel-input")).sendKeys(Keys.ENTER);
        WAIT.until(ExpectedConditions.invisibilityOfElementLocated(By.id("view-large-panel-input")));
        assertEquals("wrong name after rename in large view", newName,
                DRIVER.findElement(By.id("view-large-panel-title")).getText());
        DRIVER.findElement(By.id("button-view-large-close")).click();
        //noinspection UnnecessaryBoxing
        assertEquals("wrong name, in normal view, after rename in large view", newName,
                DRIVER.findElement(By.className("browser-table-file-name")).getText());

        //drag and drop startfile
        String newStartFileName = DRIVER.findElements(By.className("browser-table-file-name")).get(1).getText();
        dragAndDrop(DRIVER.findElements(By.className("popover-file")).get(1), DRIVER.findElement(By.id("panel-img")));
        WAIT.until(ExpectedConditions
                .textToBePresentInElementLocated(By.id("alert-area"), "Startdatei erfolgreich geändert."));
        assertEquals("wrong starfilename", "/" + newStartFileName,
                DRIVER.findElement(By.id("derivat-panel-startfile-label")).getText());

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //go to large view over popover
        BUILDER.moveToElement(DRIVER.findElements(By.className("popover-file")).get(1)).perform();
        WAIT.until(ExpectedConditions.elementToBeClickable(By.className("popover-img")));
        DRIVER.findElement(By.className("popover-img")).click();
        WAIT.until(ExpectedConditions.visibilityOfElementLocated(By.id("file-view-large")));
        assertEquals("wrong image opened", newStartFileName,
                DRIVER.findElement(By.id("view-large-panel-title")).getText());

        //delete in large view
        DRIVER.findElement(By.className("btn-delete-large")).click();
        WAIT.until(ExpectedConditions
                .textToBePresentInElementLocated(By.id("alert-area"), "Die Startdatei kann nicht gelöscht werden."));
        DRIVER.findElement(By.id("view-large-left")).click();
        DRIVER.findElement(By.className("btn-delete-large")).click();
        WAIT.until(ExpectedConditions.visibilityOfElementLocated(By.id("lightbox-alert-delete-confirm")));
        DRIVER.findElement(By.id("lightbox-alert-delete-confirm")).click();
        WAIT.until(ExpectedConditions.textToBePresentInElementLocated(By.id("alert-area"), "erfolgreich gelöscht."));
        DRIVER.findElement(By.id("button-view-large-close")).click();
        //noinspection UnnecessaryBoxing
        assertEquals("not delete in large view", Long.valueOf(1),
                (JS.executeScript("return $('tr.browser-table-file').length")));

        //create second article
        String testArticleSecName = "TestArtikel2";
        WebElement testArticleSec = createArticle(getDocFromName(testVolumeName), testArticleSecName);

        //drag and drop move derivate
        dragAndDrop(DRIVER.findElement(By.cssSelector("li.derivat > .folder-name")), testArticleSec);
        WAIT.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("li.article:nth-of-type(2) > .button-expand")));
        DRIVER.findElement(By.cssSelector("li.article:nth-of-type(2) > .button-expand")).click();
        WAIT.until(ExpectedConditions
                .elementToBeClickable(By.cssSelector("li.article:nth-of-type(2) > .button-contract")));
        WebElement newParent = DRIVER.findElement(By.cssSelector("li.derivat")).findElement(By.xpath("../.."));
        assertEquals("Derivate partent name wrong", testArticleSecName,
                newParent.findElement(By.className("folder-name")).getText());

        //create second volume
        String testVolumeSecName = "TestBand2";
        WebElement testVolumeSec = createVolume(getDocFromName(testJournalName), testVolumeSecName);

        //drag and drop move article
        dragAndDrop(DRIVER.findElement(By.cssSelector("li.article > .folder-name")),
                DRIVER.findElement(By.cssSelector("li.volume:nth-of-type(2) > .folder-name")));
        WAIT.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("li.volume:nth-of-type(2) > .button-expand")));
        DRIVER.findElement(By.cssSelector("li.volume:nth-of-type(2) > .button-expand")).click();
        WAIT.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("li.volume:nth-of-type(2) > .button-contract")));
        newParent = DRIVER.findElements(By.cssSelector("li.article")).get(1).findElement(By.xpath("../.."));
        assertEquals("Article partent name wrong, after move", testVolumeSecName,
                newParent.findElement(By.className("folder-name")).getText());

        //create another article
        String testArticleThirdName = "TestArtikel3";
        createArticle(testVolumeSec, testArticleThirdName);

        //drag and drop move multiple
        BUILDER.keyDown(Keys.SHIFT).click(DRIVER.findElements(By.cssSelector("li.article > div.folder-name")).get(1))
                .keyUp(Keys.SHIFT).perform();
        dragAndDrop(DRIVER.findElements(By.cssSelector("li.article > div.folder-name")).get(1),
                DRIVER.findElement(By.cssSelector("li.volume > .folder-name")));
        WAIT.until(ExpectedConditions
                .invisibilityOfElementLocated(By.cssSelector("li.volume:nth-of-type(2) > .button-contract")));
        newParent = DRIVER.findElements(By.cssSelector("li.article")).get(2).findElement(By.xpath("../.."));
        assertEquals("Article partent name wrong, after move multiple",
                newParent.findElement(By.className("folder-name")).getText(),
                DRIVER.findElement(By.cssSelector("li.volume > .folder-name")).getText());

        //delete multiple article
        DRIVER.findElement(By.cssSelector("li.article:nth-of-type(2) div.folder-name")).click();
        BUILDER.keyDown(Keys.SHIFT)
                .click(DRIVER.findElement(By.cssSelector("li.article:nth-of-type(3) > div.folder-name")))
                .keyUp(Keys.SHIFT).perform();
        DRIVER.findElement(By.id("journal-info-button-delete")).click();
        WAIT.until(ExpectedConditions.visibilityOfElementLocated(By.id("lightbox-alert-delete-docs-confirm")));
        DRIVER.findElement(By.id("lightbox-alert-delete-docs-confirm")).click();
        WAIT.until(ExpectedConditions.textToBePresentInElementLocated(By.id("alert-area"), "erfolgreich gelöscht."));
        assertEquals("Articels not deleted", 1, DRIVER.findElements(By.cssSelector("li.article")).size());

        //link image
        DRIVER.findElement(By.cssSelector("li.derivat div.folder-name")).click();
        String imgPath = (String) JS
                .executeScript("return $('.browser-table-file').data('docID') + $('.browser-table-file').data('path')");
        dragAndDrop(DRIVER.findElement(By.className("popover-file")),
                DRIVER.findElement(By.cssSelector("li.volume .folder-name")));
        WAIT.until(ExpectedConditions
                .textToBePresentInElementLocated(By.id("alert-area"), "Bild wurde erfolgreich verlinkt."));
        refreshUnitlLinkVisible(getDocFromName(testVolumeName));
        assertEquals("Link path wrong", imgPath, JS.executeScript("return $('div.link-info > h6').html()"));

        //test link and remove link
        DRIVER.findElement(By.className("link-preview-img")).click();
        assertEquals("Link path wrong", imgPath.substring(imgPath.lastIndexOf("/") + 1),
                DRIVER.findElement(By.id("view-large-panel-title")).getText());
        DRIVER.findElement(By.cssSelector("#view-large-link-list > li > a")).click();
        assertEquals("Link path wrong", imgPath, JS.executeScript("return $('div.link-info > h6').html()"));
        BUILDER.moveToElement(DRIVER.findElement(By.className("link-info"))).perform();
        DRIVER.findElement(By.className("btn-remove-link")).click();
        WAIT.until(ExpectedConditions
                .textToBePresentInElementLocated(By.id("alert-area"), "Verlinkung wurde erfolgreich entfernt."));
        assertTrue("link not removed",
                (Boolean) JS.executeScript("return $('#journal-info-linklist').hasClass('hidden')"));

        //add link in large view
        DRIVER.findElement(By.cssSelector("li.derivat > .folder-name")).click();
        DRIVER.findElement(By.id("btn-large-view")).click();
        dragAndDrop(DRIVER.findElement(By.id("view-large-normal")),
                DRIVER.findElement(By.cssSelector("li.volume .folder-name")));
        WAIT.until(ExpectedConditions
                .textToBePresentInElementLocated(By.id("alert-area"), "Bild wurde erfolgreich verlinkt."));
        refreshUnitlLinkVisible(getDocFromName(testVolumeName));
        assertEquals("Link path wrong", imgPath, JS.executeScript("return $('div.link-info > h6').html()"));

        //delete link again
        BUILDER.moveToElement(DRIVER.findElement(By.className("link-info"))).perform();
        DRIVER.findElement(By.className("btn-remove-link")).click();
        WAIT.until(ExpectedConditions
                .textToBePresentInElementLocated(By.id("alert-area"), "Verlinkung wurde erfolgreich entfernt."));
        assertTrue("link not removed",
                (Boolean) JS.executeScript("return $('#journal-info-linklist').hasClass('hidden')"));

        //test go To function
        String currentVolumeName = DRIVER.findElement(By.cssSelector("li.aktiv div.folder-name")).getText();
        DRIVER.findElement(By.id("journal-info-button-goToPage")).click();
        WAIT.until(ExpectedConditions.visibilityOfElementLocated(By.id("jp-maintitle")));
        assertEquals("wrong Volume name", currentVolumeName, DRIVER.findElement(By.id("jp-maintitle")).getText());
        DRIVER.navigate().back();
        WAIT.until(ExpectedConditions
                .textToBePresentInElementLocated(By.cssSelector("li.aktiv > div.folder-name"), currentVolumeName));
        DRIVER.findElement(By.cssSelector("li.aktiv span.button-expand")).click();
        DRIVER.findElement(By.cssSelector("li.aktiv span.button-expand")).click();

        //edit article
        testArticleSecName = editDoc(testArticleSecName, "TestArtikel2Rename");

        //edit volume
        testVolumeName = editDoc(testVolumeName, "TestBandRename");

        //edit journal
        testJournalName = editDoc(testJournalName, "TestZeitschriftRename");

        //delete All
        getDocFromName(derivateID).findElement(By.className("folder-name")).click();
        DRIVER.findElement(By.id("btn-deleteDeri")).click();
        WAIT.until(ExpectedConditions.visibilityOfElementLocated(By.id("lightbox-alert-deleteDoc-confirm")));
        DRIVER.findElement(By.id("lightbox-alert-deleteDoc-confirm")).click();
        WAIT.until(ExpectedConditions
                .textToBePresentInElementLocated(By.id("alert-area"), "Dokument(e) erfolgreich gelöscht."));
        assertEquals("Derivate not deleted", null, getDocFromName(derivateID));

        deleteDoc(testArticleSecName, false);

        deleteDoc(testVolumeName, false);

        deleteDoc(testJournalName, true);
    }

    private String uploadImage(String dropZone, String img) {
        File testImgFile = null;
        String testImgMD5 = "";
        try {
            BufferedImage bufferdTestImg = createImage();
            testImgFile = writeImage(bufferdTestImg, img);
            testImgMD5 = getMD5FromImage(bufferdTestImg);
        } catch (IOException e) {
            assertTrue("could not load or create Image " + img, false);
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            assertTrue("could not get MD5 from Image " + img, false);
            e.printStackTrace();
        }
        assertTrue("could not load or create Image " + img, testImgFile != null);
        assertTrue("could not get MD5 from Image " + img, !testImgMD5.equals(""));
        String inputId = "FileUploadTest";
        JS.executeScript("$('<input/>').attr({id: '" + inputId
                + "', type:'file'}).css({position: 'absolute', top: 0}).appendTo('body');");
        DRIVER.findElement(By.id(inputId)).sendKeys(testImgFile.getAbsolutePath());
        JS.executeScript("e = $.Event('drop'); e.originalEvent = {dataTransfer : { files : $('#" + inputId
                + "').get(0).files } }; $('#" + dropZone + "').trigger(e);");
        JS.executeScript("$('#" + inputId + "').remove()");
        return testImgMD5;
    }

    private void renameDerivateFile(String newName) {
        BUILDER.moveToElement(DRIVER.findElement(By.className("btns"))).build().perform();
        DRIVER.findElement(By.className("btn-edit")).click();
        WAIT.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("td.browser-table-file-name > input")));
        DRIVER.findElement(By.cssSelector("td.browser-table-file-name > input")).clear();
        DRIVER.findElement(By.cssSelector("td.browser-table-file-name > input")).sendKeys(newName);
        DRIVER.findElement(By.cssSelector("td.browser-table-file-name > input")).sendKeys(Keys.RETURN);
        WAIT.until(
                ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("td.browser-table-file-name > input")));
        assertEquals("invald name after change name", newName,
                DRIVER.findElement(By.className("browser-table-file-name")).getText());
    }

    private void deleteDerivateFile(int file) {
        BUILDER.moveToElement(DRIVER.findElements(By.className("btns")).get(file)).build().perform();
        DRIVER.findElements(By.className("btn-delete")).get(file).click();
        WAIT.until(ExpectedConditions.visibilityOfElementLocated(By.id("lightbox-alert-delete-confirm")));
        DRIVER.findElement(By.id("lightbox-alert-delete-confirm")).click();
    }

    private void dragAndDrop(WebElement elementToDrop, WebElement dropZone) {
        BUILDER.moveToElement(elementToDrop).perform();
        BUILDER.clickAndHold(elementToDrop).perform();
        sleep(500);
        BUILDER.moveToElement(dropZone).perform();
        BUILDER.release().perform();
    }

    private WebElement createJournal(String journalName) {
        DRIVER.findElement(By.id("folder-list-new-choose")).click();
        DRIVER.findElement(By.id("folder-list-new-button-journal")).click();
        WAIT.until(ExpectedConditions.presenceOfElementLocated(By.id("doc-editor-form")));
        assertTrue("can not find Journal Editor", !DRIVER.findElements(By.id("doc-editor-form")).isEmpty());
        DRIVER.findElement(By.name("/mycoreobject/metadata/maintitles/maintitle")).sendKeys(journalName);
        WebElement langSelect = DRIVER.findElement(By.name("/mycoreobject/metadata/languages/language/@categid"));
        langSelect.findElement(By.xpath("option[@value='de']")).click();
        WebElement templateSelect = DRIVER
                .findElement(By.name("/mycoreobject/metadata/hidden_templates/hidden_template"));
        templateSelect.findElement(By.xpath("option[@value='template_DynamicLayoutTemplates']")).click();
        DRIVER.findElement(By.id("journal-info-button-save")).click();
        WAIT.until(ExpectedConditions
                .textToBePresentInElementLocated(By.cssSelector("li.aktiv > .folder-name"), journalName));
        return DRIVER.findElement(By.cssSelector("li.aktiv"));
    }

    private WebElement createVolume(WebElement parent, String volumeName) {
        parent.findElement(By.className("folder-name")).click();
        DRIVER.findElement(By.id("folder-list-new-choose")).click();
        DRIVER.findElement(By.id("folder-list-new-button-volume")).click();

        WAIT.until(ExpectedConditions.presenceOfElementLocated(By.id("doc-editor-form")));
        assertTrue("can not find Volume Editor", !DRIVER.findElements(By.id("doc-editor-form")).isEmpty());
        DRIVER.findElement(By.name("/mycoreobject/metadata/maintitles/maintitle")).sendKeys(volumeName);
        DRIVER.findElement(By.id("journal-info-button-save")).click();
        WAIT.until(ExpectedConditions
                .textToBePresentInElementLocated(By.cssSelector("li.aktiv > .folder-name"), volumeName));
        return DRIVER.findElement(By.cssSelector("li.aktiv"));
    }

    private WebElement createArticle(WebElement parent, String articleName) {
        parent.findElement(By.className("folder-name")).click();
        DRIVER.findElement(By.id("folder-list-new-choose")).click();
        DRIVER.findElement(By.id("folder-list-new-button-article")).click();

        WAIT.until(ExpectedConditions.presenceOfElementLocated(By.id("doc-editor-form")));
        assertTrue("can not find Volume Editor", !DRIVER.findElements(By.id("doc-editor-form")).isEmpty());
        DRIVER.findElement(By.name("/mycoreobject/metadata/maintitles/maintitle")).sendKeys(articleName);
        DRIVER.findElement(By.id("journal-info-button-save")).click();
        WAIT.until(ExpectedConditions
                .textToBePresentInElementLocated(By.cssSelector("li.aktiv > .folder-name"), articleName));
        return DRIVER.findElement(By.cssSelector("li.aktiv"));
    }

    private WebElement createDerivate(WebElement parent, String fileName) {
        parent.findElement(By.className("folder-name")).click();
        DRIVER.findElement(By.id("folder-list-new-choose")).click();
        DRIVER.findElement(By.id("folder-list-new-button-derivate")).click();
        String MD5 = uploadImage("lightbox-new-derivate-main", fileName);
        DRIVER.findElement(By.id("lightbox-new-derivate-confirm")).click();
        WAIT.until(ExpectedConditions.presenceOfElementLocated(By.id("lightbox-new-derivate-done")));
        DRIVER.findElement(By.id("lightbox-new-derivate-done")).click();
        assertTrue("can not find DerivateView", !DRIVER.findElements(By.id("derivate-browser")).isEmpty());
        assertEquals("MD5 from uploaded Image " + fileName + " wrong", MD5,
                JS.executeScript("return $('.browser-table-file').data('md5')"));

        return DRIVER.findElement(By.cssSelector("li.aktiv"));
    }

    private void uploadImagesToDerivate(WebElement derivate, Map<String, String> imgNames) {
        derivate.findElement(By.className("folder-name")).click();
        JS.executeScript("$('#upload-overlay').removeClass('hidden')");
        for (Map.Entry<String, String> imgName : imgNames.entrySet()) {
            JS.executeScript("$('#upload-overlay').removeClass('hidden')");
            imgName.setValue(uploadImage("upload-overlay", imgName.getKey()));
        }
        WAIT.until(ExpectedConditions
                .textToBePresentInElementLocated(By.className("upload-preview-status"), "Hochgeladen"));
        assertTrue("can not find UploadBar", !DRIVER.findElements(By.id("upload-status-bar-body")).isEmpty());
        DRIVER.findElement(By.className("btn-close-usb")).click();
        WAIT.until(ExpectedConditions.invisibilityOfElementLocated(By.id("upload-status-bar-body")));
        for (Map.Entry<String, String> imgName : imgNames.entrySet()) {
            assertNotSame("could not get MD5 from Image " + imgName.getKey(), "", imgName.getValue());
            assertEquals("MD5 from uploaded Image " + imgName.getKey() + " wrong", imgName.getValue(), JS.executeScript(
                    "return $('tr.browser-table-file > td.browser-table-file-name:contains(" + imgName.getKey()
                            + ")').parent().data('md5')"));
        }
    }

    private void deleteDoc(final String docName, boolean isJournal) {
        getDocFromName(docName).findElement(By.className("folder-name")).click();
        DRIVER.findElement(By.id("journal-info-button-delete")).click();
        WAIT.until(ExpectedConditions.visibilityOfElementLocated(By.id("lightbox-alert-deleteDoc-confirm")));
        DRIVER.findElement(By.id("lightbox-alert-deleteDoc-confirm")).click();
        if (isJournal) {
            WAIT.until(new Function<WebDriver, Boolean>() {
                public Boolean apply(WebDriver driver) {
                    return JS.executeScript("return $('li.aktiv').length") == Long.valueOf(0);
                }
            });
        } else {
            WAIT.until(ExpectedConditions.not(ExpectedConditions
                    .textToBePresentInElementLocated(By.cssSelector("li.aktiv > div.folder-name"), docName)));
        }
        assertEquals(docName + " not deleted", null, getDocFromName(docName));
    }

    private String getDocName(WebElement elm) {
        return elm.findElement(By.className("folder-name")).getText();
    }

    private WebElement getDocFromName(String name) {
        return (WebElement) JS.executeScript(
                "return $('div.folder-name').filter(function(){ return $(this).text() === '" + name
                        + "';}).parent()[0]");
    }

    private String editDoc(String oldName, String newName) {
        getDocFromName(oldName).findElement(By.cssSelector("div.folder-name")).click();
        DRIVER.findElement(By.id("journal-info-button-edit")).click();
        WAIT.until(ExpectedConditions.presenceOfElementLocated(By.id("doc-editor-form")));
        DRIVER.findElement(By.name("/mycoreobject/metadata/maintitles/maintitle")).clear();
        DRIVER.findElement(By.name("/mycoreobject/metadata/maintitles/maintitle")).sendKeys(newName);
        DRIVER.findElement(By.id("journal-info-button-save")).click();
        WAIT.until(ExpectedConditions
                .textToBePresentInElementLocated(By.id("alert-area"), "Dokument erfolgreich geändert."));
        WAIT.until(
                ExpectedConditions.textToBePresentInElementLocated(By.cssSelector("li.aktiv > .folder-name"), newName));
        return newName;
    }

    private void refreshUnitlLinkVisible(final WebElement doc) {
        doc.findElement(By.cssSelector("div.folder-name")).click();
        WAIT.until(new Function<WebDriver, WebElement>() {
            public WebElement apply(WebDriver driver) {
                doc.findElement(By.cssSelector("div.folder-name")).click();
                return driver.findElement(By.cssSelector("div.link-info > h6"));
            }
        });
    }

    private BufferedImage createImage() throws IOException {
        BufferedImage newBufferedImage = new BufferedImage(600, 1000, BufferedImage.TYPE_INT_RGB);
        Graphics2D newGraphic = newBufferedImage.createGraphics();
        newGraphic.setPaint(Color.magenta);
        newGraphic.fillRect(0, 0, newBufferedImage.getWidth(), newBufferedImage.getHeight());
        //            newGraphic.setFont(new Font("TimesRoman", Font.PLAIN, 50));
        //            newGraphic.setColor(Color.white);
        //            newGraphic.rotate(Math.toRadians(-65));
        //            newGraphic.drawString(imgName, -300, 220);
        return newBufferedImage;
    }

    private File writeImage(BufferedImage image, String imgName) throws IOException {
        File outputPath = new File(OUTPUT_DIRECTORY, this.getClass().getName() + "/tmp/imgs");
        if (!outputPath.exists()) {
            //noinspection ResultOfMethodCallIgnored
            outputPath.mkdirs();
        }
        File outputFile = new File(outputPath, "/" + imgName);
        ImageIO.write(image, "png", outputFile);
        return outputFile;
    }

    private String getMD5FromImage(BufferedImage image) throws IOException, NoSuchAlgorithmException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        byte[] data = baos.toByteArray();
        md5.update(data);
        byte[] md5bytes = md5.digest();
        StringBuilder sb = new StringBuilder();
        for (byte md5byte : md5bytes) {
            sb.append(Integer.toString((md5byte & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }

    private boolean displayedImageCorrect(int rightPixelCount, Color rightColor) {
        BufferedImage testScreen;
        try {
            testScreen = takeScreenshot();
        } catch (IOException e) {
            return false;
        }
        int colorCount = 0;
        if (testScreen != null) {
            BufferedImage reColoredScreen = new BufferedImage(testScreen.getColorModel(), testScreen.copyData(null),
                    testScreen.isAlphaPremultiplied(), null);
            int imageSize = testScreen.getHeight() * testScreen.getWidth();
            for (int i = 0; i < imageSize; i++) {
                int x = i % testScreen.getWidth();
                int y = i / testScreen.getWidth();
                Color colorAtPixel = new Color(testScreen.getRGB(x, y));
                if (similarColor(colorAtPixel, rightColor)) {
                    colorCount++;
                    reColoredScreen.setRGB(x, y, Color.cyan.getRGB());
                }
            }
            if (colorCount != rightPixelCount) {
                System.out.println("Pixel Count differs, expected: " + rightPixelCount + " got: " + colorCount);
                try {
                    ImageIO.write(testScreen, "png",
                            new File(OUTPUT_DIRECTORY, this.getClass().getName() + "/tmp/imgs/screenTestFailed.png"));
                    ImageIO.write(reColoredScreen, "png", new File(OUTPUT_DIRECTORY,
                            this.getClass().getName() + "/tmp/imgs/screenTestFailedReColored.png"));
                } catch (IOException e) {
                    LOGGER.error("Failed to write failed Screenshots");
                }
                return false;
            }
            return true;
        }
        return false;
    }

    private boolean similarColor(Color color1, Color color2) {
        if (color1.equals(color2)) {
            return true;
        }
        int redDiff = Math.abs(color1.getRed() - color2.getRed());
        int greenDiff = Math.abs(color1.getGreen() - color2.getGreen());
        int blueDiff = Math.abs(color1.getBlue() - color2.getBlue());

        return ((redDiff + greenDiff + blueDiff) < COLORMARGIN);
    }

    private void checkUploadedImage(WebElement popover) {
        DRIVER.findElement(By.id("collapse-btn")).click();
        sleep(500);
        BUILDER.moveToElement(popover).perform();
        WAIT.until(ExpectedConditions.visibilityOfElementLocated(By.className("popover-img")));
        sleep(500);
        assertTrue("displayed image differs from uploaded image", displayedImageCorrect(POPOVERPIXELCOUNT, IMAGECOLOR));
        DRIVER.findElement(By.id("collapse-btn")).click();
        sleep(500);
    }

    private void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupted();
        }
    }
}
