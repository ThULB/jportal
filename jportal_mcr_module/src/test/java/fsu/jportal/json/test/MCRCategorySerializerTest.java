package fsu.jportal.json.test;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;
import org.mycore.datamodel.classifications2.MCRCategory;
import org.mycore.datamodel.classifications2.MCRCategoryID;
import org.mycore.datamodel.classifications2.MCRLabel;
import org.mycore.datamodel.classifications2.impl.MCRCategoryImpl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import fsu.jportal.gson.MCRCategoryDeserializer;
import fsu.jportal.gson.MCRCategorySerializer;
import fsu.jportal.metadata.Rubric;
import fsu.jportal.utils.MCRCategUtils;

public class MCRCategorySerializerTest {
    private Gson gson;

    private JsonObject jsonTestRubric;

    enum TestRubric {
        DE("de", "Text1", "descr1"), EN("en", "Text2", "descr2");

        private String lang;

        private String text;

        private String description;

        TestRubric(String lang, String text, String description) {
            this.lang = lang;
            this.text = text;
            this.description = description;
        }
    }

    @Before
    public void init() {
        System.setProperty("MCR.Configuration.File", "config/test.properties");
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(MCRCategoryImpl.class, new MCRCategorySerializer());
        gsonBuilder.registerTypeAdapter(MCRCategoryImpl.class, new MCRCategoryDeserializer());
        gson = gsonBuilder.create();
        jsonTestRubric = jsonTestRubric();
    }

    @Test
    public void rubricSerialization() throws Exception {
        HashSet<MCRLabel> labels = categLabels();
        MCRCategoryID rootID = MCRCategoryID.rootID("rootID");
        MCRCategory category = MCRCategUtils.newCategory(rootID, labels, null);

        String serializedRubric = gson.toJson(category);
        System.out.println(serializedRubric);
        assertEquals(jsonTestRubric.toString(), serializedRubric);

        MCRCategory deserializedRubric = gson.fromJson(serializedRubric, MCRCategoryImpl.class);
        assertTrue("Expected ID " + rootID + " but was " + deserializedRubric.getId(), deserializedRubric.getId().equals(rootID));
        assertEquals(2, deserializedRubric.getLabels().size());
    }
    
    @Test
    public void serializationNoID() throws Exception {
        HashSet<MCRLabel> labels = categLabels();
        MCRCategory category = MCRCategUtils.newCategory(null, labels, null);

        String serializedRubric = gson.toJson(category);
        System.out.println(serializedRubric);
//        assertEquals(jsonTestRubric.toString(), serializedRubric);

        MCRCategory deserializedRubric = gson.fromJson(serializedRubric, MCRCategoryImpl.class);
        assertEquals(2, deserializedRubric.getLabels().size());
    }

    private HashSet<MCRLabel> categLabels() {
        HashSet<MCRLabel> labels = new HashSet<MCRLabel>();
        labels.add(new MCRLabel(TestRubric.DE.lang, TestRubric.DE.text, TestRubric.DE.description));
        labels.add(new MCRLabel(TestRubric.EN.lang, TestRubric.EN.text, TestRubric.EN.description));
        return labels;
    }

    private JsonObject jsonTestRubric() {
        JsonArray jsonRubric = new JsonArray();
        jsonRubric.add(newLabelJsonObj(TestRubric.DE.lang, TestRubric.DE.text, TestRubric.DE.description));
        jsonRubric.add(newLabelJsonObj(TestRubric.EN.lang, TestRubric.EN.text, TestRubric.EN.description));
        JsonObject jsonObject = new JsonObject();
        
        JsonObject idJsonObj = new JsonObject();
        idJsonObj.addProperty(Rubric.ROOTID, "rootID");
        idJsonObj.addProperty(Rubric.CATEGID, "");
        jsonObject.add(Rubric.ID, idJsonObj);
        jsonObject.add(Rubric.LABEL, jsonRubric);
        return jsonObject;

    }

    private JsonObject newLabelJsonObj(String lang, String text, String description) {
        JsonObject label = new JsonObject();
        label.addProperty(Rubric.LANG, lang);
        label.addProperty(Rubric.TEXT, text);
        label.addProperty(Rubric.DESCRIPTION, description);
        return label;
    }
}
