package com.example.musicquix.service;

import com.example.musicquix.bot.Language;
import com.example.musicquix.dto.SongDTO;
import com.example.musicquix.model.Song;
import com.example.musicquix.repository.BandRepository;
import com.example.musicquix.repository.SongRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MusicService {

    @Autowired
    BandRepository bandRepository;

    @Autowired
    SongRepository songRepository;

    Random rnd = new Random();


    public SongDTO songLyrics(Language language) {

        List<Song> songs;
       if (language == Language.ENGLISH){
           songs = songRepository.getRandomSongs();
       } else {
        songs = songRepository.getRandomSongsRus();
       }


        int i = rnd.nextInt(songs.size());
        Song song = songs.get(i);

        List<String> texts = textList(song.getTextSong());

        StringBuilder stringBuilder = new StringBuilder();

        for (String s : texts) {
            stringBuilder.append(s).append("\n");
        }

        String bandName = bandRepository.getReferenceById(song.getBandId()).getName();
        songs.remove(i);


        List<String> bandsList = bandsName(songs);
        Map<String, String> songBands = new HashMap<>();
        songBands.put(stringBuilder.toString(), bandName);

        SongDTO songDTO = SongDTO.builder()
                .songBand(songBands)
                .bandNames(bandsList)
                .build();

        return songDTO;
    }


    private List<String> textList(String text) {

        String[] str = text.split("\n");

        int start = rnd.nextInt(str.length - 5);
        int stop = start + 4;
        List<String> textSongs = new ArrayList<>();

        for (int i = start; i <= stop; i++) {
            textSongs.add(str[i]);
        }

        return textSongs;
    }


    private List<String> bandsName(List<Song> songs) {

        List<String> bands = new ArrayList<>();

        for (Song s : songs) {
            long bandId = s.getBandId();
            bands.add(bandRepository.getReferenceById(bandId).getName());
        }
        return bands;

    }


}
