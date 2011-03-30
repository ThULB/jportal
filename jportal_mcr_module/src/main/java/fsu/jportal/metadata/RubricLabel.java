package fsu.jportal.metadata;

public class RubricLabel extends XMLMetaElementEntry{
    private String lang;
    final transient String TEXT = "text";
    final transient String DESCRIPTION = "description";
    
    public RubricLabel() {}
    
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