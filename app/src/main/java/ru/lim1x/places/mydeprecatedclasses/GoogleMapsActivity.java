package ru.lim1x.places.mydeprecatedclasses;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import ru.lim1x.places.R;
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
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

/**
 * НЕОБХОДИМО СДЕЛАТЬ УДАЛЕНИЕ МЕТОК = ready
 * НЕОБХОДИМО СДЕЛАТЬ ХРАНЕНИЕ ДАННЫХ = ready
 * НЕОБХОДИМА АВТОРИЗАЦИЯ
 * НЕОБХОДИМО ДЕЛИТЬСЯ МЕСТАМИ
 * НЕОБХОДИМО СДЕЛАТЬ КАК-ТО ОТМЕЧАНИЕ ПОСЕЩЕННЫХ МЕСТ
 */
@Deprecated
public class GoogleMapsActivity extends AppCompatActivity
        implements OnInfoWindowCloseListener, OnMarkerClickListener, OnMarkerDragListener,
                    OnInfoWindowClickListener, OnMapLongClickListener, OnMapReadyCallback {

    private LinearLayout llBottomSheet;
    private BottomSheetBehavior bottomSheetBehavior;
    private int marker_counter = 0;
    SQLiteDatabase database;

    private static final String TAG = GoogleMapsActivity.class.getSimpleName();
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

    // [START maps_current_place_on_create]
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = openOrCreateDatabase("myplacesx.db", MODE_PRIVATE, null);
        database.execSQL("CREATE TABLE IF NOT EXISTS markers (latitude REAL, longitude REAL, title TEXT, snippet TEXT, drag INT, color REAL)");

        //



        // [START_EXCLUDE silent]
        // [START maps_current_place_on_create_save_instance_state]
        // Retrieve location and camera position from saved instance state.
        if (savedInstanceState != null) {
            lastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            cameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }
        // [END maps_current_place_on_create_save_instance_state]
        // [END_EXCLUDE]

        // Retrieve the content view that renders the map.

        setContentView(R.layout.fragment_places_google);

        llBottomSheet = (LinearLayout) findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(llBottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        // [START_EXCLUDE silent]
        // Construct a PlacesClient


        Places.initialize(getApplicationContext(), "AIzaSyCd41NcKGeylMpBOrGn1J8wh8mp3YkA-MA");
        placesClient = Places.createClient(this);

        // Construct a FusedLocationProviderClient.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Build the map.
        // [START maps_current_place_map_fragment]
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.google_map);
        mapFragment.getMapAsync(this);

        // [END maps_current_place_map_fragment]
        // [END_EXCLUDE]


    }
    // [END maps_current_place_on_create]

    /**
     * Saves the state of the map when the activity is paused.
     */
    // [START maps_current_place_on_save_instance_state]
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (map != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, map.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, lastKnownLocation);
        }
        super.onSaveInstanceState(outState);
    }
    // [END maps_current_place_on_save_instance_state]

    /**
     * Sets up the options menu.
     * @param menu The options menu.
     * @return Boolean.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.current_place_menu, menu);
        return true;
    }

    /**
     * Handles a click on the menu option to get a place.
     * @param item The menu item to handle.
     * @return Boolean.
     */
    // [START maps_current_place_on_options_item_selected]
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.option_get_place) {
            markItself();
            //showCurrentPlace();
        }
        return true;
    }
    // [END maps_current_place_on_options_item_selected]

    /**
     * Manipulates the map when it's available.
     * This callback is triggered when the map is ready to be used.
     */
    // [START maps_current_place_on_map_ready]
    @Override
    public void onMapReady(GoogleMap map) {
        this.map = map;
        this.map.setOnMapLongClickListener(this);
        this.map.setOnInfoWindowClickListener(this);
        this.map.setOnMarkerClickListener(this);
        this.map.setOnInfoWindowCloseListener(this);
        this.map.setOnMarkerDragListener(this);

        // SHOW MARKERS FROM DATABASE

        // [START_EXCLUDE]
        // [START map_current_place_set_info_window_adapter]
        // Use a custom info window adapter to handle multiple lines of text in the
        // info window contents.
        this.map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            // Return null here, so that getInfoContents() is called next.
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                // Inflate the layouts for the info window, title and snippet.
                View infoWindow = getLayoutInflater().inflate(R.layout.custom_info_contents,
                        (FrameLayout) findViewById(R.id.google_map), false);

                TextView title = infoWindow.findViewById(R.id.title);
                title.setText(marker.getTitle());

                TextView snippet = infoWindow.findViewById(R.id.snippet);
                snippet.setText(marker.getSnippet());

                return infoWindow;
            }
        });
        // [END map_current_place_set_info_window_adapter]

        // Prompt the user for permission.
        getLocationPermission();
        // [END_EXCLUDE]

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();

        getmarkerDB();
    }
    // [END maps_current_place_on_map_ready]

    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    // [START maps_current_place_get_device_location]
    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
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
    // [END maps_current_place_get_device_location]

    /**
     * Prompts the user for permission to use the device location.
     */
    // [START maps_current_place_location_permission]
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
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
        updateLocationUI();
    }
    // [END maps_current_place_on_request_permissions_result]




    private void showCurrentPlace() {
        if (map == null) {
            return;
        }

        if (locationPermissionGranted) {
            // Use fields to define the data types to return.
            List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME, Place.Field.ADDRESS,
                    Place.Field.LAT_LNG);

            // Use the builder to create a FindCurrentPlaceRequest.
            FindCurrentPlaceRequest request =
                    FindCurrentPlaceRequest.newInstance(placeFields);

            // Get the likely places - that is, the businesses and other points of interest that
            // are the best match for the device's current location.
            @SuppressWarnings("MissingPermission") final
            Task<FindCurrentPlaceResponse> placeResult =
                    placesClient.findCurrentPlace(request);
            placeResult.addOnCompleteListener (new OnCompleteListener<FindCurrentPlaceResponse>() {
                @Override
                public void onComplete(@NonNull Task<FindCurrentPlaceResponse> task) {
                    if (task.isSuccessful() && task.getResult() != null) {
                        FindCurrentPlaceResponse likelyPlaces = task.getResult();

                        // Set the count, handling cases where less than 5 entries are returned.
                        int count;
                        if (likelyPlaces.getPlaceLikelihoods().size() < M_MAX_ENTRIES) {
                            count = likelyPlaces.getPlaceLikelihoods().size();
                        } else {
                            count = M_MAX_ENTRIES;
                        }

                        int i = 0;
                        likelyPlaceNames = new String[count];
                        likelyPlaceAddresses = new String[count];
                        likelyPlaceAttributions = new List[count];
                        likelyPlaceLatLngs = new LatLng[count];

                        for (PlaceLikelihood placeLikelihood : likelyPlaces.getPlaceLikelihoods()) {
                            // Build a list of likely places to show the user.
                            likelyPlaceNames[i] = placeLikelihood.getPlace().getName();
                            likelyPlaceAddresses[i] = placeLikelihood.getPlace().getAddress();
                            likelyPlaceAttributions[i] = placeLikelihood.getPlace()
                                    .getAttributions();
                            likelyPlaceLatLngs[i] = placeLikelihood.getPlace().getLatLng();

                            i++;
                            if (i > (count - 1)) {
                                break;
                            }
                        }

                        // Show a dialog offering the user the list of likely places, and add a
                        // marker at the selected place.
                        GoogleMapsActivity.this.openPlacesDialog();
                    }
                    else {
                        Log.e(TAG, "Exception: %s", task.getException());
                    }
                }
            });
        } else {
            // The user has not granted permission.
            Log.i(TAG, "The user did not grant location permission.");

            // Add a default marker, because the user hasn't selected a place.
            map.addMarker(new MarkerOptions()
                    .title(getString(R.string.default_info_title))
                    .position(defaultLocation)
                    .snippet(getString(R.string.default_info_snippet)));

            // Prompt the user for permission.
            getLocationPermission();
        }
    }




    private void openPlacesDialog() {
        // Ask the user to choose the place where they are now.
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // The "which" argument contains the position of the selected item.
                LatLng markerLatLng = likelyPlaceLatLngs[which];
                String markerSnippet = likelyPlaceAddresses[which];
                if (likelyPlaceAttributions[which] != null) {
                    markerSnippet = markerSnippet + "\n" + likelyPlaceAttributions[which];
                }

                // Add a marker for the selected place, with an info window
                // showing information about that place.
                map.addMarker(new MarkerOptions()
                        .title(likelyPlaceNames[which])
                        .position(markerLatLng)
                        .snippet(markerSnippet));

                // Position the map's camera at the location of the marker.
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(markerLatLng,
                        DEFAULT_ZOOM));
            }
        };

        // Display the dialog.
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.pick_place)
                .setItems(likelyPlaceNames, listener)
                .show();
    }
    // [END maps_current_place_open_places_dialog]

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    // [START maps_current_place_update_location_ui]

    private void updateLocationUI() {
        if (map == null) {
            return;
        }
        try {
            if (locationPermissionGranted) {
                map.setMyLocationEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                map.setMyLocationEnabled(false);
                map.getUiSettings().setMyLocationButtonEnabled(false);
                lastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    public void markItself(){
        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
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

    @Override
    public void onInfoWindowClick(@NonNull Marker marker) {
        Log.i("Zaebal", "YA TUT!!!!!!!!!!!!");
        LayoutInflater li = LayoutInflater.from(this);
        View inputDialogView = li.inflate(R.layout.input_dialog, null);
        AlertDialog.Builder aDialogBuilder = new AlertDialog.Builder(this);
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

    @Override
    public void onInfoWindowClose(@NonNull Marker marker) {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
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

    private void getmarkerDB(){
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
    public void onMarkerDragStart(@NonNull Marker marker) {

    }

    @Override
    public void onMarkerDrag(@NonNull Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(@NonNull Marker marker) {
        dragmarkerDB(marker);
    }

    @Override
    protected void onStop() {
        // call the superclass method first
        super.onStop();
        database.close();
    }
}
