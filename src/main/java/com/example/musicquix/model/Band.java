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

    @Column(name = "name")
    private String name;

    @ElementCollection(targetClass = String.class, fetch = FetchType.LAZY)
    @CollectionTable(name = "countrys", joinColumns = @JoinColumn(name = "band_id"))
    @Column(name = "countrys", nullable = false)
    private List<String> countrys;

    @ElementCollection(targetClass = String.class, fetch = FetchType.LAZY)
    @CollectionTable(name = "genres", joinColumns = @JoinColumn(name = "band_id"))
    @Column(name = "genres", nullable = false)
    private List<String> genres;

    @ElementCollection(targetClass = String.class, fetch = FetchType.LAZY)
    @CollectionTable(name = "language_texts", joinColumns = @JoinColumn(name = "band_id"))
    @Column(name = "languages", nullable = false)
    private List<String> languageList = new ArrayList<>();
}
