package com.example.android.sunshine.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import java.util.ArrayList;

public class MainFragment extends Fragment {

    public final static String EXTRA_MESSAGE = "com.example.android.sunshine.app.MESSAGE";

    private ArrayAdapter<String> mForecastAdapter;
    private WeatherUpdater weatherUpdater;

    public MainFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        weatherUpdater.updateWeather(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        initializeForecastAdapter(listView);
        weatherUpdater = new WeatherUpdater(mForecastAdapter);
        AdapterView.OnItemClickListener clickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView textView = (TextView) view;
                CharSequence text = textView.getText();
                Intent detailIntent = new Intent(getActivity(), DetailActivity.class);
                detailIntent.putExtra(EXTRA_MESSAGE, text);
                startActivity(detailIntent);
            }
        };
        listView.setOnItemClickListener(clickListener);
        return rootView;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_refresh) {
            weatherUpdater.updateWeather(getContext());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initializeForecastAdapter(ListView listView) {
        mForecastAdapter =
                new ArrayAdapter<>(
                        getActivity(),
                        R.layout.list_item_forecast,
                        R.id.list_item_forecast_textView,
                        new ArrayList<String>());
        listView.setAdapter(mForecastAdapter);
    }

}
