package spike;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.mycore.solr.search.MCRSolrSearchUtils;

public class JPPiwikReport {

    private static String SOLR_URL = "http://ulbdb2.thulb.uni-jena.de:18080/solr/jportal";

    private static String PIWIK_URL = "https://piwik.thulb.uni-jena.de/index.php";

    private static String PIWIK_API_URL = PIWIK_URL + "?module=API";

    public String token;

    public JPPiwikReport(String token) {
        this.token = token;
    }

    private Collection<PiwikResult> getPiwikResults(int year) throws IOException {
        Map<String, PiwikResult> results = new HashMap<>();
        for (int month = 1; month <= 12; month++) {
            getPiwikResults(year, month).forEach(r -> {
                PiwikResult mapResult = results.get(r.journalId);
                if (mapResult == null) {
                    results.put(r.journalId, r);
                    return;
                }
                mapResult.mixin(r);
            });
        }
        return results.values();
    }

    private Collection<PiwikResult> getPiwikResults(int year, int month) throws IOException {
        List<PiwikResult> results = new ArrayList<>();
        String url = PIWIK_API_URL + "&method=CustomVariables.getCustomVariablesValuesFromNameId";
        url += "&idSite=1";
        url += "&period=month";
        url += "&date=" + year + "-" + month + "-01";
        url += "&format=JSON";
        url += "&idSubtable=1";
        url += "&filter_limit=-1";
        url += "&token_auth=" + token;

        System.out.println("query: " + url);

        String jsonAsString = readUrl(url);
        JsonArray jsonArray = new JsonParser().parse(jsonAsString).getAsJsonArray();
        for (JsonElement object : jsonArray) {
            PiwikResult r = new PiwikResult();
            r.journalId = object.getAsJsonObject().getAsJsonPrimitive("label").getAsString();
            r.actions = object.getAsJsonObject().getAsJsonPrimitive("nb_actions").getAsInt();
            r.visits = object.getAsJsonObject().getAsJsonPrimitive("nb_visits").getAsInt();
            results.add(r);
        }

        return results;
    }

    private static void updateJournalLabels(Collection<PiwikResult> results) {
        for (PiwikResult r : results) {
            try {
                r.label = PiwikCSVEnhancer.getJournalTitle(r.journalId);
            } catch (Exception exc) {
                exc.printStackTrace();
            }
        }
    }

    public static Collection<PiwikResult> filter(Collection<PiwikResult> results, String solrQuery) {
        List<PiwikResult> filteredResults = new ArrayList<>();
        Map<String, PiwikResult> helper = results.stream().collect(
                Collectors.toMap(PiwikResult::getJournalId, result -> result));
        ModifiableSolrParams p = new ModifiableSolrParams();
        p.add("q", solrQuery);
        p.add("fq", "objectType:jpjournal");
        p.add("fl", "id");
        SolrClient solrClient = new HttpSolrClient.Builder(SOLR_URL).build();
        MCRSolrSearchUtils.stream(solrClient, p).forEach(solrDocument -> {
            String solrDocumentId = solrDocument.getFieldValue("id").toString();
            PiwikResult result = helper.get(solrDocumentId);
            if (result != null) {
                filteredResults.add(result);
            }
        });
        return filteredResults;
    }

    public static String toCSV(Collection<PiwikResult> results, String header, String footer) {
        String csv = header + "\n\n";
        csv += "Link,Name,Besuche,Aktionen\n";
        csv += results.stream().sorted().map(PiwikResult::toCSV).collect(Collectors.joining("\n"));
        csv += "\n\n";
        int visits = results.stream().map(PiwikResult::getVisits).mapToInt(value -> value).sum();
        int actions = results.stream().map(PiwikResult::getActions).mapToInt(value -> value).sum();
        csv += ",," + visits + "," + actions;
        csv += "\n\n";
        csv += footer;
        return csv;
    }

    public static void writeToFile(String filePath, String data) throws IOException {
        System.out.println("write data to " + filePath);
        try (InputStream is = new ByteArrayInputStream(data.getBytes(Charset.defaultCharset()))) {
            Files.copy(is, Paths.get(filePath), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public static String readUrl(String urlString) throws IOException {
        BufferedReader reader = null;
        try {
            URL url = new URL(urlString);
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuilder buffer = new StringBuilder();
            int read;
            char[] chars = new char[1024];
            while ((read = reader.read(chars)) != -1) {
                buffer.append(chars, 0, read);
            }
            return buffer.toString();
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    private class PiwikResult implements Comparable<PiwikResult> {

        String journalId;

        String label;

        int visits;

        int actions;

        public String getJournalId() {
            return journalId;
        }

        public int getVisits() {
            return visits;
        }

        public int getActions() {
            return actions;
        }

        public void mixin(PiwikResult other) {
            this.visits += other.visits;
            this.actions += other.actions;
        }

        @Override
        public String toString() {
            return journalId + ": " + visits;
        }

        public String toCSV() {
            return "http://zs.thulb.uni-jena.de/receive/" + journalId + ",\"" + label + "\"," + visits + "," + actions;
        }

        @Override
        public int compareTo(PiwikResult o) {
            return -Integer.compare(visits, o.visits);
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            throw new Exception("missing piwik api token as first argument!");
        }
        if (args.length < 2) {
            throw new Exception("missing path to store csv file as second argument!");
        }

        JPPiwikReport report = new JPPiwikReport(args[0]);
        Collection<PiwikResult> results = report.getPiwikResults(2018);
        /*results = JPPiwikReport
                .filter(results, "+journalType:jportal_class_00000200\\:parliamentDocuments +objectType:jpjournal");*/
        JPPiwikReport.updateJournalLabels(results);

        String header = "2017";
        String footer =
                "\"Besuche: Wenn ein Besucher zum ersten Mal die Webseite besucht oder seit dem letzten Seitenaufruf"
                        + " mehr als 30 Minuten vergangen sind, wird dies als neuer Besuch gewertet.\"\n"
                        + "\"Aktionen: Die Anzahl der Aktionen, die ihre Besucher durchgeführt haben. Aktionen sind Seitenansichten,"
                        + " Downloads, der Aufruf von ausgehenden Verweisen und interne Suchen.\"\n";

        String csv = JPPiwikReport.toCSV(results, header, footer);
        JPPiwikReport.writeToFile(args[1], csv);
    }

}
