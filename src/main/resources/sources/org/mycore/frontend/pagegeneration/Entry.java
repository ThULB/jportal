/**
 * 
 */
package org.mycore.frontend.pagegeneration;

public class Entry implements Comparable<Entry> {
    private String title;

    private String id;

    protected Entry() {
    }

    public Entry(String title, String id) {
        setTitle(title);
        setId(id);
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public int compareTo(Entry o) {
        int titleEq = this.title.compareToIgnoreCase(o.getTitle());
        
        if(titleEq == 0) {
            return this.id.compareToIgnoreCase(o.getId());
        }
        
        return titleEq;
    }
}