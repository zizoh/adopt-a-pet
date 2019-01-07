package com.zizohanto.adoptapet.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zizohanto.adoptapet.Constants;
import com.zizohanto.adoptapet.R;
import com.zizohanto.adoptapet.data.FormState;
import com.zizohanto.adoptapet.data.Page;
import com.zizohanto.adoptapet.data.Pet;
import com.zizohanto.adoptapet.utils.ActivityUtils;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity implements FormFragment.OnPageChangeListener,
        FormFragment.OnFragmentActivityCreatedListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String KEY_CURRENT_PAGE_NUMBER = "com.zizohanto.adoptapet.ui.KEY_CURRENT_PAGE_NUMBER";

    int mCurrentPageNumber;
    private Page mPage;
    private Pet mPet;

    private FormFragment mFormFragment;
    private FormFragment mSubsequentFragment;
    private FormState mExistingFragmentFormState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form);

        if (savedInstanceState != null) {
            mCurrentPageNumber = savedInstanceState.getInt(KEY_CURRENT_PAGE_NUMBER);
        }

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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(KEY_CURRENT_PAGE_NUMBER, mCurrentPageNumber);

        super.onSaveInstanceState(outState);
    }

    public String loadJSONFromAsset(Context context) {
        String json;
        try {
            InputStream is = context.getAssets().open("pet_adoption-1.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            //noinspection ResultOfMethodCallIgnored
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException exception) {
            Log.e(TAG, "Error writing/reading from the JSON file.", exception);
            return null;
        }
        return json;
    }

    private void createFragment(Page page) {
        mFormFragment = (FormFragment)
                getSupportFragmentManager().findFragmentByTag(String.valueOf(mCurrentPageNumber));
        if (mFormFragment == null) {
            // Create the fragment
            mFormFragment = FormFragment.newInstance(page, mCurrentPageNumber, getPagePosition());
            ActivityUtils.addFirstFragmentToActivity(
                    getSupportFragmentManager(), mFormFragment, R.id.content_frame, String.valueOf(mCurrentPageNumber));
        }
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
            goToPage();
        }
    }

    @Override
    public void onNextPageClick() {
        if (mCurrentPageNumber < mPet.getPages().size() - 1) {
            mCurrentPageNumber++;
            goToPage();
        }
    }

    private void goToPage() {
        Page page = mPet.getPages().get(mCurrentPageNumber);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.executePendingTransactions();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        mSubsequentFragment = (FormFragment)
                fragmentManager.findFragmentByTag(String.valueOf(mCurrentPageNumber));
        if (mSubsequentFragment == null) {
            mSubsequentFragment = FormFragment.newInstance(page, mCurrentPageNumber, getPagePosition());
            ActivityUtils.replaceExistingFragmentInActivity(
                    fragmentManager, mSubsequentFragment, R.id.content_frame, String.valueOf(mCurrentPageNumber));

        } else {
            // If the fragment already existed, we need to pull the existing form state.
            mExistingFragmentFormState = mSubsequentFragment.getFormState();

            if (mSubsequentFragment.isAdded()) { // if the fragment is already in container
                fragmentTransaction.show(mSubsequentFragment);
            } else { // fragment needs to be added to frame container
                fragmentTransaction.replace(R.id.content_frame, mSubsequentFragment, String.valueOf(mCurrentPageNumber));
            }
            fragmentTransaction.commit();
        }
    }

    @Override
    public void onBackPressed() {
        if (mCurrentPageNumber > 0) {
            mCurrentPageNumber--;
            goToPage();
        } else {
            Toast.makeText(this, "Already on the first page", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * @param isFirstTimeFragmentCreation is true if this fragment has not been created before
     *                                    If true then there is nothing to restore
     */
    @Override
    public void onFragmentActivityCreated(Boolean isFirstTimeFragmentCreation) {
        if (mSubsequentFragment != null && !isFirstTimeFragmentCreation) {
            // dump the existing form state in the fragment displayed
            mSubsequentFragment.restoreState(mExistingFragmentFormState);
        }
    }

}