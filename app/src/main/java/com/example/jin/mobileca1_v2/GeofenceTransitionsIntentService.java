package com.example.jin.mobileca1_v2;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.HardwarePropertiesManager;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.firebase.firestore.GeoPoint;

import java.util.List;


public class GeofenceTransitionsIntentService extends IntentService {
    private static final String TAG = GeofenceTransitionsIntentService.class.getSimpleName();
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;

    public GeofenceTransitionsIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        int geofenceTransition = geofencingEvent.getGeofenceTransition();
        Log.i("TAGR", "Service: HELLO");

        Bundle bundle = intent.getExtras();
//        if (bundle != null) {
            Messenger messenger = (Messenger) bundle.get("messenger");
            Message msg = Message.obtain();

//        }
        Bundle b = new Bundle();
//        mSharedPreferences = getApplicationContext().getSharedPreferences("geofence", Context.MODE_PRIVATE);
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL || geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();
            Location location = geofencingEvent.getTriggeringLocation();
            GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
            b.putDouble("lat",geoPoint.getLatitude());
            b.putDouble("long",geoPoint.getLongitude());

            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {

                Log.i("exit123", "onHandleIntent: HELLO");
                b.putBoolean("exit",false);

            } else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
                Log.i("exit123", "onHandleIntent: BYE");
                b.putBoolean("exit",true);

            }
            try {
                msg.setData(b);
                messenger.send(msg);
            } catch (RemoteException e) {
                Log.i("error", "error");
            }

        }

    }


}
