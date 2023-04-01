package ru.lim1x.places.ui.main.places

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.libraries.places.api.Places
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.mapview.MapView
import ru.lim1x.places.R
import ru.lim1x.places.databinding.FragmentPlacesGoogleBinding
import ru.lim1x.places.databinding.FragmentPlacesYandexBinding


class PlacesFragmentView : Fragment(), PlacesInterface {
    private lateinit var google_binding: FragmentPlacesGoogleBinding
    private lateinit var yandex_binding: FragmentPlacesYandexBinding
    private lateinit var presenter: PlacesPresenter
    private var mapView: MapView? = activity?.findViewById(R.id.yandex_map)

companion object {
    private const val ARG_SECTION_NUMBER = "section_number"

    fun newInstance(index: Int): PlacesFragmentView {
        val fragment = PlacesFragmentView()
        val bundle = Bundle()
        bundle.putInt(ARG_SECTION_NUMBER, index)
        fragment.arguments = bundle
        return fragment
    }
}

    override fun context(): Context? {
       return context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        presenter = PlacesPresenter(this)
        context?.let { Places.initialize(it, "AIzaSyCd41NcKGeylMpBOrGn1J8wh8mp3YkA-MA") }

        val mSharedPreferences = requireActivity().getSharedPreferences("s1paraX", Context.MODE_PRIVATE)
        val mapkit: String = mSharedPreferences?.getString("mapkit", "")!!
        if(mapkit.equals("google_mapkit"))
        {
            Log.e("Pizda, google 1", "111111 GOOOOOOOOGLE")
            return FragmentPlacesGoogleBinding.inflate(inflater, container, false).root
        }
        else  if(mapkit.equals("yandex_mapkit")){
            Log.e("Pizda, yandex 1", "111111 YAAAAAAAAANDEX")
            val ai = activity?.packageManager?.getApplicationInfo(requireActivity().packageName, PackageManager.GET_META_DATA)!!
            val bundle = ai.metaData
            //bundle.getString("com.yandex.API_KEY")?.let { Log.e("PFV yandex_api", it) }
            //bundle.getString("com.yandex.API_KEY")?.let { MapKitFactory.setApiKey(it) }

            MapKitFactory.initialize(context());
            return  FragmentPlacesYandexBinding.inflate(inflater, container, false).root
        }

        Log.e("Pizda, CHECK NE RABOTAET", "google pobedil")
        return FragmentPlacesGoogleBinding.inflate(inflater, container, false).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
      //  mapView?.let { presenter.initYandexMap(mapView!!) }

    }


    private val googleMapCallback : OnMapReadyCallback = OnMapReadyCallback {

        it.setOnMapLongClickListener {

        }
    }

    override fun onStart() {
        mapView?.onStart()
        presenter.onStart()
        super.onStart()
    }

    override fun onStop() {
        mapView?.onStart()
        presenter.onStop()
        super.onStop()
    }

    override fun mapView(): MapView? {
        return mapView
    }

    override fun activity(): Activity? {
        return activity
    }

    override fun sharedPreferences(): SharedPreferences? {
        return  activity?.getSharedPreferences("s1paraX", Context.MODE_PRIVATE)
    }
}