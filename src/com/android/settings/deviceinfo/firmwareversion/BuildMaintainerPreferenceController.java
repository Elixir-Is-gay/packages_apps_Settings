package com.android.settings.deviceinfo.firmwareversion;

import android.content.Context;
import android.os.SystemProperties;
import androidx.annotation.VisibleForTesting;

import org.elixir.resources.cardlayout.R;
import com.android.settings.core.BasePreferenceController;

public class BuildMaintainerPreferenceController extends BasePreferenceController {

    @VisibleForTesting

    public BuildMaintainerPreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    // xdOSS Build Status
    /* This code was taken from BootleggersROM
       https://github.com/BootleggersROM/packages_apps_BootlegDumpster
    */
    @Override
    public CharSequence getSummary() {

        String xdMaintainer = mContext.getString(R.string.elixir_maintainer);
        String xdMaintainer2 = SystemProperties.get("ro.elixir.maintainer", "Unmaintained");
        String xdBuildStatusSummary;

        if(xdMaintainer.equalsIgnoreCase("Elixir Maintainer") || xdMaintainer.equalsIgnoreCase(null)){
            if(xdMaintainer2.equalsIgnoreCase("Unmaintained") || xdMaintainer2.equalsIgnoreCase(null)){ 
                xdBuildStatusSummary = mContext.getString(R.string.xd_build_unmaintained_summary);
            } else {
                xdBuildStatusSummary = mContext.getString(R.string.xd_build_maintained_summary, xdMaintainer2);
            }
        } else {
            xdBuildStatusSummary = mContext.getString(R.string.xd_build_maintained_summary, xdMaintainer);
        }
	    return xdBuildStatusSummary;
    }
}
