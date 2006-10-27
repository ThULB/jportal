/*
 * $RCSfile: MCRConfiguration.java,v $
 * $Revision: 1.25 $ $Date: 2005/09/02 14:26:23 $
 *
 * This file is part of ***  M y C o R e  ***
 * See http://www.mycore.de/ for details.
 *
 * This program is free software; you can use it, redistribute it
 * and / or modify it under the terms of the GNU General Public License
 * (GPL) as published by the Free Software Foundation; either version 2
 * of the License or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program, in a file called gpl.txt or license.txt.
 * If not, write to the Free Software Foundation Inc.,
 * 59 Temple Place - Suite 330, Boston, MA  02111-1307 USA
 */

package org.mycore.sample;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.mycore.backend.lucene.LuceneCStoreQueryParser;
import org.mycore.backend.lucene.MCRCStoreLucene;
import org.mycore.common.MCRConfiguration;
import org.mycore.datamodel.ifs.MCRContentStore;
import org.mycore.datamodel.ifs.MCRContentStoreFactory;
import org.mycore.services.query.MCRQueryInterface;

/**
 * @author Thomas Scheffler (yagee)
 * 
 * Need to insert some things here
 * 
 */
public class FulltextTester {
    private static MCRConfiguration conf = MCRConfiguration.instance();

    private static Logger logger = Logger.getLogger(FulltextTester.class);

    private static MCRContentStore contentStore = null;

    private static MCRCStoreLucene lucene = null;

    private static MCRQueryInterface queryint;

    /**
     * 
     */
    public FulltextTester() {
        super();
        init();
    }

    private static void init() {
        if (contentStore == null) {
            contentStore = MCRContentStoreFactory.getStore("Lucene");
        }

        if (contentStore instanceof MCRCStoreLucene) {
            lucene = (MCRCStoreLucene) contentStore;
        }

        if (lucene == null) {
            System.exit(1);
        }

        queryint = (MCRQueryInterface) conf.getInstanceOf("MCR.persistence_" + conf.getString("MCR.XMLStore.Type").toLowerCase() + "_query_name");
    }

    private static void printItStrings(String[] derivateIDs) {
        for (int i = 1; i <= derivateIDs.length; i++) {
            logger.info(i + ". Result: " + derivateIDs[i - 1]);
        }
    }

    private static void search(String queryText) {
        logger.info("Starting fulltext search for " + queryText);
        printItStrings(lucene.getDerivateIDs(queryText));
    }

    public static void main(String[] args) {
        org.mycore.common.MCRConfiguration.instance();
        init();
        search("\"foo bar\"");
        search("foo bar");
        search("foo -bar");
        search("-\"foo bar\"");
        logger.info(queryToString("(+DerivateID:\"MyCoReDemoDC_derivate_0014\" +foo) OR (+DerivateID:\"mycoredemodc derivate_0014\" +bar)"));
        logger.info(queryToString("(+DerivateID:\"MyCoReDemoDC_derivate_0014\" +foo) OR (+DerivateID:\"mycoredemodc derivate_0014\" -bar)"));
        logger.info(lqueryToString("foo bar"));
        logger.info(lqueryToString("foo -bar"));
        logger.info(lqueryToString("\"foo bar\""));
        logger.info(queryint.getObjectID("MyCoReDemoDC_derivate_0014"));

        // logger.info(queryint.getObjectForDerivate("MyCoReDemoDC_derivate_0014").getId(0));
        // conf.
    }

    public static String queryToString(String query) {
        try {
            return QueryParser.parse(query, "content", new StandardAnalyzer()).toString("content");
        } catch (ParseException e) {
            logger.error("unable to parse " + query, e);
        }

        return null;
    }

    public static String lqueryToString(String query) {
        LuceneCStoreQueryParser lparser = new LuceneCStoreQueryParser("content", new StandardAnalyzer());
        lparser.setGroupingValue("MyCoReDemoDC_derivate_0014");

        try {
            return lparser.parse(query).toString("content");
        } catch (ParseException e) {
            logger.error("unable to parse " + query, e);
        }

        return null;
    }
}
