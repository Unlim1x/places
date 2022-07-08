package com.example.places.room;

import androidx.room.Entity;

@Entity
public class Tracker {
    double latitude;
    double longitude;
    String title;
    String snippet;
    float color;
}
