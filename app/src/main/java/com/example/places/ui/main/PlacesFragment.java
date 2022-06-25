package com.example.places.ui.main;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.places.MainActivity;
import com.example.places.MapsActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowCloseListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.example.places.R;
import com.example.places.databinding.FragmentPlacesBinding;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


/**
 * A placeholder fragment containing a simple view.
 */
public class PlacesFragment extends Fragment {

    Context mContext;
    private SQLiteDatabase database;
    private static final String ARG_SECTION_NUMBER = "section_number";
    private LinearLayout llBottomSheet;
    private BottomSheetBehavior bottomSheetBehavior;
    private int marker_counter = 0;
    private static final String TAG = MapsActivity.class.getSimpleName();
    private GoogleMap map;
    private CameraPosition cameraPosition;
    // The entry point to the Places API.
    private PlacesClient placesClient;

    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient fusedLocationProviderClient;

    // A default location (Saint-Petersburg, Russia) and default zoom to use when location permission is
    // not granted.
    private final LatLng defaultLocation = new LatLng(59.938955, 30.315644);
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean locationPermissionGranted;

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location lastKnownLocation;

    // Keys for storing activity state.
    // [START maps_current_place_state_keys]
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";
    // [END maps_current_place_state_keys]

    // Used for selecting the current place.
    private static final int M_MAX_ENTRIES = 5;
    private String[] likelyPlaceNames;
    private String[] likelyPlaceAddresses;
    private List[] likelyPlaceAttributions;
    private LatLng[] likelyPlaceLatLngs;


    private FragmentPlacesBinding binding;
    private PlacesFragment(Context context){
        mContext = context;
    }


    public static PlacesFragment newInstance(int index, Context context) {
        PlacesFragment fragment = new PlacesFragment(context);
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root;
        binding = FragmentPlacesBinding.inflate(inflater, container, false);
        root = binding.getRoot();
        database = ((MainActivity)getActivity()).getDataBase();
        Places.initialize(mContext, "AIzaSyCd41NcKGeylMpBOrGn1J8wh8mp3YkA-MA");
        placesClient = Places.createClient(mContext);

        // Construct a FusedLocationProviderClient.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(mContext);
        return root;
        }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        llBottomSheet = (LinearLayout) getView().findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(llBottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }

    private OnMapReadyCallback callback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap map) {

            map.setOnMapLongClickListener(new OnMapLongClickListener() {
                @Override
                public void onMapLongClick(@NonNull LatLng currentPressedPosition) {
                    int a = LocalDateTime.now().hashCode();
                    float color = (float)Math.random()*359;
                    marker_counter++;
                    map.addMarker(new MarkerOptions()
                            .position(currentPressedPosition)
                            .title("Маркер #"+marker_counter)
                            .snippet(String.valueOf(a))
                            .draggable(true)
                            .icon(BitmapDescriptorFactory.defaultMarker(color)));
                    putmarkerDB(currentPressedPosition.latitude, currentPressedPosition.longitude,
                            "Маркер #"+marker_counter, String.valueOf(a), true, color);
                }
            });

            map.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(@NonNull Marker marker) {
                    Log.i("Zaebal", "YA TUT!!!!!!!!!!!!");
                    LayoutInflater li = LayoutInflater.from(mContext);
                    View inputDialogView = li.inflate(R.layout.input_dialog, null);
                    AlertDialog.Builder aDialogBuilder = new AlertDialog.Builder(mContext);
                    aDialogBuilder.setView(inputDialogView);
                    aDialogBuilder
                            .setCancelable(false)
                            .setPositiveButton("Применить",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,int id) {
                                            final EditText input = inputDialogView.findViewById(R.id.input_text);
                                            //Вводим текст и отображаем в строке ввода на основном экране:
                                            marker.setTitle(input.getText().toString());
                                            TextView tv = llBottomSheet.findViewById(R.id.marker_header);
                                            TextView dv = llBottomSheet.findViewById(R.id.marker_description);
                                            renamemarkerDB(dv.getText().toString(), marker.getTitle()); /// ЭТО НАДО ПЕРЕПИСАТЬ ПОД СНИППЕТ И КАК_ТО ПОЛУЧШЕ СДЕЛАТЬ
                                            tv.setText(input.getText().toString());
                                            marker.showInfoWindow();

                                        }
                                    })
                            .setNegativeButton("Отменить",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,int id) {
                                            dialog.cancel();
                                        }
                                    });
                    AlertDialog inputDialog = aDialogBuilder.create();
                    inputDialog.show();
                }
            });
            map.setOnMarkerClickListener(new OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(@NonNull Marker marker) {
                    Log.i("Zaebal!!!", "NAZHAL NA MARKER PITUH!");
                    marker.showInfoWindow();
                    TextView textViewH = llBottomSheet.findViewById(R.id.marker_header);

                    textViewH.setText(marker.getTitle());

                    TextView textViewD = llBottomSheet.findViewById(R.id.marker_description);
                    if (marker.getSnippet() !=null)
                        textViewD.setText(marker.getSnippet());
                    else
                        textViewD.setText("Описание маркера");

                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    llBottomSheet.findViewById(R.id.marker_delete_button).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            marker.setVisible(false);
                            deletemarkerDB(marker);
                        }
                    });


                    return false;
                }
            });


            map.setOnInfoWindowCloseListener(new OnInfoWindowCloseListener() {
                @Override
                public void onInfoWindowClose(@NonNull Marker marker) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                }
            });

            map.setOnMarkerDragListener(new OnMarkerDragListener() {
                @Override
                public void onMarkerDragStart(@NonNull Marker marker) {

                }

                @Override
                public void onMarkerDrag(@NonNull Marker marker) {

                }

                @Override
                public void onMarkerDragEnd(@NonNull Marker marker) {
                    dragmarkerDB(marker);
                }
            });


            // SHOW MARKERS FROM DATABASE
            getmarkerDB(map);

            map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

                @Override
                // Return null here, so that getInfoContents() is called next.
                public View getInfoWindow(Marker arg0) {
                    return null;
                }

                @Override
                public View getInfoContents(Marker marker) {
                    // Inflate the layouts for the info window, title and snippet.
                    View infoWindow = getLayoutInflater().inflate(R.layout.custom_info_contents,
                            (FrameLayout) getView().findViewById(R.id.map), false);

                    TextView title = infoWindow.findViewById(R.id.title);
                    title.setText(marker.getTitle());

                    TextView snippet = infoWindow.findViewById(R.id.snippet);
                    snippet.setText(marker.getSnippet());

                    return infoWindow;
                }
            });
            // [END map_current_place_set_info_window_adapter]

            // Prompt the user for permission.
            getLocalLocationPermission();
            // [END_EXCLUDE]

            // Turn on the My Location layer and the related control on the map.
            updateLocationUI(map);

            // Get the current location of the device and set the position of the map.
            getDeviceLocation(map);

            //getmarkerDB();
        }


    };

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


    public void markItself(GoogleMap map){
        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(getActivity(), new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            lastKnownLocation = task.getResult();
                            LatLng current = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                            if (lastKnownLocation != null) {
                                float color = (float)Math.random()*100;
                                LocalDateTime currentDateTime = LocalDateTime.now();
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
                                String formattedDateTime = currentDateTime.format(formatter);
                                marker_counter++;
                                int a = LocalDateTime.now().hashCode();
                                map.addMarker(new MarkerOptions()
                                        .position(current)
                                        .title("Я был тут #"+marker_counter)
                                        .snippet(String.valueOf(a))
                                        .draggable(true)
                                        .icon(BitmapDescriptorFactory.defaultMarker(color)));
                                putmarkerDB(current.latitude, current.longitude, "Я был тут #"+marker_counter, String.valueOf(a), true, color); //ТОЖЕ ПЕРЕПИСАТЬ НАДО БЫ
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









    // [END maps_current_place_update_location_ui]

    private void putmarkerDB(double latitude, double longitude, String title, String snippet, boolean draggable, float color){
        ContentValues dataput = new ContentValues();
        dataput.put("latitude", latitude);
        dataput.put("longitude", longitude);
        dataput.put("title", title);
        dataput.put("snippet", snippet);
        Log.i("I am trying to put snippet", snippet);
        if (draggable)
            dataput.put("drag", 1);
        else
            dataput.put("drag", 0);
        dataput.put("color", color);

        database.insert("markers", null, dataput);
    }

    private void getmarkerDB(GoogleMap map){
        Cursor cursor = database.rawQuery("SELECT * FROM markers", null);
        while(cursor.moveToNext()){
            marker_counter++;
            double latitude = cursor.getDouble(0);
            double longitude = cursor.getDouble(1);
            String title = cursor.getString(2);
            String snippet = cursor.getString(3);
            int drag = cursor.getInt(4);
            float color = cursor.getFloat(5);

            boolean draggable = drag == 1;
            Log.i("WTF?????????", title);
            LatLng position = new LatLng(latitude, longitude);

            map.addMarker(new MarkerOptions()
                    .position(position)
                    .title(title)
                    .snippet(snippet)
                    .draggable(draggable)
                    .icon(BitmapDescriptorFactory.defaultMarker(color)));
        }
        cursor.close();
    }

    private void renamemarkerDB(String snippet, String title_new){
        ContentValues cv = new ContentValues();
        cv.put("title", title_new);
        database.update("markers", cv, "snippet = "+snippet, null);
    }
    private void dragmarkerDB(Marker marker){
        ContentValues cv = new ContentValues();
        cv.put("latitude", marker.getPosition().latitude);
        cv.put("longitude", marker.getPosition().longitude);
        database.update("markers", cv, "snippet = "+marker.getSnippet(), null);
    }

    private void deletemarkerDB(Marker marker){
        Log.i("deleting",""+database.delete("markers", "snippet="+marker.getSnippet(), null));
    }




    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void getLocalLocationPermission(){
        ((MainActivity)getActivity()).getLocationPermission();
        locationPermissionGranted =  ((MainActivity)getActivity()).getLocationPermissionGranted();
    }
}