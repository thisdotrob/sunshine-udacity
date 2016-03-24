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
        new FetchWeatherTask().execute(location);
    }

    private class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

        @Override
        protected String[] doInBackground(String... params) {
            if (params.length == 0) return null;
            int days = 7;
            String postcode = params[0];
            String urlStr = OpenWeatherApiUrlBuilder.build(postcode, days);
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

        private static String build(String postcodeStr, int days) {
            Uri.Builder uriBuilder = new Uri.Builder();
            String units = "metric";
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