package com.example.jin.mobileca1_v2;

import com.example.jin.mobileca1_v2.LocalBoundService.localBinder;
import android.Manifest;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import com.google.firebase.firestore.CollectionReference;

public class Home extends AppCompatActivity implements OnMapReadyCallback, LocationListener{

    LocalBoundService myService;
    boolean bounded = false ;

    //AppCompat
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int GEOFENCE_RADIUS_IN_METERS = 200;
    private static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS = Geofence.NEVER_EXPIRE;

    private ArrayList<Geofence> mGeofenceList;
    private PendingIntent mGeofencePendingIntent;
    private GeofencingClient mGeofencingClient;
    private GoogleMap googleMap;
    private LocationManager locationManager;
    private static final long MIN_TIME = 400;
    private static final float MIN_DISTANCE = 1000;
    private LatLng latlng;
    private boolean clockIn = false;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private Button mClockInBtn;
    private Button mClockOutBtn;

    private FirebaseUser user;
    private double radius;
    ArrayList<LatLng> center = new ArrayList<>();
    ArrayList<String> centerName = new ArrayList<>();

    private String currentLocation;

    private HashMap<String,LatLng> sitenames;

    //location
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location mLastKnownLocation;

private  Handler handler;
    FloatingActionButton floatingButton_one;
    FloatingActionButton floatingButton_two;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        sitenames = new HashMap<>();
        mGeofenceList = new ArrayList<>();
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        floatingButton_one = findViewById(R.id.clock_in);
        floatingButton_one.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(!clockIn) {
                    addClockInToDatabase();
                    clockIn = true;
                    floatingButton_one.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.ic_remove_circle_black_24dp));
                }else{
                    addClockOutToDatabase();
                    clockIn = false;
                    floatingButton_one.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.ic_work_black_24dp));
                }
            }
        });

        initMap();
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Bundle reply = msg.getData();
                LatLng geopoint = new LatLng((double)reply.get("lat"),(double)reply.get("long"));
                boolean exit = (boolean)reply.get("exit");
                currentLocation  = (String)reply.get("siteName");

                String s= ""+exit;
                Log.i("exit123", s);
                Log.i("exit123", currentLocation);

                //triggered latlng
                Log.i("exit123 ", geopoint.latitude+"");
                Log.i("exit123 ", geopoint.longitude+"");
                getCurrentLocation();

                updateUI(exit);
            }
        };


//
    }

    @Override
    protected void onStart(){
        super.onStart();
        // Bind to LocalService
        Intent intent = new Intent(this, LocalBoundService.class);
        bindService(intent,myConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop(){
        super.onStop();
        if(bounded){
            unbindService(myConnection);
            bounded = false;
        }
    }

    private ServiceConnection myConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            localBinder binder = (localBinder) service;
            myService = binder.getService();
            bounded = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bounded = false;
        }
    };

private void updateUI(boolean exit){

if(exit){
    floatingButton_one.setEnabled(false);
            floatingButton_two.setEnabled(false);
}else{
    floatingButton_one.setEnabled(true);
    floatingButton_two.setEnabled(true);
}
//    float[] distance = new float[2];
//
//    Location locationA = new Location("Current Location");
//
//    //Real Time Location
//    locationA.setLatitude(latlng.latitude);
//    locationA.setLongitude(latlng.longitude);
//
//    //Inside Dkit
//    //locationA.setLatitude(53.98988);
//    //locationA.setLongitude(-6.3941);
//
//    //Outside Dkit
////    locationA.setLatitude(54.0050);
////    locationA.setLongitude(-6.3933);
//
//    for(int i = 0 ; i < center.size();i++){
//        Location locationB = new Location("point B");
//        locationB.setLatitude(center.get(i).latitude);
//        locationB.setLongitude(center.get(i).longitude);
//
//        Location.distanceBetween(locationA.getLatitude(),locationA.getLongitude(),locationB.getLatitude(),locationB.getLongitude(),distance);
//
//        if(distance[0] > GEOFENCE_RADIUS_IN_METERS){
//            floatingButton_one.setEnabled(false);
//            floatingButton_two.setEnabled(false);
//            Log.i("Tage 1", "disable" );
//        }else{
//            floatingButton_one.setEnabled(true);
//            floatingButton_two.setEnabled(true);
//            currentLocation=centerName.get(i);
//            Log.i("Tage 1", "enable" );
//            i = center.size() + 10;
//        }
//    }

}
    @Override
    public void onMapReady(GoogleMap map) {
            googleMap = map;
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                googleMap.setMyLocationEnabled(true);
            }
        checkPermission();

        setupGeofence();
    }
    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

        } else {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }
    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, (LocationListener) this); //You can also use LocationManager.GPS_PROVIDER and LocationManager.PASSIVE_PROVIDER
        }
    }

    private void getCurrentLocation() {

        try {
            Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();

            locationResult.addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    mLastKnownLocation = location;
                    if (mLastKnownLocation != null) {
                        if(latlng.latitude!=mLastKnownLocation.getLatitude()){
                        latlng = new LatLng(mLastKnownLocation.getLatitude(),mLastKnownLocation.getLongitude());
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(mLastKnownLocation.getLatitude(),
                                            mLastKnownLocation.getLongitude()), 15));
                        }
                    }

                }
            }).addOnFailureListener(this, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });

        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }
    @Override
    public void onLocationChanged(final Location location) {

        if (location != null & googleMap != null) {

            latlng = new LatLng(location.getLatitude(), location.getLongitude());
//    locationA.setLatitude(54.0050);
//    locationA.setLongitude(-6.3933);
//            latlng = new LatLng(54.0050, -6.3933);
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latlng, 15);

            googleMap.animateCamera(cameraUpdate);

            locationManager.removeUpdates(this);
            if(sitenames.size()!=0){
                addGeofence();
//                updateUI();
            }

//            new Handler().postDelayed(new Runnable() {
////
//                @Override
//               public void run() {
//                    FloatingActionButton floatingButton_one = findViewById(R.id.clock_in);
//                    FloatingActionButton floatingButton_two = findViewById(R.id.clock_out);
//                    float[] distance = new float[2];
//
//                    Location locationA = new Location("Current Location");
//
//                    //Real Time Location
//                    locationA.setLatitude(latlng.latitude);
//                    locationA.setLongitude(latlng.longitude);
//
//                    //Inside Dkit
//                    //locationA.setLatitude(53.98988);
//                    //locationA.setLongitude(-6.3941);
//
//                    //Outside Dkit
//                    //locationA.setLatitude(55.98488);
//                    //locationA.setLongitude(-6.3961837);
//
//                    for(int i = 0 ; i < center.size();i++){
//                        Location locationB = new Location("point B");
//                        locationB.setLatitude(center.get(i).latitude);
//                        locationB.setLongitude(center.get(i).longitude);
//
//                        Location.distanceBetween(locationA.getLatitude(),locationA.getLongitude(),locationB.getLatitude(),locationB.getLongitude(),distance);
//
//                        if(distance[0] > GEOFENCE_RADIUS_IN_METERS){
//                            floatingButton_one.setEnabled(false);
//                            floatingButton_two.setEnabled(false);
//                            Log.i("Tage 1", "disable" );
//                        }else{
//                            floatingButton_one.setEnabled(true);
//                            floatingButton_two.setEnabled(true);
//                            currentLocation=centerName.get(i);
//                            Log.i("Tage 1", "enable" );
//                            i = center.size() + 10;
//                        }
//                    }
//
//                }}, 2000);
        }
    }

    private void setupGeofence() {
        initGeofencingClient();
    }

    private void initGeofencingClient() {
        getLocationFromDatabase();
        mGeofencePendingIntent = getGeofencePendingIntent();
//        generateGeofence();
        mGeofencingClient = LocationServices.getGeofencingClient(this);
    }


    private void addGeofence() {
            mGeofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            drawCircle();
                        }
                    });
    }

    private void generateGeofence(){
        for(Map.Entry<String,LatLng> entry: sitenames.entrySet()){
            mGeofenceList.add(new Geofence.Builder()
                    .setRequestId(entry.getKey())
                    .setCircularRegion(
                            entry.getValue().latitude,
                            entry.getValue().longitude,
                            GEOFENCE_RADIUS_IN_METERS
                    )
                    .setExpirationDuration(GEOFENCE_EXPIRATION_IN_MILLISECONDS)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build());

        }
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent() {
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        intent.putExtra("messenger", new Messenger(handler));

        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

    }

    private void removeGeofence() {
        if (googleMap != null) {
            googleMap.clear();
        }
        if (mGeofencingClient != null) {
            mGeofencingClient.removeGeofences(mGeofencePendingIntent);
        }
    }

    private void drawCircle() {
        for(Map.Entry<String,LatLng> entry: sitenames.entrySet()) {

            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(new LatLng(entry.getValue().latitude,entry.getValue().longitude));
            googleMap.addMarker(markerOptions);
            CircleOptions circleOptions = new CircleOptions();
            circleOptions.center(new LatLng(entry.getValue().latitude,entry.getValue().longitude));
            center.add(new LatLng(entry.getValue().latitude,entry.getValue().longitude));
            centerName.add(entry.getKey());
            circleOptions.radius(GEOFENCE_RADIUS_IN_METERS);
            circleOptions.fillColor(0x50666b75);
            circleOptions.strokeColor(0x50666b75);
            googleMap.addCircle(circleOptions);
            radius = circleOptions.getRadius();
        }



    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private void addClockInToDatabase(){
        Map<String, Object> clockIn= new HashMap<>();
        clockIn.put("checkInTime",getCurrentTimeUsingDate());
        clockIn.put("checkOutTime","");
        clockIn.put("lat",latlng.latitude);
        clockIn.put("long",latlng.longitude);
        clockIn.put("Site Name",currentLocation);
        clockIn.put("workerId",user.getUid());
        Log.i("exit123", "DocumentSnapshot added with ID: 11");

        db.collection("clock")
                .add(clockIn)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        if(bounded){
                            String currentTime = myService.getTime();
                            Log.i("Tage 1", "DocumentSnapshot added with ID: " + documentReference.getId());
                            Toast.makeText(Home.this, "Clock in success at " + currentLocation + " at " + currentTime ,
                                    Toast.LENGTH_SHORT).show();
                        }else{
                            Log.i("Tage 1", "DocumentSnapshot added with ID: " + documentReference.getId());
                            Toast.makeText(Home.this, "Clock in success at " + currentLocation,
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("Tag 2", "Error adding document", e);
                        Toast.makeText(Home.this, "Clock in failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addClockOutToDatabase(){
        Map<String, Object> clockOut = new HashMap<>();
        clockOut.put("checkInTime","");
        clockOut.put("checkOutTime",getCurrentTimeUsingDate());
        clockOut.put("lat",latlng.latitude);
        clockOut.put("long",latlng.longitude);
        clockOut.put("Site Name",currentLocation);
        clockOut.put("workerId",user.getUid());
        Log.i("exit123", "DocumentSnapshot added with ID: out");
        db.collection("clock")
                .add(clockOut)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        if(bounded){
                            String currentTime = myService.getTime();
                            Log.d("Tag 1", "DocumentSnapshot added with ID: " + documentReference.getId());
                            Toast.makeText(Home.this, "Clock out success at " + currentLocation + " at " + currentTime,
                                    Toast.LENGTH_SHORT).show();
                        }else{
                            Log.d("Tag 1", "DocumentSnapshot added with ID: " + documentReference.getId());
                            Toast.makeText(Home.this, "Clock out success at " + currentLocation,
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("Tag 2", "Error adding document", e);
                        Toast.makeText(Home.this, "Clock out failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });

    }

    public static String getCurrentTimeUsingDate() {

        Date date = new Date();

        String strDateFormat = "hh:mm:ss a";

        DateFormat dateFormat = new SimpleDateFormat(strDateFormat);

        String formattedDate = dateFormat.format(date);
        return formattedDate;
    }

    public void getLocationFromDatabase(){

        db.collection("sitename").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                if (task.isSuccessful()){
                    for(QueryDocumentSnapshot document:task.getResult()){
                        Log.d("Databbas123", document.getId()+"=> " + document.getData());
                        GeoPoint geoPoint = (GeoPoint)document.get("location");
                        sitenames.put((String)document.get("siteName"),new LatLng(geoPoint.getLatitude(),geoPoint.getLongitude()));
                    }
                    generateGeofence();
                    addGeofence();

                }else{
                    Log.d("Error123","Error");
                    Toast.makeText(Home.this, "Error", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }

}
