package com.zizohanto.adoptapet.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zizohanto.adoptapet.R;
import com.zizohanto.adoptapet.data.Page;
import com.zizohanto.adoptapet.data.Pet;
import com.zizohanto.adoptapet.utils.ActivityUtils;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form);

        String petJsonString = loadJSONFromAsset(this);

        //Create gson
        Gson gson = new GsonBuilder().create();
        Pet pet = gson.fromJson(petJsonString, Pet.class);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(pet.getName());
        }

        addFragmentToActivity(pet.getPages().get(2));

    }

    public String loadJSONFromAsset(Context context) {

        String json;
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

    private void addFragmentToActivity(Page page) {
        FormFragment formFragment =
                (FormFragment) getSupportFragmentManager().findFragmentById(R.id.content_frame);
        if (formFragment == null) {
            // Create the fragment
            formFragment = FormFragment.newInstance(page);
            ActivityUtils.addFragmentToActivity(
                    getSupportFragmentManager(), formFragment, R.id.content_frame);
        }
    }
}