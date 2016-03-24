package com.example.android.sunshine.app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

public class SettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        bindPreferencesToSummaries();
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen()
                .getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen()
                .getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        bindPreferencesToSummaries();
    }

    private void bindPreferencesToSummaries() {
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        int preferenceCount = preferenceScreen.getPreferenceCount();
        for (int i = 0; i < preferenceCount; i++) {
            Preference preference = preferenceScreen.getPreference(i);
            bindPreferenceToSummary(preference);
        }
    }

    private void bindPreferenceToSummary(Preference preference) {
        if (preference instanceof PreferenceCategory) {
            PreferenceCategory category = (PreferenceCategory) preference;
            for (int i = 0; i < category.getPreferenceCount(); i++) {
                bindPreferenceToSummary(category.getPreference(i));
            }
        } else {
            bindSummary(preference);
        }
    }

    private void bindSummary(Preference preference) {
        if (preference instanceof EditTextPreference) {
            String preferenceValue = ((EditTextPreference) preference).getText();
            preference.setSummary(preferenceValue);
        }
        if (preference instanceof ListPreference) {
            String preferenceValue = (String) ((ListPreference) preference).getEntry();
            preference.setSummary(preferenceValue);
        }
    }
}
