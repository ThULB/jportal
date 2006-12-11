/*
 * $RCSfile: MCRDataExtractorPDF.java,v $
 * $Revision: 1.3 $ $Date: 2006/09/15 11:37:50 $
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

package org.mycore.datamodel.ifs.extractors;

import java.io.InputStream;
import java.util.Calendar;

import org.jdom.Element;
import org.mycore.datamodel.metadata.MCRMetaISO8601Date;
import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.pdmodel.PDDocumentInformation;
import org.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;

/**
 * Extracts metadata from PDF files using the PDFBox library. The number of
 * pages, document information like author and title, and the titles of all
 * outline items (table of contents) are extracted. See http://www.pdfbox.org/
 * for details.
 * 
 * @author Frank L�tzenkirchen
 * @version $Revision: 1.3 $ $Date: 2006/09/15 11:37:50 $
 */
public class MCRDataExtractorPDF extends MCRDataExtractor {

    protected String getSupportedContentTypeIDs() {
        return "jpeg";
    }

    protected void extractData(Element container, InputStream in) throws Exception {
        PDDocument pdf = PDDocument.load(in);

        // Number of pages
        addDataValue(container, "numPages", String.valueOf(pdf.getNumberOfPages()));

        // Document information
        PDDocumentInformation info = pdf.getDocumentInformation();
        MCRMetaISO8601Date iso = new MCRMetaISO8601Date();
        Calendar cal = info.getCreationDate();
        if (cal != null) {
            iso.setDate(cal.getTime());
            addDataValue(container, "created", iso.getISOString());
        }
        cal = info.getModificationDate();
        if (cal != null) {
            iso.setDate(cal.getTime());
            addDataValue(container, "modified", iso.getISOString());
        }

        addDataValue(container, "author", info.getAuthor());
        addDataValue(container, "creator", info.getCreator());
        addDataValue(container, "keywords", info.getKeywords());
        addDataValue(container, "producer", info.getProducer());
        addDataValue(container, "subject", info.getSubject());
        addDataValue(container, "title", info.getTitle());

        // Document outline
        PDDocumentOutline root = pdf.getDocumentCatalog().getDocumentOutline();
        Element outline = new Element("outline");
        addOutlineItems(outline, root.getFirstChild());
        if (outline.getChildren().size() > 0)
            container.addContent(outline);

        pdf.close();
    }

    /**
     * Extracts the titles of outline items
     */
    private void addOutlineItems(Element parent, PDOutlineItem item) {
        while (item != null) {
            Element xItem = new Element("item");
            xItem.setAttribute("title", item.getTitle());
            parent.addContent(xItem);
            addOutlineItems(xItem, item.getFirstChild());
            item = item.getNextSibling();
        }
    }

    /**
     * Test application that outputs extracted metadata for a given local file.
     * 
     * @param args
     *            the path to a locally stored PDF file
     */
    public static void main(String[] args) {
        new MCRDataExtractorPDF().testLocalFile(args[0]);
    }
}
