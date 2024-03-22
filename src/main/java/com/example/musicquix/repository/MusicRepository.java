package com.example.musicquix.repository;

import com.example.musicquix.model.Band;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MusicRepository extends JpaRepository<Band, Long> {
}
