package com.example.musicquix.controller;

import com.example.musicquix.parse.ParseHTML;
import com.example.musicquix.service.MusicService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class MusicController {
    final MusicService musicService;

    public MusicController(MusicService musicService) {
        this.musicService = musicService;
    }

    @GetMapping("test")
    public @ResponseBody String test() throws IOException {
      return   ParseHTML.parse();
    }
}
