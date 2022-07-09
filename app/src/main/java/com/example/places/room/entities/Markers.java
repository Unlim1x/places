package com.example.places.room.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Markers {
    public double latitude;
    public double longitude;
    public String title;

    @NonNull @PrimaryKey
    public String snippet;
    public int drag;
    public float color;

    public String date;
}
