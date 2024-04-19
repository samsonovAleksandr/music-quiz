package com.example.musicquix.parse;

import com.example.musicquix.repository.BandRepository;
import com.example.musicquix.repository.SongRepository;
import com.example.musicquix.utils.AddDatabaseData;
import lombok.Data;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
@Data
public class ParseHTML {
    final static String LINK = "https://muztext.com";

    final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    //Получение ссылок на алфавит  чилд 1 или 2 это выбор алфавита(англ или рус)
    public static List<String> linksAlphabets(int i) throws IOException {
        Document doc = Jsoup.connect(LINK).userAgent("Chrome/4.0.249.0 Safari/532.5").validateTLSCertificates(false).get();
        Elements element = doc.select("#page > div > main > center > div > div:nth-child(" + i + ") > a");
        List<String> linksAlphabets = element.eachAttr("href");
        List<String> linksPagination = new ArrayList<>();

        for (String linksAlphabet : linksAlphabets) {
            Document doc1 = Jsoup.connect(LINK + linksAlphabet).userAgent("Chrome/4.0.249.0 Safari/532.5").validateTLSCertificates(false).get();
            linksPagination.addAll(paginationList(doc1));
        }
        linksAlphabets.addAll(linksPagination);

        if (i == 1) {
            linksAlphabets.remove("/alphabet/0-9/1");
            linksAlphabets.remove("/alphabet/0-9/2");
            linksAlphabets.remove("/alphabet/0-9/3");
        }
        return linksAlphabets;
    }

    //Определяет все страницы с исполнителями если их больше 1
    public static List<String> paginationList(Document doc) {
        Elements element2 = doc.select("#page > div > main > div > center > ul > li > a");
        List<String> links = new ArrayList<>();
        for (int i = 0; i <= element2.size(); i++) {
            Elements element1 = doc.select("#page > div > main > div > center > ul > li:nth-child(" + (i - 1) + ") > a");
            links.addAll(element1.eachAttr("href"));
        }
        return links;
    }

    //Получение ссылок на исполнтелей по буквам.
    public static List<String> authorList(String linkAlphabet) throws IOException {
        Document doc = Jsoup.connect(LINK + linkAlphabet).userAgent("Chrome/4.0.249.0 Safari/532.5").validateTLSCertificates(false).get();
        Elements element2 = doc.getElementsByClass("indentation");
        return new ArrayList<>(element2.eachAttr("href"));
    }

    //Получени списка песен каждого исполнителя
    public static List<String> songList(String authorLink) throws IOException {
        Document document = Jsoup.connect(LINK + authorLink).userAgent("Chrome/4.0.249.0 Safari/532.5").validateTLSCertificates(false).get();
        Elements element = document.getElementsByAttributeValueContaining("href", "lyrics");
        return new ArrayList<>(element.eachAttr("href"));
    }

    //Имя исполнителя
    public static String nameAuthor(String authorLink) throws IOException {
        Document document = Jsoup.connect(LINK + authorLink).userAgent("Chrome/4.0.249.0 Safari/532.5").validateTLSCertificates(false).get();
        Elements elements = document.select("#page > div > div > div:nth-child(3) > h1");
        String[] str = elements.text().split(" - ");
        return str[0];
    }

    //Текст песни
    public static String textSong(String songLink) throws IOException {
        try {
            Document document = Jsoup.connect(LINK + songLink).userAgent("Chrome/4.0.249.0 Safari/532.5").validateTLSCertificates(false).get();
            Elements elements = document.getElementsByTag("tbody");
            List<String> list = elements.get(0).getElementsByTag("td").eachText();
            StringBuilder strBild = new StringBuilder();
            for (String str : list) {
                strBild.append(str).append("\n");
            }
            return String.valueOf(strBild);
        } catch (UnknownHostException ignored) {
            return "TextError";
        }
    }

    // Массив строчек песни
    public static List<String> textSongList(String songLink) throws IOException {
        Document document = Jsoup.connect(LINK + songLink).userAgent("Chrome/4.0.249.0 Safari/532.5").validateTLSCertificates(false).get();
        Elements elements = document.getElementsByTag("tbody");
        return elements.get(0).getElementsByTag("td").eachText();
    }

    //Название песни
    public static String songName(String songLink) throws IOException {
        try {
            Document document = Jsoup.connect(LINK + songLink).userAgent("Chrome/4.0.249.0 Safari/532.5").validateTLSCertificates(false).get();
            Elements elements = document.select("#text > table > thead > tr > td > h2");
            return elements.text();
        } catch (UnknownHostException ignored) {
           return "NameError";
        }
    }

    // Дата релиза трека
    public static Date releaseDateSong(String songLink) throws IOException, ParseException {
        try {
            Document document = Jsoup.connect(LINK + songLink).userAgent("Chrome/4.0.249.0 Safari/532.5").validateTLSCertificates(false).get();
            Elements elements = document.select("#page > div > main > div.entry-content.gray");
            List<String> str = Arrays.stream(elements.text().split(" ")).toList();
            SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
            Date date = null;
            for (String s : str) {
                try {
                    date = format.parse(s);
                } catch (ParseException ignored) {
                }
            }
            return date;
        } catch (UnknownHostException ignored) {
            return null;
        }
    }

    // Информация о группе, Языки Жанры и Страны
    public static List<String> bandInfo(String authorLink, String value) throws IOException {
        Document document = Jsoup.connect(LINK + authorLink).userAgent("Chrome/4.0.249.0 Safari/532.5").validateTLSCertificates(false).get();
        Elements elements = document.getElementsByAttributeValueContaining("itemprop", "text");
        String elementString = elements.toString().replace("\n", "");
        String[] str = elementString.split("<li>");
        List<String> list = new ArrayList<>();
        for (String s : str) {
            if (s.contains(value)) {
                String[] s1 = s.split(": ");
                String[] s2 = s1[1].split(", ");
                List<String> info = new ArrayList<>();
                for (String la : s2) {
                    if (la.contains("</li>")) {
                        info.add(la.substring(0, la.indexOf("<")));
                    } else {
                        info.add(la);
                    }
                }
                list.addAll(info);
            }
        }
        return list;
    }


}
