package com.beyond.musique;

public class Track {
    private String name;
    private String duration;

    public Track(String name, String duration) {
        this.name = name;
        this.duration = duration;
    }

    public String getName() {
        return name;
    }

    public String getDuration() {
        return duration;
    }
}