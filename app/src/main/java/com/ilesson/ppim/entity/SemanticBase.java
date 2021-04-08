package com.ilesson.ppim.entity;

public class SemanticBase {
    private String code;
    private String intent;
    private String text;
    private Semantic semantic;
    public void setCode(String code) {
        this.code = code;
    }
    public String getCode() {
        return code;
    }

    public void setIntent(String intent) {
        this.intent = intent;
    }
    public String getIntent() {
        return intent;
    }

    public void setText(String text) {
        this.text = text;
    }
    public String getText() {
        return text;
    }

    public void setSemantic(Semantic semantic) {
        this.semantic = semantic;
    }
    public Semantic getSemantic() {
        return semantic;
    }
}
