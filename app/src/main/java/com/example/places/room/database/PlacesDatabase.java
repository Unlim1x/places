package com.example.places.room.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.places.room.daos.InitAppDao;
import com.example.places.room.daos.MarkerDao;
import com.example.places.room.daos.ProfileDao;
import com.example.places.room.daos.TrackerDao;
import com.example.places.room.entities.Profile;
import com.example.places.room.entities.Tracker;
import com.example.places.room.entities.Markers;
import com.example.places.room.entities.InitApp;

@Database(entities = {Markers.class, Tracker.class, Profile.class, InitApp.class}, version = 1)
public abstract class PlacesDatabase extends RoomDatabase {
    public abstract MarkerDao markerDao();
    public abstract TrackerDao trackerDao();
    public abstract ProfileDao profileDao();
    public abstract InitAppDao initAppDao();

}
