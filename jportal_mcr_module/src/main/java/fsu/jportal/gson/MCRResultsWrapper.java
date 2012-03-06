package fsu.jportal.gson;

import java.util.Iterator;
import java.util.List;

import org.jdom.Element;
import org.mycore.services.fieldquery.MCRHit;
import org.mycore.services.fieldquery.MCRResults;
import org.mycore.services.fieldquery.MCRSortBy;

public class MCRResultsWrapper{
    private MCRResults results;
    
    public int hashCode() {
        return getResults().hashCode();
    }

    public String getID() {
        return getResults().getID();
    }

    public void addHit(MCRHit hit) {
        getResults().addHit(hit);
    }

    public boolean equals(Object obj) {
        return getResults().equals(obj);
    }

    public void addHits(Iterable<MCRHit> hits) {
        getResults().addHits(hits);
    }

    public MCRHit getHit(int i) {
        return getResults().getHit(i);
    }

    public int getNumHits() {
        return getResults().getNumHits();
    }

    public void cutResults(int maxResults) {
        getResults().cutResults(maxResults);
    }

    public void setSorted(boolean value) {
        getResults().setSorted(value);
    }

    public boolean isSorted() {
        return getResults().isSorted();
    }

    public void sortBy(List<MCRSortBy> sortByList) {
        getResults().sortBy(sortByList);
    }

    public Element buildXML(int min, int max) {
        return getResults().buildXML(min, max);
    }

    public Element buildXML() {
        return getResults().buildXML();
    }

    public String toString() {
        return getResults().toString();
    }

    public Iterator<MCRHit> iterator() {
        return getResults().iterator();
    }

    public void setHostConnection(String host, String msg) {
        getResults().setHostConnection(host, msg);
    }

    public boolean isReadonly() {
        return getResults().isReadonly();
    }

    public MCRResultsWrapper(MCRResults results) {
        this.setResults(results);
    }

    public void setResults(MCRResults results) {
        this.results = results;
    }

    public MCRResults getResults() {
        return results;
    }
}
