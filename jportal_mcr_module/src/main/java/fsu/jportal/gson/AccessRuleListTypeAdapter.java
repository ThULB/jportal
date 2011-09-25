package fsu.jportal.gson;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.List;

import javax.ws.rs.core.UriBuilder;

import org.mycore.backend.hibernate.tables.MCRACCESSRULE;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;

public class AccessRuleListTypeAdapter extends GsonTypeAdapter<AccessRuleList> {

    @Override
    public JsonElement serialize(AccessRuleList src, Type typeOfSrc, JsonSerializationContext context) {
        List<MCRACCESSRULE> ruleList = src.getRuleList();
        URI uri = src.getUri();
        JsonObject ruleListJson = new JsonObject();
        for (MCRACCESSRULE accessRule : ruleList) {
            JsonObject accessRuleJson = new JsonObject();
            accessRuleJson.addProperty("rid", accessRule.getRid());
            accessRuleJson.addProperty("rule", accessRule.getRule());
            accessRuleJson.addProperty("descr", accessRule.getDescription());
            ruleListJson.add(UriBuilder.fromUri(uri).path(accessRule.getRid()).build().toString(), accessRuleJson);
        }
        
        return ruleListJson;
    }

    @Override
    public AccessRuleList deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        // TODO Auto-generated method stub
        return null;
    }

   
}
