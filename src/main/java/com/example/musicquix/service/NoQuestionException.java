package com.example.musicquix.service;

public class NoQuestionException extends RuntimeException {
    public NoQuestionException(String message) {
        super(message);
    }
}
