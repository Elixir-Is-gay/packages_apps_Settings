/*
 * Copyright (C) 2019 The LineageOS Project
 * Copyright (C) 2019-2021 The Evolution X Project
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

package com.android.settings.deviceinfo.firmwareversion;

import android.content.Context;
import android.os.SystemProperties;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ElixirBuildDatePreferenceController extends BasePreferenceController {

    private static final String TAG = "ElixirBuildDateCtrl";

    private static final String KEY_BUILD_DATE_PROP = "org.elixir.build_date_utc";

    public ElixirBuildDatePreferenceController(Context context, String key) {
        super(context, key);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    private static String sortDate(long timestamp) {
        Date date = new Date(timestamp * 1000);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(date);

    }

    @Override
    public CharSequence getSummary() {
        return sortDate(SystemProperties.getLong(KEY_BUILD_DATE_PROP, 0L));
    }
}
