package com.example.musicquix.repository;

import com.example.musicquix.model.Song;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SongRepository extends JpaRepository<Song, Long> {

    // Random song of one language without ORDER BY RANDOM(): the start point is drawn once
    // (uncorrelated sub-select) inside the id range of that language and the first row at or after it
    // is read from the (language, song_id) index. Texts are validated when the dataset is built.
    @Query(value = """
            SELECT s.* FROM song s
            WHERE s.song_id = (SELECT x.song_id FROM song x
                               WHERE x.language = :language
                                 AND x.song_id >= (SELECT CAST(min(song_id) + floor(random() * (max(song_id) - min(song_id) + 1)) AS BIGINT)
                                                   FROM song WHERE language = :language)
                               ORDER BY x.song_id LIMIT 1)
            """, nativeQuery = true)
    Optional<Song> randomSong(@Param("language") String language);
}
