package com.example.android.sunshine.app;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ForecastAdapter extends CursorAdapter {

    public ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }


    private String formatHighLows(double high, double low) {
        boolean isMetric = Utility.isMetric(mContext);
        String formattedHigh = Utility.formatTemperature(high, isMetric);
        String formattedLow = Utility.formatTemperature(low, isMetric);
        return formattedHigh + "/" + formattedLow;
    }

    private String convertCursorRowToUXFormat(Cursor cursor) {
        String highAndLow = formatHighLows(
                cursor.getDouble(MainFragment.COL_WEATHER_MAX_TEMP),
                cursor.getDouble(MainFragment.COL_WEATHER_MIN_TEMP));

        return Utility.formatDate(cursor.getLong(MainFragment.COL_WEATHER_DATE)) +
                " - " + cursor.getString(MainFragment.COL_WEATHER_DESC) +
                " - " + highAndLow;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_forecast, parent, false);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        //TextView tv = (TextView)view;
        //tv.setText(convertCursorRowToUXFormat(cursor));
    }
}