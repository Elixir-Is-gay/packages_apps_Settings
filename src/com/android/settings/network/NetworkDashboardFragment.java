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
package com.android.settings.network;

import android.app.settings.SettingsEnums;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.Nullable;

import android.util.Log;

import androidx.preference.Preference;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.SettingsDumpService;
import com.android.settings.core.OnActivityResultListener;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.vpn2.VpnInfoPreference;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.search.SearchIndexable;

import java.util.ArrayList;
import java.util.List;

@SearchIndexable
public class NetworkDashboardFragment extends DashboardFragment implements
        OnActivityResultListener {

    private static final String TAG = "NetworkDashboardFrag";

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.SETTINGS_NETWORK_CATEGORY;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.network_provider_internet;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        use(AirplaneModePreferenceController.class).setFragment(this);
        use(NetworkProviderCallsSmsController.class).init(this);
    }

    @Override
    public int getHelpResource() {
        return R.string.help_url_network_dashboard;
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
            Log.i(TAG, "Checking if Preference is Visible :- " + key);
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
        Log.i(TAG, "All preferences :- " + mPrefGroup.getPreferenceCount() + ", Visible :- " + visibleCount);
        int count = 0;
        for (String currentPref : prefList) {
            try {
                count++;
                final Preference preference = mPrefGroup.findPreference(currentPref);
                Log.i(TAG, "Setting layout for preference :- " + currentPref);
                if (preference instanceof VpnInfoPreference) {
                    preference.setLayoutResource(org.elixir.resources.cardlayout.R.layout.preference_two_target_mid);
                } else {
                    preference.setLayoutResource(org.elixir.resources.cardlayout.R.layout.xd_pref_card_mid);
                }

                if (count == 4) {
                    if (preference instanceof VpnInfoPreference) {
                        preference.setLayoutResource(org.elixir.resources.cardlayout.R.layout.preference_two_target_bot);
                    } else {
                        preference.setLayoutResource(org.elixir.resources.cardlayout.R.layout.xd_pref_card_bot);
                    }
                }
                if (count == 5) {
                    if (preference instanceof VpnInfoPreference) {
                        preference.setLayoutResource(org.elixir.resources.cardlayout.R.layout.preference_two_target_top);
                    } else {
                        preference.setLayoutResource(org.elixir.resources.cardlayout.R.layout.xd_pref_card_top);
                    }
                    count = 0;
                }
            } catch (Exception ex) {
                Log.e(TAG, "Error applying layout :- " + ex.toString());
            }
        }
        try {
            String firstPref = prefList.get(0);
            String lastPref = prefList.get(prefList.size() - 1);
            final Preference firstPreference = mPrefGroup.findPreference(firstPref);
            final Preference lastPreference = mPrefGroup.findPreference(lastPref);
            if (firstPreference instanceof VpnInfoPreference) {
                firstPreference.setLayoutResource(org.elixir.resources.cardlayout.R.layout.preference_two_target_top);
            } else {
                firstPreference.setLayoutResource(org.elixir.resources.cardlayout.R.layout.xd_pref_card_top);
            }

            if (lastPreference instanceof VpnInfoPreference) {
                lastPreference.setLayoutResource(org.elixir.resources.cardlayout.R.layout.preference_two_target_bot);
            } else {
                lastPreference.setLayoutResource(org.elixir.resources.cardlayout.R.layout.xd_pref_card_bot);
            }
        } catch (Exception exe) {
            Log.e(TAG, "Error applying layout :- " + exe.toString());
        }
        prefList.clear();
    }

    @Override
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildPreferenceControllers(context, getSettingsLifecycle());
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(Context context,
            @Nullable Lifecycle lifecycle) {
        final VpnPreferenceController vpnPreferenceController =
                new VpnPreferenceController(context);
        final PrivateDnsPreferenceController privateDnsPreferenceController =
                new PrivateDnsPreferenceController(context);

        if (lifecycle != null) {
            lifecycle.addObserver(vpnPreferenceController);
            lifecycle.addObserver(privateDnsPreferenceController);
        }

        final List<AbstractPreferenceController> controllers = new ArrayList<>();

        controllers.add(vpnPreferenceController);
        controllers.add(privateDnsPreferenceController);

        // Start SettingsDumpService after the MobileNetworkRepository is created.
        Intent intent = new Intent(context, SettingsDumpService.class);
        intent.putExtra(SettingsDumpService.EXTRA_KEY_SHOW_NETWORK_DUMP, true);
        context.startService(intent);
        return controllers;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case AirplaneModePreferenceController.REQUEST_CODE_EXIT_ECM:
                use(AirplaneModePreferenceController.class)
                        .onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.network_provider_internet) {
                @Override
                public List<AbstractPreferenceController> createPreferenceControllers(Context
                        context) {
                    return buildPreferenceControllers(context, null /* lifecycle */);
                }
            };
}
