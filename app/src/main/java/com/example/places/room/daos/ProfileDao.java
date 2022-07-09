package com.example.places.room.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.places.room.entities.Profile;

import java.util.List;

@Dao
public interface ProfileDao {

    @Query("SELECT * FROM profile")
    List<Profile>getAll();

    @Query("SELECT * FROM profile WHERE type = 0")
    Profile getLocal();

    @Query("SELECT * FROM profile WHERE phone LIKE :phone")
    Profile getByPhone(String phone);

    @Insert
    void insert(Profile profile);

    @Update
    void update(Profile profile);

    @Delete
    void delete(Profile profile);
}
