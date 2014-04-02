package fsu.jportal.it.test;

import junit.framework.TestCase;

import org.junit.Test;
import org.openqa.selenium.By;

import fsu.jportal.it.BaseIntegrationTest;
import fsu.jportal.it.TestUtils;

public class CreateITCase extends BaseIntegrationTest {

    @Test
    public void createPerson() {
        home();
        TestUtils.login(DRIVER);
        DRIVER.findElement(By.xpath("//a[@path='create/person']")).click();
        TestCase.assertEquals("title does not match", "Person anlegen - JPortal", DRIVER.getTitle());

        // fill form
        DRIVER.findElement(By.name("/mycoreobject/metadata/def.heading/heading/lastName")).sendKeys("Goethe");
        DRIVER.findElement(By.name("/mycoreobject/metadata/def.heading/heading/firstName")).sendKeys("Johann Wolfgang");
        DRIVER.findElement(By.name("/mycoreobject/metadata/def.heading/heading/nameAffix")).sendKeys("von");
        DRIVER.findElements(By.name("/mycoreobject/metadata/def.gender/gender/@categid")).get(0).click();
        DRIVER.findElement(By.name("/mycoreobject/metadata/def.dateOfBirth/dateOfBirth")).sendKeys("1749-08-28");
        DRIVER.findElement(By.name("/mycoreobject/metadata/def.placeOfBirth/placeOfBirth")).sendKeys("Frankurt am Main");
        DRIVER.findElement(By.name("/mycoreobject/metadata/def.dateOfDeath/dateOfDeath")).sendKeys("1832-03-22");
        DRIVER.findElement(By.name("/mycoreobject/metadata/def.placeOfDeath/placeOfDeath")).sendKeys("Weimar");
        DRIVER.findElement(By.xpath("//input[@type='submit' and @value='Speichern']")).click();

        // test page
        TestCase.assertEquals("title does not match", "Goethe, Johann Wolfgang - JPortal", DRIVER.getTitle());
        TestCase.assertEquals("header does not match", "Goethe, Johann Wolfgang von", DRIVER.findElement(By.id("jp-maintitle")).getText());
    }

    @Test
    public void createInstitution() {
        home();
        TestUtils.login(DRIVER);
        DRIVER.findElement(By.xpath("//a[@path='create/jpinst']")).click();
        TestCase.assertEquals("title does not match", "Institution anlegen - JPortal", DRIVER.getTitle());

        // fill form
        DRIVER.findElement(By.name("/mycoreobject/metadata/names/name/fullname")).sendKeys(
                "Thüringer Universitäts- und Landesbibliothek Jena");
        DRIVER.findElement(By.name("/mycoreobject/metadata/names/name/nickname")).sendKeys("ThULB");
        DRIVER.findElement(By.name("/mycoreobject/metadata/addresses/address/country")).sendKeys("Deutschland");
        DRIVER.findElement(By.name("/mycoreobject/metadata/addresses/address/zipcode")).sendKeys("07743");
        DRIVER.findElement(By.name("/mycoreobject/metadata/addresses/address/city")).sendKeys("Jena");
        DRIVER.findElement(By.name("/mycoreobject/metadata/addresses/address/street")).sendKeys("Bibliotheksplatz");
        DRIVER.findElement(By.name("/mycoreobject/metadata/addresses/address/number")).sendKeys("2");
        DRIVER.findElement(By.name("/mycoreobject/metadata/urls/url/@xlink:title")).sendKeys("ThULB");
        DRIVER.findElement(By.name("/mycoreobject/metadata/urls/url/@xlink:href")).sendKeys("http://www.thulb.uni-jena.de");
        DRIVER.findElement(By.xpath("//input[@type='submit' and @value='Speichern']")).click();

        // test page
        TestCase.assertEquals("title does not match", "Thüringer Universitäts- und Landesbibliothek Jena - JPortal", DRIVER.getTitle());
        TestCase.assertEquals("header does not match", "Thüringer Universitäts- und Landesbibliothek Jena",
                DRIVER.findElement(By.id("jp-maintitle")).getText());
    }

}
