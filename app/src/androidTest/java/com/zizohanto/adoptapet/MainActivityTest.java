package com.zizohanto.adoptapet;

import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.zizohanto.adoptapet.ui.MainActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.io.InputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@LargeTest
@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void loadTestDataFromAssets_opensPetAdoption1JsonFile() {

        InputStream is = null;
        try {
            is = mActivityTestRule.getActivity().getAssets().open("pet_adoption-1.json");
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertThat(is, notNullValue());
    }
}
