package com.example.musicquix.bot;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MessagesTest {

    @Test
    void escapesHtml() {
        assertEquals("a &lt;b&gt; &amp; c", Messages.esc("a <b> & c"));
        assertEquals("", Messages.esc(null));
    }

    @Test
    void barScalesAndClamps() {
        assertEquals("🟩🟩🟩🟩🟩🟩🟩⬜⬜⬜", Messages.bar(70));
        assertEquals("⬜".repeat(10), Messages.bar(0));
        assertEquals("🟩".repeat(10), Messages.bar(100));
        assertEquals("🟩".repeat(10), Messages.bar(250));
    }

    @Test
    void questionShowsStreakOnlyFromTwo() {
        assertFalse(Messages.question(Language.ENGLISH, "x", 1).contains("🔥"));
        assertTrue(Messages.question(Language.ENGLISH, "x", 3).contains("🔥 3"));
        assertTrue(Messages.question(Language.RUSSIAN, "<hi>", 0).contains("&lt;hi&gt;"));
    }

    @Test
    void resultMarksCorrectAndWrongOptions() {
        List<String> options = List.of("Queen", "Muse", "ABBA", "Blur");
        String wrong = Messages.result(Language.ENGLISH, "l", "Song <1>", options, 1, 0, 0, 4);
        assertTrue(wrong.contains("✅ <b>Queen</b>"));
        assertTrue(wrong.contains("❌ Muse"));
        assertTrue(wrong.contains("▫️ ABBA"));
        assertTrue(wrong.contains("Мимо"));
        assertTrue(wrong.contains("«Song &lt;1&gt;»"));

        String right = Messages.result(Language.ENGLISH, "l", null, options, 0, 0, 5, 5);
        assertTrue(right.contains("Верно"));
        assertTrue(right.contains("Серия: 5"));
        assertFalse(right.contains("❌"));
        assertFalse(right.contains("🎵"));
    }

    @Test
    void statsHandlesEmptyAndPercent() {
        assertTrue(Messages.stats(0, 0, 0, 0).contains("/game"));
        String s = Messages.stats(7, 10, 2, 5);
        assertTrue(s.contains("70%"));
        assertTrue(s.contains("🟩🟩🟩🟩🟩🟩🟩⬜⬜⬜"));
    }
}
