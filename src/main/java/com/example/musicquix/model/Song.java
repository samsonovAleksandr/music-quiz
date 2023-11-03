package com.example.musicquix.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "songs")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Song {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "author_id")
    private Long authorId;
    @Column(name = "song_name")
    private String songName;

}
