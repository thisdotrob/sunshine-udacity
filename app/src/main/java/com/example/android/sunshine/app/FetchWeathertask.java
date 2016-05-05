package com.example.android.sunshine.app;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

public class FetchWeatherTask extends AsyncTask<String, Void, Void> {

    private final Context mContext;
    private final static int NUM_DAYS = 14;

    public FetchWeatherTask(Context context) {
        mContext = context;
    }

    @Override
    protected Void doInBackground(String... params) {
        if (params.length < 1) return null;
        String locationSetting = params[0];
        String urlStr = buildOpenWeatherApiUrl(locationSetting, NUM_DAYS);
        String forecastJsonStr = WeatherDataRetriever.retrieveJsonStr(urlStr);
        if (null != forecastJsonStr) {
            new WeatherDataParser(mContext).parse(forecastJsonStr, locationSetting);
        }
        return null;
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