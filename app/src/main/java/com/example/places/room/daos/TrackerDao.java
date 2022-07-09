package com.example.places.room.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.places.room.entities.Tracker;

import java.util.List;

@Dao
public interface TrackerDao {

    @Query("SELECT * FROM tracker")
    List<Tracker>getAll();

    @Insert
    void insert(Tracker tracker);

    @Update
    void update(Tracker tracker);

    @Delete
    void delete(Tracker tracker);

    @Query("DELETE FROM markers")
    void nukeTable();
}
