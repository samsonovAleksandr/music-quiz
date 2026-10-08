package com.example.musicquix.dto;

import java.util.List;

/**
 * One quiz round: a lyrics excerpt, the answer options (already shuffled)
 * and the index of the correct option.
 */
public record QuizQuestion(String lyrics, List<String> options, int correctIndex) {
}
