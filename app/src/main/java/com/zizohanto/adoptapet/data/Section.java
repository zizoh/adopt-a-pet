package com.zizohanto.adoptapet.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Section {

    @SerializedName("label")
    private String label;

    @SerializedName("elements")
    private List<Element> elements = null;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<Element> getElements() {
        return elements;
    }

    public void setElements(List<Element> elements) {
        this.elements = elements;
    }

}
