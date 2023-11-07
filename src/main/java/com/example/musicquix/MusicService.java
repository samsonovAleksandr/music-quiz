package com.example.musicquix;

import com.example.musicquix.model.Music;
import com.example.musicquix.repository.MusicRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
        long randomId = ThreadLocalRandom.current().nextLong(150L);

        List<Music> musics = new ArrayList<>();
        for (int i = 0; i <= 2; i++) {
            musics.add(musicRepository.getReferenceById(randomId));
        }
        return musicMapper.requestMusic(musics);
    }
}
