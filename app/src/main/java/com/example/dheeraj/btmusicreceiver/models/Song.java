package com.example.dheeraj.btmusicreceiver.models;

/**
 * Created by dheeraj on 17/5/18.
 */

public class Song {
    public long _id;
    public String title;
    public String artist;

    public Song(long _id, String title, String artist) {
        this._id = _id;
        this.title = title;
        this.artist = artist;
    }

    public long get_id() {
        return _id;
    }

    public void set_id(long _id) {
        this._id = _id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }
}
