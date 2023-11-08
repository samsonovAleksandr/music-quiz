package com.example.musicquix.service;

import com.example.musicquix.dto.MusicJSON;
import com.example.musicquix.mapper.MusicMapper;
import com.example.musicquix.model.Music;
import com.example.musicquix.repository.MusicRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class MusicService {

    private final MusicRepository musicRepository;
    private final MusicMapper musicMapper;

    public MusicService(MusicRepository musicRepository, MusicMapper musicMapper) {
        this.musicRepository = musicRepository;
        this.musicMapper = musicMapper;
    }

    public MusicJSON getMusics() {
        List<Music> musics = new ArrayList<>();
        for (int i = 0; i <= 3; i++) {
            long randomId = ThreadLocalRandom.current().nextLong(150L);
            musics.add(musicRepository.getReferenceById(randomId));
            musics.get(i).setLyrics(textSongSplit(musics.get(i).getLyrics()));

        }
        return musicMapper.requestMusic(musics);
    }

    private String textSongSplit(String text) {
        String finalText = "";
        String[] textSong = text.split("\n");
        Random random = new Random();
        int index = random.nextInt(textSong.length);
        if (index <= 5) index = 5;
        if (index >= (textSong.length - 5)) index = textSong.length - 5;
        for (int i = 0; i <= 4; i++) {
            finalText += textSong[index + i] + "\n";
        }
        return finalText;
    }
}
