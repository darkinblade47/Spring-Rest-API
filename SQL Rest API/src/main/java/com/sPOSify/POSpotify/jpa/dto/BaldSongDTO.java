package com.sPOSify.POSpotify.jpa.dto;

import lombok.Getter;
import lombok.Setter;

public class BaldSongDTO {
    @Getter
    @Setter
    private String name;

    public BaldSongDTO(String name) {
        this.name = name;
    }

}
