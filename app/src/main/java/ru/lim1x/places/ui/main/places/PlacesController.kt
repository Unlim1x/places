package ru.lim1x.places.ui.main.places

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.PointF
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
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKit
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.layers.GeoObjectTapEvent
import com.yandex.mapkit.layers.GeoObjectTapListener
import com.yandex.mapkit.layers.ObjectEvent
import com.yandex.mapkit.location.FilteringMode
import com.yandex.mapkit.location.LocationListener
import com.yandex.mapkit.location.LocationManager
import com.yandex.mapkit.location.LocationStatus
import com.yandex.mapkit.map.*
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.user_location.UserLocationLayer
import com.yandex.mapkit.user_location.UserLocationObjectListener
import com.yandex.mapkit.user_location.UserLocationView
import com.yandex.runtime.image.ImageProvider
import ru.lim1x.places.R
import ru.lim1x.places.activities.MainActivity
import ru.lim1x.places.room.App
import ru.lim1x.places.room.entities.Markers
import ru.lim1x.places.ui.YandexStyles
import java.time.LocalDateTime


class PlacesController constructor(val view: PlacesInterface) : UserLocationObjectListener,
    GeoObjectTapListener, InputListener , CameraListener, MapObjectTapListener{
    lateinit var mapView: MapView
    lateinit var mSharedPreferences : SharedPreferences
    var bundle : Bundle? = null
    private lateinit var ai : ApplicationInfo
    private var yandex : Boolean = false
    private var isMapInitialised = false
    private var locationPermissionGranted : Boolean? = false
    private val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    private lateinit var gmap : GoogleMap
    private var lastKnownLocation: Location? = null
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private val DEFAULT_ZOOM = 15
    private val defaultLocation = LatLng(59.938955, 30.315644)
    private val TARGET_LOCATION: Point = Point(59.945933, 30.320045)
    private var myLocation:Point? = null
    var was_centered = false
    private var locationManager: LocationManager? = null
    private var myLocationListener: LocationListener? = null
    private val DESIRED_ACCURACY = 5.0
    private val MINIMAL_TIME: Long = 1000
    private val MINIMAL_DISTANCE = 1.0
    private val USE_IN_BACKGROUND = false
    private var userLocationLayer: UserLocationLayer? = null
    lateinit var mapKit: MapKit
    private var marker_counter = 0
    private var database = App.getInstance().database
    private var markerDao = database.markerDao()
    private var iconIsSet:Boolean = false


    fun moveCamera(position:Point){
        mapView.map.move(
            CameraPosition(position, 14.0f, 0.0f, 0.0f),
            Animation(Animation.Type.SMOOTH, 2F),
            null
        )
    }

    override fun onCameraPositionChanged(
        p0: Map,
        p1: CameraPosition,
        p2: CameraUpdateReason,
        p3: Boolean
    ) {
        Log.e("c", "camera moved to ${p1.azimuth}")
    }

    override fun onObjectTap(geoObjectTapEvent: GeoObjectTapEvent): Boolean {
      //  TODO("ПЕРЕПИСАТЬ ВСЕ ВНИЗУ, ПОТОМУ ЧТО ЭТО КОСТЫЛЬ!!!!")
        val selectionMetadata: GeoObjectSelectionMetadata = geoObjectTapEvent
            .getGeoObject()
            .getMetadataContainer()
            .getItem(GeoObjectSelectionMetadata::class.java)
        view.bottomSheetBehavior()?.state = BottomSheetBehavior.STATE_COLLAPSED
        mapView.map.selectGeoObject(selectionMetadata.id, selectionMetadata.layerId)
        val textViewH: TextView? = view.llbottomSheet()?.findViewById<TextView>(R.id.marker_header)
        textViewH?.text = selectionMetadata.id
        val textViewD: TextView? = view.llbottomSheet()?.findViewById<TextView>(R.id.marker_description)
        textViewD?.text= "Описание маркера"
        view.bottomSheetBehavior()?.state = BottomSheetBehavior.STATE_COLLAPSED
        view.llbottomSheet()?.findViewById<View>(R.id.marker_delete_button)
            ?.setOnClickListener(
                View.OnClickListener {
                    //marker.isVisible = false
                    //deletemarkerDB(marker)
                })
        false



        return selectionMetadata != null
    }

    override fun onMapObjectTap(p0: MapObject, p1: Point): Boolean {

        view.bottomSheetBehavior()?.state = BottomSheetBehavior.STATE_COLLAPSED

        val textViewH: TextView? =
            view.llbottomSheet()?.findViewById<TextView>(R.id.marker_header)
        textViewH?.text = p1.latitude.toString().subSequence(0, 5)
        val textViewD: TextView? =
            view.llbottomSheet()?.findViewById<TextView>(R.id.marker_description)
        textViewD?.text = "Описание маркера"
        view.bottomSheetBehavior()?.state = BottomSheetBehavior.STATE_COLLAPSED
        view.llbottomSheet()?.findViewById<View>(R.id.marker_delete_button)
            ?.setOnClickListener(
                View.OnClickListener {
                    //marker.isVisible = false
                    //deletemarkerDB(marker)
                })
        return false
    }

    override fun onMapTap(p0: Map, p1: Point) {
        mapView.getMap().deselectGeoObject();
    }

    override fun onMapLongTap(map: Map, p1: Point) {
        mapView.map.mapObjects.addPlacemark(p1, ImageProvider.fromResource(view.context(), R.drawable.marker_very_small))
        mapView.map.mapObjects.addTapListener(this)
    }

    fun initYandexMap(){
    initSharedPreferences()
    if (!isMapInitialised) {
        MapKitFactory.initialize(view.context())
        mapKit = MapKitFactory.getInstance()
        mapView.map.move(CameraPosition(TARGET_LOCATION, 10f, 0f,0f))
        mapView.getMap().addTapListener(this)
        mapView.getMap().addInputListener(this)
        mapView.getMap().addCameraListener(this)
        isMapInitialised = true
        locationManager = MapKitFactory.getInstance().createLocationManager()
        userLocationLayer = mapKit.createUserLocationLayer(mapView.mapWindow)
        userLocationLayer!!.isVisible = true
        userLocationLayer!!.isHeadingEnabled = true
        userLocationLayer!!.setObjectListener(this)



        if (mSharedPreferences.contains("map_style")) {
            val style: String = mSharedPreferences.getString("map_style", "")!!

            if (style == "grayscale") mapView.map.setMapStyle(YandexStyles.grayscale)
            else if(style == "night") mapView.map.setMapStyle(YandexStyles.night)



        }

        myLocationListener = object : LocationListener{
            override fun onLocationUpdated(p0: com.yandex.mapkit.location.Location) {
                myLocation = p0.position
                if (!was_centered) {
                    moveCamera(myLocation!!)
                    was_centered = true
                }

            }

            override fun onLocationStatusUpdated(p0: LocationStatus) {
                if (p0 == LocationStatus.NOT_AVAILABLE)
                    Log.e("oops", "oops")

            }
        }



    }
}

    override fun onObjectAdded(userLocationView: UserLocationView) {
//        userLocationLayer!!.setAnchor(
//            PointF((mapView.width * 0.5).toFloat(), (mapView.height * 0.5).toFloat()),
//            PointF((mapView.width * 0.5).toFloat(), (mapView.height * 0.83).toFloat())
//        )

        userLocationView.arrow.setIcon(

            ImageProvider.fromResource(
                view.context(), R.drawable.me_icon_small
            )
        )
//
//        val pinIcon = userLocationView.pin.useCompositeIcon()
//        if (!iconIsSet) {
//            userLocationView.accuracyCircle.fillColor = R.color.back_icon_yandex
//            pinIcon.setIcon(
//                "icon",
//                ImageProvider.fromResource(view.context(), R.drawable.me_icon_small),
//                IconStyle()
//                    .setRotationType(RotationType.ROTATE)
//                    .setZIndex(0f)
//                    .setScale(1f)
//            )
//            iconIsSet = true
//        }




    }



    override fun onObjectRemoved(p0: UserLocationView) {
        Log.e("", "removed icon?")
    }

    override fun onObjectUpdated(p0: UserLocationView, p1: ObjectEvent) {
        Log.e("", "update ${p1.toString()}")
        val pinIcon = p0.pin.useCompositeIcon()

        pinIcon.setIcon(
            "icon",
            ImageProvider.fromResource(view.context(), R.drawable.me_icon_small),
            IconStyle().setAnchor(PointF((mapView.width * 0.5).toFloat(), (mapView.height * 0.5).toFloat()))
                .setRotationType(RotationType.NO_ROTATION)
                .setZIndex(0f)
                .setScale(1f)
        )
    }

    fun initGoogleMap(){
        val mapFragment = view.childFragmentManager()?.findFragmentById(R.id.google_map) as SupportMapFragment
        mapFragment.getMapAsync(view.onMapReadyCallback())

        // Construct a FusedLocationProviderClient.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(view.context()!!)
    }

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
            map.addMarker(
                MarkerOptions()
                    .position(currentPressedPosition)
                    .title("Маркер #$marker_counter")
                    .snippet(a.toString())
                    .draggable(true)
                    .icon(BitmapDescriptorFactory.defaultMarker(color))
            )
            putmarkerDB(
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
                    renamemarkerDB(
                        dv?.text.toString(),
                        marker.title!!
                    ) /// ЭТО НАДО ПЕРЕПИСАТЬ ПОД СНИППЕТ И КАК_ТО ПОЛУЧШЕ СДЕЛАТЬ
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
            Log.i("Zaebal!!!", "NAZHAL NA MARKER PITUH!")
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
                        //deletemarkerDB(marker)
                    })
            false
        }


        map.setOnInfoWindowCloseListener { view.bottomSheetBehavior()?.setState(BottomSheetBehavior.STATE_HIDDEN) }

        map.setOnMarkerDragListener(object : OnMarkerDragListener {
            override fun onMarkerDragStart(marker: Marker) {}
            override fun onMarkerDrag(marker: Marker) {}
            override fun onMarkerDragEnd(marker: Marker) {
                //dragmarkerDB(marker)
            }
        })


        // SHOW MARKERS FROM DATABASE


        // SHOW MARKERS FROM DATABASE
        getmarkerDB(map)


        // [END_EXCLUDE]
        getLocationPermission()
        // Turn on the My Location layer and the related control on the map.
        updateLocationUI(map)

        // Get the current location of the device and set the position of the map.
        getDeviceLocation(map)
    }

    // [END maps_current_place_get_device_location]
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
    fun setMapKit(){
        val mapkit: String = mSharedPreferences.getString("mapkit", "")!!
        yandex = mapkit.isNotEmpty() && mapkit == "yandex_mapkit";
    }


    fun onStart(){
        if (yandex) {
            mapView.onStart()
            subscribeForLocationUpdates()
            MapKitFactory.getInstance().onStart()
        }
    }
    fun  onStop(){
        if (yandex) {
            mapView.onStop()
            locationManager?.unsubscribe(myLocationListener!!)
            MapKitFactory.getInstance().onStop()
            was_centered = false
        }
    }

    private fun subscribeForLocationUpdates() {
        if (locationManager != null && myLocationListener != null) {
            locationManager!!.subscribeForLocationUpdates(
                DESIRED_ACCURACY,
                MINIMAL_TIME,
                MINIMAL_DISTANCE,
                USE_IN_BACKGROUND,
                FilteringMode.OFF,
                myLocationListener!!
            )
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
    private fun putmarkerDB(
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
        return
    }

    private fun getmarkerDB(map: GoogleMap) {
        val markers: List<Markers> = markerDao?.all as List<Markers>
        val iterator = markers.iterator()
        while (iterator.hasNext()) {
            val marker = iterator.next()
            val position = LatLng(marker.latitude, marker.longitude)
            val draggable = marker.drag == 1
            map.addMarker(
                MarkerOptions()
                    .position(position)
                    .title(marker.title)
                    .snippet(marker.snippet)
                    .draggable(draggable)
                    .icon(BitmapDescriptorFactory.defaultMarker(marker.color))
            )
        }
        return
    }
    private fun renamemarkerDB(snippet: String, title_new: String) {
        val runnable = Runnable {
            val marker = markerDao!!.getBySnippet(snippet)
            marker.title = title_new
            markerDao.update(marker)
        }
        val thread = Thread(runnable)
        thread.start()
    }

    private fun dragmarkerDB(marker: Marker) {
        val runnable = Runnable {
            val markerDB = markerDao!!.getBySnippet(marker.snippet)
            markerDB.latitude = marker.position.latitude
            markerDB.longitude = marker.position.longitude
            markerDao.update(markerDB)
        }
        val thread = Thread(runnable)
        thread.start()
    }

    private fun deletemarkerDB(marker: Marker) {
        val runnable = Runnable {
            val markerDB = markerDao!!.getBySnippet(marker.snippet)
            markerDao.delete(markerDB)
        }
        val thread = Thread(runnable)
        thread.start()
    }



}