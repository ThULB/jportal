package fsu.jportal.it;

import junit.framework.TestCase;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public abstract class TestUtils {

    public static void login(WebDriver driver) {
        driver.findElement(By.xpath("//div[@id='globalMenu']/ul/li[2]/a")).click();
        WebElement uid = driver.findElement(By.xpath("//input[@name='uid']"));
        uid.sendKeys("administrator");
        driver.findElement(By.xpath("//input[@name='pwd']")).sendKeys("alleswirdgut");
        uid.submit();

        TestCase.assertEquals("login failed - user name (Superuser) does not match", "Superuser",
                driver.findElement(By.xpath("//div[@id='globalMenu']/ul/li[@class='userName']")).getText());
    }

    public static void logout(WebDriver driver) {
        driver.findElement(By.xpath("//div[@id='globalMenu']/ul/li[5]/a")).click();
        TestCase.assertEquals("logout failed", 2, driver.findElements(By.xpath("//div[@id='globalMenu']/ul/li")).size());
    }

}
