package com.example.musicquix;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class MusicController {
    final MusicService musicService;

    public MusicController(MusicService musicService) {
        this.musicService = musicService;
    }

    @GetMapping("/song")
    public @ResponseBody MusicJSON getSongs() {
        return musicService.getMusics();
    }
}
