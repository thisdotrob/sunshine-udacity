package com.example.android.sunshine.app;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.android.sunshine.app.data.WeatherContract;
import com.example.android.sunshine.app.data.WeatherProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

public class WeatherDataParser {

    private static final String LOG_TAG = WeatherDataParser.class.getSimpleName();

    private static final String OWM_LIST = "list";
    final String OWM_CITY = "city";
    final String OWM_CITY_NAME = "name";
    final String OWM_COORD = "coord";
    final String OWM_LATITUDE = "lat";
    final String OWM_LONGITUDE = "lon";


    private static Context mContext;

    public WeatherDataParser(Context context) {
        mContext = context;
    }

    public String[] parse(String forecastJsonStr, String locationSetting) {

        try {

            JSONObject forecastJson = new JSONObject(forecastJsonStr);

            if (locationNotFound(forecastJson)) {

                return new String[] { "Location not found" };

            } else {

                JSONObject cityJson = forecastJson.getJSONObject(OWM_CITY);

                String cityName = cityJson.getString(OWM_CITY_NAME);

                JSONObject cityCoord = cityJson.getJSONObject(OWM_COORD);

                double cityLatitude = cityCoord.getDouble(OWM_LATITUDE);
                double cityLongitude = cityCoord.getDouble(OWM_LONGITUDE);

                getOrAddLocation(locationSetting, cityName, cityLatitude, cityLongitude);

                JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

                int days = weatherArray.length();

                String[] parsedWeatherData = new String[days];
                for(int i = 0; i < days; i++) {
                    JSONObject forecast = weatherArray.getJSONObject(i);
                    parsedWeatherData[i] = parseSingleDayForecast(forecast);
                }

                return parsedWeatherData;

            }
        } catch (JSONException e) {

            Log.e(LOG_TAG, "Error ", e);

        }
        return null;
    }

    private boolean locationNotFound(JSONObject forecastJson) {
        try {
            String cod = (String) forecastJson.get("cod");
            return cod.equals("404");
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error ", e);
            return false;
        }
    }

    private String parseSingleDayForecast(JSONObject forecast) throws JSONException {
        String weatherDescription = parseWeatherDescription(forecast);
        String highLowTemperature = parseHighLowTemperature(forecast);
        String formattedDate = parseFormattedDate(forecast);
        return formattedDate + " - " + weatherDescription + " - " + highLowTemperature;
    }

    private String parseWeatherDescription(JSONObject forecastJson) throws JSONException {
        return forecastJson.getJSONArray("weather").getJSONObject(0).getString("main");
    }

    private String parseHighLowTemperature(JSONObject forecastJson) throws JSONException {

        JSONObject tempJson = forecastJson.getJSONObject("temp");

        double tempMin = tempJson.getDouble("min");
        double tempMax = tempJson.getDouble("max");

        String unitsDefault = mContext.getString(R.string.pref_units_value_metric);
        String unitsKey = mContext.getString(R.string.pref_units_key);
        String unitsUserSetting = PreferenceManager.
                getDefaultSharedPreferences(mContext).
                getString(unitsKey, unitsDefault);

        if (!unitsDefault.equals(unitsUserSetting)) {
            tempMin = convertTemp(tempMin, unitsDefault);
            tempMax = convertTemp(tempMax, unitsDefault);
        }

        int roundedMin = (int) Math.round(tempMin);
        int roundedMax = (int) Math.round(tempMax);

        return String.format("%d / %d", roundedMin, roundedMax);
    }

    private String parseFormattedDate(JSONObject forecastJson) throws JSONException {
        long unixSeconds = forecastJson.getLong("dt");
        Date date = new Date(unixSeconds*1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d");
        return sdf.format(date);
    }

    private double convertTemp(double temp, String fromUnits) {
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

    public long getOrAddLocation(String locationSetting, String cityName, double lat, double lon) {

        long locationId;

        Cursor locationCursor = mContext.getContentResolver().query(
                WeatherContract.LocationEntry.CONTENT_URI,
                new String[]{WeatherContract.LocationEntry._ID},
                WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ?",
                new String[]{locationSetting},
                null);

        if (locationCursor.moveToFirst()) {
            int locationIdIndex = locationCursor.getColumnIndex(WeatherContract.LocationEntry._ID);
            locationId = locationCursor.getLong(locationIdIndex);
        } else {
            ContentValues locationValues = new ContentValues();

            locationValues.put(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING, locationSetting);
            locationValues.put(WeatherContract.LocationEntry.COLUMN_CITY_NAME, cityName);
            locationValues.put(WeatherContract.LocationEntry.COLUMN_COORD_LAT, lat);
            locationValues.put(WeatherContract.LocationEntry.COLUMN_COORD_LONG, lon);

            Uri insertedUri = mContext.getContentResolver().insert(
                    WeatherContract.LocationEntry.CONTENT_URI,
                    locationValues
            );

            locationId = ContentUris.parseId(insertedUri);
        }

        locationCursor.close();

        return locationId;
    }
}
