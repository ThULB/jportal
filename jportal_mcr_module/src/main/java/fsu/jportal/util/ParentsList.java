package fsu.jportal.util;

public interface ParentsList<T> {

    public abstract void addParent(String title, String id, String inherited, String referer);

    public abstract T getParents();

}