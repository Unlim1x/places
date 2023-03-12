package com.example.places.ui.main;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;

import com.example.places.back.Utils;
import com.example.places.back.recievers.LocationUpdatesBroadcastReceiver;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.example.places.room.App;
import com.example.places.activities.MainActivity;
import com.example.places.activities.MapsActivity;
import com.example.places.R;
import com.example.places.back.workers.GeoWorker;
import com.example.places.back.workers.GeoWorker2;
import com.example.places.databinding.FragmentTrackerBinding;
import com.example.places.room.daos.TrackerDao;
import com.example.places.room.database.PlacesDatabase;
import com.example.places.room.entities.Tracker;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;

import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TrackerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TrackerFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    private LinearLayout zoom;
    private LocationRequest mLocationRequest;
    float x = 0;
    float y = 0;
    GoogleMap map_observer;
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    private static final long UPDATE_INTERVAL = 60000; // Every 60 seconds.

    /**
     * The fastest rate for active location updates. Updates will never be more frequent
     * than this value, but they may be less frequent.
     */
    private static final long FASTEST_UPDATE_INTERVAL = 30000; // Every 30 seconds

    /**
     * The max time before batched results are delivered by location services. Results may be
     * delivered sooner than this interval.
     */
    private static final long MAX_WAIT_TIME = UPDATE_INTERVAL * 5; // Every 5 minutes.


    private static final String ARG_SECTION_NUMBER = "section_number";

    private static final String TAG = MapsActivity.class.getSimpleName();
    FragmentTrackerBinding binding;
    private static final String ARG_PARAM1 = "param1";
    private PlacesDatabase database;
    private TrackerDao trackerDao;

    private final LatLng defaultLocation = new LatLng(59.938955, 30.315644);
    private static final int DEFAULT_ZOOM = 15;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Context mContext;
    private boolean locationPermissionGranted;
    private PlacesClient placesClient;
    SharedPreferences mSettings;
    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location lastKnownLocation;
    private boolean helper_fab = false;
    private int update_frequency = 2;

    public TrackerFragment(){}
    public TrackerFragment(Context context){
        mContext = context;
    }


    public static TrackerFragment newInstance(int index, Context context) {
        TrackerFragment fragment = new TrackerFragment(context);
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSettings = getActivity().getSharedPreferences("s1paraX", Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mContext = getContext();
        View root;
        binding = FragmentTrackerBinding.inflate(inflater, container, false);
        root = binding.getRoot();
        database = App.getInstance().getDatabase();
        trackerDao = database.trackerDao();

        Places.initialize(mContext, "AIzaSyCd41NcKGeylMpBOrGn1J8wh8mp3YkA-MA");
        placesClient = Places.createClient(mContext);

        // Construct a FusedLocationProviderClient.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(mContext);
        return root;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.style_map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }

        FloatingActionButton fab = binding.trackerFab;


        try {
            if (!WorkManager.getInstance(getContext()).getWorkInfosByTag("geoTask").get().isEmpty())
            {
                fab.setImageResource(R.drawable.stop);
                helper_fab = true;
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (helper_fab) {
                        fab.setImageResource(R.drawable.play);
                        helper_fab = false;
                        WorkManager.getInstance(getContext()).cancelAllWork();
                        WorkManager.getInstance(getContext()).cancelAllWorkByTag("geoTask");
                        WorkManager.getInstance(getContext()).pruneWork();
                    }
                    else
                    {
                        //todo:включить трекер
                        fab.setImageResource(R.drawable.stop);
                        helper_fab = true;
                        PeriodicWorkRequest geoWorkRequest = new PeriodicWorkRequest.Builder(GeoWorker.class, 15, TimeUnit.MINUTES ).addTag("geoTask").build(); //1, TimeUnit.MINUTES
                        WorkManager.getInstance(getContext()).enqueue(geoWorkRequest);
                        OneTimeWorkRequest oneTimeWorkRequest = new OneTimeWorkRequest.Builder(GeoWorker2. class).addTag("geoTask").build();
                        WorkManager.getInstance(getContext()).enqueue(oneTimeWorkRequest);

                    }
                }
            });
        zoom = binding.getRoot().findViewById(R.id.seekBar);
        zoom.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        x = event.getX();
                        y = event.getY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (event.getY() < y) {
                            map_observer.moveCamera(CameraUpdateFactory.zoomTo(map_observer.getCameraPosition().zoom + 0.05f));
                            y = event.getY();
                        }
                        else if (event.getY() > y) {
                            map_observer.moveCamera(CameraUpdateFactory.zoomTo(map_observer.getCameraPosition().zoom - 0.05f));
                            y = event.getY();
                        }
                        break;

                }
                return true;
            }
        });

    }

    private OnMapReadyCallback callback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap map) {
            map_observer = map;
            if(mSettings.contains("tracker_map_style")){
                String style = mSettings.getString("tracker_map_style","");
                if (style.equals("tracker_grayscale"))
                    map.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.grayscale));
                if (style.equals("tracker_classic"))
                    map.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.standard));
                if (style.equals("tracker_night"))
                    map.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.night));
                if (style.equals("tracker_style"))
                    map.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.tracker_style));
            }
            else{
                map.setMapStyle(MapStyleOptions.loadRawResourceStyle(
                        mContext, R.raw.tracker_style));
            }

            getLocalLocationPermission();
            // [END_EXCLUDE]

            // Turn on the My Location layer and the related control on the map.
            updateLocationUI(map);

            // Get the current location of the device and set the position of the map.
            getDeviceLocation(map);

            //getmarkerDB(map);
            drawPath(map);
        }

    };

    private void getmarkerDB(GoogleMap map){
        //Может быть понадобится если хочется посмотреть начало и конец маршрута. Хорошая идея.
    }

    private void updateLocationUI(GoogleMap map) {
        if (map == null) {
            return;
        }
        try {
            if (locationPermissionGranted) {
                map.setMyLocationEnabled(true);

                View locationButton = ((View) getView().findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
                RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
// position on right bottom

                rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
                rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                rlp.setMargins(0, 0, 30, 60);

                map.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                map.setMyLocationEnabled(false);
                map.getUiSettings().setMyLocationButtonEnabled(false);
                lastKnownLocation = null;
                getLocalLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }
    private void getDeviceLocation(GoogleMap map) {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermissionGranted && map != null) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(((MainActivity)getActivity()), new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            lastKnownLocation = task.getResult();
                            if (lastKnownLocation != null) {
                                map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(lastKnownLocation.getLatitude(),
                                                lastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            }
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            map.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                            map.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }

    private void getLocalLocationPermission(){
        ((MainActivity)getActivity()).getLocationPermission();
        locationPermissionGranted =  ((MainActivity)getActivity()).getLocationPermissionGranted();
    }

    private void drawPath(GoogleMap map){

        float width = mSettings.getFloat("tracker_width_key", 10f);

        Polyline default_line = null;


            List<Tracker> trackerList = trackerDao.getAll();
            Iterator<Tracker> trackerIterator = trackerList.iterator();
            double lat = 0d;
            double longit = 0d;
            LatLng coords;
            while (trackerIterator.hasNext()){
                Tracker tracker = trackerIterator.next();
                Log.e("Tracker", ""+ tracker.latitude);
                if(lat != 0d)
                {
                    LatLng temp = new LatLng(lat, longit);
                    coords = new LatLng(tracker.latitude, tracker.longitude);
                     map.addPolyline(new PolylineOptions().clickable(false).add(temp, coords)
                            .color(R.color.MediumSlateBlue).startCap(new RoundCap()).endCap(new RoundCap())
                            .width(width));
                    lat = coords.latitude;
                    longit =coords.longitude;
                }
                else{
                    lat = tracker.latitude;
                    longit = tracker.longitude;
                }
            }



        }
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        // Note: apps running on "O" devices (regardless of targetSdkVersion) may receive updates
        // less frequently than this interval when the app is no longer in the foreground.
        mLocationRequest.setInterval(UPDATE_INTERVAL);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Sets the maximum time when batched location updates are delivered. Updates may be
        // delivered sooner than this interval.
        mLocationRequest.setMaxWaitTime(MAX_WAIT_TIME);
    }

    private PendingIntent getPendingIntent() {
        // Note: for apps targeting API level 25 ("Nougat") or lower, either
        // PendingIntent.getService() or PendingIntent.getBroadcast() may be used when requesting
        // location updates. For apps targeting API level O, only
        // PendingIntent.getBroadcast() should be used. This is due to the limits placed on services
        // started in the background in "O".

        // TODO(developer): uncomment to use PendingIntent.getService().
//        Intent intent = new Intent(this, LocationUpdatesIntentService.class);
//        intent.setAction(LocationUpdatesIntentService.ACTION_PROCESS_UPDATES);
//        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intent = new Intent(getContext(), LocationUpdatesBroadcastReceiver.class);
        intent.setAction(LocationUpdatesBroadcastReceiver.ACTION_PROCESS_UPDATES);
        return PendingIntent.getBroadcast(getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    //TODO: Потом можно чекнуть интент сервис как работает, пока не нужно
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (s.equals(Utils.KEY_LOCATION_UPDATES_RESULT)) {
            //mLocationUpdatesResultView.setText(Utils.getLocationUpdatesResult(getContext()));
        } else if (s.equals(Utils.KEY_LOCATION_UPDATES_REQUESTED)) {
            //updateButtonsState(Utils.getRequestingLocationUpdates(getContext()));
        }
    }

    /**
     * Handles the Request Updates button and requests start of location updates.
     */
    public void requestLocationUpdates(View view) {
        try {
            Log.i(TAG, "Starting location updates");
            Utils.setRequestingLocationUpdates(getContext(), true);
            fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, getPendingIntent());
        } catch (SecurityException e) {
            Utils.setRequestingLocationUpdates(getContext(), false);
            e.printStackTrace();
        }
    }

    /**
     * Handles the Remove Updates button, and requests removal of location updates.
     */
    public void removeLocationUpdates(View view) {
        Log.i(TAG, "Removing location updates");
        Utils.setRequestingLocationUpdates(getContext(), false);
        fusedLocationProviderClient.removeLocationUpdates(getPendingIntent());
    }
}