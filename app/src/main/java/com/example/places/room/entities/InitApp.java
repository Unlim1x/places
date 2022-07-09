package com.example.places.room.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class InitApp {
    @NonNull
    @PrimaryKey
    public int first; //if table contains, it means that app has already been used.
}
