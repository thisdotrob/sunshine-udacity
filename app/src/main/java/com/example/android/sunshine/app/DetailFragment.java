package com.example.android.sunshine.app;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.sunshine.app.data.WeatherContract.WeatherEntry;

import org.w3c.dom.Text;

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";
    private ShareActionProvider mShareActionProvider;
    private String mForecast;

    private static final int DETAIL_FORECAST_LOADER = 0;

    private static final String[] FORECAST_COLUMNS = {
            WeatherEntry.TABLE_NAME + "." + WeatherEntry._ID,
            WeatherEntry.COLUMN_DATE,
            WeatherEntry.COLUMN_SHORT_DESC,
            WeatherEntry.COLUMN_MAX_TEMP,
            WeatherEntry.COLUMN_MIN_TEMP,
            WeatherEntry.COLUMN_HUMIDITY,
            WeatherEntry.COLUMN_WIND_SPEED,
            WeatherEntry.COLUMN_DEGREES,
            WeatherEntry.COLUMN_PRESSURE
    };

    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_WEATHER_HUMIDITY = 5;
    static final int COL_WEATHER_WIND_SPEED = 6;
    static final int COL_WEATHER_WIND_DIRECTION = 7;
    static final int COL_WEATHER_PRESSURE = 8;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detail, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_FORECAST_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.detail, menu);
        MenuItem shareItem = menu.findItem(R.id.menu_item_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
        if (mForecast != null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mForecast + FORECAST_SHARE_HASHTAG);
        return shareIntent;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Intent intent = getActivity().getIntent();
        if (intent == null) {
            return null;
        }
        return new CursorLoader(
                getActivity(),
                intent.getData(),
                FORECAST_COLUMNS,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        if (!cursor.moveToFirst()) {
            return;
        }

        boolean isMetric = Utility.isMetric(getActivity());

        long dateInMillis = cursor.getLong(COL_WEATHER_DATE);

        TextView detailDayView = (TextView)getView().findViewById(R.id.detail_day);
        String dayString = Utility.getDayName(getActivity(), dateInMillis);
        detailDayView.setText(dayString);

        TextView detailDateView = (TextView)getView().findViewById(R.id.detail_date);
        String dateString = Utility.getFormattedMonthDay(getActivity(), dateInMillis);
        detailDateView.setText(dateString);

        TextView detailHighView = (TextView)getView().findViewById(R.id.detail_high);
        String highString = Utility.formatTemperature(
                getActivity(), cursor.getDouble(COL_WEATHER_MAX_TEMP), isMetric);
        detailHighView.setText(highString);

        TextView detailLowView = (TextView)getView().findViewById(R.id.detail_low);
        String lowString = Utility.formatTemperature(
                getActivity(), cursor.getDouble(COL_WEATHER_MIN_TEMP), isMetric);
        detailLowView.setText(lowString);

        TextView detailHumidityView = (TextView)getView().findViewById(R.id.detail_humidty);
        int humidityInt = cursor.getInt(COL_WEATHER_HUMIDITY);
        String humidityString = "HUMIDITY: " + humidityInt + "%";
        detailHumidityView.setText(humidityString);

        TextView detailWindView = (TextView)getView().findViewById(R.id.detail_wind);
        double windDirection = cursor.getDouble(COL_WEATHER_WIND_DIRECTION);
        double windSpeed = cursor.getDouble(COL_WEATHER_WIND_SPEED);
        String windString = Utility.getWindString(windSpeed, windDirection);
        detailWindView.setText(windString);

        TextView detailPressureView = (TextView)getView().findViewById(R.id.detail_pressure);
        int pressureDouble = (int) Math.round(cursor.getDouble(COL_WEATHER_PRESSURE));
        String pressureString = "PRESSURE: " + pressureDouble + " hpa";
        detailPressureView.setText(pressureString);

        TextView detailDescriptionView = (TextView)getView().findViewById(R.id.detail_description);
        String descriptionString = cursor.getString(COL_WEATHER_DESC);
        detailDescriptionView.setText(descriptionString);

        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) { }
}
