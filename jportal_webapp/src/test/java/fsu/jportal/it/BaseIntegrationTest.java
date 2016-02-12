package fsu.jportal.it;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

import static org.junit.Assert.assertEquals;

public class BaseIntegrationTest {

    private static final Logger LOGGER = LogManager.getLogger(BaseIntegrationTest.class);

    @ClassRule
    public static TemporaryFolder alternateDirectory = new TemporaryFolder();

    @Rule
    public TestWatcher errorLogger = new TestWatcher() {

        @Override
        protected void failed(Throwable e, Description description) {
            if (description.isTest()) {
                String className = description.getClassName();
                String method = description.getMethodName();
                File failedTestClassDirectory = new File(OUTPUT_DIRECTORY, className);
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

    public static File OUTPUT_DIRECTORY;

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
            OUTPUT_DIRECTORY = targetDirectory.isDirectory() ? targetDirectory : alternateDirectory.getRoot();
        } else {
            OUTPUT_DIRECTORY = new File(buildDirectory);
        }
        OUTPUT_DIRECTORY = new File(OUTPUT_DIRECTORY, "failed-it");
        if(!OUTPUT_DIRECTORY.exists()){
            OUTPUT_DIRECTORY.mkdirs();
        }
        LOGGER.info("Using " + OUTPUT_DIRECTORY.getAbsolutePath() + " as replacement.");
        String port = System.getProperty("it.port", "8291");
        LOCAL_PORT = Integer.parseInt(port);
        TEST_APP = System.getProperty("it.context", "");
        START_URL = "http://localhost:" + LOCAL_PORT + "/jportal" + TEST_APP;
        LOGGER.info("Server running on '" + START_URL + "'");
        DRIVER = new FirefoxDriver();
        
        //call jportal homepage
        DRIVER.get(getHomeAddress());
        assertEquals("invalid index page - title does not match", "journals@UrMEL - JPortal", DRIVER.getTitle());
//        DRIVER.findElement(By.xpath("//div[@id='navbar-collapse-globalHeader']/ul/li[2]/a")).click();
        DRIVER.findElement(By.id("jp.login.button")).click();
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

    public WebDriverWait waiting(long seconds){
        return new WebDriverWait(DRIVER, seconds);
    }
}
