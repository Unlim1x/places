package com.example.places;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;

import com.example.places.databinding.FragmentEntryBinding;
import com.example.places.databinding.FragmentMainBinding;
import com.example.places.databinding.FragmentSignupBinding;
import com.example.places.room.daos.InitAppDao;
import com.example.places.room.daos.ProfileDao;
import com.example.places.room.database.PlacesDatabase;
import com.example.places.room.entities.Profile;
import com.example.places.ui.dialogs.OneButtonDialog;
import com.example.places.ui.first_open.EntryFragment;
import com.example.places.ui.first_open.SignupFragment;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;

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
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.places.ui.main.SectionsPagerAdapter;
import com.example.places.databinding.ActivityMainBinding;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity  {

    private ActivityMainBinding binding;
    private FragmentMainBinding fbinding;
    private FragmentSignupBinding subinding;

    private byte profile_type = 0; // 0 = local_default, 1 = signed_in;
    private Bundle bundle;
    PlacesDatabase database;
    InitAppDao initAppDao;
    ProfileDao profileDao;

    SharedPreferences mSettings;
    private boolean locationPermissionGranted;

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSettings = getSharedPreferences("s1paraX", Context.MODE_PRIVATE);
        boolean light_theme = mSettings.getBoolean("light_theme", false);
        boolean dark_theme = mSettings.getBoolean("dark_theme", false);
        boolean system_theme = mSettings.getBoolean("system_theme", false);

        if (light_theme)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        else if(dark_theme)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        else if(system_theme)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);



        database = App.getInstance().getDatabase();
        initAppDao =database.initAppDao();
        profileDao = database.profileDao();


        // Here we define if application has been already used
        //If not, we have to show Entry fragment
        //else goes on
        int has_application_been_used_and_entered = initAppDao.getInit();

            if (has_application_been_used_and_entered == 0){
                fbinding = FragmentMainBinding.inflate(getLayoutInflater());
                setContentView(fbinding.getRoot());
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.container, new EntryFragment()) // or replace с теми же параметрами
                        .commit();

            }
            else {
                binding = ActivityMainBinding.inflate(getLayoutInflater());
                setContentView(binding.getRoot());
                SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
                ViewPager viewPager = binding.viewPager;
                viewPager.setAdapter(sectionsPagerAdapter);
                TabLayout tabs = binding.tabs;
                tabs.setupWithViewPager(viewPager);

                bundle = new Bundle();

                List<Profile> profiles = profileDao.getAll();
                Iterator<Profile> profileIterator = profiles.iterator();
                Profile temp_for_def = new Profile();
                boolean found = false; //Если нашли профиль, который залогинен (logged out == 0)
                while (profileIterator.hasNext()) {
                    Profile profile = profileIterator.next();
                    if (profile.loggedout == 0) {
                        bundle.putString("username", profile.username);
                        bundle.putByte("profile_type", (byte) profile.type);
                        Log.i("Profile type", "" + bundle.getByte("profile_type"));
                        found = true;
                    }
                    if (profile.type == 0)
                        temp_for_def = profile;
                }
                if (found == false) {
                    bundle.putString("username", temp_for_def.username);
                    bundle.putByte("profile_type", (byte) temp_for_def.type);
                    Log.i("Profile type", "" + bundle.getByte("profile_type"));
                    found = true;
                    temp_for_def.loggedout = 0;
                    profileDao.update(temp_for_def);
                }
            }


            }







    public void openSignin(){

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, new SignupFragment()) // or replace с теми же параметрами
                .commit();
    }

    @Override
    protected void onDestroy() {
        // call the superclass method first
       // database.close();
        super.onDestroy();
    }
    @Override
    protected void onRestart() {

        super.onRestart();
        this.recreate();
    }




    public byte profileType(){
        return bundle.getByte("profile_type");
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