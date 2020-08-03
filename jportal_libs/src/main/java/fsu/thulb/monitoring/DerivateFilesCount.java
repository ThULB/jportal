package fsu.thulb.monitoring;

import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import fsu.thulb.connections.DBConnection;
import fsu.thulb.connections.SSHTunnel;
import fsu.thulb.connections.config.DBConnectionConfig;
import fsu.thulb.connections.config.SSHTunnelConfig;
import fsu.thulb.xml.JAXBTools;

/**
 * Created by chi on 30.03.20
 *
 * @author Huu Chi Vu
 */
public class DerivateFilesCount {
    private static final Logger LOGGER = LogManager.getLogger();
    public static void main(String[] args) {
        ArrayList<String> objIdWithDerivList = new ArrayList<>();
        try {
            JAXBTools jaxbTools = JAXBTools.newInstance(SSHTunnelConfig.class, DBConnectionConfig.class);
            InputStream solrSSHConfigIS = DerivateFilesCount.class.getResourceAsStream("/solrSSHConfig.xml");
            SSHTunnelConfig solrSSHConfig = (SSHTunnelConfig)jaxbTools.getUnMarshaller().unmarshal(solrSSHConfigIS);

            Session solrTunnelSession = SSHTunnel.connect(solrSSHConfig);
            final String solrUrl = "http://localhost:"+ solrSSHConfig.getLocalPort() + "/solr/jportal";
            SolrClient solrClient = new HttpSolrClient.Builder(solrUrl)
                    .withConnectionTimeout(10000)
                    .withSocketTimeout(60000)
                    .build();

            SolrQuery solrQuery = new SolrQuery("derivateCount:[1 TO *]");
            solrQuery.addFilterQuery("journalType:\"jportal_class_00000200:parliamentDocuments\"");
            solrQuery.setRows(2679);

            QueryResponse response = solrClient.query(solrQuery);
            SolrDocumentList documents = response.getResults();

            long numFound = documents.getNumFound();
            long start = documents.getStart();

            for (SolrDocument document : documents) {
                String objId = (String)document.getFieldValue("id");

                if(objId != null && !"".equals(objId)){
                    objIdWithDerivList.add(String.format("mcrfrom = '%s'", objId));
                }
            }

            LOGGER.info("Solr documents: " + numFound);
            SSHTunnel.close(solrTunnelSession);

            LOGGER.info("obj: " + objIdWithDerivList.size());
            String orClause = "(" + String.join(" or ", objIdWithDerivList) + ")";
            String dbQuery = "select count(NODES.name) From (select * from mcrlinkhref where " + orClause +
                    " and mcrtype='derivate') LN left join mcrfsnodes NODES on LN.mcrto = NODES.owner where NODES.fctid='tiff';";

            InputStream dbConnectionConfigIS = DerivateFilesCount.class.getResourceAsStream("/dbConnection.xml");
            DBConnectionConfig dbConnectionConfig = (DBConnectionConfig)jaxbTools.getUnMarshaller().unmarshal(dbConnectionConfigIS);
            DBConnection postgresDB = new DBConnection(dbConnectionConfig);
            ResultSet filesInDeriv = postgresDB.execQuery(dbQuery);

            while (filesInDeriv.next()) {
                long num = filesInDeriv.getLong(1);
                LOGGER.info("Files: " + num);
            }

            postgresDB.closeConnection();
        } catch (JSchException | SolrServerException | IOException | JAXBException | SQLException e) {
            e.printStackTrace();
        }

    }
}
