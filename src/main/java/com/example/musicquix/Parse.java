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
        File folder = new File("C://Downloaded Web Sites/alloflyrics.cc/test");
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


                System.out.println(nameMusician);
                System.out.println(nameSong);
                System.out.println(textSong(split));
            }
        }

    }

    private String textSong(String[] split) {
        String text;
        int index;
        StringBuilder stb = new StringBuilder(split[1]);
        String textSong = split[1];
        index = textSong.indexOf("Комментарии");

        String textSong1 = stb.substring(0, index);

        if (textSong1.substring((index - 26), index).equals(" Прочитать перевод текста ")) {
            text = textSong1.substring(0, (index - 26));
        } else {
            text = stb.substring(0, index);
        }

        if (text.contains(" № Топ ")) {
           int index2 = text.indexOf(" № Топ ");
           return text.substring(0, index2);
        }
        return text;
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
