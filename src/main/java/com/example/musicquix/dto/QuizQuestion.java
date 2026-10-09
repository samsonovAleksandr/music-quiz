package com.example.musicquix.dto;

import java.util.List;

/**
 * One quiz round: a lyrics excerpt, the song title, the answer options (already shuffled)
 * and the index of the correct option.
 */
public record QuizQuestion(String lyrics, String title, List<String> options, int correctIndex) {
}
