package com.example.android.sunshine.app;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

public class WeatherDataParser {

    private static final String LOG_TAG = WeatherDataParser.class.getSimpleName();
    private static final String OWM_LIST = "list";

    public static String[] parse(Context context, String forecastJsonStr) {
        try {
            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            if (locationNotFound(forecastJson)) {
                return new String[] { "Location not found" };
            } else {
                JSONArray forecasts = forecastJson.getJSONArray(OWM_LIST);
                int days = forecasts.length();
                String[] parsedWeatherData = new String[days];
                for(int i = 0; i < days; i++) {
                    JSONObject forecast = forecasts.getJSONObject(i);
                    parsedWeatherData[i] = parseSingleDayForecast(context, forecast);
                }
                return parsedWeatherData;
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error ", e);
        }
        return null;
    }

    private static boolean locationNotFound(JSONObject forecastJson) {
        try {
            String cod = (String) forecastJson.get("cod");
            return cod.equals("404");
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error ", e);
            return false;
        }
    }

    private static String parseSingleDayForecast(
            Context context, JSONObject forecast) throws JSONException {
        String weatherDescription = parseWeatherDescription(forecast);
        String highLowTemperature = parseHighLowTemperature(context, forecast);
        String formattedDate = parseFormattedDate(forecast);
        return formattedDate + " - " + weatherDescription + " - " + highLowTemperature;
    }

    private static String parseWeatherDescription(JSONObject forecastJson) throws JSONException {
        return forecastJson.getJSONArray("weather").getJSONObject(0).getString("main");
    }

    private static String parseHighLowTemperature(Context context,
            JSONObject forecastJson) throws JSONException {

        JSONObject tempJson = forecastJson.getJSONObject("temp");

        double tempMin = tempJson.getDouble("min");
        double tempMax = tempJson.getDouble("max");

        String unitsDefault = context.getString(R.string.pref_units_value_metric);
        String unitsKey = context.getString(R.string.pref_units_key);
        String unitsUserSetting = PreferenceManager.
                getDefaultSharedPreferences(context).
                getString(unitsKey, unitsDefault);

        if (!unitsDefault.equals(unitsUserSetting)) {
            tempMin = convertTemp(tempMin, unitsDefault);
            tempMax = convertTemp(tempMax, unitsDefault);
        }

        int roundedMin = (int) Math.round(tempMin);
        int roundedMax = (int) Math.round(tempMax);

        return String.format("%d / %d", roundedMin, roundedMax);
    }

    private static String parseFormattedDate(JSONObject forecastJson) throws JSONException {
        long unixSeconds = forecastJson.getLong("dt");
        Date date = new Date(unixSeconds*1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d");
        return sdf.format(date);
    }

    private static double convertTemp(double temp, String fromUnits) {
        double convertedTemp;
        switch (fromUnits) {
            case "metric":
                convertedTemp = (temp * 1.8) + 32;
                break;
            case "imperial":
                convertedTemp = (temp - 32) / 1.8;
                break;
            default:
                throw new UnsupportedOperationException("Unknown units: " + fromUnits);
        }
        return convertedTemp;
    }
}
