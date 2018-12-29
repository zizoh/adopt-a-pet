package com.zizohanto.adoptapet;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zizohanto.adoptapet.data.Pet;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String petJsonString = loadJSONFromAsset(this);

        //Create gson
        Gson gson = new GsonBuilder().create();
        Pet pet = gson.fromJson(petJsonString, Pet.class);

    }

    public String loadJSONFromAsset(Context context) {

        String json = null;
        try {
            InputStream is = context.getAssets().open("pet_adoption-1.json");

            int size = is.available();

            byte[] buffer = new byte[size];

            is.read(buffer);

            is.close();

            json = new String(buffer, "UTF-8");


        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;

    }
}