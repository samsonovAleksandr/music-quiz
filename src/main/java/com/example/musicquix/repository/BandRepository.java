package com.example.musicquix.repository;

import com.example.musicquix.model.Band;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BandRepository extends JpaRepository<Band, Long> {

    /** Random band names of the given language (may repeat by name) used as wrong answers. */
    @Query(value = """
            SELECT b.name FROM band b
            WHERE b.id <> :excludeId
              AND EXISTS (SELECT 1 FROM language_texts l
                          WHERE l.band_id = b.id AND l.languages = :language)
            ORDER BY RANDOM() LIMIT :limit
            """, nativeQuery = true)
    List<String> randomNames(@Param("excludeId") long excludeId, @Param("language") String language,
                             @Param("limit") int limit);
}
