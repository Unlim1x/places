package ru.lim1x.places.room.daos;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import ru.lim1x.places.room.entities.InitApp;

@Dao
public interface InitAppDao {

    @Query("SELECT COUNT (*) FROM InitApp")
    int getInit();

    @Insert
    void insert(InitApp initapp);

}
