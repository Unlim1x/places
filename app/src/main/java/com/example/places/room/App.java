package com.example.places.room;

import android.app.Application;

import androidx.room.Room;

import com.example.places.room.database.PlacesDatabase;

public class App extends Application {
    public static App instance;
    private PlacesDatabase database;

    @Override
    public void onCreate(){
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
