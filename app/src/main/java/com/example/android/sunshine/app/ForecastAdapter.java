package com.example.android.sunshine.app;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class ForecastAdapter extends CursorAdapter {

    private final int VIEW_TYPE_TODAY = 0;
    private final int VIEW_TYPE_FUTURE_DAY = 1;

    public ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        int viewType = getItemViewType(cursor.getPosition());

        int layoutId = -1;

        if (viewType == VIEW_TYPE_TODAY) {
            layoutId = R.layout.list_item_forecast_today;
        } else if (viewType == VIEW_TYPE_FUTURE_DAY) {
            layoutId = R.layout.list_item_forecast;
        }

        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ImageView iconView = (ImageView) view.findViewById(R.id.list_item_icon);
        TextView dateView = (TextView) view.findViewById(R.id.list_item_date_textview);
        TextView forecastView = (TextView) view.findViewById(R.id.list_item_forecast_textview);
        TextView highView = (TextView) view.findViewById(R.id.list_item_high_textview);
        TextView lowView = (TextView) view.findViewById(R.id.list_item_low_textview);

        int iconId = cursor.getInt(MainFragment.COL_WEATHER_ID);
        double high = cursor.getDouble(MainFragment.COL_WEATHER_MAX_TEMP);
        double low = cursor.getDouble(MainFragment.COL_WEATHER_MIN_TEMP);
        String forecast = cursor.getString(MainFragment.COL_WEATHER_DESC);
        long dateInMillis = cursor.getLong(MainFragment.COL_WEATHER_DATE);

        boolean isMetric = Utility.isMetric(context);

        iconView.setImageResource(R.drawable.ic_launcher);
        dateView.setText(Utility.formatDate(dateInMillis));
        forecastView.setText(forecast);
        highView.setText(Utility.formatTemperature(high, isMetric));
        lowView.setText(Utility.formatTemperature(low, isMetric));
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0) ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
    }
}