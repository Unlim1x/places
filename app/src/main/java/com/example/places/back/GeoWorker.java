package com.example.places.back;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;

import com.example.places.App;
import com.example.places.room.daos.TrackerDao;
import com.example.places.room.database.PlacesDatabase;
import com.example.places.room.entities.Tracker;
import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.LocationRequest;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.places.MainActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import android.os.Parcelable;
import android.util.Log;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class GeoWorker extends Worker {


    private FusedLocationProviderClient fusedLocationProviderClient;
    Location location;
    PlacesDatabase database;
    TrackerDao trackerDao;
    private final CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();


    public GeoWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        database = App.getInstance().getDatabase();
        trackerDao = database.trackerDao();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
    }

    @NonNull
    @Override
    public Result doWork() {
        SharedPreferences mSettings = getApplicationContext().getSharedPreferences("s1paraX", Context.MODE_PRIVATE);
        byte i =7;
        while(i>0) {

            Log.i("Geo", "starts at" + LocalTime.now());

            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.e("GEO", "SOMETHING WENT WRONG");
                return Result.failure();
            }
            Task<Location> locationResult = fusedLocationProviderClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.getToken());
            int a = LocalDateTime.now().hashCode();
            float color = (float) Math.random() * 359;

            locationResult.addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {

                        location = task.getResult();
                        Tracker tracker = new Tracker();
                        tracker.latitude = location.getLatitude();
                        tracker.longitude = location.getLongitude();
                        tracker.title = String.valueOf(a);
                        tracker.snippet = "default";
                        tracker.color = color;
                        tracker.date = LocalDateTime.now().toString();
                        trackerDao.insert(tracker);
                        Log.e("GEO", "okay now, latitude= " + location.getLatitude() + "  longitude =" + location.getLongitude());
                        Log.i("Geo", "ends at" + LocalTime.now());


                }
            });
            try {
                TimeUnit.MINUTES.sleep(mSettings.getInt("tracker_freq", 2));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            i--;
        }


        return Result.success();
    }


}
