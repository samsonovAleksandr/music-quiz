package com.example.musicquix.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "song")
public class Song {
    @Column(name = "song_id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long songId;
    @Column(name = "band_id")
    private Long bandId;
    @Column(name = "name_song")
    private String nameSong;
    @Column(name = "text_song")
    private String textSong;
    @Column(name = "release_date")
    private Date releaseDate;


}
