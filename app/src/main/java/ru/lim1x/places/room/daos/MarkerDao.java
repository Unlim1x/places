package ru.lim1x.places.room.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import ru.lim1x.places.room.entities.Markers;

import java.util.List;

@Dao
public interface MarkerDao {

    @Query("SELECT * FROM Markers")
    List<Markers>getAll();

    @Query("SELECT * FROM Markers WHERE snippet LIKE :snippet")
    Markers getBySnippet(String snippet);

    @Insert
    void insert(Markers markers);

    @Update
    void update(Markers markers);

    @Delete
    void delete(Markers markers);

    @Query("DELETE FROM markers")
    void nukeTable();
}
