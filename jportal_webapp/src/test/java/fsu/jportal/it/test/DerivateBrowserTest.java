package fsu.jportal.it.test;

import static org.junit.Assert.*;

import java.io.File;

import com.google.common.base.Function;
import org.junit.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import fsu.jportal.it.BaseIntegrationTest;
import fsu.jportal.it.TestUtils;

public class DerivateBrowserTest extends BaseIntegrationTest {
    WebDriverWait wait = new WebDriverWait(DRIVER, 10);
    static JavascriptExecutor js = (JavascriptExecutor)DRIVER;
    Actions builder = new Actions(DRIVER);

    @Test
    public void testCreateJournal(){
        TestUtils.home(DRIVER);
        TestUtils.login(DRIVER);
        DRIVER.get(BaseIntegrationTest.getStartUrl() + "/rsc/derivatebrowser/start");
        assertEquals("invald index page - title does not match", "Jportal Derivate Browser", DRIVER.getTitle());

        //create documents
        WebElement testJournal = createJournal("TestZeitschrift");
        String testJournalName = getDocName(testJournal);

        WebElement testVolume = createVolume(testJournal, "TestBand");
        String testVolumeName = getDocName(testVolume);

        WebElement testArticle = createArticle(testVolume, "TestArtikel");

        //upload derivate
        String testImgName = "test.tif";
        WebElement testDerivate = createDerivate(testArticle, testImgName);
        String derivateID = getDocName(testDerivate);

        //test derivate functions
        DRIVER.findElement(By.id("btn-urnAll")).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("btn-urnAll")));
        
        DRIVER.findElement(By.id("btn-hide")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("derivate-hidden")));
        DRIVER.findElement(By.id("btn-hide")).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("derivate-hidden")));
        
        DRIVER.findElement(By.id("btn-viewer")).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector("ol.chapterTreeDesktop > li > a"), derivateID));
        DRIVER.navigate().back();
        assertEquals("invald index page - title does not match", "Jportal Derivate Browser", DRIVER.getTitle());
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector("li.derivat > div.folder-name"), derivateID));
        
        DRIVER.findElement(By.id("btn-tileDeri")).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("alert-area"), "Derivat wird gekachelt."));
        
        //upload second image
        String testImgSecName = "test2.tif";
        uploadImagesToDerivate(getDocFromName(derivateID), new String[]{testImgSecName});

        //filter table
        DRIVER.findElement(By.id("btn-filter-table")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("btn-filter-table-input")));
        DRIVER.findElement(By.cssSelector("#btn-filter-table-input > input")).sendKeys(testImgName);
        DRIVER.findElement(By.cssSelector("#btn-filter-table-input > input")).sendKeys(Keys.RETURN);
        assertTrue(testImgSecName + " not filtered", (boolean)
                js.executeScript("return ($('tr.browser-table-file > td.browser-table-file-name:contains(" + testImgSecName + "):visible').length == 0)"));
        assertTrue(testImgName + " filtered", (boolean)
                js.executeScript("return ($('tr.browser-table-file > td.browser-table-file-name:contains(" + testImgName + "):visible').length != 0)"));
        DRIVER.findElement(By.id("btn-filter-table-input-remove")).click();

        //get URN
        builder.moveToElement(DRIVER.findElement(By.className("no-urn"))).build().perform();
        DRIVER.findElement(By.className("btn-new-urn")).click();

        //rename
        renameDerivateFile("test3.tif");
        assertTrue("invald startfile name after change name", DRIVER.findElement(By.id("derivat-panel-startfile-label")).getText().endsWith("test3.tif"));

        //select all
        DRIVER.findElement(By.className("btn-check-all")).click();
        //noinspection UnnecessaryBoxing
        assertEquals("not all selected", Long.valueOf(2), (js.executeScript("return $('tr.browser-table-file.checked').length")));
        DRIVER.findElement(By.className("btn-check-all")).click();
        //noinspection UnnecessaryBoxing
        assertEquals("not all selected", Long.valueOf(0), (js.executeScript("return $('tr.browser-table-file.checked').length")));

        //delete single file
        builder.moveToElement(DRIVER.findElement(By.className("btns"))).build().perform();
        DRIVER.findElement(By.className("btn-delete")).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("alert-area"), "Die Startdatei kann nicht gelöscht werden."));
        deleteDerivateFile(1);

        //create Folder
        String folderName = "TestFolder";
        DRIVER.findElement(By.className("btn-add")).click();
        DRIVER.findElement(By.className("input-new")).sendKeys(folderName);
        DRIVER.findElement(By.cssSelector("td.browser-table-file-name > input")).sendKeys(Keys.RETURN);
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("td.browser-table-file-name > input")));
        assertEquals("invald name after create folder", folderName, DRIVER.findElement(By.cssSelector("tr.browser-table-folder  > td.browser-table-file-name")).getText());

        //go in folder
        DRIVER.findElement(By.className("btn-folder")).click();
        assertTrue("new folder is not empty", !DRIVER.findElements(By.id("browser-table-alert")).isEmpty());

        //upload 2 more images
        uploadImagesToDerivate(getDocFromName(folderName), new String[]{"test.tif", "test2.tif"});

        //rename file
        renameDerivateFile("test4.tif");
        deleteDerivateFile(1);

        //move file
        String moveName =  DRIVER.findElement(By.className("browser-table-file-name")).getText();
        builder.moveToElement(DRIVER.findElement(By.className("btns"))).perform();
        DRIVER.findElement(By.className("btn-check")).click();
        DRIVER.findElement(By.className("btn-move-all")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("lightbox-multi-move-confirm")));
        WebElement moveTarget = (WebElement) js.executeScript("return $('li.target-folder-entry > div.folder-name:contains(" + derivateID + ")')[0]");
        moveTarget.click();
        DRIVER.findElement(By.id("lightbox-multi-move-confirm")).click();
        assertTrue("after move, folder is not empty", !DRIVER.findElements(By.id("browser-table-alert")).isEmpty());
        DRIVER.findElement(By.className("derivate-browser-breadcrumb-entry")).click();
        assertTrue("moved file not found", (boolean) js.executeScript("return ($('td.browser-table-file-name:contains(" + moveName + ")').length > 0)"));

        //delete folder
        builder.moveToElement(DRIVER.findElement(By.cssSelector("tr.browser-table-folder div.btns"))).build().perform();
        DRIVER.findElement(By.cssSelector("tr.browser-table-folder div.btns > span.btn-delete")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("lightbox-alert-delete-confirm")));
        DRIVER.findElement(By.id("lightbox-alert-delete-confirm")).click();
        assertTrue("folder not deleted", (boolean) js.executeScript("return ($('td.browser-table-folder:contains(" + folderName + ")').length == 0)"));

        //go to large view
        DRIVER.findElement(By.id("btn-large-view")).click();
        assertTrue("large view not opened", !DRIVER.findElements(By.id("view-large-normal")).isEmpty());

        //select in large view
        DRIVER.findElement(By.className("btn-check-large")).click();
        assertTrue("file in large view not selected", DRIVER.findElement(By.id("view-large-panel-collapse")).getAttribute("class").contains("checked"));
        DRIVER.findElement(By.id("button-view-large-close")).click();
        //noinspection UnnecessaryBoxing
        assertEquals("file in normal view not selected", Long.valueOf(1), (js.executeScript("return $('tr.browser-table-file.checked').length")));
        DRIVER.findElement(By.className("btn-check")).click();
        DRIVER.findElement(By.id("btn-large-view")).click();
        assertTrue("file in large view selected",!DRIVER.findElement(By.id("view-large-panel-collapse")).getAttribute("class").contains("checked"));

        //rename in large view
        String newName = "test5.tif";
        DRIVER.findElement(By.className("btn-edit-large")).click();
        DRIVER.findElement(By.id("view-large-panel-input")).clear();
        DRIVER.findElement(By.id("view-large-panel-input")).sendKeys(newName);
        DRIVER.findElement(By.id("view-large-panel-input")).sendKeys(Keys.ENTER);
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("view-large-panel-input")));
        assertEquals("wrong name after rename in large view", newName, DRIVER.findElement(By.id("view-large-panel-title")).getText());
        DRIVER.findElement(By.id("button-view-large-close")).click();
        //noinspection UnnecessaryBoxing
        assertEquals("wrong name, in normal view, after rename in large view", newName, DRIVER.findElement(By.className("browser-table-file-name")).getText());

        //drag and drop startfile
        String newStartFileName = DRIVER.findElements(By.className("browser-table-file-name")).get(1).getText();
        dragAndDrop(DRIVER.findElements(By.className("popover-file")).get(1), DRIVER.findElement(By.id("panel-img")));
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("alert-area"), "Startdatei erfolgreich geändert."));
        assertEquals("wrong starfilename", "/" + newStartFileName, DRIVER.findElement(By.id("derivat-panel-startfile-label")).getText());

        //go to large view over popover
        builder.moveToElement(DRIVER.findElements(By.className("popover-file")).get(1)).perform();
        wait.until(ExpectedConditions.elementToBeClickable(By.className("popover-img")));
        DRIVER.findElement(By.className("popover-img")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("file-view-large")));
        assertEquals("wrong image opened", newStartFileName, DRIVER.findElement(By.id("view-large-panel-title")).getText());

        //delete in large view
        DRIVER.findElement(By.className("btn-delete-large")).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("alert-area"), "Die Startdatei kann nicht gelöscht werden."));
        DRIVER.findElement(By.id("view-large-left")).click();
        DRIVER.findElement(By.className("btn-delete-large")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("lightbox-alert-delete-confirm")));
        DRIVER.findElement(By.id("lightbox-alert-delete-confirm")).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("alert-area"), "erfolgreich gelöscht."));
        DRIVER.findElement(By.id("button-view-large-close")).click();
        //noinspection UnnecessaryBoxing
        assertEquals("not delete in large view", Long.valueOf(1), (js.executeScript("return $('tr.browser-table-file').length")));

        //create second article
        String testArticleSecName = "TestArtikel2";
        WebElement testArticleSec = createArticle(getDocFromName(testVolumeName), testArticleSecName);

        //drag and drop move derivate
        dragAndDrop(DRIVER.findElement(By.cssSelector("li.derivat > .folder-name")), testArticleSec);
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("li.article:nth-of-type(2) > .button-expand")));
        DRIVER.findElement(By.cssSelector("li.article:nth-of-type(2) > .button-expand")).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("li.article:nth-of-type(2) > .button-contract")));
        WebElement newParent = DRIVER.findElement(By.cssSelector("li.derivat")).findElement(By.xpath("../.."));
        assertEquals("Derivate partent name wrong", testArticleSecName, newParent.findElement(By.className("folder-name")).getText());

        //create second volume
        String testVolumeSecName = "TestBand2";
        WebElement testVolumeSec = createVolume(getDocFromName(testJournalName), testVolumeSecName);

        //drag and drop move article
        dragAndDrop(DRIVER.findElement(By.cssSelector("li.article > .folder-name")), DRIVER.findElement(By.cssSelector("li.volume:nth-of-type(2) > .folder-name")));
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("li.volume:nth-of-type(2) > .button-expand")));
        DRIVER.findElement(By.cssSelector("li.volume:nth-of-type(2) > .button-expand")).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("li.volume:nth-of-type(2) > .button-contract")));
        newParent = DRIVER.findElements(By.cssSelector("li.article")).get(1).findElement(By.xpath("../.."));
        assertEquals("Article partent name wrong, after move", testVolumeSecName, newParent.findElement(By.className("folder-name")).getText());

        //create another article
        String testArticleThirdName = "TestArtikel3";
        createArticle(testVolumeSec, testArticleThirdName);

        //drag and drop move multiple
        builder.keyDown(Keys.SHIFT).click(DRIVER.findElements(By.cssSelector("li.article > div.folder-name")).get(1)).keyUp(Keys.SHIFT).perform();
        dragAndDrop(DRIVER.findElements(By.cssSelector("li.article > div.folder-name")).get(1), DRIVER.findElement(By.cssSelector("li.volume > .folder-name")));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("li.volume:nth-of-type(2) > .button-contract")));
        newParent = DRIVER.findElements(By.cssSelector("li.article")).get(2).findElement(By.xpath("../.."));
        assertEquals("Article partent name wrong, after move multiple", newParent.findElement(By.className("folder-name")).getText(),
                DRIVER.findElement(By.cssSelector("li.volume > .folder-name")).getText());

        //delete multiple article
        DRIVER.findElement(By.cssSelector("li.article:nth-of-type(2) div.folder-name")).click();
        builder.keyDown(Keys.SHIFT).click(DRIVER.findElement(By.cssSelector("li.article:nth-of-type(3) > div.folder-name"))).keyUp(Keys.SHIFT).perform();
        DRIVER.findElement(By.id("journal-info-button-delete")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("lightbox-alert-delete-docs-confirm")));
        DRIVER.findElement(By.id("lightbox-alert-delete-docs-confirm")).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("alert-area"), "erfolgreich gelöscht."));
        assertEquals("Articels not deleted", 1, DRIVER.findElements(By.cssSelector("li.article")).size());

        //link image
        DRIVER.findElement(By.cssSelector("li.derivat div.folder-name")).click();
        String imgPath = (String) js.executeScript("return $('.browser-table-file').data('docID') + $('.browser-table-file').data('path')");
        dragAndDrop(DRIVER.findElement(By.className("popover-file")), DRIVER.findElement(By.cssSelector("li.volume .folder-name")));
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("alert-area"), "Bild wurde erfolgreich verlinkt."));
        refreshUnitlLinkVisible(getDocFromName(testVolumeName));
        assertEquals("Link path wrong", imgPath, js.executeScript("return $('div.link-info > h6').html()"));

        //test link and remove link
        DRIVER.findElement(By.className("link-preview-img")).click();
        assertEquals("Link path wrong", imgPath.substring(imgPath.lastIndexOf("/") + 1), DRIVER.findElement(By.id("view-large-panel-title")).getText());
        DRIVER.findElement(By.cssSelector("#view-large-link-list > li > a")).click();
        assertEquals("Link path wrong", imgPath, js.executeScript("return $('div.link-info > h6').html()"));
        builder.moveToElement(DRIVER.findElement(By.className("link-info"))).perform();
        DRIVER.findElement(By.className("btn-remove-link")).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("alert-area"), "Verlinkung wurde erfolgreich entfernt."));
        assertTrue("link not removed", (Boolean) js.executeScript("return $('#journal-info-linklist').hasClass('hidden')"));

        //add link in large view
        DRIVER.findElement(By.cssSelector("li.derivat > .folder-name")).click();
        DRIVER.findElement(By.id("btn-large-view")).click();
        dragAndDrop(DRIVER.findElement(By.id("view-large-normal")), DRIVER.findElement(By.cssSelector("li.volume .folder-name")));
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("alert-area"), "Bild wurde erfolgreich verlinkt."));
        refreshUnitlLinkVisible(getDocFromName(testVolumeName));
        assertEquals("Link path wrong", imgPath, js.executeScript("return $('div.link-info > h6').html()"));

        //delete link again
        builder.moveToElement(DRIVER.findElement(By.className("link-info"))).perform();
        DRIVER.findElement(By.className("btn-remove-link")).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("alert-area"), "Verlinkung wurde erfolgreich entfernt."));
        assertTrue("link not removed", (Boolean) js.executeScript("return $('#journal-info-linklist').hasClass('hidden')"));

        //test go To function
        String currentVolumeName = DRIVER.findElement(By.cssSelector("li.aktiv div.folder-name")).getText();
        DRIVER.findElement(By.id("journal-info-button-goToPage")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("jp-maintitle")));
        assertEquals("wrong Volume name", currentVolumeName, DRIVER.findElement(By.id("jp-maintitle")).getText());
        DRIVER.navigate().back();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector("li.aktiv > div.folder-name"), currentVolumeName));
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
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("lightbox-alert-deleteDoc-confirm")));
        DRIVER.findElement(By.id("lightbox-alert-deleteDoc-confirm")).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("alert-area"), "Dokument(e) erfolgreich gelöscht."));
        assertEquals("Derivate not deleted", null, getDocFromName(derivateID));

        deleteDoc(testArticleSecName, false);

        deleteDoc(testVolumeName, false);

        deleteDoc(testJournalName, true);
    }

    private void uploadImage (String dropZone, String img) {
        File testImg = new File(img);
        String inputId = "FileUploadTest";
        js.executeScript("$('<input/>').attr({id: '" + inputId + "', type:'file'}).css({position: 'absolute', top: 0}).appendTo('body');");
        DRIVER.findElement(By.id(inputId)).sendKeys(testImg.getAbsolutePath());
        js.executeScript("e = $.Event('drop'); e.originalEvent = {dataTransfer : { files : $('#" + inputId + "').get(0).files } }; $('#" + dropZone + "').trigger(e);");
        js.executeScript("$('#" + inputId + "').remove()");
    }

    private void renameDerivateFile (String newName){
        builder.moveToElement(DRIVER.findElement(By.className("btns"))).build().perform();
        DRIVER.findElement(By.className("btn-edit")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("td.browser-table-file-name > input")));
        DRIVER.findElement(By.cssSelector("td.browser-table-file-name > input")).clear();
        DRIVER.findElement(By.cssSelector("td.browser-table-file-name > input")).sendKeys(newName);
        DRIVER.findElement(By.cssSelector("td.browser-table-file-name > input")).sendKeys(Keys.RETURN);
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("td.browser-table-file-name > input")));
        assertEquals("invald name after change name", newName, DRIVER.findElement(By.className("browser-table-file-name")).getText());
    }

    private void deleteDerivateFile (int file) {
        builder.moveToElement(DRIVER.findElements(By.className("btns")).get(file)).build().perform();
        DRIVER.findElements(By.className("btn-delete")).get(file).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("lightbox-alert-delete-confirm")));
        DRIVER.findElement(By.id("lightbox-alert-delete-confirm")).click();
    }

    private void dragAndDrop (WebElement elementToDrop, WebElement dropZone) {
        builder.moveToElement(elementToDrop).perform();
        builder.clickAndHold(elementToDrop).perform();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        builder.moveToElement(dropZone).perform();
        builder.release().perform();
    }

    private WebElement createJournal (String journalName) {
        DRIVER.findElement(By.id("folder-list-new-choose")).click();
        DRIVER.findElement(By.id("folder-list-new-button-journal")).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("doc-editor-form")));
        assertTrue("can not find Journal Editor", !DRIVER.findElements(By.id("doc-editor-form")).isEmpty());
        DRIVER.findElement(By.name("/mycoreobject/metadata/maintitles/maintitle")).sendKeys(journalName);
        WebElement langSelect = DRIVER.findElement(By.name("/mycoreobject/metadata/languages/language/@categid"));
        langSelect.findElement(By.xpath("option[@value='de']")).click();
        WebElement templateSelect = DRIVER.findElement(By.name("/mycoreobject/metadata/hidden_templates/hidden_template"));
        templateSelect.findElement(By.xpath("option[@value='template_DynamicLayoutTemplates']")).click();
        DRIVER.findElement(By.id("journal-info-button-save")).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector("li.aktiv > .folder-name"), journalName));
        return DRIVER.findElement(By.cssSelector("li.aktiv"));
    }

    private WebElement createVolume (WebElement parent, String volumeName) {
        parent.findElement(By.className("folder-name")).click();
        DRIVER.findElement(By.id("folder-list-new-choose")).click();
        DRIVER.findElement(By.id("folder-list-new-button-volume")).click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("doc-editor-form")));
        assertTrue("can not find Volume Editor", !DRIVER.findElements(By.id("doc-editor-form")).isEmpty());
        DRIVER.findElement(By.name("/mycoreobject/metadata/maintitles/maintitle")).sendKeys(volumeName);
        DRIVER.findElement(By.id("journal-info-button-save")).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector("li.aktiv > .folder-name"), volumeName));
        return DRIVER.findElement(By.cssSelector("li.aktiv"));
    }

    private WebElement createArticle (WebElement parent, String articleName) {
        parent.findElement(By.className("folder-name")).click();
        DRIVER.findElement(By.id("folder-list-new-choose")).click();
        DRIVER.findElement(By.id("folder-list-new-button-article")).click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("doc-editor-form")));
        assertTrue("can not find Volume Editor", !DRIVER.findElements(By.id("doc-editor-form")).isEmpty());
        DRIVER.findElement(By.name("/mycoreobject/metadata/maintitles/maintitle")).sendKeys(articleName);
        DRIVER.findElement(By.id("journal-info-button-save")).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector("li.aktiv > .folder-name"), articleName));
        return DRIVER.findElement(By.cssSelector("li.aktiv"));
    }

    private WebElement createDerivate (WebElement parent, String fileName) {
        parent.findElement(By.className("folder-name")).click();
        DRIVER.findElement(By.id("folder-list-new-choose")).click();
        DRIVER.findElement(By.id("folder-list-new-button-derivate")).click();
        uploadImage("lightbox-new-derivate-main", "src/test/resources/img/" + fileName);
        DRIVER.findElement(By.id("lightbox-new-derivate-confirm")).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("lightbox-new-derivate-done")));
        DRIVER.findElement(By.id("lightbox-new-derivate-done")).click();
        assertTrue("can not find DerivateView", !DRIVER.findElements(By.id("derivate-browser")).isEmpty());
        return DRIVER.findElement(By.cssSelector("li.aktiv"));
    }

    private void uploadImagesToDerivate (WebElement derivate, String[] imgNames) {
        derivate.findElement(By.className("folder-name")).click();
        js.executeScript("$('#upload-overlay').removeClass('hidden')");
        for (String imgName : imgNames) {
            js.executeScript("$('#upload-overlay').removeClass('hidden')");
            uploadImage("upload-overlay", "src/test/resources/img/" + imgName);
        }
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.className("upload-preview-status"), "Hochgeladen"));
        assertTrue("can not find UploadBar", !DRIVER.findElements(By.id("upload-status-bar-body")).isEmpty());
        DRIVER.findElement(By.className("btn-close-usb")).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("upload-status-bar-body")));
    }

    private void deleteDoc (final String docName, boolean isJournal) {
        getDocFromName(docName).findElement(By.className("folder-name")).click();
        DRIVER.findElement(By.id("journal-info-button-delete")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("lightbox-alert-deleteDoc-confirm")));
        DRIVER.findElement(By.id("lightbox-alert-deleteDoc-confirm")).click();
        if (isJournal){
            wait.until(new Function<WebDriver, Boolean>() {
                public Boolean apply(WebDriver driver) {
                    return js.executeScript("return $('li.aktiv').length") == Long.valueOf(0);
                }
            });
        }
        else {
            wait.until(ExpectedConditions.not(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector("li.aktiv > div.folder-name"), docName)));
        }
        assertEquals(docName + " not deleted", null, getDocFromName(docName));
    }

    private String getDocName (WebElement elm) {
        return elm.findElement(By.className("folder-name")).getText();
    }

    private WebElement getDocFromName(String name) {
        return (WebElement) js.executeScript("return $('div.folder-name').filter(function(){ return $(this).text() === '" + name +"';}).parent()[0]");
    }

    private String editDoc (String oldName, String newName) {
        getDocFromName(oldName).findElement(By.cssSelector("div.folder-name")).click();
        DRIVER.findElement(By.id("journal-info-button-edit")).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("doc-editor-form")));
        DRIVER.findElement(By.name("/mycoreobject/metadata/maintitles/maintitle")).clear();
        DRIVER.findElement(By.name("/mycoreobject/metadata/maintitles/maintitle")).sendKeys(newName);
        DRIVER.findElement(By.id("journal-info-button-save")).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("alert-area"), "Dokument erfolgreich geändert."));
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector("li.aktiv > .folder-name"), newName));
        return newName;
    }

    private void refreshUnitlLinkVisible (final WebElement doc) {
        doc.findElement(By.cssSelector("div.folder-name")).click();
        wait.until(new Function<WebDriver, WebElement>() {
            public WebElement apply(WebDriver driver) {
                doc.findElement(By.cssSelector("div.folder-name")).click();
                return driver.findElement(By.cssSelector("div.link-info > h6"));
            }
        });
    }
}
