package com.example.mark.mysunshine;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mark.mysunshine.data.WeatherContract;

/**
 * Created by Sril Kunal on 28-03-2017.
 */

public class ForecastAdapter extends CursorAdapter{

    private final int VIEW_TYPE_TODAY=0;
    private final int VIEW_TYPE_FUTURE_DAY=1;

    private boolean isSinglePaneLayout;
    public void isTwoPane(boolean twoPane){
        this.isSinglePaneLayout=!twoPane;
    }

    public ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public int getItemViewType(int position) {
        return (position==0&&isSinglePaneLayout)?VIEW_TYPE_TODAY:VIEW_TYPE_FUTURE_DAY;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        int viewType=getItemViewType(cursor.getPosition());
        int layoutId=-1;
        if(viewType==VIEW_TYPE_TODAY){
            layoutId=R.layout.list_item_forecast_today;
        }else if(viewType==VIEW_TYPE_FUTURE_DAY){
            layoutId=R.layout.list_item_forecast;
        }
        View view= LayoutInflater.from(context).inflate(layoutId, parent, false);
        ViewHolder viewHolder=new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    /*
        This is where we fill-in the views with the contents of the cursor.
     */
    @Override
    public void bindView(View rootView, Context context, Cursor cursor) {
        // our view is pretty simple here --- just a text view
        // we'll keep the UI functional with a simple (and slow!) binding.

        ViewHolder viewHolder= (ViewHolder) rootView.getTag();
        Log.d("Cursor",""+cursor.getPosition());
        int weatherId=cursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID);
        int viewType=getItemViewType(cursor.getPosition());
        switch (viewType)
        {
            case VIEW_TYPE_TODAY:
                viewHolder.iconView.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));
                break;
            case VIEW_TYPE_FUTURE_DAY:
                viewHolder.iconView.setImageResource(Utility.getIconResourceForWeatherCondition(weatherId));
                break;
        }

        boolean isMetric=Utility.isMetric(context);
        viewHolder.maxTempTextView.setText(Utility.formatTemperature(context,cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP),isMetric));
        viewHolder.minTempTextView.setText(Utility.formatTemperature(context,cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP),isMetric));

        viewHolder.detailTextView.setText(cursor.getString(ForecastFragment.COL_WEATHER_DESC));
        viewHolder.dateTextView.setText(Utility.getFriendlyDayString(context,cursor.getLong(ForecastFragment.COL_WEATHER_DATE)));
    }


    public static class ViewHolder{
        public final ImageView iconView;
        public final TextView detailTextView;
        public final TextView dateTextView;
        public final TextView maxTempTextView;
        public final TextView minTempTextView;
        public ViewHolder(View rootView){
            iconView= (ImageView) rootView.findViewById(R.id.list_item_icon);
            dateTextView= (TextView) rootView.findViewById(R.id.list_item_date_textview);
            detailTextView = (TextView) rootView.findViewById(R.id.list_item_forecast_textview);
            maxTempTextView= (TextView) rootView.findViewById(R.id.list_item_high_textview);
            minTempTextView= (TextView) rootView.findViewById(R.id.list_item_low_textview);
        }
    }
}
