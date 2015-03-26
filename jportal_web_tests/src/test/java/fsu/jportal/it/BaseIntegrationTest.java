package fsu.jportal.it;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
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
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

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

    public static String TEST_APP;

    public static String START_URL, SOURCE_HTML, TEST_URL;

    public static WebDriver DRIVER;

    public byte[] screenShot;

    @BeforeClass
    public static void setupClass() {
//        String buildDirectory = System.getProperty("project.build.directory");
//        LOGGER.info("########## build Dir: " + buildDirectory);
//        if (buildDirectory == null) {
//            LOGGER.warn("Did not get System property 'project.build.directory'");
//            File targetDirectory = new File("target");
//            MAVEN_OUTPUT_DIRECTORY = targetDirectory.isDirectory() ? targetDirectory : alternateDirectory.getRoot();
//        } else {
//            MAVEN_OUTPUT_DIRECTORY = new File(buildDirectory);
//        }
//        MAVEN_OUTPUT_DIRECTORY = new File(MAVEN_OUTPUT_DIRECTORY, "failed-it");
        MAVEN_OUTPUT_DIRECTORY = new File("/Users/chi/Development/projects/jportal/jportal_web_tests/build/reports/tests/failed-it");
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
    }

    @AfterClass
    public static void tearDownClass() {
        DRIVER.quit();
    }

    @Before
    public void setup() {
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
}
