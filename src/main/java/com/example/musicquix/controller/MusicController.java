package com.example.musicquix.controller;

import com.example.musicquix.parse.ParseHTML;
import com.example.musicquix.service.MusicService;
import com.example.musicquix.utils.AddDataBaseThead;
import com.example.musicquix.utils.AddDatabaseData;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.text.ParseException;

@RestController
public class MusicController {
    final MusicService musicService;
    final AddDataBaseThead addDatabaseData;

    public MusicController(MusicService musicService, AddDataBaseThead addDatabaseData) {
        this.musicService = musicService;
        this.addDatabaseData = addDatabaseData;
    }


    @GetMapping("test")
    public @ResponseBody String test() throws IOException, ParseException {
      AddDataBaseThead.addSong(2);
      AddDataBaseThead.addSong(1);
      return "OK";
    }
}
