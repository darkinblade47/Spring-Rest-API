package com.sPOSify.POSpotify.jpa.song_related;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "artists")
public class Artist {
    @Id
    @Getter
    @Setter
    private String uuid;
    @Getter
    @Setter
    private String name;
    @Column(name = "is_active")
    @Getter
    @Setter
    private Boolean isActive;

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.REMOVE})
    @JoinTable(
            name = "songs_artists",
            joinColumns = {@JoinColumn(name = "artist_id")},
            inverseJoinColumns = {@JoinColumn(name = "song_id")})
    private final Set<Song> songs = new HashSet<>();

    public Artist() {
    }

    public Artist(String uuid, String name, Boolean isActive) {
        this.uuid = uuid;
        this.name = name;
        this.isActive = isActive;
    }

    public Set<Song> getSongs() {
        return songs;
    }

    public void addSongs(Song song) {
        this.songs.add(song);
        song.getArtists().add(this);
    }

    public void removeSongs(String idSong) {
        Song song = this.songs.stream().filter(a -> Objects.equals(a.getId(), idSong)).findFirst().orElse(null);
        if (song != null) {
            this.songs.remove(song);
            song.getArtists().remove(this);
        }
    }

    @Override
    public String toString() {
        return "Artist{" +
                ", Uuid = '" + uuid + '\'' +
                ", Name = '" + name + '\'' +
                ", IsActive = " + isActive.toString() +
                '}';
    }
}
