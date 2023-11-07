package com.example.musicquix.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class MusicDTO {

    public String title;
    public String author;
    public boolean right = false;
}
