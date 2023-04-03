package ru.lim1x.places.activities

import android.app.Activity
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.mapview.MapView

class YandexMapsActivity : Activity() {

    private val TARGET_LOCATION = Point(59.945933, 30.320045)

    private val mapView: MapView? = null
    var bundle : Bundle? = null
    private val ai : ApplicationInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bundle = ai.metaData
        bundle!!.getString("com.yandex.API_KEY")?.let { MapKitFactory.setApiKey(it) }

    }
}