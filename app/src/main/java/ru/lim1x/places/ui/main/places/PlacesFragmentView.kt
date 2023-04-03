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
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.libraries.places.api.Places
import com.google.android.material.bottomsheet.BottomSheetBehavior
import ru.lim1x.places.R
import ru.lim1x.places.databinding.FragmentPlacesGoogleBinding
import ru.lim1x.places.databinding.FragmentPlacesYandexBinding


class PlacesFragmentView : Fragment(), PlacesInterface, OnMapReadyCallback {
    private lateinit var presenter: PlacesPresenter
    private lateinit var mSharedPreferences: SharedPreferences
    lateinit var mapkit: String
    private var llBottomSheet: LinearLayout? = null
    private var bottomSheetBehavior: BottomSheetBehavior<*>? = null


    override fun context(): Context? {
       return context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.e("head", "onCreateView CALLED")
        mSharedPreferences = sharedPreferences()
            context?.let { Places.initialize(it, "AIzaSyCd41NcKGeylMpBOrGn1J8wh8mp3YkA-MA") }
        presenter = PlacesPresenter(this)

         return when (mapkit) {
             "google_mapkit" -> {
                 Log.e("google 1", "111111 GOOOOOOOOGLE")
                 FragmentPlacesGoogleBinding.inflate(inflater, container, false).root
             }
             "yandex_mapkit" -> {
                 Log.e("yandex 1", "111111 YAAAAAAAAANDEX")
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
        if(mapkit == "google_mapkit") {
            presenter.initGoogleMap()
        }
        if(mapkit == "yandex_mapkit") {
            presenter.mapView = activity?.findViewById(R.id.yandex_map)!!
            presenter.initYandexMap()
        }
        llBottomSheet = requireView().findViewById<View>(R.id.bottom_sheet) as LinearLayout
        bottomSheetBehavior = BottomSheetBehavior.from<LinearLayout>(llBottomSheet!!)
        (bottomSheetBehavior as BottomSheetBehavior<*>).state = BottomSheetBehavior.STATE_HIDDEN
        Log.e("head", "onViewCreated CALLED")
        super.onViewCreated(view, savedInstanceState)
        presenter.initSharedPreferences()
        presenter.setMapKit()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        sharedPreferences()
        if(mapkit == "yandex_mapkit"){
            outState.putBoolean("yandex_mapkit", true)
        }
        super.onSaveInstanceState(outState)
    }

    override fun onMapReady(p0: GoogleMap) {
        presenter.googleMapSetting(p0)
    }


    override fun onStart() {
        Log.e("head", "onStart CALLED")
        presenter.onStart()
        super.onStart()
    }

    override fun onStop() {
        Log.e("head", "onStop CALLED")
        presenter.onStop()
        super.onStop()
    }


    override fun activity(): Activity? {
        return activity
    }

    override fun sharedPreferences(): SharedPreferences {
        val shared =   activity?.getSharedPreferences("s1paraX", Context.MODE_PRIVATE)
         mapkit = shared?.getString("mapkit", "")!!
        return shared
    }

    override fun bottomSheetBehavior(): BottomSheetBehavior<*>? {
        return bottomSheetBehavior
    }

    override fun llbottomSheet(): LinearLayout? {
        return llBottomSheet
    }
}