package com.example.musicquix.mapper;

import com.example.musicquix.dto.MusicDTO;
import com.example.musicquix.dto.MusicJSON;
import com.example.musicquix.model.Music;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class MusicMapper {


    public MusicJSON requestMusic(List<Music> musics) {
        List<MusicDTO> musicDTOS = new ArrayList<>();
        for (Music m : musics) {
            MusicDTO musicDTO = MusicDTO.builder()
                    .author(m.getArtist())
                    .title(m.getTitle())
                    .build();
            musicDTOS.add(musicDTO);
        }

        Random random = new Random();
        int rd = random.nextInt(0,3);
        musicDTOS.get(rd).setRight(true);
        return MusicJSON.builder()
                .text(musics.get(rd).getLyrics())
                .options(musicDTOS)
                .build();
    }
}
