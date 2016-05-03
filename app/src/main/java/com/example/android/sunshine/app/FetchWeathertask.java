package com.example.android.sunshine.app;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.ArrayAdapter;

public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

    private ArrayAdapter<String> mForecastAdapter;
    private final Context mContext;

    public FetchWeatherTask(Context context, ArrayAdapter<String> forecastAdapter) {
        mForecastAdapter = forecastAdapter;
        mContext = context;
    }

    @Override
    protected String[] doInBackground(String... params) {
        if (params.length < 1) return null;
        int days = 7;
        String locationSetting = params[0];
        String urlStr = buildOpenWeatherApiUrl(locationSetting, days);
        String forecastJsonStr = WeatherDataRetriever.retrieveJsonStr(urlStr);
        return new WeatherDataParser(mContext).parse(forecastJsonStr, locationSetting);
    }

    @Override
    protected void onPostExecute(String[] strings) {
        mForecastAdapter.clear();
        for(String s: strings) mForecastAdapter.add(s);
        super.onPostExecute(strings);
    }

    private String buildOpenWeatherApiUrl(String postcodeStr, int days) {
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
                .appendQueryParameter(UNITS_PARAM, "metric")
                .appendQueryParameter(DAYS_PARAM, Integer.toString(days))
                .appendQueryParameter(APPID_PARAM, APP_ID);
        return uriBuilder.build().toString();
    }
}