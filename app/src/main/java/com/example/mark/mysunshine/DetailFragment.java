package com.example.mark.mysunshine;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.support.v4.content.CursorLoader;

import com.example.mark.mysunshine.data.WeatherContract;

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
    //share option not working

    private String forecastStr;
    static final String DETAIL_URI = "URI";
    private static final int DETAIL_LOADER=0;
    private static final String[] DETAIL_COLUMNS={
            WeatherContract.WeatherEntry.TABLE_NAME+"."+ WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_DEGREES,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING
    };
    private static final int COL_WEATHER_ID=0;
    private static final int COL_WEATHER_DATE=1;
    private static final int COL_SHORT_DESC=2;
    private static final int COL_MAX_TEMP=3;
    private static final int COL_MIN_TEMP=4;
    private static final int COL_HUMIDITY=5;
    private static final int COL_PRESSURE=6;
    private static final int COL_WIND_SPEED=7;
    private static final int COL_DEGREES=8;
    private static final int COL_WEATHER_CONDITION_ID=9;


    private ShareActionProvider mShareActionProvider;

    private ImageView iconView;
    private TextView dateView,friendlyDateView,descriptionView,highTempView,lowTempView,humidityView,windView,pressureView;
    private Uri mUri;

    public DetailFragment() {
        setHasOptionsMenu(true);
        // Required empty public constructor
    }

    void onLocationChanged( String newLocation ) {
        // replace the uri, since the location has changed
        Uri uri = mUri;
        if (null != uri) {
            long date = WeatherContract.WeatherEntry.getDateFromUri(uri);
            Uri updatedUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(newLocation, date);
            mUri = updatedUri;
            getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
        }
    }

    private Intent createShareForecastIntent(){
        Intent shareIntent=new Intent(Intent.ACTION_SEND);
        //shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,forecastStr);
        return shareIntent;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER,null,this);
        super.onActivityCreated(savedInstanceState);
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_detail_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.action_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
        if (mShareActionProvider != null) {
            if (forecastStr != null) {
                mShareActionProvider.setShareIntent(createShareForecastIntent());
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        Bundle arguments=getArguments();
        if(arguments!=null){
            mUri=arguments.getParcelable(DetailFragment.DETAIL_URI);
        }
        if(getActivity().getIntent()!=null&&getActivity().getIntent().getData()!=null){
            mUri=getActivity().getIntent().getData();
        }
        // Inflate the layout for this fragment
        View rootView= inflater.inflate(R.layout.fragment_detail, container, false);
        iconView= (ImageView) rootView.findViewById(R.id.detail_icon);
        dateView= (TextView) rootView.findViewById(R.id.detail_date_textview);
        friendlyDateView= (TextView) rootView.findViewById(R.id.detail_day_textview);
        descriptionView= (TextView) rootView.findViewById(R.id.detail_forecast_textview);
        highTempView= (TextView) rootView.findViewById(R.id.detail_high_textview);
        lowTempView= (TextView) rootView.findViewById(R.id.detail_low_textview);
        humidityView= (TextView) rootView.findViewById(R.id.detail_humidity_textview);
        windView= (TextView) rootView.findViewById(R.id.detail_wind_textview);
        pressureView= (TextView) rootView.findViewById(R.id.detail_pressure_textview);
        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if(null!=mUri) {
            return new CursorLoader(getActivity(), mUri, DETAIL_COLUMNS, null, null, null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(data!=null&&data.moveToFirst()) {
            int weatherId = data.getInt(COL_WEATHER_CONDITION_ID);
            iconView.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));

            long date = data.getLong(COL_WEATHER_DATE);
            String friendlyDateText = Utility.getDayName(getActivity(), date);
            String dateText = Utility.getFormattedMonthDay(getActivity(), date);
            friendlyDateView.setText(friendlyDateText);
            dateView.setText(dateText);

            String weatherDescription = data.getString(COL_SHORT_DESC);
            descriptionView.setText(weatherDescription);

            boolean isMetric = Utility.isMetric(getActivity());
            String high = Utility.formatTemperature(getActivity(), data.getDouble(COL_MAX_TEMP), isMetric);
            String low = Utility.formatTemperature(getActivity(), data.getDouble(COL_MIN_TEMP), isMetric);
            highTempView.setText(high);
            lowTempView.setText(low);

            String humidity = Utility.formatHumidity(getActivity(), data.getDouble(COL_HUMIDITY));
            humidityView.setText(humidity);

            String pressure = Utility.formatPressure(getActivity(), data.getDouble(COL_PRESSURE));
            pressureView.setText(pressure);

            String formatWindSpeed = Utility.formatWindSpeed(getActivity(), data.getDouble(COL_WIND_SPEED), data.getString(COL_DEGREES));
            windView.setText(formatWindSpeed);

            forecastStr = String.format("%s - %s - %s/%s", dateText, weatherDescription, high, low);

            //for accessibility,blind users
            iconView.setContentDescription("Weather is "+weatherDescription);
            highTempView.setContentDescription("Maximum temperature "+high);
            lowTempView.setContentDescription("Minimum temperature "+low);
            humidityView.setContentDescription("Humdity "+data.getDouble(COL_HUMIDITY)+" percent");
            pressureView.setContentDescription("Pressure "+pressure);
            windView.setContentDescription("wind speed "+ formatWindSpeed);

            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createShareForecastIntent());
            }
        }
    }
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
