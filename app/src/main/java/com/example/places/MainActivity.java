package com.example.places;

import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.example.places.ui.main.SectionsPagerAdapter;
import com.example.places.databinding.ActivityMainBinding;

import java.time.LocalDateTime;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private byte profile_type = 0; // 0 = local_default, 1 = signed_in;
    private Bundle bundle;
    SQLiteDatabase database;

    private boolean locationPermissionGranted;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = openOrCreateDatabase("myplacesx.db", MODE_PRIVATE, null);
        database.execSQL("CREATE TABLE IF NOT EXISTS markers (latitude REAL, longitude REAL, title TEXT, snippet TEXT, drag INT, color REAL)");
        database.execSQL("CREATE TABLE IF NOT EXISTS profiles (username TEXT, password TEXT, type INT, loggedout INT)");



        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = binding.viewPager;
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = binding.tabs;
        tabs.setupWithViewPager(viewPager);

        bundle = new Bundle();
        Cursor cursor = database.rawQuery("SELECT COUNT (*) FROM profiles", null);
        if (cursor.moveToNext())
            if (cursor.getInt(0) == 0){
                int a = LocalDateTime.now().hashCode();
                a = Math.abs(a);
                String username = "User_" + String.valueOf(a);
                String password = "default";
                ContentValues cv = new ContentValues();
                cv.put("username", username);
                cv.put("password", password);
                cv.put("type", 0);
                cv.put("loggedout", 0);
                database.insert("profiles", null, cv);
                bundle.putString("username", username);
            }
            else if(cursor.getInt(0) == 1){
                cursor = database.rawQuery("SELECT * FROM profiles", null);
                cursor.moveToNext();
                bundle.putString("username", cursor.getString(0));
            }
            cursor.close();




        /*
        setContentView(R.layout.wtf);

        llBottomSheet = (LinearLayout) findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(llBottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
         */



        // [START_EXCLUDE silent]
        // Construct a PlacesClient






    }

    @Override
    protected void onStop() {
        // call the superclass method first
        super.onStop();
        database.close();
    }

    public byte profileType(){
        return profile_type;
    }

    public SQLiteDatabase getDataBase(){
        return database;
    }

    public void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }
    // [END maps_current_place_location_permission]

    /**
     * Handles the result of the request for location permissions.
     */
    // [START maps_current_place_on_request_permissions_result]
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        locationPermissionGranted = false;
        if (requestCode
                == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public boolean getLocationPermissionGranted(){
        return locationPermissionGranted;
    }

    public String getUsername(){
        return bundle.getString("username");
    }
}