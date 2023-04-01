package ru.lim1x.places.room;

import android.app.Application;

import androidx.room.Room;

import com.yandex.mapkit.MapKitFactory;

import ru.lim1x.places.room.database.PlacesDatabase;

public class App extends Application {
    public static App instance;
    private PlacesDatabase database;

    @Override
    public void onCreate(){
        MapKitFactory.setApiKey("a5b650e8-16b6-49f9-afe8-d7694c035651");
        super.onCreate();
        instance = this;
        database = Room.databaseBuilder(this, PlacesDatabase.class, "placesX.db").allowMainThreadQueries().build();

    }

    public static App getInstance(){
        return instance;
    }

    public PlacesDatabase getDatabase(){
        return database;
    }
}
