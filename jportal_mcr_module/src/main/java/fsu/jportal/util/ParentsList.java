package fsu.jportal.util;

public interface ParentsList<T> {

    public abstract void addParent(String title, String id, String inherited);

    public abstract T getParents();

}