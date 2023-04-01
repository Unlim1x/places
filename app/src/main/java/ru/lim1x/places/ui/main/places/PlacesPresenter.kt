package ru.lim1x.places.ui.main.places


import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentManager
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition

class PlacesPresenter constructor(val view: PlacesInterface){
    lateinit var mSharedPreferences : SharedPreferences
    var bundle : Bundle? = null
    private lateinit var ai : ApplicationInfo
    var yandex : Boolean = false

fun initYandexMap(fragmentManager: FragmentManager){
    ai = view.activity()?.packageManager?.getApplicationInfo(view.activity()!!.packageName, PackageManager.GET_META_DATA)!!
    mSharedPreferences = view.activity()!!.getSharedPreferences("s1paraX", Context.MODE_PRIVATE)
    if (mSharedPreferences.contains("mapkit")){
        val mapkit: String = mSharedPreferences.getString("mapkit", "")!!
        if (mapkit.isNotEmpty() && mapkit.equals("yandex_mapkit")){
                yandex = true
                bundle = ai.metaData
                bundle!!.getString("com.yandex.API_KEY")?.let { MapKitFactory.setApiKey(it) }
                bundle!!.getString("com.yandex.API_KEY")?.let { Log.e("yandex_api", it) }
                MapKitFactory.initialize(view.context());
                view.mapView()!!.map.move(CameraPosition(Point(55.751574, 37.573856), 11.0f, 0.0f, 0.0f), Animation(Animation.Type.SMOOTH,
                    0F
                ), null)


        }
    }

}


    fun onStart(){

        MapKitFactory.getInstance().onStart()
    }
    fun  onStop(){

        MapKitFactory.getInstance().onStop()
    }

}