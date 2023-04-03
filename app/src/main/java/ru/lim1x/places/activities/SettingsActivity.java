package ru.lim1x.places.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import ru.lim1x.places.R;
import ru.lim1x.places.room.App;
import ru.lim1x.places.room.daos.MarkerDao;
import ru.lim1x.places.room.daos.TrackerDao;
import ru.lim1x.places.room.database.PlacesDatabase;

public class SettingsActivity extends AppCompatActivity {
    Preference pf;
    SharedPreferences mSettings;
    public SharedPreferences.Editor editor;
    PlacesDatabase database;
    MarkerDao markerDao;
    TrackerDao trackerDao;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSettings = getSharedPreferences("s1paraX", Context.MODE_PRIVATE);
        editor = mSettings.edit();
        database = App.instance.getDatabase();
        markerDao = database.markerDao();
        trackerDao = database.trackerDao();
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);

        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        PlacesDatabase database;
        MarkerDao markerDao;
        TrackerDao trackerDao;
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            database = App.instance.getDatabase();
            markerDao = database.markerDao();
            trackerDao = database.trackerDao();
            ListPreference theme = getPreferenceManager().findPreference("theme");
            if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_NO)
                theme.setValueIndex(1);
            if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES)
                theme.setValueIndex(2);
            if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                theme.setValueIndex(0);


            theme.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    SharedPreferences.Editor editor = getActivity().getSharedPreferences("s1paraX", Context.MODE_PRIVATE).edit();

                    if (newValue.equals("light_theme")) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                        theme.setValueIndex(1);
                        editor.putBoolean("light_theme", true);
                        editor.putBoolean("dark_theme", false);
                        editor.putBoolean("system_theme", false);
                        editor.apply();
                        return true;
                    }
                    if (newValue.equals("dark_theme")){
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                        theme.setValueIndex(2);
                        editor.putBoolean("light_theme", false);
                        editor.putBoolean("dark_theme", true);
                        editor.putBoolean("system_theme", false);
                        editor.apply();
                        return true;
                    }
                    if (newValue.equals("system_theme")){
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                        theme.setValueIndex(0);
                        editor.putBoolean("light_theme", false);
                        editor.putBoolean("dark_theme", false);
                        editor.putBoolean("system_theme", true);
                        editor.apply();
                        return true;
                    }


                    return false;
                }
            });
            ListPreference mapkit = getPreferenceManager().findPreference("mapkit");
            mapkit.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(@NonNull Preference preference, Object newValue) {
                    SharedPreferences.Editor editor = getActivity().getSharedPreferences("s1paraX", Context.MODE_PRIVATE).edit();
                    editor.putString("mapkit", newValue.toString());
                    editor.apply();
                    mapkit.setValue(newValue.toString());
                    return true;
                }
            });



            ListPreference places_map_style = getPreferenceManager().findPreference("places_map_style");
            places_map_style.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                     SharedPreferences.Editor editor = getActivity().getSharedPreferences("s1paraX", Context.MODE_PRIVATE).edit();
                     editor.putString("map_style", newValue.toString());
                     editor.apply();
                     places_map_style.setValue(newValue.toString());
                    return false;
                }
            });

            ListPreference tracker_map_style = getPreferenceManager().findPreference("tracker_map_style");
            tracker_map_style.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    SharedPreferences.Editor editor = getActivity().getSharedPreferences("s1paraX", Context.MODE_PRIVATE).edit();
                    editor.putString("tracker_map_style", newValue.toString());
                    editor.apply();
                    tracker_map_style.setValue(newValue.toString());
                    return false;
                }
            });

            ListPreference tracker_width = getPreferenceManager().findPreference("tracker_width_keys");
            tracker_width.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    SharedPreferences.Editor editor = getActivity().getSharedPreferences("s1paraX", Context.MODE_PRIVATE).edit();
                    float width = 0f;
                    switch((String)newValue){
                        case "slim":
                            width = 5f;
                            break;
                        case "mid":
                            width = 12f;
                            break;
                        case "large":
                            width = 20f;
                            break;
                        default:
                            width = 10f;
                            break;
                    }
                    editor.putFloat("tracker_width_key", width);
                    editor.apply();
                    tracker_width.setValue(newValue.toString());
                    return false;
                }
            });

            ListPreference tracker_freq = getPreferenceManager().findPreference("tracker_update_keys");
            tracker_width.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    SharedPreferences.Editor editor = getActivity().getSharedPreferences("s1paraX", Context.MODE_PRIVATE).edit();
                    int interval = 0;
                    switch((String)newValue){
                        case "two":
                            interval = 2;
                            break;
                        case "five":
                            interval = 5;
                            break;
                        case "ten":
                            interval = 10;
                            break;
                        default:
                            interval = 2;
                            break;
                    }
                    editor.putInt("tracker_freq", interval);
                    editor.apply();
                    tracker_freq.setValue(newValue.toString());
                    return false;
                }
            });

            PreferenceScreen GEO =  getPreferenceManager().findPreference("open_geo_system");

            GEO.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent viewIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", getContext().getPackageName(), null));
                    startActivity(viewIntent);
                    return false;
                }
            });

            PreferenceScreen clear_places =  getPreferenceManager().findPreference("clear_places");

            clear_places.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Log.i("Zaebal", "YA TUT!!!!!!!!!!!!");
                    LayoutInflater li = LayoutInflater.from(getContext());
                    View inputDialogView = li.inflate(R.layout.clearing_dialog, null);
                    AlertDialog.Builder aDialogBuilder = new AlertDialog.Builder(getContext());
                    aDialogBuilder.setView(inputDialogView);
                    aDialogBuilder
                            .setCancelable(false)
                            .setPositiveButton("Очистить",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,int id) {
                                            Log.i("deleting","");
                                            markerDao.nukeTable();
                                        }
                                    })
                            .setNegativeButton("Отменить",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,int id) {
                                            dialog.cancel();
                                        }
                                    });
                    AlertDialog inputDialog = aDialogBuilder.create();
                    TextView header = inputDialogView.findViewById(R.id.clearing_dialog_header);
                    header.setText("Очистка истории мест");
                    inputDialog.show();

                    return true;
                }
            });

            PreferenceScreen clear_tracker =  getPreferenceManager().findPreference("clear_tracker");

            clear_tracker.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference preference) {

                    LayoutInflater li = LayoutInflater.from(getContext());
                    View inputDialogView = li.inflate(R.layout.clearing_dialog, null);
                    AlertDialog.Builder aDialogBuilder = new AlertDialog.Builder(getContext());
                    aDialogBuilder.setView(inputDialogView);
                    aDialogBuilder
                            .setCancelable(false)
                            .setPositiveButton("Очистить",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,int id) {
                                            trackerDao.nukeTable();
                                        }
                                    })
                            .setNegativeButton("Отменить",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,int id) {
                                            dialog.cancel();
                                        }
                                    });
                    AlertDialog inputDialog = aDialogBuilder.create();
                    TextView header = inputDialogView.findViewById(R.id.clearing_dialog_header);
                    header.setText("Очистка истории трекера");
                    inputDialog.show();

                    return true;
                }
            });



        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


}