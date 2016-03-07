package com.example.android.sunshine.app;

import android.content.Intent;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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

    public final static String EXTRA_MESSAGE = "com.mycompany.myfirstapp.MESSAGE";

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
        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        initializeForecastAdapter(listView);
        AdapterView.OnItemClickListener clickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView textView = (TextView) view;
                CharSequence text = textView.getText();
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(getContext(), text, duration);
                toast.show();

                Intent detailIntent = new Intent(getActivity(), DetailActivity.class);
                detailIntent.putExtra(EXTRA_MESSAGE, text);
                startActivity(detailIntent);
            }
        };
        listView.setOnItemClickListener(clickListener);
        return rootView;
    }

    private void initializeForecastAdapter(ListView listView) {
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
        listView.setAdapter(mForecastAdapter);
    }

    private class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

        @Override
        protected String[] doInBackground(String... params) {
            if (params.length == 0) return null;
            int days = 7;
            String postcode = params[0];
            String urlStr = OpenWeatherApiUrlBuilder.build(postcode, days);
            String forecastJsonStr = ForecastJsonRetriever.retrieveJsonStr(urlStr);
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

    private static class ForecastJsonRetriever {
        private static final String LOG_TAG = ForecastJsonRetriever.class.getSimpleName();

        private static String retrieveJsonStr(String urlStr) {
            String forecastJsonStr = null;
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
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
            return forecastJsonStr;
        }
    }

    private static class WeatherDataParser {
        private static final String LOG_TAG = WeatherDataParser.class.getSimpleName();

        private static String[] parse(String forecastJsonStr, int days) {
            try {
                String[] parsedWeatherData = new String[days];
                JSONArray forecasts = new JSONObject(forecastJsonStr).getJSONArray("list");
                for(int i=0; i<days; i++) {
                    JSONObject forecast = forecasts.getJSONObject(i);
                    parsedWeatherData[i] = parseSingleDayForecast(forecast);
                }
                return parsedWeatherData;
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Error ", e);
            }
            return null;
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

}
