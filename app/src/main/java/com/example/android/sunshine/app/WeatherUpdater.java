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
        String location = prefs.getString(locationKey, locationDefault);
        String unitsDefault = context.getString(R.string.pref_units_value_metric);
        String unitsKey = context.getString(R.string.pref_units_key);
        String units = prefs.getString(unitsKey, unitsDefault);
        new FetchWeatherTask().execute(location, units);
    }

    private class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

        @Override
        protected String[] doInBackground(String... params) {
            if (params.length < 2) return null;
            int days = 7;
            String location = params[0];
            String units = params[1];
            String urlStr = OpenWeatherApiUrlBuilder.build(location, units, days);
            String forecastJsonStr = WeatherDataRetriever.retrieveJsonStr(urlStr);
            return WeatherDataParser.parse(forecastJsonStr, days);
        }

        @Override
        protected void onPostExecute(String[] strings) {
            mForecastAdapter.clear();
            for(String s: strings) mForecastAdapter.add(s);
            super.onPostExecute(strings);
        }
    }

    private static class OpenWeatherApiUrlBuilder {
        static final String APP_ID = "cc94715e94287e49ed67f30b455b4761";
        static final String QUERY_PARAM = "q";
        static final String UNITS_PARAM = "units";
        static final String DAYS_PARAM = "cnt";
        static final String APPID_PARAM = "APPID";

        private static String build(String postcodeStr, String units, int days) {
            Uri.Builder uriBuilder = new Uri.Builder();
            uriBuilder.scheme("http")
                    .authority("api.openweathermap.org")
                    .appendPath("data/2.5/forecast/daily")
                    .appendQueryParameter(QUERY_PARAM, postcodeStr)
                    .appendQueryParameter(UNITS_PARAM, units)
                    .appendQueryParameter(DAYS_PARAM, Integer.toString(days))
                    .appendQueryParameter(APPID_PARAM, APP_ID);
            return uriBuilder.build().toString();
        }
    }
}