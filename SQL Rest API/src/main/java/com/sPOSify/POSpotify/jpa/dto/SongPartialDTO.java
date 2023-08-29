package com.sPOSify.POSpotify.jpa.dto;

import com.sPOSify.POSpotify.jpa.song_related.Song;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Builder
public class SongPartialDTO {
    @Getter
    @Setter
    private Integer id;

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private Song.Genre genre;

    @Getter
    @Setter
    private Song.Type type;

    public SongPartialDTO(Integer id, String name, Song.Genre genre, Song.Type type) {
        this.id = id;
        this.name = name;
        this.genre = genre;
        this.type = type;
    }

}
