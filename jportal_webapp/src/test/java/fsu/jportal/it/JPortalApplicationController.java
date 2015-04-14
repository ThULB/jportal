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

import org.apache.log4j.Logger;
import org.mycore.iview.tests.controller.ApplicationController;
import org.mycore.iview.tests.model.TestDerivate;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import fsu.jportal.it.TestUtils;
import fsu.jportal.it.PaintTestPics;

public class JPortalApplicationController extends ApplicationController {

	@Override
	public void init() {
	}

	static final Logger LOGGER = Logger.getLogger(JPortalApplicationController.class);

	private static final String FILE_LOCATION = "testImages";

	private boolean firstRun = true;
	private String derivateUrl = null;
	private List <File> files = new ArrayList<File>();

	@Override
	public void setUpDerivate(WebDriver webdriver, TestDerivate testDerivate) {
		if (firstRun) {
			creteDerivateFiles();

			TestUtils.home(webdriver);
			TestUtils.login(webdriver);
			TestUtils.creatMinJournal(webdriver, "testJournal");
			
			derivateUrl = webdriver.getCurrentUrl();
			uploadFiles(webdriver);
			firstRun = false;
		} else {
			webdriver.get(derivateUrl);
		}
	}

	private void uploadFiles(WebDriver webdriver) {
		for (int i = 0; i < files.size(); i++) {
			boolean isFirstFile = i % 3 == 0;
			boolean isLastFile = i % 3 == 2;

			if (isFirstFile) {
				if (i == 0) {
					TestUtils.clickCreatSelect(webdriver, "Datei hochladen");
				} else {
					webdriver.findElement(By.linkText("Dateien hinzufügen")).click();
				}
			}

			int slot = (i % 3) + 1;
			setFileInput(webdriver, files.get(i), slot);

			if (isLastFile || i == files.size() - 1) {
				webdriver.findElement(By.xpath("//input[@value='Speichern']")).click();
			}
		}
	}

	private void setFileInput(WebDriver webdriver, File file, int slot) {
		WebElement fileInput;
		if (slot == 1) {
			fileInput = webdriver.findElement(By.name("/upload/path"));
		} else {
			fileInput = webdriver.findElement(By.name("/upload/path[" + slot + "]"));
		}
		fileInput.sendKeys(file.getAbsolutePath());
	}

	@Override
	public void shutDownDerivate(WebDriver webdriver, TestDerivate testDerivate) {
//		try {
//			webdriver.findElement(By.xpath("//button[@data-id='CloseViewerButton']")).click();
//			for (int i = 0; i < files.size(); i++) {
//				files.get(i).delete();
//			}
//		
//			webdriver.findElement(By.linkText("Derivat löschen"));
//			webdriver.findElement(By.cssSelector(".bootstrap-dialog-footer-buttons > button:last-child"));
//			Thread.sleep(1000);
//			TestUtils.deletObj(webdriver, "");
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}

	@Override
	public void openViewer(WebDriver webdriver, TestDerivate testDerivate) {
		webdriver.findElement(By.className("thumbnail")).click();
	}
	
	private void creteDerivateFiles() {
		try {
			Path tempDirectory = Files.createTempDirectory(FILE_LOCATION);
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