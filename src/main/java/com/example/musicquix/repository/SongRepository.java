package com.example.musicquix.repository;

import com.example.musicquix.model.Song;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SongRepository extends JpaRepository<Song, Long> {


    @Query(value = "SELECT * FROM song ORDER BY RANDOM() LIMIT 4", nativeQuery = true)
     List<Song> getRandomSongs();

    @Query(value = "SELECT song.* FROM song LEFT JOIN language_texts ON song.band_id = language_texts.band_id WHERE language_texts.languages = 'Русский язык' ORDER BY RANDOM() LIMIT 4", nativeQuery = true)
     List<Song> getRandomSongsRus();
}
