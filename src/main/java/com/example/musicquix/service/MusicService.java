package com.example.musicquix.service;

import com.example.musicquix.model.Song;
import com.example.musicquix.repository.BandRepository;
import com.example.musicquix.repository.SongRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class MusicService {

    @Autowired
    BandRepository bandRepository;

    @Autowired
    SongRepository songRepository;

    Random rnd = new Random();


    public String songLyrics() {
        List<Song> songs = songRepository.getRandomSongs();
        int i = rnd.nextInt(songs.size());
        Song song = songs.get(i);

        List<String> texts = textList(song.getTextSong());

        StringBuilder stringBuilder = new StringBuilder();

        for (String s: texts){
            stringBuilder.append(s).append("\n");
        }


        return stringBuilder.toString();
    }


    private List<String> textList(String text) {

        String[] str = text.split("\n");

        int start = rnd.nextInt(str.length - 5);
        int stop = start + 4;
        List<String> textSongs = new ArrayList<>();

        for(int i = start; i <= stop; i++ ) {
            textSongs.add(str[i]);
        }

        return textSongs;
    }



}
