package fsu.jportal.it;

import junit.framework.TestCase;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public abstract class TestUtils {

    public static void home(WebDriver driver) {
        driver.get(BaseIntegrationTest.getHomeAddress());
        TestCase.assertEquals("invald index page - title does not match", "journals@UrMEL - JPortal", driver.getTitle());
    }

    public static void login(WebDriver driver) {
        if (isLoggedIn(driver)) {
            return;
        }
        driver.findElement(By.xpath("//div[@id='globalMenu']/ul/li[2]/a")).click();
        WebElement uid = driver.findElement(By.xpath("//input[@name='uid']"));
        uid.sendKeys("administrator");
        driver.findElement(By.xpath("//input[@name='pwd']")).sendKeys("alleswirdgut");
        uid.submit();
    }

    public static void logout(WebDriver driver) {
        driver.findElement(By.xpath("//div[@id='globalMenu']/ul/li[5]/a")).click();
        TestCase.assertEquals("logout failed", 2, driver.findElements(By.xpath("//div[@id='globalMenu']/ul/li")).size());
    }

    public static void createPerson(WebDriver driver, String lastName) {
        login(driver);
        home(driver);
        driver.findElement(By.xpath("//a[@path='create/person']")).click();
        driver.findElement(By.name("/mycoreobject/metadata/def.heading/heading/lastName")).sendKeys(lastName);
        saveForm(driver);
    }

    public static void saveForm(WebDriver driver) {
        // TODO: don't use german 'Speichern'
        driver.findElement(By.xpath("//input[@type='submit' and @value='Speichern']")).click();
    }

    public static boolean isLoggedIn(WebDriver driver) {
        try {
            String userName = driver.findElement(By.xpath("//div[@id='globalMenu']/ul/li[@class='userName']")).getText();
            return "Superuser".equals(userName);
        } catch (Exception exc) {
            return false;
        }
    }

}
