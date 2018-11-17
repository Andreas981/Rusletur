package no.hiof.informatikk.gruppe6.rusletur.MapsAndTrips;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import no.hiof.informatikk.gruppe6.rusletur.MainScreen;
import no.hiof.informatikk.gruppe6.rusletur.fragment.MainMenuFragment;

import static java.lang.Double.valueOf;
import static no.hiof.informatikk.gruppe6.rusletur.MapsAndTrips.MapsActivity.TAG;


/*
    * In it's current state, this tracker is mainly for saving and storing trips.
    * All coordinates are sent to <LatLng> array where they will stay until the
    * service is terminated.
    *
    * Todo:
    *
     */


public class TripTracker extends Service {

    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    public static final ArrayList<LatLng> savedLocations = new ArrayList<>();
    private long timeStart;
    private long timeStop;
    private long timeDiff;



    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Tracker - onCreate called");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Tracker - onStartCommmand called");

        timeStart = 0;
        timeStop = 0;
        timeDiff = 0;


        testMethod();

        savedLocations.clear();



        return START_STICKY;
    }

    private void testMethod() {
        Log.i(TAG, "Tracker - testMethod called");

        /*
        * Define how often LocationRequest pings. Currently at 10s and fastet at 5, and
        * highest possible accuracy.
         */

        timeStart = System.nanoTime();

        locationRequest = new LocationRequest();
        locationRequest.setInterval(30000);
        locationRequest.setFastestInterval(25000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


        /*
        * FusedLocationProvider gives the most accurate results.
        * On each LocationCallback (set by the interval above), convert that location into a
        * LatLng object, and add that object to an array (savedLocations) which consists
        * of LatLng objects. In this format, it's ready to be sent directly to our firebase.
         */
        int trackingGranted = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
        if (trackingGranted == PackageManager.PERMISSION_GRANTED) {
            FusedLocationProviderClient fClient = LocationServices.getFusedLocationProviderClient(this);
            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {

                    locationResult.getLocations();
                    Log.i(TAG, "Tracker - onLocationResult resolved");
                    Log.i(TAG, locationResult.getLocations().toString());
                    Log.i(TAG, "Size of getLocations" + Integer.toString(locationResult.getLocations().size()));
                    LatLng previousLocation = new LatLng(valueOf(locationResult.getLastLocation().getLatitude()), valueOf(locationResult.getLastLocation().getLongitude()));
                    Log.i(TAG, previousLocation.toString());
                    savedLocations.add(previousLocation);
                    Log.i(MainScreen.TAG3, "TripTracker sin SavedLocations:" + Integer.toString(savedLocations.size()));


                    super.onLocationResult(locationResult);
                }


            };
            //Initialize requestLocationUpdates. Use looper once this class has been thread(ed)?
            fClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }

    }

    public static ArrayList<LatLng> fetchArray(){
        Log.i(MainScreen.TAG3, "FetchArray blir kalt : " + String.valueOf(savedLocations.size()));
        return savedLocations;
    }


    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy called");
        timeStop = System.nanoTime();
        timeDiff = timeStop - timeStart;

        /* Credits to Keppil @ stackoverflow */
        long seconds = TimeUnit.SECONDS.convert(timeDiff, TimeUnit.NANOSECONDS);
        int day = (int) TimeUnit.SECONDS.toDays(seconds);
        long hours = TimeUnit.SECONDS.toHours(seconds) -
                TimeUnit.DAYS.toHours(day);
        long minute = TimeUnit.SECONDS.toMinutes(seconds) -
                TimeUnit.DAYS.toMinutes(day) -
                TimeUnit.HOURS.toMinutes(hours);
        long second = TimeUnit.SECONDS.toSeconds(seconds) -
                TimeUnit.DAYS.toSeconds(day) -
                TimeUnit.HOURS.toSeconds(hours) -
                TimeUnit.MINUTES.toSeconds(minute);

        Log.i(TAG, "Day : " + day + " Hour " + hours + " minutes: " + minute + " seconds : " + second);

        String timeSpent = ((day != 0) ? (day + "dager, "):"") + "" + ((hours != 0) ? (hours + " timer, ") : "") + "" + ((minute != 0) ? (minute + " minutter, ") : "0 minutter, ") + "" + ((second != 0) ? (second + " sekunder") : "0 sekunder");

        /*
        * When service is terminated, check if boolean is true in MainMenuFragment.
        * If true, send an Intent with the arraylist of LatLng's through a broadCast.
        * This broadcast will be picked up by a broadcastreceiver in MainScreen class.
        * Check MainScreen for further info.
         */


        if(MainMenuFragment.saveWasClicked == true) {
            Intent startSaveTripIntent = new Intent();
            startSaveTripIntent.setClass(this, SaveTripActivity.class);
            startSaveTripIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startSaveTripIntent.putExtra("coordsArray", savedLocations);
            Log.i(MainScreen.TAG3, "onDestroy blir kalt : " + String.valueOf(savedLocations.size()));
            startSaveTripIntent.putExtra("timeSpent", timeSpent);
            startSaveTripIntent.putExtra("sender",this.getClass().getSimpleName());
            startActivity(startSaveTripIntent);
        }

        /*
        * Regardless of previously mentioned boolean, terminate the service and disconnect LocationServices.
        * You have to do both because Android Service and LocationServices are independent, despite LocationServices
        * running as a background service when initialized here.
         */
        this.stopSelf();
        LocationServices.getFusedLocationProviderClient(this).removeLocationUpdates(locationCallback);
        super.onDestroy();
    }


}