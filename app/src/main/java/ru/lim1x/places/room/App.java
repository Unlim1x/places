package ru.lim1x.places.room;

import android.app.Application;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.room.Room;

import com.yandex.mapkit.MapKitFactory;

import ru.lim1x.places.room.database.PlacesDatabase;

public class App extends Application {
    public static App instance;
    private PlacesDatabase database;

    @Override
    public void onCreate(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Bundle bundle;
            try {
                bundle = getPackageManager().getApplicationInfo(getPackageName()
                        , PackageManager.ApplicationInfoFlags.of(PackageManager.GET_META_DATA)).metaData;
                if (!bundle.isEmpty()){
                    MapKitFactory.setApiKey(bundle.getString("com.yandex.API_KEY"));
                }
            } catch (PackageManager.NameNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
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
