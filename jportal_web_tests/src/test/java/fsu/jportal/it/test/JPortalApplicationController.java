package fsu.jportal.it.test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.mycore.iview.tests.DriverFactory;
import org.mycore.iview.tests.model.TestDerivate;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

import org.mycore.iview.tests.controller.*;
import org.mycore.iview.tests.base.NavbarIT;

import fsu.jportal.it.BaseIntegrationTest;
import fsu.jportal.it.TestUtils;

public class JPortalApplicationController extends ApplicationController {
	
	@Override
	public void init() {

	}

	static final Logger LOGGER = Logger
			.getLogger(JPortalApplicationController.class);

	private static final String FILE_LOCATION = "target/files";

	private static Map<TestDerivate, String> derivateURLCache = new Hashtable<>();
	private static Map<TestDerivate, List<String>> derivateFilesCache = new Hashtable<>();

	// in HashSet derivat merken ---- hashMap hashTable
	@Override
	public void setUpDerivate(WebDriver webdriver, TestDerivate testDerivate) {
		String derivateUrl = null;
		if (!derivateURLCache.containsKey(testDerivate)) {
			ArrayList<String> files = new ArrayList<String>();
			downloadDerivateFiles(testDerivate, files);
			derivateFilesCache.put(testDerivate, files);

			// den Bereich dann ändern zu
			 TestUtils.home(webdriver);
			 TestUtils.login(webdriver);
			 TestUtils.creatMinJournal(webdriver, "testJournal");
//			 TestUtils.clickCreatSelect(webdriver, "Datei Hochladen");
//			setUpJournal(webdriver);
			derivateUrl = webdriver.getCurrentUrl();
			derivateURLCache.put(testDerivate, derivateUrl);
			uploadFiles(webdriver, testDerivate);
		} else {
			derivateUrl = derivateURLCache.get(testDerivate);
			webdriver.get(derivateUrl);
		}
	}

	private void uploadFiles(WebDriver webdriver, TestDerivate testDerivate) {
		List<String> files = derivateFilesCache.get(testDerivate);
		for (int i = 0; i < files.size(); i++) {
			boolean isFirstFile = i % 3 == 0;
			boolean isLastFile = i % 3 == 2;

			if (isFirstFile) {
				if (i == 0) {
					TestUtils.clickCreatSelect(webdriver, "Datei Hochladen");
//					clickUploadButton(webdriver);
				} else {
					webdriver.findElement(By.linkText("Dateien hinzufügen")).click();
				}
			}

			int slot = (i % 3) + 1;
			String file = files.get(i);
			setFileInput(webdriver, file, slot);

			if (isLastFile || i == files.size() - 1) {
				webdriver.findElement(By.xpath("//input[@value='Speichern']")).click();
			}
		}
	}

	private void setFileInput(WebDriver webdriver, String file, int slot) {
		WebElement fileInput;
		if (slot == 1) {
			fileInput = webdriver.findElement(By.name("/upload/path"));
		} else {
			fileInput = webdriver.findElement(By.name("/upload/path[" + slot + "]"));
		}

		fileInput.sendKeys(new File(file).getAbsolutePath());
	}

	private void clickUploadButton(WebDriver webdriver) {
		webdriver
				.findElement(
						By.xpath("//button[@class='btn btn-default fa fa-gear dropdown-toggle']"))
				.click();
		webdriver.findElement(By.name("Datei hochladen")).click();
	}

	private void setUpJournal(WebDriver webdriver) {
		/***************************************************************************************/
		webdriver.get("http://localhost:8291/jportal/content/below/index.xml");
		webdriver.findElement(
				By.xpath("//div[@id='navbar-collapse-globalHeader']/ul/li[2]/a"))
				.click();
		WebElement uid = webdriver.findElement(By.xpath("//input[@name='uid']"));
		uid.sendKeys("administrator");
		webdriver.findElement(By.xpath("//input[@name='pwd']")).sendKeys(
				"alleswirdgut");
		uid.submit();
		webdriver
				.get("http://localhost:8291/jportal/editor/start.xed?type=jpjournal&action=create");
		webdriver.findElement(
				By.name("/mycoreobject/metadata/maintitles/maintitle")).sendKeys(
				"testJournal");
		WebElement langSelect = webdriver.findElement(By
				.name("/mycoreobject/metadata/languages/language/@categid"));
		langSelect.findElement(By.xpath("option[@value='de']")).click();
		WebElement templateSelect = webdriver.findElement(By
				.name("/mycoreobject/metadata/hidden_templates/hidden_template"));
		templateSelect.findElement(
				By.xpath("option[@value='template_DynamicLayoutTemplates']")).click();
		webdriver.findElement(
				By.xpath("//input[@type='submit' and @value='Speichern']")).click();
		/***************************************************************************************/
	}

	private void downloadDerivateFiles(TestDerivate testDerivate,
			ArrayList<String> extractedFiles) {
		try {
			URL zipLocation = testDerivate.getZipLocation();
			InputStream openStream = zipLocation.openStream();
			new File(FILE_LOCATION).mkdirs();
			if (testDerivate.getStartFile().endsWith(".pdf")) {
				String file = FILE_LOCATION + "/" + testDerivate.getStartFile();
				extractedFiles.add(file);
				IOUtils.copy(openStream, new FileOutputStream(file));
			} else {
				ZipInputStream zipInputStream = new ZipInputStream(
						new ByteArrayInputStream(IOUtils.toByteArray(openStream)));
				ControllerUtil.extractZip(FILE_LOCATION, extractedFiles, zipInputStream);
				LOGGER.info("Extracting ZIP...");
			}
		} catch (IOException e) {
			LOGGER.error("Error while setting up derivate", e);
		}
	}

	@Override
	public void shutDownDerivate(WebDriver webdriver, TestDerivate testDerivate) {
		webdriver.findElement(By.xpath("//button[@data-id='CloseViewerButton']"))
				.click();
	}

	@Override
	public void openViewer(WebDriver webdriver, TestDerivate testDerivate) {
		webdriver.findElement(By.className("thumbnail")).click();
	}
}