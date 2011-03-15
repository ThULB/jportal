package fsu.jportal.metadata;

public class RubricLabel extends XMLMetaElementEntry{
    private String lang;
    final String TEXT = "text";
    final String DESCRIPTION = "description";
    
    public RubricLabel(String lang, String text, String description) {
        this.lang = lang;
        getTagValueMap().put(TEXT, text);
        getTagValueMap().put(DESCRIPTION, description);
    }

    @Override
    public String getLang() {
        return lang;
    }

    @Override
    public String getMetaElemName() {
        return "label";
    }
}