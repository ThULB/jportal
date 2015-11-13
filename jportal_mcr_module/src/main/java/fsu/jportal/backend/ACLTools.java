package fsu.jportal.backend;

import org.jdom2.Document;
import org.jdom2.Element;
import org.mycore.access.MCRAccessInterface;
import org.mycore.access.MCRAccessManager;
import org.mycore.common.MCRException;
import org.mycore.common.content.MCRStreamContent;
import org.mycore.common.xml.MCRXMLParserFactory;
import org.mycore.frontend.cli.MCRAccessCommands;
import org.mycore.frontend.cli.annotation.MCRCommand;
import org.xml.sax.SAXParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.text.ParseException;

public class ACLTools {
    public void createRules(Path toCmdFile){
        Path parentPath = toCmdFile.getParent();
        InputStream cmdFileIS = getClass().getResourceAsStream(toCmdFile.toString());
        BufferedReader cmdFileReader = new BufferedReader(new InputStreamReader(cmdFileIS));

        try {
            for (String cmdLine; (cmdLine = cmdFileReader.readLine()) != null; ) {
                if (cmdLine != null && !cmdLine.trim().equals("")) {
                    System.out.println(cmdLine);

                    Object[] params = parseParams(cmdLine);

                    String permission = (String) params[0];
                    String id = (String) params[1];
                    Element rule = getRuleXML(parentPath.resolve((String) params[2]).toString());
                    String description = (String) params[3];

                    addRule(id, permission, rule, description);
                }
            }
        } catch (IOException | NoSuchMethodException | ParseException e) {
            e.printStackTrace();
        }
    }

    public Element getRuleXML(String ruleXMLPath) {
        InputStream resourceIS = getClass().getResourceAsStream(ruleXMLPath);
        try {
            Document ruleDom = MCRXMLParserFactory.getParser().parseXML(new MCRStreamContent(resourceIS));
            return ruleDom.getRootElement();
        } catch (MCRException | SAXParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Object[] parseParams(String cmdLine) throws NoSuchMethodException, ParseException {
        Method method = MCRAccessCommands.class
                .getMethod("permissionUpdateForID", String.class, String.class, String.class, String.class);
        String pattern = method.getAnnotation(MCRCommand.class).syntax();
        MessageFormat mf = new MessageFormat(pattern);
        Object[] params = mf.parse(cmdLine);
        return params;
    }

    public void addRule(String id, String permission, Element rule, String description) {
        MCRAccessInterface AI = MCRAccessManager.getAccessImpl();

        AI.addRule(id, permission, rule, description);
    }
}