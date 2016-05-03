package com.example.android.sunshine.app;

import android.net.Uri;
import android.os.AsyncTask;
import android.widget.ArrayAdapter;

public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

    private ArrayAdapter<String> mForecastAdapter;

    public FetchWeatherTask(ArrayAdapter<String> forecastAdapter) {
        mForecastAdapter = forecastAdapter;
    }

    @Override
    protected String[] doInBackground(String... params) {
        if (params.length < 3) return null;
        int days = 7;
        String location = params[0];
        String baseUnits = params[1];
        String userUnits = params[2];
        String urlStr = buildOpenWeatherApiUrl(location, baseUnits, days);
        String forecastJsonStr = WeatherDataRetriever.retrieveJsonStr(urlStr);
        return WeatherDataParser.parse(forecastJsonStr, baseUnits, userUnits);
    }

    @Override
    protected void onPostExecute(String[] strings) {
        mForecastAdapter.clear();
        for(String s: strings) mForecastAdapter.add(s);
        super.onPostExecute(strings);
    }

    private String buildOpenWeatherApiUrl(String postcodeStr, String units, int days) {
        final String APP_ID = "cc94715e94287e49ed67f30b455b4761";
        final String QUERY_PARAM = "q";
        final String UNITS_PARAM = "units";
        final String DAYS_PARAM = "cnt";
        final String APPID_PARAM = "APPID";

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