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

package com.android.settings.gestures;

import android.app.settings.SettingsEnums;
import android.content.Context;
import android.hardware.display.AmbientDisplayConfiguration;
import android.util.Log;
import android.view.View;
import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

import java.util.List;

@SearchIndexable
public class GestureSettings extends DashboardFragment {

    private static final String TAG = "GestureSettings";
    private static final String PREF_KEY_PREVENT_RINGING = "gesture_prevent_ringing_summary";

    private AmbientDisplayConfiguration mAmbientDisplayConfig;

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.SETTINGS_GESTURES;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.gestures;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        use(PickupGesturePreferenceController.class).setConfig(getConfig(context));
        use(DoubleTapScreenPreferenceController.class).setConfig(getConfig(context));
        use(ScreenOffUdfpsPreferenceController.class).setConfig(getConfig(context));
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        applyCardLayout();
    }

    private void applyCardLayout() {
        final PreferenceScreen screen = getPreferenceScreen();
        if (screen == null ) {
            Log.e(TAG, "preferenceScreen is null!");
            return;
        }
        int count = screen.getPreferenceCount();
        for (int i = 0; i < count; i++) {
            final Preference preference = screen.getPreference(i);
            String key = preference.getKey();
            Log.i(TAG, "Setting layout for preference :- " + key);
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

    private AmbientDisplayConfiguration getConfig(Context context) {
        if (mAmbientDisplayConfig == null) {
            mAmbientDisplayConfig = new AmbientDisplayConfiguration(context);
        }
        return mAmbientDisplayConfig;
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.gestures) {
                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    final List<String> keys = super.getNonIndexableKeys(context);
                    // de-duplicated due to another same entry in Sound page
                    keys.add(PREF_KEY_PREVENT_RINGING);
                    return keys;
                }
            };
}
