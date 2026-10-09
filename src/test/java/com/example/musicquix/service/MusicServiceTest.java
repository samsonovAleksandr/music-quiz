package com.example.musicquix.service;

import com.example.musicquix.bot.Language;
import com.example.musicquix.dto.QuizQuestion;
import com.example.musicquix.model.Band;
import com.example.musicquix.model.Song;
import com.example.musicquix.repository.BandRepository;
import com.example.musicquix.repository.SongRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class MusicServiceTest {

    private BandRepository bandRepository;
    private SongRepository songRepository;
    private MusicService service;

    @BeforeEach
    void setUp() {
        bandRepository = mock(BandRepository.class);
        songRepository = mock(SongRepository.class);
        service = new MusicService(bandRepository, songRepository, new Random(42));
    }

    private static Song song(String text) {
        Song s = new Song();
        s.setBandId(1L);
        s.setTextSong(text);
        return s;
    }

    @Test
    void extractLyricsReturnsFourNonBlankLines() {
        String lyrics = service.extractLyrics("a\n\n b \nc\r\nd\ne\nf");
        assertNotNull(lyrics);
        assertEquals(4, lyrics.split("\n").length);
    }

    @Test
    void extractLyricsSkipsSectionMarkers() {
        String lyrics = service.extractLyrics("[Chorus]\na\nb\n[Куплет 1]\nc\nd");
        assertEquals("a\nb\nc\nd", lyrics);
        assertEquals("a\nb\nc\nd", service.extractLyrics("Припев\na\nChorus 2:\nb\nc\nd"));
    }

    @Test
    void extractLyricsRejectsShortOrMissingText() {
        assertNull(service.extractLyrics(null));
        assertNull(service.extractLyrics("TextError"));
        assertNull(service.extractLyrics("a\nb\nc"));
    }

    @Test
    void buildsQuestionWithUniqueOptionsAndCorrectIndex() {
        when(songRepository.randomSong(anyString())).thenReturn(Optional.of(song("1\n2\n3\n4\n5\n6")));
        when(bandRepository.findById(1L)).thenReturn(Optional.of(Band.builder().id(1L).name("Queen").build()));
        // duplicates and the correct answer must be filtered out
        when(bandRepository.randomNames(anyLong(), eq("English"), anyInt()))
                .thenReturn(List.of("Muse", "muse", "queen", "ABBA", "Muse", "Blur"));

        QuizQuestion q = service.newQuestion(Language.ENGLISH);

        assertEquals(4, q.options().size());
        assertEquals(4, new HashSet<>(q.options()).size());
        assertEquals("Queen", q.options().get(q.correctIndex()));
    }

    @Test
    void usesRussianQueriesForRussianLanguage() {
        when(songRepository.randomSong("Русский язык")).thenReturn(Optional.of(song("1\n2\n3\n4\n5")));
        when(bandRepository.findById(1L)).thenReturn(Optional.of(Band.builder().id(1L).name("Кино").build()));
        when(bandRepository.randomNames(anyLong(), eq("Русский язык"), anyInt())).thenReturn(List.of("ДДТ", "Ария", "Любэ"));

        QuizQuestion q = service.newQuestion(Language.RUSSIAN);

        assertEquals("Кино", q.options().get(q.correctIndex()));
        verify(songRepository, never()).randomSong("English");
    }

    @Test
    void retriesThenFailsWhenNoUsableSongs() {
        when(songRepository.randomSong(anyString())).thenReturn(Optional.of(song("too\nshort")));
        assertThrows(NoQuestionException.class, () -> service.newQuestion(Language.ENGLISH));
        verify(songRepository, atLeast(2)).randomSong(anyString());
    }

    @Test
    void failsWhenNotEnoughDistractors() {
        when(songRepository.randomSong(anyString())).thenReturn(Optional.of(song("1\n2\n3\n4\n5")));
        when(bandRepository.findById(1L)).thenReturn(Optional.of(Band.builder().id(1L).name("Queen").build()));
        when(bandRepository.randomNames(anyLong(), eq("English"), anyInt())).thenReturn(List.of("Muse"));
        assertThrows(NoQuestionException.class, () -> service.newQuestion(Language.ENGLISH));
    }
}
