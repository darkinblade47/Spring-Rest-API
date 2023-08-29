package com.sPOSify.POSpotify.jpa.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.hateoas.EntityModel;

import java.util.List;

public class ArtistWithSongsDTO {
    @Getter
    @Setter
    private String id;

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private List<EntityModel<SongPartialDTO>> songs;

    public ArtistWithSongsDTO(String id, String name) {
        this.id = id;
        this.name = name;
    }
}
