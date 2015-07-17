package fsu.jportal.it;

import static org.junit.Assert.assertEquals;

import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.System;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

import javax.imageio.ImageIO;

public class BaseIntegrationTest {

    private static final Logger LOGGER = Logger.getLogger(BaseIntegrationTest.class);

    @ClassRule
    public static TemporaryFolder alternateDirectory = new TemporaryFolder();

    @Rule
    public TestWatcher errorLogger = new TestWatcher() {

        @Override
        protected void failed(Throwable e, Description description) {
            if (description.isTest()) {
                String className = description.getClassName();
                String method = description.getMethodName();
                File failedTestClassDirectory = new File(MAVEN_OUTPUT_DIRECTORY, className);
                File failedTestDirectory = new File(failedTestClassDirectory, method);
                failedTestDirectory.mkdirs();
                if (e != null) {
                    File error = new File(failedTestDirectory, "error.txt");
                    try (FileOutputStream fout = new FileOutputStream(error);
                            OutputStreamWriter osw = new OutputStreamWriter(fout, "UTF-8");
                            PrintWriter pw = new PrintWriter(osw)) {
                        pw.println(TEST_URL);
                        e.printStackTrace(pw);
                    } catch (IOException e1) {
                        throw new RuntimeException(e1);
                    }
                }
                File screenshot = new File(failedTestDirectory, "screenshot.png");
                try (FileOutputStream fout = new FileOutputStream(screenshot);) {
                    System.out.println("Saving screenshot to " + screenshot.getAbsolutePath());
                    fout.write(screenShot);
                } catch (IOException e1) {
                    throw new RuntimeException(e1);
                }
                File html = new File(failedTestDirectory, "dom.html");
                try (FileOutputStream fout = new FileOutputStream(html); OutputStreamWriter osw = new OutputStreamWriter(fout, "UTF-8")) {
                    System.out.println("Saving DOM to " + html.getAbsolutePath());
                    osw.write(SOURCE_HTML);
                } catch (IOException e1) {
                    throw new RuntimeException(e1);
                }
            }
            super.failed(e, description);
        }
    };

    public static File MAVEN_OUTPUT_DIRECTORY;

    public static int LOCAL_PORT;

    public static String TEST_APP, START_URL;
    
    public String SOURCE_HTML, TEST_URL;

    public static WebDriver DRIVER;

    public byte[] screenShot;

    @BeforeClass
    public static void setupClass() {
        String buildDirectory = System.getProperty("project.build.directory");
        if (buildDirectory == null) {
            LOGGER.warn("Did not get System property 'project.build.directory'");
            File targetDirectory = new File("target");
            MAVEN_OUTPUT_DIRECTORY = targetDirectory.isDirectory() ? targetDirectory : alternateDirectory.getRoot();
        } else {
            MAVEN_OUTPUT_DIRECTORY = new File(buildDirectory);
        }
        MAVEN_OUTPUT_DIRECTORY = new File(MAVEN_OUTPUT_DIRECTORY, "failed-it");
        if(!MAVEN_OUTPUT_DIRECTORY.exists()){
            MAVEN_OUTPUT_DIRECTORY.mkdirs();
        }
        LOGGER.info("Using " + MAVEN_OUTPUT_DIRECTORY.getAbsolutePath() + " as replacement.");
//        String port = System.getProperty("it.port", "8080");
        String port = System.getProperty("it.port", "8291");
        LOCAL_PORT = Integer.parseInt(port);
        TEST_APP = System.getProperty("it.context", "");
//        START_URL = "http://localhost:" + LOCAL_PORT + "/" + TEST_APP;
        START_URL = "http://localhost:" + LOCAL_PORT + "/jportal-webTests" + TEST_APP;
        LOGGER.info("Server running on '" + START_URL + "'");
        DRIVER = new FirefoxDriver();
        
        //call jportal homepage
        DRIVER.get(getHomeAddress());
        assertEquals("invald index page - title does not match", "journals@UrMEL - JPortal", DRIVER.getTitle());
        DRIVER.findElement(By.xpath("//div[@id='navbar-collapse-globalHeader']/ul/li[2]/a")).click();
        WebElement uid = DRIVER.findElement(By.xpath("//input[@name='uid']"));
        uid.sendKeys("administrator");
        DRIVER.findElement(By.xpath("//input[@name='pwd']")).sendKeys("alleswirdgut");
        uid.submit();
    }

    @AfterClass
    public static void tearDownClass() {
        DRIVER.quit();
    }

    @Before
    public void setup() {
    	DRIVER.get(getHomeAddress());
      assertEquals("invald index page - title does not match", "journals@UrMEL - JPortal", DRIVER.getTitle());
    }

    @After
    public void tearDown() {
        SOURCE_HTML = DRIVER.getPageSource();
        if (DRIVER instanceof TakesScreenshot) {
            screenShot = ((TakesScreenshot) DRIVER).getScreenshotAs(OutputType.BYTES);
        }
        TEST_URL = DRIVER.getCurrentUrl();
    }

    public static String getHomeAddress() {
        return START_URL + "/content/below/index.xml";
    }

    public static String getStartUrl() {
        return START_URL;
    }

    public BufferedImage takeScreenshot() throws IOException {
        if (DRIVER instanceof TakesScreenshot) {
            ByteArrayInputStream inputStream;
            inputStream =  new ByteArrayInputStream(((TakesScreenshot) DRIVER).getScreenshotAs(OutputType.BYTES));
            return ImageIO.read(inputStream);
        }
        return null;
    }
}
