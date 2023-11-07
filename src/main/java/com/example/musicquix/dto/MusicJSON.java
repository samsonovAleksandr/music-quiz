package com.example.musicquix.dto;

import com.example.musicquix.dto.MusicDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class MusicJSON {

    public String text;
    public List<MusicDTO> options;
}
