package fsu.jportal.parser;

public interface IPAddressList<T>{
    public T getList();

    public void add(IPAddress address);
}