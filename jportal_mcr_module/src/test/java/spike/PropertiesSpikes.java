package spike;

import org.apache.commons.io.output.WriterOutputStream;
import org.junit.Test;

import java.io.*;
import java.util.Properties;

public class PropertiesSpikes {
    @Test
    public void defaultProperties() throws Exception {
        Properties defaults = new Properties();
        defaults.setProperty("hardwareTurbo", "false");
        defaults.setProperty("color", "green");
        Properties properties = new Properties(defaults);
        System.out.println("Prop: " + properties.getProperty("color"));
        printProps(properties);
        properties.setProperty("color", "red");
        System.out.println("Prop: " + properties.getProperty("color"));
        printProps(properties);
        
    }

    private void printProps(Properties properties) throws IOException {
        StringWriter stringWriter = new StringWriter();
        WriterOutputStream writerOutputStream = new WriterOutputStream(stringWriter, "UTF-8");
        properties.store(writerOutputStream, "Print");
        System.out.println(stringWriter.toString());
    }
    
    @Test
    public void savePropsToFile() throws Exception {
        Properties properties = new Properties();
        properties.setProperty("color", "red");
        properties.setProperty("size", "M");
        File tempFile = File.createTempFile("properties", "test");
        tempFile.deleteOnExit();
        
        properties.store(new FileOutputStream(tempFile), "First time");
        printPropFile(tempFile);
        
        properties.setProperty("label", "GoodChi");
        properties.store(new FileOutputStream(tempFile), "Second time");
        printPropFile(tempFile);
    }

    private void printPropFile(File tempFile) throws IOException, FileNotFoundException {
        Properties newProp = new Properties();
        newProp.load(new FileInputStream(tempFile));
        
        printProps(newProp);
    }
}
