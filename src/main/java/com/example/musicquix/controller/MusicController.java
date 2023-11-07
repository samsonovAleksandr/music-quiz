package com.example.musicquix.controller;

import com.example.musicquix.service.MusicService;
import com.example.musicquix.dto.MusicJSON;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MusicController {
    final MusicService musicService;

    public MusicController(MusicService musicService) {
        this.musicService = musicService;
    }

    @CrossOrigin
    @GetMapping("/song")
    public @ResponseBody MusicJSON getSongs() {
        return musicService.getMusics();
    }
}
