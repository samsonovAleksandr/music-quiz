package com.example.musicquix.utils;

import com.example.musicquix.model.Band;
import com.example.musicquix.model.Song;
import com.example.musicquix.parse.ParseHTML;
import com.example.musicquix.repository.BandRepository;
import com.example.musicquix.repository.SongRepository;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
@Component
public class AddDataBaseThead {

    private static final int THREAD_POOL_SIZE = 8; // Количество потоков в пуле

    static SongRepository songRepository;

    static BandRepository bandRepository;

    public AddDataBaseThead(SongRepository songRepository, BandRepository bandRepository) {
        AddDataBaseThead.songRepository = songRepository;
        AddDataBaseThead.bandRepository = bandRepository;
    }

    public static void addSong(int i) throws IOException, ParseException {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        try {
            List<String> listAlphabet = ParseHTML.linksAlphabets(i);

            for (String link : listAlphabet) {
                CompletableFuture<Void> futureAuthor = CompletableFuture.runAsync(() -> {
                    try {
                        for (String linkAuthor : ParseHTML.authorList(link)) {
                            Band band = new Band();
                            try {
                                band.setName(ParseHTML.nameAuthor(linkAuthor));
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            try {
                                band.setGenres(ParseHTML.bandInfo(linkAuthor, "Жанры"));
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            try {
                                band.setLanguageList(ParseHTML.bandInfo(linkAuthor, "Язык"));
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            try {
                                band.setCountrys(ParseHTML.bandInfo(linkAuthor, "Страна"));
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
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
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                }, executor);
                futureAuthor.thenAccept((v) -> System.out.println("Finished processing authors for link " + link));
            }

            executor.shutdown();
            while (!executor.isTerminated()) {}
        } finally {
            if (!executor.isShutdown()) {
                executor.shutdownNow();
            }
        }
    }
}
