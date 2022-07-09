package com.example.places.room.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Tracker {
    public double latitude;
    public double longitude;
    @NonNull
    @PrimaryKey
    public String title;
    public String snippet;
    public float color;
    public String date;
}
