package com.sPOSify.POSpotify.jpa.dto;

import com.sPOSify.POSpotify.jpa.song_related.Artist;
import com.sPOSify.POSpotify.jpa.song_related.Song;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

public class SongFullDTO {
    @Getter
    @Setter
    private Integer id;

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
    private Song album;

    @Getter
    @Setter
    private Set<Song> albumSongs;

    @Getter
    @Setter
    private Set<Artist> artists;

    public SongFullDTO(Song song) {
        this.id = song.getId();
        this.name = song.getName();
        this.year = song.getYear();
        this.type = Song.Type.valueOf(song.getType());
        this.genre = Song.Genre.valueOf(song.getGenre());
        this.album = song.getAlbum();
        this.albumSongs = song.getAlbumSongs();
        this.artists = new HashSet<>();
    }

    public void addArtist(Artist artist) {
        artists.add(artist);
    }

}
