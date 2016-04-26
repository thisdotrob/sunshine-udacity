package com.example.android.sunshine.app;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

public class WeatherDataParser {

    private static final String LOG_TAG = WeatherDataParser.class.getSimpleName();
    private static final String OWM_LIST = "list";

    public static String[] parse(String forecastJsonStr) {
        try {
            JSONObject forecastJson = new JSONObject((forecastJsonStr));
            if (locationNotFound(forecastJson)) {
                return new String[] { "Location not found" };
            } else {
                JSONArray forecasts = forecastJson.getJSONArray(OWM_LIST);
                int days = forecasts.length();
                String[] parsedWeatherData = new String[days];
                for(int i = 0; i < days; i++) {
                    JSONObject forecast = forecasts.getJSONObject(i);
                    parsedWeatherData[i] = parseSingleDayForecast(forecast);
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

    private static String parseSingleDayForecast(JSONObject forecast) throws JSONException {
        String weatherDescription = parseWeatherDescription(forecast);
        String highLowTemperature = parseHighLowTemperature(forecast);
        String formattedDate = parseFormattedDate(forecast);
        return formattedDate + " - " + weatherDescription + " - " + highLowTemperature;
    }

    private static String parseWeatherDescription(JSONObject forecastJson) throws JSONException {
        return forecastJson.getJSONArray("weather").getJSONObject(0).getString("main");
    }

    private static String parseHighLowTemperature(JSONObject forecastJson) throws JSONException {
        JSONObject tempJson = forecastJson.getJSONObject("temp");
        int tempMin = (int) Math.round(tempJson.getDouble("min"));
        int tempMax = (int) Math.round(tempJson.getDouble("max"));
        return String.format("%d / %d", tempMin, tempMax);
    }

    private static String parseFormattedDate(JSONObject forecastJson) throws JSONException {
        long unixSeconds = forecastJson.getLong("dt");
        Date date = new Date(unixSeconds*1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d");
        return sdf.format(date);
    }
}
