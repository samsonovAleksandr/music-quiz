package com.example.musicquix;

import com.example.musicquix.model.Music;
import com.example.musicquix.repository.MusicRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class MusicService {

    private final MusicRepository musicRepository;

    public MusicService(MusicRepository musicRepository) {
        this.musicRepository = musicRepository;
    }

    public HashMap<String, String> getMusics() {
        long randomId = ThreadLocalRandom.current().nextLong(5134856L);

        HashMap<String, String> musicMap = new HashMap<>();
        for (int i = 0; i <= 2; i++) {
            String artist = musicRepository.getReferenceById(randomId).getArtist();
            musicMap.put(artist, null);
        }

        Music music = musicRepository.getReferenceById(randomId);
        musicMap.put(music.getArtist(), music.getLyrics());

        return musicMap;
    }
}
