package ru.lim1x.places.room.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import ru.lim1x.places.room.daos.InitAppDao;
import ru.lim1x.places.room.daos.MarkerDao;
import ru.lim1x.places.room.daos.ProfileDao;
import ru.lim1x.places.room.daos.TrackerDao;
import ru.lim1x.places.room.entities.Profile;
import ru.lim1x.places.room.entities.Tracker;
import ru.lim1x.places.room.entities.Markers;
import ru.lim1x.places.room.entities.InitApp;

@Database(entities = {Markers.class, Tracker.class, Profile.class, InitApp.class}, version = 1)
public abstract class PlacesDatabase extends RoomDatabase {
    public abstract MarkerDao markerDao();
    public abstract TrackerDao trackerDao();
    public abstract ProfileDao profileDao();
    public abstract InitAppDao initAppDao();

}
