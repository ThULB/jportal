package fsu.jportal.it;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.mycore.iview.tests.DriverFactory;
import org.mycore.iview.tests.controller.ApplicationController;
import org.mycore.iview.tests.model.TestDerivate;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import fsu.jportal.it.TestUtils;
import fsu.jportal.it.PaintTestPics;

public class JPortalApplicationController extends ApplicationController {

	@Override
	public void init() {
		createDerivateFiles();
  	WebDriver initDriver = DriverFactory.getFactory().getDriver();
		
  	TestUtils.home(initDriver);
  	TestUtils.login(initDriver);
  	TestUtils.creatMinJournal(initDriver, "testJournal");
		
		journalURl = initDriver.getCurrentUrl();
		uploadFiles(initDriver);
		deleteFiles();
		
  	initDriver.quit();
	}

	static final Logger LOGGER = LogManager.getLogger(JPortalApplicationController.class);

	private static final String FILE_LOCATION = "testImages";

	private String journalURl = null;
	private List <File> files = new ArrayList<File>();
	Path tempDirectory = null; 
	private int testNumber = 5;

	@Override
	public void setUpDerivate(WebDriver webdriver, TestDerivate testDerivate) {
			webdriver.get(journalURl);
	}

	private void uploadFiles(WebDriver webdriver) {
		TestUtils.clickCreatSelect(webdriver, "Datei hochladen");
		
		for(int i = 0, j = 1; i < files.size(); i++, j ++) {
			webdriver.findElement(By.xpath("//table[@class='editorRepeater']/tbody/tr[" + j + "]/td/input[@class='editorFile']")).sendKeys(files.get(i).getAbsolutePath());
			if(j == 3 && i < files.size()) {
				webdriver.findElement(By.xpath("//input[@value='Speichern']")).click();
				webdriver.findElement(By.linkText("Dateien hinzufügen")).click();
				j = 0;
			}
		}
		webdriver.findElement(By.xpath("//input[@value='Speichern']")).click();
	}
	
	private void deleteFiles() {
		for (int i = 0; i < files.size(); i++) {
			if(!files.get(i).getName().equals("mets.xml")){
				files.get(i).delete();
			}
		}
		tempDirectory.toFile().delete();
	}

	@Override
	public void shutDownDerivate(WebDriver webdriver, TestDerivate testDerivate) {
		if(--testNumber == 0) {
			try {
				TestUtils.home(webdriver);
				TestUtils.login(webdriver);
				TestUtils.goToObj(webdriver, "testJournal");
				Thread.sleep(600);
				webdriver.findElement(By.linkText("Derivat löschen")).click();
				Thread.sleep(600);
				webdriver.findElement(By.cssSelector(".bootstrap-dialog-footer-buttons > button:last-child")).click();
				webdriver.navigate().refresh();
				TestUtils.deletObj(webdriver, "");
			} catch (Exception e) {
				LOGGER.error("shutDownDerivate error: " + e.toString());
			}
		}
	}

	@Override
	public void openViewer(WebDriver webdriver, TestDerivate testDerivate) {
		webdriver.findElement(By.className("thumbnail")).click();
	}
	
	private void createDerivateFiles() {
		try {
			tempDirectory = Files.createTempDirectory(FILE_LOCATION);
			PaintTestPics pics = new PaintTestPics();
			
			int width = 1000;
			writeImage(pics.oneColorPic(width, Color.BLUE), tempDirectory.resolve("b.png")); 
			writeImage(pics.oneColorPic(width, Color.GREEN), tempDirectory.resolve("g.png"));
			writeImage(pics.oneColorPic(width, Color.RED), tempDirectory.resolve("r.png"));
			writeImage(pics.threeColorPic(width * 10), tempDirectory.resolve("rgb.png"));
			files.add(new File(getClass().getClassLoader().getResource("viewerTestMets/mets.xml").getFile()));
		} catch (IOException e) {
			LOGGER.error("Error while setting up derivate", e);
		}
	}
	
  private void writeImage(BufferedImage img, Path imagePath) {
  	try {
  		ImageIO.write(img, "PNG", imagePath.toFile());
  		files.add(imagePath.toFile());
  	} catch (IOException e) {
  		LOGGER.error("Error while setting up derivate", e);
  	}
  }
}