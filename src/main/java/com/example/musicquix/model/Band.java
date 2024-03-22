package com.example.musicquix.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
@Entity
@Table(name = "band")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Band {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "countrys")
    private String countrys;

    @Column(name = "genre")
    private String genre;

    @ElementCollection(targetClass = String.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "language_texts", joinColumns = @JoinColumn(name = "band_id"))
    @Column(name = "languages", nullable = false)
    List<String> languageList = new ArrayList<>();
}
