package com.example.musicquix.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "song_lyric_snippets")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TextTrack {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "song_id")
    private Long songId;
    @Column(name = "snippet_text")
    private String snippetText;
}
