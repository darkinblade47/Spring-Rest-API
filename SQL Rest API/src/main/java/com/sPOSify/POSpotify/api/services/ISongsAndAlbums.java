package com.sPOSify.POSpotify.api.services;

import com.sPOSify.POSpotify.jpa.dto.InsertMusicDTO;
import com.sPOSify.POSpotify.jpa.song_related.Song;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

public interface ISongsAndAlbums {

    CollectionModel<EntityModel<Song>> getAllSongs(Optional<Pageable> pageable);


    EntityModel<?> getSongById(Integer id);

    CollectionModel<EntityModel<Song>> getByName(String name, Optional<Pageable> pageable);

    CollectionModel<EntityModel<Song>> getByNameLike(String name, String pattern, Optional<Pageable> pageable);

    CollectionModel<EntityModel<Song>> getByGenre(String genre, Optional<Pageable> pageable);

    CollectionModel<EntityModel<Song>> getByType(String type, Optional<Pageable> pageable);

    CollectionModel<EntityModel<Song>> getByYear(Integer year, Optional<Pageable> pageable);

    CollectionModel<EntityModel<Song>> getByExactNameGenreYearType(String name, String match, String genre, Integer year, String type, Optional<Pageable> pageable);

    CollectionModel<EntityModel<Song>> getByExactNameGenreYear(String name, String match, String genre, Integer year, Optional<Pageable> pageable);

    CollectionModel<EntityModel<Song>> getByExactNameGenreType(String name, String match, String genre, String type, Optional<Pageable> pageable);

    CollectionModel<EntityModel<Song>> getByExactNameGenre(String name, String match, String genre, Optional<Pageable> pageable);

    CollectionModel<EntityModel<Song>> getByExactNameYearType(String name, Integer year, String type, Optional<Pageable> pageable);

    CollectionModel<EntityModel<Song>> getByExactNameYear(String name, Integer year, Optional<Pageable> pageable);

    CollectionModel<EntityModel<Song>> getByExactNameType(String name, String type, Optional<Pageable> pageable);

    CollectionModel<EntityModel<Song>> getByNameLikeGenreYearType(String name, String genre, Integer year, String type, Optional<Pageable> pageable);

    CollectionModel<EntityModel<Song>> getByNameLikeGenreYear(String name, String genre, Integer year, Optional<Pageable> pageable);

    CollectionModel<EntityModel<Song>> getByNameLikeGenreType(String name, String genre, String type, Optional<Pageable> pageable);

    CollectionModel<EntityModel<Song>> getByNameLikeGenre(String name, String genre, Optional<Pageable> pageable);

    CollectionModel<EntityModel<Song>> getByNameLikeYearType(String name, Integer year, String type, Optional<Pageable> pageable);

    CollectionModel<EntityModel<Song>> getByNameLikeYear(String name, Integer year, Optional<Pageable> pageable);

    CollectionModel<EntityModel<Song>> getByNameLikeType(String name, String type, Optional<Pageable> pageable);

    CollectionModel<EntityModel<Song>> getByGenreYearType(String genre, Integer year, String type, Optional<Pageable> pageable);

    CollectionModel<EntityModel<Song>> getByGenreYear(String genre, Integer year, Optional<Pageable> pageable);

    CollectionModel<EntityModel<Song>> getByYearType(Integer year, String type, Optional<Pageable> pageable);

    CollectionModel<EntityModel<Song>> getByGenreType(String genre, String type, Optional<Pageable> pageable);

    CollectionModel<EntityModel<Song>> complexSearch(Optional<String> name,
                                                     Optional<String> match,
                                                     Optional<String> genre,
                                                     Optional<Integer> year,
                                                     Optional<String> type,
                                                     Optional<Pageable> pageable
    );

    EntityModel<Song> newMusic(InsertMusicDTO newMusic);

    ResponseEntity<?> replaceSong(Song song, Integer id);

    EntityModel<Song> deleteSong(@PathVariable Integer id);
}
