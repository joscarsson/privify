package se.joscarsson.privify;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

public class SettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        this.setPreferencesFromResource(R.xml.preferences, rootKey);
    }
}
