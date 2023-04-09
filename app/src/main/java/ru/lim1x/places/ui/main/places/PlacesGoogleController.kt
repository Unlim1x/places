package ru.lim1x.places.ui.main.places

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*

import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.*
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomsheet.BottomSheetBehavior

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.CompletableObserver
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers

import ru.lim1x.places.R
import ru.lim1x.places.activities.MainActivity
import ru.lim1x.places.room.App
import ru.lim1x.places.room.entities.Markers
import java.time.LocalDateTime


class PlacesGoogleController constructor(val view: PlacesInterface) {

    lateinit var mSharedPreferences : SharedPreferences
    var bundle : Bundle? = null

    private var locationPermissionGranted : Boolean? = false
    private val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    private lateinit var gmap : GoogleMap
    private var lastKnownLocation: Location? = null
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private val DEFAULT_ZOOM = 15
    private val defaultLocation = LatLng(59.938955, 30.315644)

    private var marker_counter = 0
    private val database = App.getInstance().database
    private val markerDao = database.markerDao()

    private val compositeDisposable = CompositeDisposable()


    fun initGoogleMap(){
        val mapFragment = view.childFragmentManager()?.findFragmentById(R.id.google_map) as SupportMapFragment
        mapFragment.getMapAsync(view.onMapReadyCallback())

        // Construct a FusedLocationProviderClient.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(view.context()!!)
    }

    @SuppressLint("CutPasteId")
    fun googleMapSetting(map : GoogleMap) {
        gmap = map
        if (mSharedPreferences.contains("map_style")) {
            val style: String = mSharedPreferences.getString("map_style", "")!!
            if (style == "grayscale") map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    view.context()!!,
                    R.raw.grayscale
                )
            )
            if (style == "classic") map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    view.context()!!,
                    R.raw.standard
                )
            )
            if (style == "night") map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    view.context()!!,
                    R.raw.night
                )
            )
        }
        map.setOnMapLongClickListener { currentPressedPosition ->
            val a = LocalDateTime.now().hashCode()
            val color = Math.random().toFloat() * 359
            marker_counter++
            putmarker(
                currentPressedPosition.latitude, currentPressedPosition.longitude,
                "Маркер #$marker_counter", a.toString(), true, color
            )
        }

        map.setOnInfoWindowClickListener { marker ->
            Log.i("Z", "YA TUT!!!!!!!!!!!!")
            val li = LayoutInflater.from(view.context())
            val inputDialogView = li.inflate(R.layout.input_dialog, null)
            val aDialogBuilder = AlertDialog.Builder(view.context()!!)
            aDialogBuilder.setView(inputDialogView)
            aDialogBuilder
                .setCancelable(false)
                .setPositiveButton(
                    "Применить"
                ) { dialog, id ->
                    val input = inputDialogView.findViewById<EditText>(R.id.input_text)
                    //Вводим текст и отображаем в строке ввода на основном экране:
                    marker.title = input.text.toString()
                    val tv: TextView? =view.llbottomSheet()?.findViewById<TextView>(R.id.marker_header)
                    val dv: TextView? = view.llbottomSheet()?.findViewById<TextView>(R.id.marker_description)
                    renamemarker(
                        dv?.text.toString(),
                        marker.title!!
                    ) /// ЭТО НАДО ПЕРЕПИСАТЬ
                    tv?.text = input.text.toString()
                    marker.showInfoWindow()
                }
                .setNegativeButton(
                    "Отменить"
                ) { dialog, id -> dialog.cancel() }
            val inputDialog = aDialogBuilder.create()
            inputDialog.show()
        }
        map.setOnMarkerClickListener { marker ->
            Log.i("Z!!!", "NAZHAL NA MARKER!")
            marker.showInfoWindow()
            val textViewH: TextView? = view.llbottomSheet()?.findViewById<TextView>(R.id.marker_header)
            textViewH?.text = marker.title
            val textViewD: TextView? = view.llbottomSheet()?.findViewById<TextView>(R.id.marker_description)
            if (marker.snippet != null) textViewD?.text = marker.snippet else textViewD?.text =
                "Описание маркера"
            view.bottomSheetBehavior()?.state = BottomSheetBehavior.STATE_COLLAPSED
            view.llbottomSheet()?.findViewById<View>(R.id.marker_delete_button)
                ?.setOnClickListener(
                    View.OnClickListener {
                        marker.isVisible = false
                        //TODO: удалить маркер
                        //deletemarker(marker)
                    })
            false
        }


        map.setOnInfoWindowCloseListener { view.bottomSheetBehavior()?.setState(BottomSheetBehavior.STATE_HIDDEN) }

        map.setOnMarkerDragListener(object : OnMarkerDragListener {
            override fun onMarkerDragStart(marker: Marker) {}
            override fun onMarkerDrag(marker: Marker) {}
            override fun onMarkerDragEnd(marker: Marker) {
                //dragmarker(marker)
            }
        })

        // SHOW MARKERS FROM DATABASE
        getMarker(map)


        // [END_EXCLUDE]
        getLocationPermission()
        // Turn on the My Location layer and the related control on the map.
        updateLocationUI(map)

        // Get the current location of the device and set the position of the map.
        getDeviceLocation(map)
    }


    private fun getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(
                view.context()!!,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionGranted = true
        } else {
            ActivityCompat.requestPermissions(
                view.activity()!!, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
        }
    }
fun initSharedPreferences(){
    mSharedPreferences = view.activity()!!.getSharedPreferences("s1paraX", Context.MODE_PRIVATE)
}

    private fun updateLocationUI(map: GoogleMap?) {
        if (map == null) {
            return
        }
        try {
            if (locationPermissionGranted!!) {
                map.isMyLocationEnabled = true
                val locationButton = (view.activity()!!.findViewById<View>("1".toInt())
                    .parent as View).findViewById<View>("2".toInt())
                val rlp = locationButton.layoutParams as RelativeLayout.LayoutParams
                // position on right bottom
                rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0)
                rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
                rlp.setMargins(0, 0, 30, 60)
                map.uiSettings.isMyLocationButtonEnabled = true
            } else {
                map.isMyLocationEnabled = false
                map.uiSettings.isMyLocationButtonEnabled = false
                lastKnownLocation = null
                getLocationPermission()
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message!!)
        }
    }



    private fun getDeviceLocation(map: GoogleMap?) {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermissionGranted!! && map != null) {
                val locationResult: Task<Location> = fusedLocationProviderClient?.lastLocation!!
                locationResult.addOnCompleteListener(
                    (view.activity() as MainActivity?)!!
                ) { task ->
                    if (task.isSuccessful) {
                        // Set the map's camera position to the current location of the device.
                        lastKnownLocation = task.result
                        if (lastKnownLocation != null) {
                            map.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(
                                        lastKnownLocation!!.latitude,
                                        lastKnownLocation!!.longitude
                                    ), DEFAULT_ZOOM.toFloat()
                                )
                            )
                        }
                    } else {
                        map.moveCamera(
                            CameraUpdateFactory
                                .newLatLngZoom(
                                    defaultLocation,
                                    DEFAULT_ZOOM.toFloat()
                                )
                        )
                        map.uiSettings.isMyLocationButtonEnabled = false
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    // [END maps_current_place_update_location_ui]
    private fun putmarker(
        latitude: Double,
        longitude: Double,
        title: String,
        snippet: String,
        draggable: Boolean,
        color: Float
    ) {
        val markers = Markers()
        markers.latitude = latitude
        markers.longitude = longitude
        markers.title = title
        markers.snippet = snippet
        markers.drag = if (draggable) 1 else 0
        markers.color = color
        val current = LocalDateTime.now()
        markers.date = current.toString()
        markerDao?.insert(markers)
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribeOn(Schedulers.io())
            ?.subscribe(object: CompletableObserver{
                override fun onSubscribe(d: Disposable) {
                }
                override fun onComplete() {
                    gmap.addMarker(
                        MarkerOptions()
                            .position(LatLng(latitude, longitude))
                            .title(title)
                            .snippet(snippet)
                            .draggable(draggable)
                            .icon(BitmapDescriptorFactory.defaultMarker(color))
                    )
                }
                override fun onError(e: Throwable) {
                }
            })
    }

    private fun getMarker(map: GoogleMap) {

       val disposable =  markerDao?.all
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribeOn(Schedulers.io())
            ?.subscribe { m ->
                for (markers in m) {
                    map.addMarker(
                        MarkerOptions()
                            .position(LatLng(markers.latitude, markers.longitude))
                            .title(markers.title)
                            .snippet(markers.snippet)
                            .draggable(markers.drag == 1)
                            .icon(BitmapDescriptorFactory.defaultMarker(markers.color))
                    )
                }
            }
        if (disposable != null) {
            compositeDisposable.add(disposable)
        }
    }
    private fun renamemarker(snippet: String, title_new: String) {
        //TODO: Используя ReactiveX.
    }

    private fun dragmarker(marker: Marker) {
        //TODO: Используя ReactiveX.
    }

    private fun deletemarker(marker: Marker) {
        //TODO: Используя ReactiveX.
    }

    fun onDestroy(){
        compositeDisposable.dispose()
    }

}