/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.deviceinfo.storage;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.UserInfo;
import android.graphics.drawable.Drawable;
import android.os.UserManager;
import android.util.Log;
import android.util.SparseArray;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.preference.Preference;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceScreen;
import androidx.preference.PreferenceCategory;

import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.deviceinfo.StorageItemPreference;
import com.android.settingslib.core.AbstractPreferenceController;

import java.util.ArrayList;
import java.util.List;

/**
 * NonCurrentUserController controls the preferences on the Storage screen which had to do with
 * other users.
 */
public class NonCurrentUserController extends AbstractPreferenceController implements
        PreferenceControllerMixin, StorageAsyncLoader.ResultHandler,
        UserIconLoader.UserIconHandler {
    // PreferenceGroupKey to try to add our preference onto.
    private static final String TARGET_PREFERENCE_GROUP_KEY = "pref_non_current_users";
    private static final String PREFERENCE_KEY_BASE = "pref_user_";
    private static final int SIZE_NOT_SET = -1;

    private @NonNull
    UserInfo mUser;
    private @Nullable
    StorageItemPreference mStoragePreference;
    private PreferenceGroup mPreferenceGroup;
    private Drawable mUserIcon;
    private long mSize;
    private long mTotalSizeBytes;
    private boolean mIsVisible;
    private int[] mProfiles;
    private StorageCacheHelper mStorageCacheHelper;

    /**
     * Adds the appropriate controllers to a controller list for handling all full non current
     * users on a device.
     *
     * @param context     Context for initializing the preference controllers.
     * @param userManager UserManagerWrapper for figuring out which controllers to add.
     */
    public static List<NonCurrentUserController> getNonCurrentUserControllers(
            Context context, UserManager userManager) {
        int currentUserId = ActivityManager.getCurrentUser();
        List<NonCurrentUserController> controllers = new ArrayList<>();
        List<UserInfo> infos = userManager.getUsers();
        for (UserInfo info : infos) {
            if (info.id == currentUserId || !info.isFull()) {
                continue;
            }
            int[] profiles = userManager.getProfileIds(info.id, false /* enabledOnly */);
            controllers.add(new NonCurrentUserController(context, info, profiles));
        }
        return controllers;
    }

    /**
     * Constructor for a given non-current user.
     *
     * @param context  Context to initialize the underlying {@link AbstractPreferenceController}.
     * @param info     {@link UserInfo} for the non-current user which these controllers cover.
     * @param profiles list of IDs or user and its profiles
     */
    @VisibleForTesting
    NonCurrentUserController(Context context, @NonNull UserInfo info, @NonNull int[] profiles) {
        super(context);
        mUser = info;
        mSize = SIZE_NOT_SET;
        mStorageCacheHelper = new StorageCacheHelper(context, info.id);
        mProfiles = profiles;
    }

    /**
     * Constructor for a given non-current user.
     *
     * @param context Context to initialize the underlying {@link AbstractPreferenceController}.
     * @param info    {@link UserInfo} for the non-current user which these controllers cover.
     */
    @VisibleForTesting
    NonCurrentUserController(Context context, @NonNull UserInfo info) {
        super(context);
        mUser = info;
        mSize = SIZE_NOT_SET;
        mStorageCacheHelper = new StorageCacheHelper(context, info.id);
        mProfiles = new int[]{info.id};
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        if (mStoragePreference == null) {
            mStoragePreference = new StorageItemPreference(screen.getContext());

            mPreferenceGroup = screen.findPreference(TARGET_PREFERENCE_GROUP_KEY);
            mStoragePreference.setTitle(mUser.name);
            mStoragePreference.setKey(PREFERENCE_KEY_BASE + mUser.id);
            setSize(mStorageCacheHelper.retrieveUsedSize(), false /* animate */);
            mPreferenceGroup.setVisible(mIsVisible);
            mPreferenceGroup.addPreference(mStoragePreference);
            applyCardLayoutToGroup(mPreferenceGroup);
            maybeSetIcon();
        }
    }

    private void applyCardLayoutToGroup(PreferenceGroup mPrefGroup) {
        if (mPrefGroup == null ) {
            Log.e(TAG, "mPrefGroup is null!");
            return;
        }
        int visibleCount = 0;
        List<String> prefList = new ArrayList<>();
        if (mPrefGroup.getPreferenceCount() <= 1 ) {
            Log.i(TAG, "Only 1 preference in NonUser Category!");
            final Preference preference = mPrefGroup.getPreference(0);
            preference.setLayoutResource(org.elixir.resources.cardlayout.R.layout.xd_pref_card_mid2);
            return;
        }
        for (int i = 0; i < mPrefGroup.getPreferenceCount(); i++) {
            final Preference preference = mPrefGroup.getPreference(i);
            String key = preference.getKey();
            Log.i(TAG, "Checking if Preference is Visible :- " + key);
            if ("null".equals(key)) {
                Log.e(TAG, "Preference at location :- " + i + " doesn't have a key assigned!");
            }
            if (preference.isVisible()) {
                //Log.i(TAG, "Preference isVisible :- " + key);
                if (key.equals("battery_header") || key.equals("battery_help_message") || key.equals("battery_tip") || key.equals("power_usage_footer")) {
                    Log.i(TAG, "Skip setting layout for preference " + key);
                } else {
                    prefList.add(key);
                    visibleCount++;
                }
            } else {
                Log.i(TAG, "Preference is not Visible :- " + key);
            }
        }
        Log.i(TAG, "All preferences :- " + mPrefGroup.getPreferenceCount() + ", Visible :- " + visibleCount);
        for (String currentPref : prefList) {
            try {
                final Preference preference = mPrefGroup.findPreference(currentPref);
                Log.i(TAG, "Setting layout for preference :- " + currentPref);
                preference.setLayoutResource(org.elixir.resources.cardlayout.R.layout.xd_pref_card_mid);
            } catch (Exception ex) {
                Log.e(TAG, "Error applying layout :- " + ex.toString());
            }
        }
        try {
            String firstPref = prefList.get(0);
            String lastPref = prefList.get(prefList.size() - 1);
            final Preference firstPreference = mPrefGroup.findPreference(firstPref);
            final Preference lastPreference = mPrefGroup.findPreference(lastPref);
            firstPreference.setLayoutResource(org.elixir.resources.cardlayout.R.layout.xd_pref_card_top);
            lastPreference.setLayoutResource(org.elixir.resources.cardlayout.R.layout.xd_pref_card_bot);
        } catch (Exception exe) {
            Log.e(TAG, "Error applying layout :- " + exe.toString());
        }
        prefList.clear();
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String getPreferenceKey() {
        return mStoragePreference != null ? mStoragePreference.getKey() : null;
    }

    /**
     * Returns the user for which this is the non-current user controller.
     */
    @NonNull
    public UserInfo getUser() {
        return mUser;
    }

    /**
     * Sets the size for the preference.
     *
     * @param size Size in bytes.
     */
    public void setSize(long size, boolean animate) {
        mSize = size;
        if (mStoragePreference != null) {
            mStoragePreference.setStorageSize(mSize, mTotalSizeBytes, animate);
        }
    }

    /**
     * Sets the total size for the preference for the progress bar.
     *
     * @param totalSizeBytes Total size in bytes.
     */
    public void setTotalSize(long totalSizeBytes) {
        mTotalSizeBytes = totalSizeBytes;
    }

    /**
     * Sets visibility of the PreferenceGroup of non-current user.
     *
     * @param visible Visibility of the PreferenceGroup.
     */
    public void setPreferenceGroupVisible(boolean visible) {
        mIsVisible = visible;
        if (mPreferenceGroup != null) {
            mPreferenceGroup.setVisible(mIsVisible);
        }
    }

    @Override
    public void handleResult(SparseArray<StorageAsyncLoader.StorageResult> stats) {
        if (stats == null) {
            setSize(mStorageCacheHelper.retrieveUsedSize(), false /* animate */);
            return;
        }
        final StorageAsyncLoader.StorageResult result = stats.get(getUser().id);

        if (result != null) {
            long totalSize = 0;
            for (int id : mProfiles) {
                totalSize += stats.get(id).externalStats.totalBytes;
            }
            setSize(totalSize, true /* animate */);
            // TODO(b/171758224): Update the source of size info
            mStorageCacheHelper.cacheUsedSize(totalSize);
        }
    }

    @Override
    public void handleUserIcons(SparseArray<Drawable> fetchedIcons) {
        mUserIcon = fetchedIcons.get(mUser.id);
        maybeSetIcon();
    }

    private void maybeSetIcon() {
        if (mUserIcon != null && mStoragePreference != null) {
            mStoragePreference.setIcon(mUserIcon);
        }
    }
}
