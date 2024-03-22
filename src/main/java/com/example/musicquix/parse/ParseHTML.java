package com.example.musicquix.parse;

import lombok.Data;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@Data
public class ParseHTML {

    final static String LINK = "https://muztext.com";

    public static String parse() throws IOException {
        Document doc = Jsoup.connect(LINK).userAgent("Chrome/4.0.249.0 Safari/532.5").validateTLSCertificates(false).get();
        Elements element = doc.select("#page > div > main > center > div > div:nth-child(2) > a");
        List<String> linksAlphabet = element.eachAttr("href");


        for (int i = 0; i < linksAlphabet.size(); i++){
            Document doc1 = Jsoup.connect(LINK + linksAlphabet.get(i)).userAgent("Chrome/4.0.249.0 Safari/532.5").validateTLSCertificates(false).get();
            paginationList(doc1);
        }

        return linksAlphabet.toString();
    }


    public static List<String> paginationList(Document doc){
        Elements element2 = doc.select("#page > div > main > div > center > ul > li > a");

        List<String> links = new ArrayList<>();
        for (int i = 0; i <= element2.size(); i++ ){
            Elements element1 = doc.select("#page > div > main > div > center > ul > li:nth-child(" + (i - 1) + ") > a");
            links.addAll(element1.eachAttr("href"));
            System.out.println(links);
        }

        return null;
    }


}
