/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.settings;

import android.app.settings.SettingsEnums;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.preference.Preference;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceScreen;
import androidx.preference.PreferenceCategory;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.display.BrightnessLevelPreferenceController;
import com.android.settings.display.CameraGesturePreferenceController;
import com.android.settings.display.LiftToWakePreferenceController;
import com.android.settings.display.ShowOperatorNamePreferenceController;
import com.android.settings.display.TapToWakePreferenceController;
import com.android.settings.display.EnableBlursPreferenceController;
import com.android.settings.display.ThemePreferenceController;
import com.android.settings.display.VrDisplayPreferenceController;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.search.SearchIndexable;

import lineageos.hardware.LineageHardwareManager;

import java.util.ArrayList;
import java.util.List;

@SearchIndexable(forTarget = SearchIndexable.ALL & ~SearchIndexable.ARC)
public class DisplaySettings extends DashboardFragment {
    private static final String TAG = "DisplaySettings";

    private static final String KEY_HIGH_TOUCH_POLLING_RATE = "high_touch_polling_rate_enable";
    private static final String KEY_HIGH_TOUCH_SENSITIVITY = "high_touch_sensitivity_enable";
    private static final String KEY_PROXIMITY_ON_WAKE = "proximity_on_wake";

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.DISPLAY;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.display_settings;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
    }

    @Override
    public void onResume() {
        super.onResume();
        applyCardLayout();
    }

    private void applyCardLayout() {
        PreferenceScreen mMainScreenPrefCat = (PreferenceScreen) getPreferenceScreen();
        applyCardLayout(mMainScreenPrefCat);
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
                continue;
            }
            if (preference instanceof PreferenceCategory) {
                PreferenceCategory mPrefCat = (PreferenceCategory) mPrefGroup.findPreference(key);
                applyCardLayoutToCategory(mPrefCat);
                continue;
            }
            if (preference.getParent() instanceof PreferenceCategory) {
                continue;
            }
            if (preference.isVisible()) {
                prefList.add(key);
                visibleCount++;
            } else {
                Log.i(TAG, "Preference is not Visible :- " + key);
            }
        }
        int remainder = visibleCount % 4;
        Log.i(TAG, "All preferences :- " + mPrefGroup.getPreferenceCount() + ", Visible :- " + visibleCount + ", Remains :- " + remainder);
        int count = 0;
        for (String currentPref : prefList) {
            count++;
            final Preference preference = mPrefGroup.findPreference(currentPref);
            //Log.i(TAG, "Setting layout for preference :- " + currentPref);
            preference.setLayoutResource(org.elixir.resources.cardlayout.R.layout.xd_pref_card_mid);

            if (count == 4) {
                preference.setLayoutResource(org.elixir.resources.cardlayout.R.layout.xd_pref_card_bot);
            }
            if (count == 5) {
                count = 1;
                preference.setLayoutResource(org.elixir.resources.cardlayout.R.layout.xd_pref_card_top);
            }
        }
        try {
            String firstPref = prefList.get(0);
            String lastPref = prefList.get(prefList.size() - 1);
            final Preference firstPreference = mPrefGroup.findPreference(firstPref);
            final Preference lastPreference = mPrefGroup.findPreference(lastPref);
            final Preference systemUpdatePreference = mPrefGroup.findPreference("additional_system_update_settings");
            firstPreference.setLayoutResource(org.elixir.resources.cardlayout.R.layout.xd_pref_card_top);
            if (remainder == 1) {
                lastPreference.setLayoutResource(org.elixir.resources.cardlayout.R.layout.xd_pref_card_mid2);
            } else {
                lastPreference.setLayoutResource(org.elixir.resources.cardlayout.R.layout.xd_pref_card_bot);
            }
        } catch (Exception exe) {
            Log.e(TAG, "Error applying layout :- " + exe.toString());
        }
        prefList.clear();
    }

    private void applyCardLayoutToCategory(PreferenceCategory mPrefCategory) {
        if (mPrefCategory == null ) {
            Log.e(TAG, "mPrefCategory is null!");
            return;
        }
        int count = mPrefCategory.getPreferenceCount();
        for (int i = 0; i < count; i++) {
            final Preference preference = mPrefCategory.getPreference(i);
            String key = preference.getKey();
            //Log.i(TAG, "Setting layout for preference :- " + key);
            preference.setLayoutResource(org.elixir.resources.cardlayout.R.layout.xd_pref_card_mid);
            if (i == 0) {
                Log.i(TAG, "Setting Top layout for preference :- " + key);
                preference.setLayoutResource(org.elixir.resources.cardlayout.R.layout.xd_pref_card_top);
            }
            if (i == count - 1) {
                Log.i(TAG, "Setting Bottom layout for preference :- " + key);
                preference.setLayoutResource(org.elixir.resources.cardlayout.R.layout.xd_pref_card_bot);
            }
        }
    }

    @Override
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildPreferenceControllers(context, getSettingsLifecycle());
    }

    @Override
    public int getHelpResource() {
        return R.string.help_uri_display;
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(
            Context context, Lifecycle lifecycle) {
        final List<AbstractPreferenceController> controllers = new ArrayList<>();
        controllers.add(new CameraGesturePreferenceController(context));
        controllers.add(new LiftToWakePreferenceController(context));
        controllers.add(new TapToWakePreferenceController(context));
        controllers.add(new EnableBlursPreferenceController(context));
        controllers.add(new VrDisplayPreferenceController(context));
        controllers.add(new ShowOperatorNamePreferenceController(context));
        controllers.add(new ThemePreferenceController(context));
        controllers.add(new BrightnessLevelPreferenceController(context, lifecycle));
        return controllers;
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.display_settings) {

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);
                    LineageHardwareManager hardware = LineageHardwareManager.getInstance(context);
                    if (!hardware.isSupported(
                            LineageHardwareManager.FEATURE_HIGH_TOUCH_POLLING_RATE)) {
                        keys.add(KEY_HIGH_TOUCH_POLLING_RATE);
                    }
                    if (!hardware.isSupported(
                            LineageHardwareManager.FEATURE_HIGH_TOUCH_SENSITIVITY)) {
                        keys.add(KEY_HIGH_TOUCH_SENSITIVITY);
                    }
                    if (!context.getResources().getBoolean(
                            org.lineageos.platform.internal.R.bool.config_proximityCheckOnWake)) {
                        keys.add(KEY_PROXIMITY_ON_WAKE);
                    }
                    return keys;
                }

                @Override
                public List<AbstractPreferenceController> createPreferenceControllers(
                        Context context) {
                    return buildPreferenceControllers(context, null);
                }
            };
}
