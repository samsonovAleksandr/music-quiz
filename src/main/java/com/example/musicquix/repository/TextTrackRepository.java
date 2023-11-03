package com.example.musicquix.repository;

import com.example.musicquix.model.TextTrack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TextTrackRepository extends JpaRepository<TextTrack, Long> {
}
