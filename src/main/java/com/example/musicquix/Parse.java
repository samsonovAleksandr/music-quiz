package com.example.musicquix;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class Parse {

//
//   private final AuthorRepository authorRepository;
//   private final SongRepository songRepository;
//   private final TextTrackRepository textTrackRepository;
//
//
//    public Parse(AuthorRepository authorRepository, SongRepository songRepository
//            , TextTrackRepository textTrackRepository) throws IOException {
//        this.authorRepository = authorRepository;
//        this.songRepository = songRepository;
//        this.textTrackRepository = textTrackRepository;
//        parseFileSong();
//    }
//
//
//    public void parseFileSong() throws IOException {
//        File folder = new File("C://Downloaded Web Sites/alloflyrics.cc/test/gg");
//        File[] listFile = folder.listFiles();
//
//        assert listFile != null;
//        for (File file : listFile) {
//            if (file.isFile()) {
//                Document doc = Jsoup.parse(file);
//                String tx = doc.outerHtml();
//
//                String text = doc.text();
//                String[] split = text.split("Текст песни");
//                String[] split2 = split[0].split(" - ");
//                String nameMusician = split2[0]; //имя исполнителя
//                String nameSong = split2[1].replace(" текст песни", ""); // название песни
//                String textSong = textSong(tx); // текст песни
//                addDatabase(nameMusician, nameSong, textSong);
//
//            }
//        }
//
//    }
//
//    private String textSong (String tx) {
//        int index;
//        index = tx.indexOf("jumbotron");
//        String tx2 = tx.substring(index+52);
//        index = tx2.indexOf("</p>");
//        tx2 = tx2.substring(0, index);
//        tx2 = tx2.replace("<br>", "");
//        if (tx2.contains("&nbsp;")) {
//            tx2 = tx2.replace("&nbsp;", " ");
//        }
//        return "         " + tx2;
//    }
//
//    private void addDatabase (String nameMusician, String nameSong, String textSong) {
//        Music music = Music.builder()
//                .authorName(nameMusician)
//                .build();
//        authorRepository.save(music);
//        Song song = Song.builder()
//                .authorId(music.getId())
//                .songName(nameSong)
//                .build();
//        songRepository.save(song);
//        TextTrack textTrack = TextTrack.builder()
//                .songId(song.getId())
//                .snippetText(textSong)
//                .build();
//        textTrackRepository.save(textTrack);
//    }

}
