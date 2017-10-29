package se.joscarsson.privify;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

import static se.joscarsson.privify.Settings.PREFERENCES_NAME;

public class SettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        this.getPreferenceManager().setSharedPreferencesName(PREFERENCES_NAME);
        this.setPreferencesFromResource(R.xml.preferences, rootKey);
    }
}
