package com.example.musicquix.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SongDTO {

    private Map<String, String> songBand;

    private List<String> bandNames;


}
