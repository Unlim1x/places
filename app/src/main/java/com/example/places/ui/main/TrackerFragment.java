package com.example.places.ui.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Paint;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.example.places.MainActivity;
import com.example.places.MapsActivity;
import com.example.places.R;
import com.example.places.back.GeoWorker;
import com.example.places.databinding.FragmentTrackerBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;

import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Cap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.sql.ResultSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TrackerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TrackerFragment extends Fragment{


    private static final String ARG_SECTION_NUMBER = "section_number";

    private static final String TAG = MapsActivity.class.getSimpleName();
    FragmentTrackerBinding binding;
    private static final String ARG_PARAM1 = "param1";
    private SQLiteDatabase database;
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
        database = ((MainActivity)getActivity()).getDataBase();
        database.execSQL("CREATE TABLE IF NOT EXISTS tracker (latitude REAL, longitude REAL, title TEXT, snippet TEXT, color REAL)");
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
                        OneTimeWorkRequest geoWorkRequest = new OneTimeWorkRequest.Builder(GeoWorker.class).addTag("geoTask").build(); //1, TimeUnit.MINUTES
                        WorkManager.getInstance(getContext()).enqueue(geoWorkRequest);
                    }
                }
            });

    }

    private OnMapReadyCallback callback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap map) {

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
        //Cursor cursor = database.rawQuery("SELECT * FROM tracker", null);
        Cursor cursor = database.rawQuery("SELECT * FROM markers", null);
        while(cursor.moveToNext()){
            double latitude = cursor.getDouble(0);
            double longitude = cursor.getDouble(1);
            String title = cursor.getString(2);
            String snippet = cursor.getString(3);
            float color = cursor.getFloat(4);

            Log.i("WTF?????????", title);
            LatLng position = new LatLng(latitude, longitude);

            map.addMarker(new MarkerOptions()
                    .position(position)
                    .title(title)
                    .snippet(snippet)
                    .draggable(false)
                    .icon(BitmapDescriptorFactory.defaultMarker(color)));
        }
        cursor.close();
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

            Cursor cursor1 = database.rawQuery("SELECT * FROM tracker", null);
            double lat = 0d;
            double longit = 0d;
            LatLng coords;
            while(cursor1.moveToNext()) {
                if(lat != 0d)
                {
                    LatLng temp = new LatLng(lat, longit);
                    coords = new LatLng(cursor1.getDouble(0), cursor1.getDouble(1));
                    default_line = map.addPolyline(new PolylineOptions().clickable(false).add(temp, coords)
                            .color(R.color.MediumSlateBlue).startCap(new RoundCap()).endCap(new RoundCap())
                            .width(width));
                    lat = coords.latitude;
                    longit =coords.longitude;
                }
                else{
                    lat = cursor1.getDouble(0);
                    longit = cursor1.getDouble(1);
                    coords = new LatLng(lat, longit);
                }
            }
        }
}