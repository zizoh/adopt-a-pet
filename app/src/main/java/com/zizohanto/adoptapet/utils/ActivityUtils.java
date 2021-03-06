package com.zizohanto.adoptapet.utils;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import static com.google.gson.internal.$Gson$Preconditions.checkNotNull;


/**
 * This provides methods to help Activities load their UI.
 */
public class ActivityUtils {

    /**
     * The {@code fragment} is added to the container view with id {@code frameId}. The operation is
     * performed by the {@code fragmentManager}.
     */
    public static void addFirstFragmentToActivity(@NonNull FragmentManager fragmentManager,
                                                  @NonNull Fragment fragment, int frameId, String tag) {
        checkNotNull(fragmentManager);
        checkNotNull(fragment);
        fragmentManager.beginTransaction()
                .add(frameId, fragment, tag)
                .commit();
    }

    public static void replaceExistingFragmentInActivity(@NonNull FragmentManager fragmentManager,
                                                         @NonNull Fragment fragment, int frameId, String tag) {
        checkNotNull(fragmentManager);
        checkNotNull(fragment);
        fragmentManager.beginTransaction()
                .replace(frameId, fragment, tag)
                .addToBackStack(tag)
                .commit();
    }
}