package com.example.android.sunshine.app;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class ForecastFragment extends Fragment {

    private ArrayAdapter<String> mForecastAdapter;

    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            new FetchWeatherTask().execute("94043");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        String[] weatherData = new String[] {
                "Today - Sunny - 88/63",
                "Tomorrow - Foggy - 70/46",
                "Weds - Cloudy - 72/63",
                "Thurs - Rainy - 64/51",
                "Fri - Foggy - 70/46",
                "Sat - Sunny - 76/68",
                "Sun - Asteroids - 99/00"
        };

        ArrayList<String> weekForecast = new ArrayList<>(Arrays.asList(weatherData));

        mForecastAdapter =
                new ArrayAdapter<>(
                        getActivity(),
                        R.layout.list_item_forecast,
                        R.id.list_item_forecast_textView,
                        weekForecast);

        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);

        listView.setAdapter(mForecastAdapter);

        return rootView;
    }

    private class FetchWeatherTask extends AsyncTask<String, Void, String[]> {
        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

        private String[] parseWeatherData(String forecastJsonStr, int days) throws JSONException {
            String[] parsedWeatherData = new String[days];
            JSONArray forecasts = new JSONObject(forecastJsonStr).getJSONArray("list");
            for(int i=0; i<days; i++) {
                JSONObject forecast = forecasts.getJSONObject(i);
                parsedWeatherData[i] = parseForecast(forecast);
            }
            return parsedWeatherData;
        }

        private String parseForecast(JSONObject forecast) throws JSONException {
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
            int tempMin = (int) Math.round(tempJson.getDouble("min"));
            int tempMax = (int) Math.round(tempJson.getDouble("max"));
            return String.format("%d / %d", tempMin, tempMax);
        }

        private String parseFormattedDate(JSONObject forecastJson) throws JSONException {
            long unixSeconds = forecastJson.getLong("dt");
            Date date = new Date(unixSeconds*1000L);
            SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d");
            return sdf.format(date);
        }

        @Override
        protected String[] doInBackground(String... params) {
            if (params.length == 0) return null;

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String forecastJsonStr = null;
            Uri.Builder uriBuilder = new Uri.Builder();

            String postcode = params[0];
            String units = "metric";
            int days = 7;
            final String APP_ID = "cc94715e94287e49ed67f30b455b4761";

            final String QUERY_PARAM = "q";
            final String UNITS_PARAM = "units";
            final String DAYS_PARAM = "cnt";
            final String APPID_PARAM = "APPID";

            uriBuilder.scheme("http")
                    .authority("api.openweathermap.org")
                    .appendPath("data/2.5/forecast/daily")
                    .appendQueryParameter(QUERY_PARAM, postcode)
                    .appendQueryParameter(UNITS_PARAM, units)
                    .appendQueryParameter(DAYS_PARAM, Integer.toString(days))
                    .appendQueryParameter(APPID_PARAM, APP_ID);

            String urlStr = uriBuilder.build().toString();

            try {
                URL url = new URL(urlStr);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder stringBuilder = new StringBuilder();
                if (inputStream == null) return null;
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                if (stringBuilder.length() == 0) return null;
                forecastJsonStr = stringBuilder.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
            try {
                return parseWeatherData(forecastJsonStr, days);
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Error ", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String[] strings) {
            mForecastAdapter.clear();
            for(String s: strings) mForecastAdapter.add(s);
            mForecastAdapter.notifyDataSetChanged();
            super.onPostExecute(strings);
        }
    }
}
