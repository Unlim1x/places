package ru.lim1x.places.ui.main.places

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.view.View
import android.widget.LinearLayout
import androidx.fragment.app.FragmentManager
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.Marker
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.mapview.MapView

interface PlacesInterface {
    fun onMapReadyCallback() : OnMapReadyCallback
    fun childFragmentManager() : FragmentManager?
    fun activity(): Activity?
    fun sharedPreferences():SharedPreferences?
    fun context(): Context?
    fun bottomSheetBehavior():BottomSheetBehavior<*>?
    fun llbottomSheet():LinearLayout?
    fun showMarker(marker:Marker)
    fun showMarker(point:Point)
}