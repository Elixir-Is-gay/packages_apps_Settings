/*
 * Copyright (C) 2021 The Android Open Source Project
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

package com.android.settings.applications;

import android.app.settings.SettingsEnums;
import android.content.Context;
import android.provider.SearchIndexableResource;
import android.util.Log;

import com.android.internal.annotations.VisibleForTesting;
import com.android.settings.R;
import com.android.settings.applications.appcompat.UserAspectRatioAppsPreferenceController;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.security.applock.AppLockSettingsPreferenceController;
import com.android.settings.widget.PreferenceCategoryController;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.search.SearchIndexable;

import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceScreen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Settings page for apps. */
@SearchIndexable
public class AppDashboardFragment extends DashboardFragment {

    private static final String TAG = "AppDashboardFragment";
    private static final String ADVANCED_CATEGORY_KEY = "advanced_category";
    private static final String ASPECT_RATIO_PREF_KEY = "aspect_ratio_apps";
    private static final String APP_LOCK_PREF_KEY = "app_lock";
    private AppsPreferenceController mAppsPreferenceController;

    private static List<AbstractPreferenceController> buildPreferenceControllers(Context context,
            DashboardFragment host, Lifecycle lifecycle) {
        final List<AbstractPreferenceController> controllers = new ArrayList<>();
        controllers.add(new AppsPreferenceController(context));

        final UserAspectRatioAppsPreferenceController aspectRatioAppsPreferenceController =
                new UserAspectRatioAppsPreferenceController(context, ASPECT_RATIO_PREF_KEY);
        final AdvancedAppsPreferenceCategoryController advancedCategoryController =
                new AdvancedAppsPreferenceCategoryController(context, ADVANCED_CATEGORY_KEY);
        advancedCategoryController.setChildren(List.of(aspectRatioAppsPreferenceController));
        controllers.add(advancedCategoryController);
        controllers.add(new AppLockSettingsPreferenceController(
                context, APP_LOCK_PREF_KEY, host, lifecycle));

        return controllers;
    }

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.MANAGE_APPLICATIONS;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    public int getHelpResource() {
        return R.string.help_url_apps_and_notifications;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.apps;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mAppsPreferenceController = use(AppsPreferenceController.class);
        mAppsPreferenceController.setFragment(this /* fragment */);
        getSettingsLifecycle().addObserver(mAppsPreferenceController);

        final HibernatedAppsPreferenceController hibernatedAppsPreferenceController =
                use(HibernatedAppsPreferenceController.class);
        getSettingsLifecycle().addObserver(hibernatedAppsPreferenceController);
    }

    @VisibleForTesting
    PreferenceCategoryController getAdvancedAppsPreferenceCategoryController() {
        return use(AdvancedAppsPreferenceCategoryController.class);
    }

    @Override
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildPreferenceControllers(context, this /* host */, getSettingsLifecycle());
    }

    @Override
    public void onResume() {
        super.onResume();
        applyCardLayout();
    }

    private void applyCardLayout() {
        PreferenceScreen mMainScreenPrefCat = (PreferenceScreen) getPreferenceScreen();
        applyCardLayoutToCategory(mMainScreenPrefCat);
        //Preference mTimeoutPref = (Preference) mMainScreenPrefCat.findPreference("screen_timeout");
    }

    private void applyCardLayoutToCategory(PreferenceGroup mPrefCategory) {
        if (mPrefCategory == null ) {
            Log.e(TAG, "mPrefCategory is null!");
            return;
        }
        int visibleCount = 0;
        List<String> prefList = new ArrayList<>();
        for (int i = 0; i < mPrefCategory.getPreferenceCount(); i++) {
            final Preference preference = mPrefCategory.getPreference(i);
            String key = preference.getKey();
            //Log.i(TAG, "Checking if Preference is Visibe :- " + key);
            if ("null".equals(key)) {
                Log.e(TAG, "Preference at location :- " + i + " doesn't have a key assigned!");
            }
            if (preference.isVisible()) {
                //Log.i(TAG, "Preference isVisible :- " + key);
                if (key.contains("category")) {
                    Log.i(TAG, "Skip setting layout for preference category");
                } else {
                    prefList.add(key);
                    visibleCount++;
                }
            }
        }
        Log.i(TAG, "All preferences :- " + mPrefCategory.getPreferenceCount() + ", Visible :- " + visibleCount);
        for (String currentPref : prefList) {
            try {
                final Preference preference = mPrefCategory.findPreference(currentPref);
                Log.i(TAG, "Setting layout for preference :- " + currentPref);
                preference.setLayoutResource(org.elixir.resources.cardlayout.R.layout.xd_pref_card_mid);
            } catch (Exception ex) {
                Log.e(TAG, "Error applying layout :- " + ex.toString());
            }

        }
        try {
            String firstPref = prefList.get(0);
            String lastPref = prefList.get(prefList.size() - 1);
            final Preference firstPreference = mPrefCategory.findPreference(firstPref);
            final Preference lastPreference = mPrefCategory.findPreference(lastPref);
            firstPreference.setLayoutResource(org.elixir.resources.cardlayout.R.layout.xd_pref_card_top);
            lastPreference.setLayoutResource(org.elixir.resources.cardlayout.R.layout.xd_pref_card_bot);
        } catch (Exception exe) {
            Log.e(TAG, "Error applying layout :- " + exe.toString());
        }
        prefList.clear();
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(
                        Context context, boolean enabled) {
                    final SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.apps;
                    return Arrays.asList(sir);
                }

                @Override
                public List<AbstractPreferenceController> createPreferenceControllers(
                        Context context) {
                    return buildPreferenceControllers(context, null /* host */, null);
                }
            };
}
