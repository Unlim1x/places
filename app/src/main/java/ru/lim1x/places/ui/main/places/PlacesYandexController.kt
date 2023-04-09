package ru.lim1x.places.ui.main.places

import android.content.Context
import android.content.SharedPreferences
import android.graphics.PointF
import android.util.Log
import android.view.View
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKit
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.layers.ObjectEvent
import com.yandex.mapkit.location.FilteringMode
import com.yandex.mapkit.location.LocationListener
import com.yandex.mapkit.location.LocationManager
import com.yandex.mapkit.location.LocationStatus
import com.yandex.mapkit.map.*
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.user_location.UserLocationLayer
import com.yandex.mapkit.user_location.UserLocationObjectListener
import com.yandex.mapkit.user_location.UserLocationView
import com.yandex.runtime.image.ImageProvider
import ru.lim1x.places.R
import ru.lim1x.places.ui.YandexStyles

class PlacesYandexController constructor(val view: PlacesInterface): UserLocationObjectListener,
     InputListener, CameraListener, MapObjectTapListener {

    lateinit var mSharedPreferences : SharedPreferences
    lateinit var mapView: MapView
    private var yandex : Boolean = false
    private var isMapInitialised = false
    private val targetLocation: Point = Point(59.945933, 30.320045)
    private var myLocation:Point? = null
    var wascentered = false
    private var locationManager: LocationManager? = null
    private var myLocationListener: LocationListener? = null
    private val desiredAccuracy = 5.0
    private val minimalTime: Long = 1000
    private val minimalDistance = 1.0
    private val useInBackground = false
    private var userLocationLayer: UserLocationLayer? = null
    lateinit var mapKit: MapKit


    private fun subscribeForLocationUpdates() {
        if (locationManager != null && myLocationListener != null) {
            locationManager!!.subscribeForLocationUpdates(
                desiredAccuracy,
                minimalTime,
                minimalDistance,
                useInBackground,
                FilteringMode.OFF,
                myLocationListener!!
            )
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
            wascentered = false
        }
    }


    fun moveCamera(position: Point){
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
        return true
    }

    override fun onMapTap(p0: Map, p1: Point) {
        mapView.getMap().deselectGeoObject();
    }

    override fun onMapLongTap(map: Map, p1: Point) {
        mapView.map.mapObjects.addPlacemark(p1, ImageProvider.fromResource(view.context(), R.drawable.marker_very_small))
        mapView.map.mapObjects.addTapListener(this)
        //TODO: Исправить отрисовывание маркера не по центру нажатия, а с якорем на центре.
        //TODO: сохранить маркер в БД
    }

    fun initYandexMap(): Map{
        initSharedPreferences()
        if (!isMapInitialised) {
            MapKitFactory.initialize(view.context())
            mapKit = MapKitFactory.getInstance()
            mapView.map.move(CameraPosition(targetLocation, 10f, 0f,0f))
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

            myLocationListener = object : LocationListener {
                override fun onLocationUpdated(p0: com.yandex.mapkit.location.Location) {
                    myLocation = p0.position
                    if (!wascentered) {
                        moveCamera(myLocation!!)
                        wascentered = true
                    }

                }

                override fun onLocationStatusUpdated(p0: LocationStatus) {
                    if (p0 == LocationStatus.NOT_AVAILABLE)
                        Log.e("oops", "oops")

                }
            }



        }
        return mapView.map
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

    fun initSharedPreferences(){
        mSharedPreferences = view.activity()!!.getSharedPreferences("s1paraX", Context.MODE_PRIVATE)
    }

    fun putMarkerDB(point:Point, name:String, parameter:Boolean){
        //TODO: Сохранение маркера в базе данных с помощью ReactiveX.
    }

    fun getMarkerDB(){
        //TODO: Чтение маркеров из БД и отрисовывание на карте.
    }
}