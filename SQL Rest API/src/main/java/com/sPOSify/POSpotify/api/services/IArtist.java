package com.sPOSify.POSpotify.api.services;

import com.sPOSify.POSpotify.jpa.dto.ArtistWithSongsDTO;
import com.sPOSify.POSpotify.jpa.song_related.Artist;
import com.sPOSify.POSpotify.jpa.song_related.Song;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

public interface IArtist {

    CollectionModel<EntityModel<Artist>> getAllArtists(Optional<Pageable> pageable);

    EntityModel<ArtistWithSongsDTO> getArtistById(String uuid);

    CollectionModel<EntityModel<Song>> getByArtistId(String artistId);

    CollectionModel<EntityModel<Artist>> getArtistByNameAndActivity(String name, String match, Boolean isActive, Optional<Pageable> pageable);

    CollectionModel<EntityModel<Artist>> getArtistByName(String name, String match, Optional<Pageable> pageable);

    CollectionModel<EntityModel<Artist>> getArtistByNameLikeAndActivity(String name, Boolean isActive, Optional<Pageable> pageable);

    CollectionModel<EntityModel<Artist>> getArtistByNameLike(String name, Optional<Pageable> pageable);

    CollectionModel<EntityModel<Artist>> getArtistByActivity(Boolean isActive, Optional<Pageable> pageable);

    CollectionModel<EntityModel<Artist>> getByComplexSearch(Optional<String> name, Optional<String> match, Optional<Boolean> isActive, Optional<Pageable> pageable);

    ResponseEntity<?> replaceArtist(Artist artist, String uuid);

    EntityModel<Artist> deleteArtist(String uuid);
}
