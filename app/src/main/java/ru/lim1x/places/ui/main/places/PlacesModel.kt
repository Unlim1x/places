package ru.lim1x.places.ui.main.places

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import ru.lim1x.places.room.App
import ru.lim1x.places.room.daos.MarkerDao
import ru.lim1x.places.room.database.PlacesDatabase
import ru.lim1x.places.room.entities.Markers

class PlacesModel {
    private var database = App.getInstance().database
    private var markerDao = database.markerDao()


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