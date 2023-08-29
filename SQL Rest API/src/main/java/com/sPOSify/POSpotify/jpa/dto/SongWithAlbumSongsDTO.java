package com.sPOSify.POSpotify.jpa.dto;

import com.sPOSify.POSpotify.jpa.song_related.Artist;
import com.sPOSify.POSpotify.jpa.song_related.Song;
import lombok.Getter;
import lombok.Setter;
import org.springframework.hateoas.EntityModel;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SongWithAlbumSongsDTO {
    @Getter
    private final Integer id;

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
    @Setter
    private Song.Genre genre;

    @Getter
    @Setter
    private EntityModel<SongPartialDTO> album;

    @Getter
    @Setter
    private List<EntityModel<BaldSongDTO>> albumSongs;

    @Getter
    @Setter
    private Set<EntityModel<Artist>> artist;

    public SongWithAlbumSongsDTO(Song song) {
        this.id = song.getId();
        this.name = song.getName();
        this.year = song.getYear();
        this.type = Song.Type.valueOf(song.getType());
        this.genre = Song.Genre.valueOf(song.getGenre());
        this.artist = new HashSet<>();
    }

    public void addArtist(EntityModel<Artist> artist) {
        this.artist.add(artist);
    }
}
