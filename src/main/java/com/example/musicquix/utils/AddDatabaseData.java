package com.example.musicquix.utils;


import com.example.musicquix.model.Band;
import com.example.musicquix.model.Song;
import com.example.musicquix.parse.ParseHTML;
import com.example.musicquix.repository.BandRepository;
import com.example.musicquix.repository.SongRepository;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

@Component
public class AddDatabaseData {

    final SongRepository songRepository;

    final BandRepository bandRepository;

    public AddDatabaseData(SongRepository songRepository, BandRepository bandRepository) {
        this.songRepository = songRepository;
        this.bandRepository = bandRepository;
    }



    public void addSong(int i) throws IOException, ParseException {

        List<String> listAlphabet = ParseHTML.linksAlphabets(i);
        for (String link : listAlphabet) {
            for (String linkAuthor : ParseHTML.authorList(link)) {
                Band band = new Band();
                band.setName(ParseHTML.nameAuthor(linkAuthor));
                band.setGenres(ParseHTML.bandInfo(linkAuthor, "Жанры"));
                band.setLanguageList(ParseHTML.bandInfo(linkAuthor, "Язык"));
                band.setCountrys(ParseHTML.bandInfo(linkAuthor, "Страна"));
                bandRepository.save(band);
                System.out.println(band);
                for (String songLink : ParseHTML.songList(linkAuthor)) {
                    Song song = new Song();
                    song.setBandId(band.getId());
                    song.setNameSong(ParseHTML.songName(songLink));
                    song.setTextSong(ParseHTML.textSong(songLink));
                    song.setReleaseDate(ParseHTML.releaseDateSong(songLink));
                    songRepository.save(song);
                    System.out.println(song);
                }
            }
        }
    }
}
