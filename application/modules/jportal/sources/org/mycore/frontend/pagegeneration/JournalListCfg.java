package org.mycore.frontend.pagegeneration;

import java.util.TreeSet;

import org.mycore.services.fieldquery.MCRQuery;

/**
 * @author Huu Chi Vu
 *
 */
public class JournalListCfg {
    public static class JournalListDef implements Comparable<JournalListDef>{
        private String fileName;
        private MCRQuery query;
        
        protected JournalListDef() {
        }
        
        public JournalListDef(String fileName, MCRQuery query) {
            this.setFileName(fileName);
            this.setQuery(query);
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getFileName() {
            return fileName;
        }

        public void setQuery(MCRQuery query) {
            this.query = query;
        }

        public MCRQuery getQuery() {
            return query;
        }

        public int compareTo(JournalListDef o) {
            return getFileName().compareTo(o.getFileName());
        }
    }

    private TreeSet<JournalListDef> listDefs = new TreeSet<JournalListDef>();
    
    public boolean addListDef(JournalListDef listDef){
        return getListDefs().add(listDef);
    }
    
    public boolean removeListDef(JournalListDef listDef){
        return getListDefs().remove(listDef);
    }

    private void setListDefs(TreeSet<JournalListDef> listDefs) {
        this.listDefs = listDefs;
    }

    public TreeSet<JournalListDef> getListDefs() {
        return listDefs;
    }
}
