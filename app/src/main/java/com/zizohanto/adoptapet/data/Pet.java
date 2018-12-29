package com.zizohanto.adoptapet.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Pet {

    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("pages")
    private List<Page> pages = null;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Page> getPages() {
        return pages;
    }

    public void setPages(List<Page> pages) {
        this.pages = pages;
    }

}
