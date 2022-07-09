package com.example.places.room.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Profile {
    public String username;
    @NonNull
    @PrimaryKey
    public String phone;
    public String image;
    public int type; //0 = default, 1 = authorized
    public int loggedout; // 0 = signed in, 1 = logged out
}
