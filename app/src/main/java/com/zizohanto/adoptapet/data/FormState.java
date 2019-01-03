package com.zizohanto.adoptapet.data;

import java.util.HashMap;

public class FormState {
    private HashMap<String, String> mElementIdAndUserInputMap;

    public FormState(HashMap<String, String> elementIdAndUserInputMap) {
        mElementIdAndUserInputMap = elementIdAndUserInputMap;
    }

    public HashMap<String, String> getElementIdAndUserInputMap() {
        return mElementIdAndUserInputMap;
    }
}
