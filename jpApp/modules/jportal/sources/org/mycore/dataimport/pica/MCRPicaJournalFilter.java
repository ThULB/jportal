package org.mycore.dataimport.pica;

import org.mycore.importer.MCRImportField;
import org.mycore.importer.MCRImportRecord;

/**
 * The journal filter tests if a <code>MCRImportRecord</code> is a journal
 * or not. To do this the field "002@_0" is checked. If the value contains
 * a "b" at the second position, the record is a journal.
 * <ul>
 *  <li>A<b>v</b>i - not a journal</li>
 *  <li>A<b>b</b>vz - journal!</li>
 * </ul>
 * 
 * @author Matthias Eichner
 */
public class MCRPicaJournalFilter implements MCRPicaRecordFilter {

    @Override
    public boolean filter(MCRImportRecord record) {
        MCRImportField field = record.getFieldById("002@_0");
        if(field == null || field.getValue().length() < 2 || field.getValue().charAt(1) != 'b')
            return false;
        return true;
    }

}
