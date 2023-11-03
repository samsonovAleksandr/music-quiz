package com.example.musicquix;

import lombok.Data;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@Component
public class Parse {


    public Parse() throws IOException {
        parseFileSong();
    }


    public void parseFileSong() throws IOException {
        File folder = new File("C://Downloaded Web Sites/alloflyrics.cc/song/Andro");
        File[] listFile = folder.listFiles();

        assert listFile != null;
        for (File file: listFile) {
            if (file.isFile()) {
                Document doc = Jsoup.parse(file);
                String text = doc.text();

                String[] split = text.split("Текст песни");

                String[] split2 = split[0].split(" - ");
                String nameMusician = split2[0];

                String nameSong = split2[1].replace(" текст песни", "");



                System.out.println(textSong(split));
                System.out.println(nameMusician);
                System.out.println(nameSong);
            }
        }

    }

    private String textSong(String[] split) {
        int index;
        StringBuilder stb = new StringBuilder(split[1]);
        String textSong = split[1];
        if (checkingTextForLanguage(textSong.substring(0,10))) {
            index = textSong.indexOf("Комментарии");
        } else {
            index = textSong.indexOf("Прочитать перевод текста");
        }

        return stb.substring(0, index);
        }

    private boolean checkingTextForLanguage(String text) {
        Pattern pattern = Pattern.compile(
                "[" +                   //начало списка допустимых символов
                        "а-яА-ЯёЁ" +    //буквы русского алфавита
                        "\\d" +         //цифры
                        "\\s" +         //знаки-разделители (пробел, табуляция и т.д.)
                        "\\p{Punct}" +  //знаки пунктуации
                        "]" +                   //конец списка допустимых символов
                        "*");                   //допускается наличие указанных символов в любом количестве

        Matcher matcher = pattern.matcher(text);
        return matcher.matches();
    }
}
