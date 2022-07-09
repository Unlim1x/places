package com.example.places.room.daos;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.places.room.entities.InitApp;

@Dao
public interface InitAppDao {

    @Query("SELECT COUNT (*) FROM InitApp")
    int getInit();

    @Insert
    void insert(InitApp initapp);

}
