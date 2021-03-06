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
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
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
            ArrayList triggeringGeofencesIdsList = new ArrayList();
            for (Geofence geofence : triggeringGeofences) {
                triggeringGeofencesIdsList.add(geofence.getRequestId());
            }
            String triggeringGeofencesIdsString = TextUtils.join(", ",  triggeringGeofencesIdsList);

            Location location = geofencingEvent.getTriggeringLocation();
            GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
            b.putDouble("lat",geoPoint.getLatitude());
            b.putDouble("long",geoPoint.getLongitude());
            b.putString("sitename",triggeringGeofencesIdsString);

            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                b.putBoolean("exit",false);
                Log.i("exit123", "Hello");

            } else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
                b.putBoolean("exit",true);
                Log.i("exit123", "bye");

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
