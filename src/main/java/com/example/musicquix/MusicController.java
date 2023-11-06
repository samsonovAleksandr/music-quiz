package com.example.musicquix;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.HashMap;

@Controller("/api")
public class MusicController {
    final MusicService musicService;

    public MusicController(MusicService musicService) {
        this.musicService = musicService;
    }

    @GetMapping("/song")
    public HashMap<String, String> getSongs() {
        return musicService.getMusics();
    }
}
