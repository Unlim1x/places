package com.example.places.ui.main;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.places.room.App;
import com.example.places.activities.MainActivity;
import com.example.places.activities.MapsActivity;
import com.example.places.room.daos.MarkerDao;
import com.example.places.room.database.PlacesDatabase;
import com.example.places.room.entities.Markers;
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
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.List;


/**
 * A placeholder fragment containing a simple view.
 */
public class PlacesFragment extends Fragment {

    Context mContext;
    private PlacesDatabase database;
    private MarkerDao markerDao;

    private static final String ARG_SECTION_NUMBER = "section_number";
    private LinearLayout llBottomSheet;
    private BottomSheetBehavior bottomSheetBehavior;
    private LinearLayout zoom;
    private int marker_counter = 0;
    private static final String TAG = MapsActivity.class.getSimpleName();
    public GoogleMap map_observer;
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
    SharedPreferences mSettings;
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

    private float x = 0;
    private float y = 0;


    private FragmentPlacesBinding binding;
    public PlacesFragment(){}
    public PlacesFragment(Context context){
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
        mSettings = getActivity().getSharedPreferences("s1paraX", Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mContext = getContext();
        View root;
        binding = FragmentPlacesBinding.inflate(inflater, container, false);
        root = binding.getRoot();
        database = App.getInstance().getDatabase();
        markerDao = database.markerDao();
        Places.initialize(mContext, "AIzaSyCd41NcKGeylMpBOrGn1J8wh8mp3YkA-MA");
        placesClient = Places.createClient(mContext);

        // Construct a FusedLocationProviderClient.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(mContext);
        return root;
        }

    @SuppressLint("ClickableViewAccessibility")
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
            if(mSettings.contains("map_style")){
                String style = mSettings.getString("map_style","");
                if (style.equals("grayscale"))
                    map.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.grayscale));
                if (style.equals("classic"))
                    map.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.standard));
                if (style.equals("night"))
                    map.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.night));
            }


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

            Markers markers = new Markers();
            markers.latitude = latitude;
            markers.longitude = longitude;
            markers.title = title;
            markers.snippet = snippet;
            markers.drag = (draggable) ? 1 : 0;
            markers.color = color;
            LocalDateTime current = LocalDateTime.now();
            markers.date = current.toString();
            markerDao.insert(markers);
            return;

    }

    private void getmarkerDB(GoogleMap map){


            List<Markers> markers = markerDao.getAll();
            Iterator<Markers> iterator = markers.iterator();
            while(iterator.hasNext()){
                Markers marker = iterator.next();
                LatLng position = new LatLng(marker.latitude, marker.longitude);
                boolean draggable = (marker.drag == 1) ? true: false;
                map.addMarker(new MarkerOptions()
                        .position(position)
                        .title(marker.title)
                        .snippet(marker.snippet)
                        .draggable(draggable)
                        .icon(BitmapDescriptorFactory.defaultMarker(marker.color)));
            }
            return;







    }

    private void renamemarkerDB(String snippet, String title_new){
        Runnable runnable = () -> {
            Markers marker = markerDao.getBySnippet(snippet);
            marker.title = title_new;
            markerDao.update(marker);
            return;
        };
        Thread thread = new Thread(runnable);
        thread.start();

    }
    private void dragmarkerDB(Marker marker){
        Runnable runnable = () -> {
            Markers markerDB = markerDao.getBySnippet(marker.getSnippet());
            markerDB.latitude = marker.getPosition().latitude;
            markerDB.longitude = marker.getPosition().longitude;
            markerDao.update(markerDB);
            return;
        };
        Thread thread = new Thread(runnable);
        thread.start();

    }

    private void deletemarkerDB(Marker marker){
        Runnable runnable = () -> {
            Markers markerDB = markerDao.getBySnippet(marker.getSnippet());
            markerDao.delete(markerDB);
            return;
        };
        Thread thread = new Thread(runnable);
        thread.start();
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