package com.sPOSify.POSpotify.jpa.song_related;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "songs_and_albums")
public class Song {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    @Setter
    private Integer id;
    @Getter
    @Setter
    private String name;
    @Enumerated(EnumType.STRING)
    private Genre genre;
    @Getter
    @Setter
    private Integer year;
    @Enumerated(EnumType.STRING)
    private Type type;
    @Getter
    @Setter
    private Integer parent;


 @ManyToMany(cascade = {CascadeType.REMOVE}, fetch = FetchType.EAGER)
    @JoinTable(
            name = "songs_artists",
            joinColumns = {@JoinColumn(name = "song_id")},
            inverseJoinColumns = {@JoinColumn(name = "artist_id")})
    @JsonIgnore
    private Set<Artist> artists = new HashSet<>();
    @ManyToOne
    @JoinColumn(name = "parent", insertable = false, updatable = false)
    private Song album;
    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "id", insertable = false, updatable = false)
    private Set<Song> albumSongs = new HashSet<>();

    public Song() {
    }

    public Song(String name, Genre genre, Integer year, Type type, Integer parent) {
        this.name = name;
        this.genre = genre;
        this.year = year;
        this.type = type;
        this.parent = parent;
    }

    public Set<Song> getAlbumSongs() {
        return albumSongs;
    }

    public void setContainingSongs(Set<Song> albumSongs) {
        this.albumSongs = albumSongs;
    }

    public Song getAlbum() {
        return album;
    }

    public void setAlbum(Song album) {
        this.album = album;
    }


    public String getGenre() {
        return genre.toString();
    }

    public void setGenre(String genre) {
        for (Genre gen : Genre.values()) {
            if (gen.name().equalsIgnoreCase(genre)) {
                this.genre = Genre.valueOf(genre.toUpperCase());
                return;
            }
        }
        this.genre = Genre.UNKNOWN;
    }

    public String getType() {
        return type.toString();
    }

    public void setType(String type) {
        for (Type tip : Type.values()) {
            if (tip.name().equalsIgnoreCase(type)) {
                this.type = Type.valueOf(type.toUpperCase());
                return;
            }
        }
        this.type = Type.UNKNOWN;
    }

    public Set<Artist> getArtists() {
        return artists;
    }

    public void setArtists(Set<Artist> artists) {
        this.artists = artists;
    }


    @Override
    public String toString() {
        return switch (type) {
            case SONG -> "Song{" +
                    ", Id='" + id + '\'' +
                    ", Name=" + name + '\'' +
                    ", Genre=" + genre.toString() + '\'' +
                    ", Release Date=" + year + '\'' +
                    ", Parent ID=" + parent +
                    '}';
            case ALBUM -> "Album{" +
                    ", Id='" + id + '\'' +
                    ", Name=" + name + '\'' +
                    ", Genre=" + genre.toString() + '\'' +
                    ", Release Date=" + year + '\'' +
                    ", Parent ID=" + parent +
                    '}';
            case SINGLE -> "Single{" +
                    ", Id='" + id + '\'' +
                    ", Name=" + name + '\'' +
                    ", Genre=" + genre.toString() + '\'' +
                    ", Release Date=" + year + '\'' +
                    ", Parent ID=" + parent +
                    '}';
            case UNKNOWN -> null;
        };
    }

//    public void removeArtists(String uuid) {
//        Artist artist = this.artists.stream().filter(x -> x.getId() == uuid).findFirst().orElse(null);
//        if (artist != null) {
//            this.artists.remove(artist);
//            //artist.getSongs().remove(this);
//        }
//    }

    public enum Genre {ROCK, TRAP, RAP, TECHNO, POP, UNKNOWN}

    public enum Type {ALBUM, SONG, SINGLE, UNKNOWN}

}
