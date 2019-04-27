package com.example.jin.mobileca1_v2;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Date;

public class LocalBoundService extends Service {
    private final IBinder binder = new localBinder();

    public LocalBoundService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
        //throw new UnsupportedOperationException("Not yet implemented");
    }

    public String getTime(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss MM/dd/yyyy", Locale.UK);
        return (dateFormat.format(new Date()));
    }

    public class localBinder extends Binder{
        LocalBoundService getService(){
            return LocalBoundService.this ;
        }
    }
}
