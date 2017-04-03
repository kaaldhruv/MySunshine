package com.example.mark.mysunshine.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.example.mark.mysunshine.sync.SyncAdapter;

/**
 * Created by Sril Kunal on 31-03-2017.
 */

public class SyncService extends Service {

    private SyncAdapter mSyncAdapter=null;
    private static final Object syncAdapterLock = new Object();

    @Override
    public void onCreate() {
        Log.d("SyncService","SyncService on create");
        synchronized (syncAdapterLock){
            if(mSyncAdapter==null){
                mSyncAdapter=new SyncAdapter(this,true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mSyncAdapter.getSyncAdapterBinder();
    }
}
