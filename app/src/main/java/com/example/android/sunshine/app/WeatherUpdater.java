package com.example.android.sunshine.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.ArrayAdapter;

public class WeatherUpdater {

    private ArrayAdapter<String> mForecastAdapter;

    public WeatherUpdater(ArrayAdapter<String> arrayAdapter) {
        mForecastAdapter = arrayAdapter;
    }

    public void updateWeather(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String locationDefault = context.getString(R.string.pref_location_default);
        String locationKey = context.getString(R.string.pref_location_key);
        String locationPref = prefs.getString(locationKey, locationDefault);
        String unitsBase = context.getString(R.string.pref_units_value_metric);
        String unitsKey = context.getString(R.string.pref_units_key);
        String unitsPref = prefs.getString(unitsKey, unitsBase);
        new FetchWeatherTask(mForecastAdapter).execute(locationPref, unitsBase, unitsPref);
    }
}