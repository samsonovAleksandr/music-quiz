package com.example.musicquix;

import com.example.musicquix.model.Music;
import com.example.musicquix.repository.MusicRepository;
import lombok.Data;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;

import org.jsoup.nodes.Document;
import java.io.File;
import java.io.IOException;

@Data
@Component
public class Parse {


   private final MusicRepository musicRepository;

    public Parse(MusicRepository musicRepository) throws IOException {
        this.musicRepository = musicRepository;
    }

    public void parseFileSong() throws IOException {
        File folder = new File("C://Downloaded Web Sites/alloflyrics.cc/test");
        File[] listFile = folder.listFiles();

        assert listFile != null;
        for (File file : listFile) {
            if (file.isFile()) {
                Document doc = Jsoup.parse(file);
                String tx = doc.outerHtml();

                String text = doc.text();
                String[] split = text.split("Текст песни");
                String[] split2 = split[0].split(" - ");
                String nameMusician = split2[0]; //имя исполнителя
                String nameSong = split2[1].replace(" текст песни", ""); // название песни
                String textSong = textSong(tx); // текст песни
                addDatabase(nameMusician, nameSong, textSong);

            }
        }

    }

    private String textSong (String tx) {
        int index;
        index = tx.indexOf("jumbotron");
        String tx2 = tx.substring(index+52);
        index = tx2.indexOf("</p>");
        tx2 = tx2.substring(0, index);
        tx2 = tx2.replace("<br>", "");
        if (tx2.contains("&nbsp;")) {
            tx2 = tx2.replace("&nbsp;", " ");
        }
        return "         " + tx2;
    }

    private void addDatabase (String nameMusician, String nameSong, String textSong) {
        Music music = Music.builder()
                .artist(nameMusician)
                .title(nameSong)
                .lyrics(textSong)
                .build();
        musicRepository.save(music);
    }

}
