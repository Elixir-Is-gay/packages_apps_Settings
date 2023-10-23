/*
 * Copyright (C) 2016 The Android Open Source Project
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
package com.android.settings.system;

import android.app.settings.SettingsEnums;
import android.provider.Settings;
import android.os.Bundle;
import android.util.Log;

import androidx.preference.Preference;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

import java.util.ArrayList;
import java.util.List;

@SearchIndexable
public class SystemDashboardFragment extends DashboardFragment {

    private static final String TAG = "SystemDashboardFrag";

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        final PreferenceScreen screen = getPreferenceScreen();
        // We do not want to display an advanced button if only one setting is hidden
        if (getVisiblePreferenceCount(screen) == screen.getInitialExpandedChildrenCount() + 1) {
            screen.setInitialExpandedChildrenCount(Integer.MAX_VALUE);
        }
    }

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.SETTINGS_SYSTEM_CATEGORY;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.system_dashboard_fragment;
    }

    @Override
    public int getHelpResource() {
        return R.string.help_url_system_dashboard;
    }

    @Override
    public void onResume() {
        super.onResume();
        applyCardLayout(getPreferenceScreen());
    }

    private void applyCardLayout(PreferenceGroup mPrefGroup) {
        if (mPrefGroup == null ) {
            Log.e(TAG, "mPrefGroup is null!");
            return;
        }
        int visibleCount = 0;
        List<String> prefList = new ArrayList<>();
        for (int i = 0; i < mPrefGroup.getPreferenceCount(); i++) {
            final Preference preference = mPrefGroup.getPreference(i);
            String key = preference.getKey();
            //Log.i(TAG, "Checking if Preference is Visible :- " + key);
            if ("null".equals(key)) {
                Log.e(TAG, "Preference at location :- " + i + " doesn't have a key assigned!");
            }
            if (preference.isVisible()) {
                prefList.add(key);
                visibleCount++;
            } else {
                Log.i(TAG, "Preference is not Visible :- " + key);
            }
        }
        boolean isDeveloperOptionsAvailable = Settings.Global.getInt(getActivity().getContentResolver(), Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) != 0;
        int remainder = visibleCount % 4;
        Log.i(TAG, "All preferences :- " + mPrefGroup.getPreferenceCount() + ", Visible :- " + visibleCount + ", Remains :- " + remainder);
        int count = 0;
        for (String currentPref : prefList) {
            count++;
            final Preference preference = mPrefGroup.findPreference(currentPref);
            //Log.i(TAG, "Setting layout for preference :- " + currentPref);
            if (preference instanceof com.android.settings.spa.preference.ComposePreference) {
                if (isDeveloperOptionsAvailable) {
                    preference.setLayoutResource(org.elixir.resources.cardlayout.R.layout.preference_compose_mid);
                }
            } else {
                preference.setLayoutResource(org.elixir.resources.cardlayout.R.layout.top_level_preference_middle);
            }

            if (count == 4) {
                if (preference instanceof com.android.settings.spa.preference.ComposePreference) {
                    if (isDeveloperOptionsAvailable) {
                        preference.setLayoutResource(org.elixir.resources.cardlayout.R.layout.preference_compose_bot);
                    }
                } else {
                    preference.setLayoutResource(org.elixir.resources.cardlayout.R.layout.top_level_preference_bottom);
                }
            }
            if (count == 5) {
                count = 1;
                if (preference instanceof com.android.settings.spa.preference.ComposePreference) {
                    if (isDeveloperOptionsAvailable) {
                        preference.setLayoutResource(org.elixir.resources.cardlayout.R.layout.preference_compose_top);
                    }
                } else {
                    preference.setLayoutResource(org.elixir.resources.cardlayout.R.layout.top_level_preference_top);
                }
            }
        }
        try {
            String firstPref = prefList.get(0);
            String lastPref = prefList.get(prefList.size() - 1);
            final Preference firstPreference = mPrefGroup.findPreference(firstPref);
            final Preference lastPreference = mPrefGroup.findPreference(lastPref);
            final Preference systemUpdatePreference = mPrefGroup.findPreference("additional_system_update_settings");
            if (firstPreference instanceof com.android.settings.spa.preference.ComposePreference) {
                if (isDeveloperOptionsAvailable) {
                    firstPreference.setLayoutResource(org.elixir.resources.cardlayout.R.layout.preference_compose_top);
                }
            } else {
                firstPreference.setLayoutResource(org.elixir.resources.cardlayout.R.layout.top_level_preference_top);
            }

            if (lastPreference instanceof com.android.settings.spa.preference.ComposePreference) {
                if (isDeveloperOptionsAvailable) {
                    if (remainder == 1) {
                        lastPreference.setLayoutResource(org.elixir.resources.cardlayout.R.layout.preference_compose_mid2);
                    } else {
                        lastPreference.setLayoutResource(org.elixir.resources.cardlayout.R.layout.preference_compose_bot);
                    }
                } else {
                    if (systemUpdatePreference != null) {
                        systemUpdatePreference.setLayoutResource(org.elixir.resources.cardlayout.R.layout.top_level_preference_bottom);
                    }
                }
            } else {
                if (remainder == 1) {
                    lastPreference.setLayoutResource(org.elixir.resources.cardlayout.R.layout.top_level_preference_middle2);
                } else {
                    lastPreference.setLayoutResource(org.elixir.resources.cardlayout.R.layout.top_level_preference_bottom);
                }
            }
        } catch (Exception exe) {
            Log.e(TAG, "Error applying layout :- " + exe.toString());
        }
        prefList.clear();
    }

    private int getVisiblePreferenceCount(PreferenceGroup group) {
        int visibleCount = 0;
        for (int i = 0; i < group.getPreferenceCount(); i++) {
            final Preference preference = group.getPreference(i);
            if (preference instanceof PreferenceGroup) {
                visibleCount += getVisiblePreferenceCount((PreferenceGroup) preference);
            } else if (preference.isVisible()) {
                visibleCount++;
            }
        }
        return visibleCount;
    }

    /**
     * For Search.
     */
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.system_dashboard_fragment);
}
