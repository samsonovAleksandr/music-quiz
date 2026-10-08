package com.example.musicquix.service;

import com.example.musicquix.bot.Language;
import com.example.musicquix.dto.QuizQuestion;
import com.example.musicquix.model.Band;
import com.example.musicquix.model.Song;
import com.example.musicquix.repository.BandRepository;
import com.example.musicquix.repository.SongRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Pattern;

@Service
public class MusicService {

    static final int OPTIONS = 4;
    static final int LYRIC_LINES = 4;
    private static final int MAX_ATTEMPTS = 10;
    private static final int DISTRACTOR_CANDIDATES = 12;

    private static final Pattern SECTION_LABEL = Pattern.compile(
            "(?iu)(припев|куплет|интро|аутро|бридж|chorus|verse|intro|outro|bridge)[\\s\\d:.]*");

    private final BandRepository bandRepository;
    private final SongRepository songRepository;
    private final Random rnd;

    @Autowired
    public MusicService(BandRepository bandRepository, SongRepository songRepository) {
        this(bandRepository, songRepository, new Random());
    }

    MusicService(BandRepository bandRepository, SongRepository songRepository, Random rnd) {
        this.bandRepository = bandRepository;
        this.songRepository = songRepository;
        this.rnd = rnd;
    }

    public QuizQuestion newQuestion(Language language) {
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            Optional<Song> song = songRepository.randomSong(language.dbValue());
            if (song.isEmpty()) {
                continue;
            }
            String lyrics = extractLyrics(song.get().getTextSong());
            if (lyrics == null) {
                continue;
            }
            Optional<Band> band = bandRepository.findById(song.get().getBandId());
            if (band.isEmpty()) {
                continue;
            }
            String correct = band.get().getName();
            List<String> wrong = distractors(band.get(), correct, language);
            if (wrong.size() < OPTIONS - 1) {
                continue;
            }
            List<String> options = new ArrayList<>(wrong);
            options.add(correct);
            Collections.shuffle(options, rnd);
            return new QuizQuestion(lyrics, song.get().getNameSong(), List.copyOf(options), options.indexOf(correct));
        }
        throw new NoQuestionException("Could not build a question after " + MAX_ATTEMPTS + " attempts");
    }

    /** Returns {@value LYRIC_LINES} consecutive non-blank lines, or null if the text is too short. */
    String extractLyrics(String text) {
        if (text == null) {
            return null;
        }
        List<String> lines = Arrays.stream(text.split("\\R"))
                .map(String::trim)
                .filter(l -> !l.isEmpty())
                .filter(l -> !(l.startsWith("[") && l.endsWith("]"))) // section markers: [Chorus], [Куплет 1]
                .filter(l -> !SECTION_LABEL.matcher(l).matches())   // bare labels: "Припев", "Chorus 2:"
                .toList();
        if (lines.size() < LYRIC_LINES) {
            return null;
        }
        int start = rnd.nextInt(lines.size() - LYRIC_LINES + 1);
        return String.join("\n", lines.subList(start, start + LYRIC_LINES));
    }

    private List<String> distractors(Band band, String correct, Language language) {
        List<String> candidates = bandRepository.randomNames(band.getId(), language.dbValue(), DISTRACTOR_CANDIDATES);
        // de-duplicate case-insensitively and never repeat the correct answer
        Map<String, String> unique = new LinkedHashMap<>();
        for (String name : candidates) {
            if (name != null && !name.isBlank() && !name.equalsIgnoreCase(correct)) {
                unique.putIfAbsent(name.toLowerCase(), name);
            }
        }
        return unique.values().stream().limit(OPTIONS - 1).toList();
    }
}
