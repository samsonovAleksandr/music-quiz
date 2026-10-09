package com.example.musicquix.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;

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
    @ToString.Exclude
    private String textSong;
    @Column(name = "release_date")
    private LocalDate releaseDate;


}
