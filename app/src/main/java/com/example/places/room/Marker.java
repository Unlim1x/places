package com.example.places.room;

import androidx.room.Entity;

@Entity
public class Marker {
    double latitude;
    double longitude;
    String title;
    String snippet;
    int drag;
    float color;
}
