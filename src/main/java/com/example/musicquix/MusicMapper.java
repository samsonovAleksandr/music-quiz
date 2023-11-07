package com.example.musicquix;

import com.example.musicquix.model.Music;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
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
        musicDTOS.get(0).setRight(true);
        return MusicJSON.builder()
                .text(musics.get(0).getLyrics())
                .options(musicDTOS)
                .build();
    }
}
