package fsu.jportal.it.test;

import junit.framework.TestCase;

import org.junit.Test;
import org.openqa.selenium.By;

import fsu.jportal.it.BaseIntegrationTest;
import fsu.jportal.it.TestUtils;

public class BaseITCase extends BaseIntegrationTest {

    @Test
    public void loginAndLogout() throws Exception {
        TestUtils.home(DRIVER);
        if (TestUtils.isLoggedIn(DRIVER)) {
            TestUtils.logout(DRIVER);
        }
        TestUtils.login(DRIVER);
        TestUtils.logout(DRIVER);
    }

    @Test
    public void language() throws Exception {
        TestUtils.home(DRIVER);
        DRIVER.findElement(By.id("languageSelect")).click();
        DRIVER.findElement(By.xpath("//ul[@id='languageList']/li[1]/a")).click();
        TestCase.assertEquals("language not changed - content does not match", "Welcome to journals@UrMEL!",
                DRIVER.findElement(By.xpath("//div[@class='jp-layout-index-intro']/h1")).getText());

        DRIVER.get(START_URL + "/content/below/index.xml");
        DRIVER.findElement(By.id("languageSelect")).click();
        DRIVER.findElement(By.xpath("//ul[@id='languageList']/li[1]/a")).click();
        TestCase.assertEquals("language not changed - content does not match", "Willkommen bei journals@UrMEL",
                DRIVER.findElement(By.xpath("//div[@class='jp-layout-index-intro']/h1")).getText());
    }

}
