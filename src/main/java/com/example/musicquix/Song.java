package com.example.musicquix;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Song {
    @Id()
    private Long id;
    private String textSong;
    private String nameMusician;
    private String nameTrack;
}
