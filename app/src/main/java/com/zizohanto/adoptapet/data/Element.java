package com.zizohanto.adoptapet.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Element {

    @SerializedName("type")
    private String type;

    @SerializedName("file")
    private String file;

    @SerializedName("unique_id")
    private String uniqueId;

    @SerializedName("rules")
    private List<Rule> rules = null;

    @SerializedName("label")
    private String label;

    @SerializedName("isMandatory")
    private Boolean isMandatory;

    @SerializedName("keyboard")
    private String keyboard;

    @SerializedName("formattedNumeric")
    private String formattedNumeric;

    @SerializedName("mode")
    private String mode;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public List<Rule> getRules() {
        return rules;
    }

    public void setRules(List<Rule> rules) {
        this.rules = rules;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Boolean getIsMandatory() {
        return isMandatory;
    }

    public void setIsMandatory(Boolean isMandatory) {
        this.isMandatory = isMandatory;
    }

    public String getKeyboard() {
        return keyboard;
    }

    public void setKeyboard(String keyboard) {
        this.keyboard = keyboard;
    }

    public String getFormattedNumeric() {
        return formattedNumeric;
    }

    public void setFormattedNumeric(String formattedNumeric) {
        this.formattedNumeric = formattedNumeric;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

}
