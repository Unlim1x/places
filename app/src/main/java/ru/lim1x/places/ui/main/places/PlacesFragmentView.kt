package ru.lim1x.places.ui.main.places

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.yandex.mapkit.geometry.Point
import com.yandex.runtime.image.ImageProvider
import ru.lim1x.places.R
import ru.lim1x.places.databinding.FragmentPlacesGoogleBinding
import ru.lim1x.places.databinding.FragmentPlacesYandexBinding


class PlacesFragmentView : Fragment(), PlacesInterface, OnMapReadyCallback {
    private lateinit var yandexController: PlacesYandexController
    private lateinit var googleController: PlacesGoogleController
    private lateinit var mSharedPreferences: SharedPreferences
    private val mapkit by lazy (::retMapkit)
    private var llBottomSheet: LinearLayout? = null
    private var bottomSheetBehavior: BottomSheetBehavior<*>? = null

    private lateinit var yandexMap : com.yandex.mapkit.map.Map
    private lateinit var googleMap : GoogleMap

    fun retMapkit(): String {
        val shared = activity?.getSharedPreferences("s1paraX", Context.MODE_PRIVATE)
        return shared?.getString("mapkit", "")!!
    }


    override fun context(): Context? {
       return context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.e("head", "onCreateView CALLED")
        sharedPreferences()
         return when (mapkit) {
             "google_mapkit" -> {
                 Log.e("google 1", "111111 GOOOOOOOOGLE")
                 googleController = PlacesGoogleController(this)
                 FragmentPlacesGoogleBinding.inflate(inflater, container, false).root
             }
             "yandex_mapkit" -> {
                 Log.e("yandex 1", "111111 YAAAAAAAAANDEX")
                 yandexController = PlacesYandexController(this)
                 FragmentPlacesYandexBinding.inflate(inflater, container, false).root
             }
             else -> FragmentPlacesGoogleBinding.inflate(inflater, container, false).root
         }
    }

    override fun childFragmentManager(): FragmentManager {
        return childFragmentManager
    }

    override fun onMapReadyCallback(): OnMapReadyCallback {
        return this
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mSharedPreferences = sharedPreferences()
        if(mapkit == "google_mapkit") {
            googleController.initGoogleMap()
            googleController.initSharedPreferences()
        }
        if(mapkit == "yandex_mapkit") {
            yandexController.mapView = activity?.findViewById(R.id.yandex_map)!!
            yandexMap = yandexController.initYandexMap()
            yandexController.initSharedPreferences()
            yandexController.setMapKit()
        }
        llBottomSheet = requireView().findViewById<View>(R.id.bottom_sheet) as LinearLayout
        bottomSheetBehavior = BottomSheetBehavior.from<LinearLayout>(llBottomSheet!!)
        (bottomSheetBehavior as BottomSheetBehavior<*>).state = BottomSheetBehavior.STATE_HIDDEN
        Log.e("head", "onViewCreated CALLED")
        super.onViewCreated(view, savedInstanceState)

    }

    override fun showMarker(marker: Marker) {

            val color = Math.random().toFloat() * 359
            googleMap.addMarker(
                MarkerOptions()
                    .position(marker.position)
                    .title(marker.title)
                    .snippet(marker.snippet)
                    .draggable(marker.isDraggable)
                    .icon(BitmapDescriptorFactory.defaultMarker(color))
            )
    }
    override fun showMarker(point : Point) {
        yandexMap.mapObjects.addPlacemark(point, ImageProvider.fromResource(view?.context, R.drawable.marker_very_small))
        yandexMap.mapObjects.addTapListener(yandexController)

    }

    override fun onSaveInstanceState(outState: Bundle) {
        if(mapkit == "yandex_mapkit"){
            outState.putBoolean("yandex_mapkit", true)
        }
        super.onSaveInstanceState(outState)
    }

    override fun onMapReady(p0: GoogleMap) {
        googleMap = p0
        googleController.googleMapSetting(p0)
    }


    override fun onStart() {
        Log.e("head", "onStart CALLED")
        if(mapkit == "yandex_mapkit")
        yandexController.onStart()
        super.onStart()
    }

    override fun onStop() {
        Log.e("head", "onStop CALLED")
        if(mapkit == "yandex_mapkit")
         yandexController.onStop()
        super.onStop()
    }


    override fun activity(): Activity? {
        return activity
    }

    override fun sharedPreferences(): SharedPreferences {
        val shared =   activity?.getSharedPreferences("s1paraX", Context.MODE_PRIVATE)
        return shared!!
    }

    override fun bottomSheetBehavior(): BottomSheetBehavior<*>? {
        return bottomSheetBehavior
    }

    override fun llbottomSheet(): LinearLayout? {
        return llBottomSheet
    }

    override fun onDestroy() {
        if(mapkit == "google_mapkit")
            googleController.onDestroy()
        super.onDestroy()
    }
}

