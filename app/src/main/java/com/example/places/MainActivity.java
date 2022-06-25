package com.example.places;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Bundle;

import com.example.places.databinding.FragmentEntryBinding;
import com.example.places.databinding.FragmentMainBinding;
import com.example.places.ui.first_open.EntryFragment;
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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.places.ui.main.SectionsPagerAdapter;
import com.example.places.databinding.ActivityMainBinding;

import java.time.LocalDateTime;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private FragmentEntryBinding fbinding;

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
        database.execSQL("CREATE TABLE IF NOT EXISTS init (first INT)");




        // Here we define if application has been already used
        //If not, we have to show Entry fragment

        Cursor cursor1 = database.rawQuery("SELECT COUNT (*) FROM init", null);
        if (cursor1.moveToNext())
            if (cursor1.getInt(0) == 0){
                cursor1.close();
                fbinding = FragmentEntryBinding.inflate(getLayoutInflater());
                setContentView(fbinding.getRoot());
                TextView already_have = findViewById(R.id.entry_text_profile_already_exist);
                Button signup = findViewById(R.id.entry_signup_button);
                Button signin = findViewById(R.id.entry_signin_button);
                Button local = findViewById(R.id.entry_local_profile_button);

                local.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ContentValues cv = new ContentValues();
                        cv.put("first", 1);
                        database.insert("init", null, cv);
                        database.close();
                        Intent intent = getIntent();
                        finish();
                        startActivity(intent);
                    }
                });

                ImageView local_info = findViewById(R.id.entry_local_profile_info_button);

                already_have.setVisibility(View.INVISIBLE);
                signup.setVisibility(View.INVISIBLE);
                signin.setVisibility(View.INVISIBLE);
                local.setVisibility(View.INVISIBLE);
                local_info.setVisibility(View.INVISIBLE);

                TextView hello = findViewById(R.id.entry_text_hello);
                TextView permission = findViewById(R.id.entry_text_permission);
                TextView profile = findViewById(R.id.entry_text_profile);
                Button button = findViewById(R.id.entry_button);
                hello.setVisibility(View.INVISIBLE);
                permission.setVisibility(View.INVISIBLE);
                profile.setVisibility(View.INVISIBLE);
                button.setVisibility(View.INVISIBLE);


                Animation animation_hello = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.text_anim_rise);
                Animation animation_hello_fade = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.text_anim_fade);
                Animation animation_permissions = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.text_anim_rise);
                Animation animation_permission_fade = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.text_anim_fade);
                Animation animation_profile = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.text_anim_rise);

                animation_hello.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        hello.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {

                        hello.clearAnimation();
                        hello.startAnimation(animation_hello_fade);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                animation_hello_fade.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        hello.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        hello.setVisibility(View.INVISIBLE);
                        permission.startAnimation(animation_permissions);
                        hello.clearAnimation();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });

                animation_permissions.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        permission.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        button.setVisibility(View.VISIBLE);
                        permission.clearAnimation();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });

                animation_profile.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        profile.setVisibility(View.VISIBLE);

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        profile.clearAnimation();
                        signup.setVisibility(View.VISIBLE);
                        local.setVisibility(View.VISIBLE);
                        signin.setVisibility(View.VISIBLE);
                        local_info.setVisibility(View.VISIBLE);
                        already_have.setVisibility(View.VISIBLE);

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });

                animation_permission_fade.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        button.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        permission.setVisibility(View.INVISIBLE);
                        profile.startAnimation(animation_profile);
                        permission.clearAnimation();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });

                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getLocationPermission();
                        permission.startAnimation(animation_permission_fade);
                    }
                });

                local_info.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getApplicationContext(), "Вы сможете пользоваться приложением, но не сможете добавить друзей," +
                                        " поделиться с ними местами, а данные будут храниться только на Вашем устройстве",
                                Toast.LENGTH_LONG).show();
                    }
                });

                hello.startAnimation(animation_hello);
            }
        else{
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
            }






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