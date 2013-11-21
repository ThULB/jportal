package fsu.jportal.gson;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import fsu.jportal.util.ParentsList;

public class ParentsListJSON implements ParentsList<String> {
    JsonArray parentsList = new JsonArray();

    @Override
    public void addParent(String title, String id, String inherited, String referer) {
        JsonObject parent = new JsonObject();
        parent.addProperty("title", title);
        parent.addProperty("id", id);
        parent.addProperty("inherited", inherited);
        parent.addProperty("referer", referer);
        parentsList.add(parent);
    }

    @Override
    public String getParents() {
        return parentsList.toString();
    }

}