package ru.lim1x.places.ui.main.places

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import com.yandex.mapkit.mapview.MapView

interface PlacesInterface {
    fun activity(): Activity?
    fun sharedPreferences():SharedPreferences?
    fun context(): Context?
    fun mapView(): MapView?
}