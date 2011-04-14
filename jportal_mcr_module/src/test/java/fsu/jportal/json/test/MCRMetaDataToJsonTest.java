package fsu.jportal.json.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.mycore.common.xml.MCRXMLHelper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import fsu.jportal.gson.RubricDeserializer;
import fsu.jportal.gson.RubricSerializer;
import fsu.jportal.metadata.Rubric;

public class MCRMetaDataToJsonTest {

    private Gson gson;
    private JsonObject jsonTestRubric;
    
    enum TestRubric{
        DE ("de","Text1","descr1"),
        EN ("en","Text2","descr2");
        
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
        gsonBuilder.registerTypeAdapter(Rubric.class, new RubricSerializer());
        gsonBuilder.registerTypeAdapter(Rubric.class, new RubricDeserializer());
        gson = gsonBuilder.create();
        jsonTestRubric = jsonTestRubric();
    }

    @Test
    public void rubricSerialization() throws Exception {
        Rubric rubric = new Rubric();
        rubric.addLabel(TestRubric.DE.lang, TestRubric.DE.text, TestRubric.DE.description);
        rubric.addLabel(TestRubric.EN.lang, TestRubric.EN.text, TestRubric.EN.description);
        
        String serializedRubric = gson.toJson(rubric);
        assertEquals(jsonTestRubric.toString(), serializedRubric);
        
        
        Rubric deserializedRubric = gson.fromJson(serializedRubric, Rubric.class);
        
        assertTrue(MCRXMLHelper.deepEqual(rubric.getRubricMetaElement().createXML(false), deserializedRubric.getRubricMetaElement().createXML(false)));
    }

    private JsonObject jsonTestRubric() {
        JsonArray jsonRubric = new JsonArray();
        jsonRubric.add(newLabelJsonObj(TestRubric.DE.lang, TestRubric.DE.text, TestRubric.DE.description));
        jsonRubric.add(newLabelJsonObj(TestRubric.EN.lang, TestRubric.EN.text, TestRubric.EN.description));
        JsonObject jsonObject = new JsonObject();
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
