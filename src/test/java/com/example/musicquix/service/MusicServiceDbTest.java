package com.example.musicquix.service;

import com.example.musicquix.bot.Language;
import com.example.musicquix.dto.QuizQuestion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Runs against a real, populated PostgreSQL. Skipped unless SPRING_DATASOURCE_URL is set, e.g.
 * SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/musicquiz POSTGRES_USER=... POSTGRES_PASSWORD=... mvn test
 */
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "SPRING_DATASOURCE_URL", matches = ".+")
class MusicServiceDbTest {

    @Autowired
    MusicService service;

    @Test
    void generatesValidQuestionsForEveryLanguage() {
        for (Language language : Language.values()) {
            long start = System.nanoTime();
            for (int i = 0; i < 300; i++) {
                QuizQuestion q = service.newQuestion(language);
                assertEquals(4, q.options().size());
                assertEquals(4, new HashSet<>(q.options()).size(), "duplicate options: " + q.options());
                assertTrue(q.correctIndex() >= 0 && q.correctIndex() < 4);
                assertEquals(4, q.lyrics().split("\n").length, q.lyrics());
                assertFalse(q.lyrics().contains("["), q.lyrics());
            }
            System.out.printf("%s: 300 questions in %d ms%n", language, (System.nanoTime() - start) / 1_000_000);
        }
    }
}
