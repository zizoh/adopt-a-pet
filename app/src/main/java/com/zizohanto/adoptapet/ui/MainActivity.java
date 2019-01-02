package com.zizohanto.adoptapet.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zizohanto.adoptapet.Constants;
import com.zizohanto.adoptapet.R;
import com.zizohanto.adoptapet.data.Page;
import com.zizohanto.adoptapet.data.Pet;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity implements FormFragment.OnPageChangeListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    int mCurrentPageNumber;
    private Page mPage;
    private Pet mPet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form);

        String petJsonString = loadJSONFromAsset(this);

        //Create gson
        Gson gson = new GsonBuilder().create();
        mPet = gson.fromJson(petJsonString, Pet.class);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(mPet.getName());
        }

        mPage = mPet.getPages().get(mCurrentPageNumber);
        createFragment(mPage);

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

    private void createFragment(Page page) {
        FormFragment formFragment = FormFragment.newInstance(page, mCurrentPageNumber, getPagePosition());
        getSupportFragmentManager().beginTransaction().
                add(R.id.content_frame, formFragment).
                commit();
    }

    private int getPagePosition() {
        if (mCurrentPageNumber == 0) {
            return Constants.CURRENT_PAGE_POSITION_FIRST;
        } else if (mCurrentPageNumber < mPet.getPages().size() - 1) {
            return Constants.CURRENT_PAGE_POSITION_BETWEEN;
        }
        return Constants.CURRENT_PAGE_POSITION_LAST;
    }

    @Override
    public void onPrevPageClick() {
        if (mCurrentPageNumber > 0) {
            mCurrentPageNumber--;
            goToPage(mPet.getPages().get(mCurrentPageNumber));
        }
    }

    @Override
    public void onNextPageClick() {
        if (mCurrentPageNumber < mPet.getPages().size() - 1) {
            mCurrentPageNumber++;
            goToPage(mPet.getPages().get(mCurrentPageNumber));
        }
    }

    private void goToPage(Page page) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        FormFragment newFragment = FormFragment.newInstance(page, mCurrentPageNumber, getPagePosition());
        transaction.replace(R.id.content_frame, newFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if (mCurrentPageNumber > 0) mCurrentPageNumber--;
    }
}