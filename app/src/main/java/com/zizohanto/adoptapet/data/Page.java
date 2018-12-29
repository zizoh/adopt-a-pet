package com.zizohanto.adoptapet.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Page {

    @SerializedName("label")
    private String label;

    @SerializedName("sections")
    private List<Section> sections = null;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<Section> getSections() {
        return sections;
    }

    public void setSections(List<Section> sections) {
        this.sections = sections;
    }

}
