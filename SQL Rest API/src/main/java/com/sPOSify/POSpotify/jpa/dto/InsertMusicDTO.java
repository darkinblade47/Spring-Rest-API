package com.sPOSify.POSpotify.jpa.dto;

import com.sPOSify.POSpotify.jpa.song_related.Song;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
public class InsertMusicDTO {

    @Getter
    @Setter
    private List<String> artistIds;
    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private Integer year;

    @Getter
    @Setter
    private Song.Type type;

    @Getter
    private Song.Genre genre;

    @Getter
    @Setter
    private Integer parent;

    public void setGenre(String genre) {
        for (Song.Genre gen : Song.Genre.values()) {
            if (gen.name().equalsIgnoreCase(genre)) {
                this.genre = Song.Genre.valueOf(genre.toUpperCase());
                return;
            }
        }
        this.genre = Song.Genre.UNKNOWN;
    }

}
