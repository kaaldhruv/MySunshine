package com.example.mark.mysunshine;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.mark.mysunshine.data.WeatherContract;

/**
 * Created by Sril Kunal on 28-03-2017.
 */

public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ViewHolder>{

    private Context context;
    private Cursor cursor;

    private ForecastAdapter.OnClickHandler onClickHandler;

    private final int VIEW_TYPE_TODAY=0;
    private final int VIEW_TYPE_FUTURE_DAY=1;

    private boolean isSinglePaneLayout;
    public void isTwoPane(boolean twoPane){
        this.isSinglePaneLayout=!twoPane;
    }

    public ForecastAdapter(Context context,ForecastAdapter.OnClickHandler onClickHandler) {
        this.context=context;
        this.onClickHandler=onClickHandler;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(parent instanceof RecyclerView){
            int layoutId=-1;
            if(viewType==VIEW_TYPE_TODAY){
                layoutId=R.layout.list_item_forecast_today;
            }else if(viewType==VIEW_TYPE_FUTURE_DAY){
                layoutId=R.layout.list_item_forecast;
            }
            View view= LayoutInflater.from(context).inflate(layoutId, parent, false);
            view.setFocusable(true);
            ViewHolder viewHolder=new ViewHolder(view);
            return viewHolder;
        }
        throw new RuntimeException("Not bound to Recycler View Selection");
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        cursor.moveToPosition(position);
        Log.d("Cursor",""+cursor.getPosition());
        int weatherId=cursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID);
        int viewType=getItemViewType(position);
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

    @Override
    public int getItemViewType(int position) {
        return (position==0&&isSinglePaneLayout)?VIEW_TYPE_TODAY:VIEW_TYPE_FUTURE_DAY;
    }

    @Override
    public int getItemCount() {
        if(cursor==null)return 0;
        return cursor.getCount();
    }

    public void swapCursor(Cursor newCursor){
        cursor=newCursor;
        notifyDataSetChanged();
    }
    public Cursor getCursor(){
        return this.cursor;
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public final ImageView iconView;
        public final TextView detailTextView;
        public final TextView dateTextView;
        public final TextView maxTempTextView;
        public final TextView minTempTextView;
        public ViewHolder(View rootView){
            super(rootView);
            iconView= (ImageView) rootView.findViewById(R.id.list_item_icon);
            dateTextView= (TextView) rootView.findViewById(R.id.list_item_date_textview);
            detailTextView = (TextView) rootView.findViewById(R.id.list_item_forecast_textview);
            maxTempTextView= (TextView) rootView.findViewById(R.id.list_item_high_textview);
            minTempTextView= (TextView) rootView.findViewById(R.id.list_item_low_textview);
            rootView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int adapterPosition=getAdapterPosition();
            cursor.moveToPosition(adapterPosition);
            onClickHandler.onClick(cursor.getLong(cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DATE)),this);
        }
    }

    public interface OnClickHandler {
        public void onClick(Long date,ForecastAdapter.ViewHolder viewHolder);
    }
}
