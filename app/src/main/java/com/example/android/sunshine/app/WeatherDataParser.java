package com.example.android.sunshine.app;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.util.Log;

import com.example.android.sunshine.app.data.WeatherContract;
import com.example.android.sunshine.app.data.WeatherContract.WeatherEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

public class WeatherDataParser {

    private static final String LOG_TAG = WeatherDataParser.class.getSimpleName();

    private static final String OWM_LIST = "list";

    private static final String OWM_CITY = "city";
    private static final String OWM_CITY_NAME = "name";
    private static final String OWM_COORD = "coord";
    private static final String OWM_LATITUDE = "lat";
    private static final String OWM_LONGITUDE = "lon";
    private static final String OWM_PRESSURE = "pressure";
    private static final String OWM_HUMIDITY = "humidity";
    private static final String OWM_WINDSPEED = "speed";
    private static final String OWM_WIND_DIRECTION = "deg";
    private static final String OWM_TEMPERATURE = "temp";
    private static final String OWM_MAX = "max";
    private static final String OWM_MIN = "min";
    private static final String OWM_WEATHER = "weather";
    private static final String OWM_DESCRIPTION = "main";
    private static final String OWM_WEATHER_ID = "id";


    private static Context mContext;

    public WeatherDataParser(Context context) {
        mContext = context;
    }

    public void parse(String forecastJsonStr, String locationSetting) {
        try {
            JSONObject forecastJson = new JSONObject(forecastJsonStr);

            if (locationFound(forecastJson)) {
                JSONObject cityJson = forecastJson.getJSONObject(OWM_CITY);

                String cityName = cityJson.getString(OWM_CITY_NAME);
                JSONObject cityCoord = cityJson.getJSONObject(OWM_COORD);
                double cityLatitude = cityCoord.getDouble(OWM_LATITUDE);
                double cityLongitude = cityCoord.getDouble(OWM_LONGITUDE);

                long locationId =
                        getOrAddLocation(locationSetting, cityName, cityLatitude, cityLongitude);

                JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);
                int days = weatherArray.length();

                Vector<ContentValues> cVVector = new Vector<>(days);

                int julianStartDay = getJulianStartDay();
                Time dayTime = new Time();
                long dateTime;

                for(int i = 0; i < days; i++) {
                    JSONObject dayForecast = weatherArray.getJSONObject(i);
                    dateTime = dayTime.setJulianDay(julianStartDay+i);
                    ContentValues weatherValues =
                            parseSingleDayForecast(dayForecast, dateTime, locationId);
                    cVVector.add(weatherValues);
                }

                if ( cVVector.size() > 0 ) {
                    ContentValues[] cvArray = new ContentValues[cVVector.size()];
                    cVVector.toArray(cvArray);
                    mContext.getContentResolver().
                            bulkInsert(WeatherContract.WeatherEntry.CONTENT_URI, cvArray);
                }
            }
        } catch (JSONException e) {

            Log.e(LOG_TAG, "Error ", e);

        }
    }

    private boolean locationFound(JSONObject forecastJson) {
        try {
            String cod = (String) forecastJson.get("cod");
            return !cod.equals("404");
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error ", e);
            return true;
        }
    }

    private ContentValues parseSingleDayForecast(
            JSONObject dayForecast, long dateTime, long locationId) throws JSONException {

        JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
        JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);

        ContentValues weatherValues = new ContentValues();

        weatherValues.put(WeatherEntry.COLUMN_LOC_KEY, locationId);
        weatherValues.put(WeatherEntry.COLUMN_DATE, dateTime);
        weatherValues.put(WeatherEntry.COLUMN_HUMIDITY, dayForecast.getInt(OWM_HUMIDITY));
        weatherValues.put(WeatherEntry.COLUMN_PRESSURE, dayForecast.getDouble(OWM_PRESSURE));
        weatherValues.put(WeatherEntry.COLUMN_WIND_SPEED, dayForecast.getDouble(OWM_WINDSPEED));
        weatherValues.put(WeatherEntry.COLUMN_DEGREES, dayForecast.getDouble(OWM_WIND_DIRECTION));
        weatherValues.put(WeatherEntry.COLUMN_MAX_TEMP, temperatureObject.getDouble(OWM_MAX));
        weatherValues.put(WeatherEntry.COLUMN_MIN_TEMP, temperatureObject.getDouble(OWM_MIN));
        weatherValues.put(WeatherEntry.COLUMN_SHORT_DESC, weatherObject.getString(OWM_DESCRIPTION));
        weatherValues.put(WeatherEntry.COLUMN_WEATHER_ID, weatherObject.getInt(OWM_WEATHER_ID));

        return weatherValues;
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

    private int getJulianStartDay() {
        Time dayTime = new Time();
        dayTime.setToNow();
        return Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);
    }
}
