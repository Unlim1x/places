package ru.lim1x.places.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;

import ru.lim1x.places.R;
import ru.lim1x.places.databinding.FragmentMainBinding;
import ru.lim1x.places.databinding.FragmentSignupBinding;
import ru.lim1x.places.room.App;
import ru.lim1x.places.room.daos.InitAppDao;
import ru.lim1x.places.room.daos.ProfileDao;
import ru.lim1x.places.room.database.PlacesDatabase;
import ru.lim1x.places.room.entities.Profile;
import ru.lim1x.places.ui.first_open.EntryFragment;
import ru.lim1x.places.ui.first_open.SignupFragment;

import com.google.android.material.tabs.TabLayout;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;

import ru.lim1x.places.ui.main.SectionsPagerAdapter;
import ru.lim1x.places.databinding.ActivityMainBinding;

import java.util.Iterator;
import java.util.List;


public class MainActivity extends AppCompatActivity  {

    private ActivityMainBinding binding;
    private FragmentMainBinding fbinding;
    private FragmentSignupBinding subinding;

    private final byte profile_type = 0; // 0 = local_default, 1 = signed_in;
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
                        .add(R.id.container, new EntryFragment())
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
                if (!found) {
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
                .replace(R.id.container, new SignupFragment())
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