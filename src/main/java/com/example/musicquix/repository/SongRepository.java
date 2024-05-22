package com.example.musicquix.repository;

import com.example.musicquix.model.Song;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SongRepository extends JpaRepository<Song, Long> {


    @Query(value = "SELECT * FROM song ORDER BY RANDOM() LIMIT 4", nativeQuery = true)
    public List<Song> getRandomSongs();
}
