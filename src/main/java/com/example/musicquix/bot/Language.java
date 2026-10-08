package com.example.musicquix.bot;

public enum Language {
    RUSSIAN("Русский", "Русский язык"),
    ENGLISH("English", "English");

    private final String label;
    private final String dbValue;

    Language(String label, String dbValue) {
        this.label = label;
        this.dbValue = dbValue;
    }

    public String label() {
        return label;
    }

    /** Value stored in {@code language_texts.languages}. */
    public String dbValue() {
        return dbValue;
    }
}
