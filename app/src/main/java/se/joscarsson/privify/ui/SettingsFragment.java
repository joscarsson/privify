package se.joscarsson.privify.ui;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

import se.joscarsson.privify.R;

import static se.joscarsson.privify.Settings.PREFERENCES_NAME;

public class SettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        getPreferenceManager().setSharedPreferencesName(PREFERENCES_NAME);
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }
}
