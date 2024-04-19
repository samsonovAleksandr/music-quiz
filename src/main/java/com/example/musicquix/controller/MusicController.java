package com.example.musicquix.controller;

import com.example.musicquix.parse.ParseHTML;
import com.example.musicquix.service.MusicService;
import com.example.musicquix.utils.AddDatabaseData;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.text.ParseException;

@RestController
public class MusicController {
    final MusicService musicService;
    final AddDatabaseData addDatabaseData;

    public MusicController(MusicService musicService, AddDatabaseData addDatabaseData) {
        this.musicService = musicService;
        this.addDatabaseData = addDatabaseData;
    }

    @GetMapping("test")
    public @ResponseBody String test() throws IOException, ParseException {
        addDatabaseData.addSong(2);
        addDatabaseData.addSong(1);
      return "OK";
    }
}
