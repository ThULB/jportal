package org.mycore.dataimport.pica;

import org.mycore.importer.MCRImportRecord;

/**
 * A record filter could be added to a pica catalog to verify if
 * a record is used or not.
 * 
 * @author Matthias Eichner
 */
public interface MCRPicaRecordFilter {

    /**
     * Tests if a record is valid or not. If its valid
     * true is returned, otherwise false.
     * 
     * @param record the record to test
     * @return true if valid, otherwise false
     */
    public boolean filter(MCRImportRecord record);

}
