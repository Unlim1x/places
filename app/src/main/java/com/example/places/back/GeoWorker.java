package com.example.places.back;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;

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
    SQLiteDatabase database;
    private final CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();


    public GeoWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        database = getApplicationContext().openOrCreateDatabase("myplacesx.db", android.content.Context.MODE_PRIVATE, null);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
        database.execSQL("CREATE TABLE IF NOT EXISTS tracker (latitude REAL, longitude REAL, title TEXT, snippet TEXT, color REAL)");

    }

    @NonNull
    @Override
    public Result doWork() {
        SharedPreferences mSettings = getApplicationContext().getSharedPreferences("s1paraX", Context.MODE_PRIVATE);

        while(true) {

            Log.i("Geo", "starts at" + LocalTime.now());

            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.e("GEO", "SOMETHING WENT WRONG");
                return Result.failure();
            }
            Task<Location> locationResult = fusedLocationProviderClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.getToken());
            int a = LocalDateTime.now().hashCode();
            float color = (float) Math.random() * 359;
            ContentValues dataput = new ContentValues();

            locationResult.addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    location = task.getResult();

                    dataput.put("latitude", location.getLatitude());
                    dataput.put("longitude", location.getLongitude());
                    dataput.put("title", String.valueOf(a));
                    dataput.put("snippet", "default");
                    dataput.put("color", color);
                    database.insert("tracker", null, dataput);
                    Log.e("GEO", "okay now, latitude= " + location.getLatitude() + "  longitude =" + location.getLongitude());
                    Log.i("Geo", "ends at" + LocalTime.now());

                }
            });
            try {
                TimeUnit.MINUTES.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(a == 3)
                break;
        }


        return Result.success();
    }


}