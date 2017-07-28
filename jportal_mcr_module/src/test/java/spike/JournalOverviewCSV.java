package spike;

import org.apache.solr.common.params.ModifiableSolrParams;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.common.config.MCRConfigurationLoaderFactory;
import org.mycore.solr.MCRSolrCore;
import org.mycore.solr.search.MCRSolrSearchUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Creates a CSV of all journals with id, maintitle, EZB, ZDB and the Projektbezeichnung.
 */
public class JournalOverviewCSV {

    public Document loadClassification(String className) throws IOException, JDOMException {
        SAXBuilder builder = new SAXBuilder();
        return builder.build(new URL("http://zs.thulb.uni-jena.de/rsc/classifications/export/" + className));
    }

    public List<CSVEntry> createCSV() {
        MCRSolrCore core = new MCRSolrCore("http://141.35.20.115:18080/solr/", "jportal");
        ModifiableSolrParams params = new ModifiableSolrParams();
        // all journals but no calendars
        params.set("q", "objectType:jpjournal -journalType:*calendars");
        params.set("fl", "id,maintitle,category");
        return MCRSolrSearchUtils.stream(core.getClient(), params).map(doc -> {
            CSVEntry entry = new CSVEntry();
            entry.id = (String) doc.getFieldValue("id");
            entry.maintitle = (String) doc.getFieldValue("maintitle");
            @SuppressWarnings("unchecked")
            List<String> categories = (List<String>) doc.getFieldValue("category");
            if (categories != null && !categories.isEmpty()) {
                entry.project = categories.stream().filter(c -> c.startsWith("jportal_class_00000062"))
                    .collect(Collectors.toList());
                entry.ezb = categories.stream().filter(c -> c.startsWith("jportal_class_00000068"))
                    .collect(Collectors.toList());
                entry.zdb = categories.stream().filter(c -> c.startsWith("jportal_class_00000069"))
                    .collect(Collectors.toList());
            }
            return entry;
        }).collect(Collectors.toList());
    }

    public void save(String file, List<CSVEntry> csv) {
        Path path = Paths.get(file);
        List<String> collect = csv.stream().map(CSVEntry::toString).collect(Collectors.toList());
        String result = String.join("\r\n", collect);
        try (BufferedWriter writer = Files.newBufferedWriter(path, Charset.forName("UTF-8"))) {
            writer.write(result, 0, result.length());
        } catch (IOException exp) {
            exp.printStackTrace();
        }
        System.out.println("saved to " + file);
    }

    public static class CSVEntry {

        String id;

        String maintitle;

        List<String> project;

        List<String> ezb;

        List<String> zdb;

        public void resolveProject(Element projectClassification) {
            project = project.stream().map(id -> {
                return resolve(projectClassification, id);
            }).collect(Collectors.toList());
        }

        public void resolveEZB(Element ezbClassification) {
            ezb = ezb.stream().map(id -> {
                return resolve(ezbClassification, id);
            }).collect(Collectors.toList());
        }

        public void resolveZDB(Element zdbClassification) {
            zdb = zdb.stream().map(id -> {
                return resolve(zdbClassification, id);
            }).collect(Collectors.toList());
        }

        protected String resolve(Element classification, String id) {
            String category = id.substring(id.indexOf(":") + 1);
            XPathExpression<Attribute> expression = XPathFactory.instance()
                .compile("//category[@ID='" + category + "']/label[@xml:lang='de']/@text", Filters.attribute());
            Attribute attribute = expression.evaluateFirst(classification);
            if (attribute == null) {
                throw new RuntimeException("not found " + id);
            }
            return attribute.getValue();
        }

        @Override
        public String toString() {
            String rowDelimiter = "|";
            String textDelimiter = ",";

            StringBuilder b = new StringBuilder();
            b.append(id).append(rowDelimiter);
            b.append(maintitle.replaceAll("\\\r\\\n", "")).append(rowDelimiter);
            b.append(String.join(textDelimiter, project)).append(rowDelimiter);
            b.append(String.join(textDelimiter, ezb)).append(rowDelimiter);
            b.append(String.join(textDelimiter, zdb));
            return b.toString();
        }
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("MCR.Home", "~/.mycore/jportal/");
        System.setProperty("MCR.AppName", "jportal");
        Map<String, String> properties = MCRConfigurationLoaderFactory.getConfigurationLoader().load();
        MCRConfiguration.instance().initialize(properties, true);

        JournalOverviewCSV journalCSV = new JournalOverviewCSV();
        Element projectClassification = journalCSV.loadClassification("jportal_class_00000062").getRootElement();
        Element ezbClassification = journalCSV.loadClassification("jportal_class_00000068").getRootElement();
        Element zdbClassification = journalCSV.loadClassification("jportal_class_00000069").getRootElement();

        List<CSVEntry> csv = journalCSV.createCSV();
        for (CSVEntry csvEntry : csv) {
            csvEntry.resolveProject(projectClassification);
            csvEntry.resolveEZB(ezbClassification);
            csvEntry.resolveZDB(zdbClassification);
            System.out.println(csvEntry);
        }
        String userHome = System.getProperty( "user.home" );
        journalCSV.save(userHome + "/journal.csv", csv);
    }

}
