package com.example.mark.mysunshine;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.CursorLoader;
import android.widget.TextView;

import com.example.mark.mysunshine.data.WeatherContract;
import com.example.mark.mysunshine.sync.SyncAdapter;

public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int FORECAST_LOADER=0;
    private static final String SELECETED_KEY = "currPosition";

    private ForecastAdapter mForecastAdapter;
    private int mPosition=RecyclerView.NO_POSITION;
    private RecyclerView recyclerView;
    private TextView tv;

    private static final String[] FORECAST_COLUMNS = {
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG
    };

    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_LOCATION_SETTING = 5;
    static final int COL_WEATHER_CONDITION_ID = 6;
    static final int COL_COORD_LAT = 7;
    static final int COL_COORD_LONG = 8;
    private boolean mTwoPane;

    public ForecastFragment() {
    }

    void onLocationChanged( ) {
        updateWeather();
        getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(FORECAST_LOADER,null,this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            updateWeather();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        mForecastAdapter = new ForecastAdapter(getActivity(), new ForecastAdapter.OnClickHandler() {
            @Override
            public void onClick(Long date,ForecastAdapter.ViewHolder holder) {
                String locationSetting=Utility.getPreferredLocation(getActivity());
                ((Callback)getActivity()).onItemSelected(WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                        locationSetting,date));
                mPosition=holder.getAdapterPosition();
            }
        });
        mForecastAdapter.isTwoPane(mTwoPane);
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview_forecast);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(mForecastAdapter);
        tv= (TextView) rootView.findViewById(R.id.listview_forecast_empty);
        if(savedInstanceState!=null&&savedInstanceState.containsKey(SELECETED_KEY)){
            mPosition=savedInstanceState.getInt(SELECETED_KEY);
        }
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if(mPosition!=ListView.INVALID_POSITION){
            outState.putInt(SELECETED_KEY,mPosition);
        }
        super.onSaveInstanceState(outState);
    }

    private void updateWeather() {
        SyncAdapter.syncImmediately(getActivity());
    }

    @Override
    public void onStart() {
        super.onStart();
        updateWeather();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String locationSetting=Utility.getPreferredLocation(getActivity());
        String sortOrder=WeatherContract.WeatherEntry.COLUMN_DATE+" ASC";
        Uri weatherForLocationUri= WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(locationSetting,System.currentTimeMillis());
        return new CursorLoader(getActivity(),weatherForLocationUri,FORECAST_COLUMNS,null,null,sortOrder);
    }
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        tv.setVisibility(View.GONE);
        mForecastAdapter.swapCursor(cursor);
        if(mPosition!=RecyclerView.NO_POSITION){
            recyclerView.smoothScrollToPosition(mPosition);
        }else {
            updateEmptyView();
        }
    }
    private void updateEmptyView() {
        if(mForecastAdapter.getItemCount()==0){
            if(tv!=null){
                if(!Utility.isNetworkAvailable(getActivity())){
                    tv.setText(getString(R.string.empty_forecast_list_no_connection));
                }else{
                    tv.setText(getString(R.string.empty_forecast_list));
                }
            }
            tv.setVisibility(View.VISIBLE);
        }
    }
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mForecastAdapter.swapCursor(null);
    }

    public void isTwoPane(boolean twoPane) {
        this.mTwoPane=twoPane;
        if(mForecastAdapter!=null) {
            mForecastAdapter.isTwoPane(mTwoPane);
        }
    }

    public interface Callback{
        public void onItemSelected(Uri dateUri);
    }
}
