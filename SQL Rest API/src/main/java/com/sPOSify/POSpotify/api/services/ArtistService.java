package com.sPOSify.POSpotify.api.services;

import com.sPOSify.POSpotify.api.assemblers.ArtistsAssembler;
import com.sPOSify.POSpotify.api.assemblers.SongPartialDTOAssembler;
import com.sPOSify.POSpotify.api.controllers.ArtistController;
import com.sPOSify.POSpotify.api.controllers.SongController;
import com.sPOSify.POSpotify.errorHandling.*;
import com.sPOSify.POSpotify.errorHandling.customExceptions.ArtistNotFound;
import com.sPOSify.POSpotify.errorHandling.customExceptions.ArtistParameterException;
import com.sPOSify.POSpotify.errorHandling.customExceptions.JPAException;
import com.sPOSify.POSpotify.errorHandling.customExceptions.SongsAlbumsNotFound;
import com.sPOSify.POSpotify.jpa.dto.ArtistWithSongsDTO;
import com.sPOSify.POSpotify.jpa.dto.SongPartialDTO;
import com.sPOSify.POSpotify.jpa.song_related.Artist;
import com.sPOSify.POSpotify.jpa.song_related.ArtistRepository;
import com.sPOSify.POSpotify.jpa.song_related.Song;
import com.sPOSify.POSpotify.jpa.song_related.SongsAlbumsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Service
public class ArtistService implements IArtist {

    @Autowired
    private final ArtistRepository artistRepository;

    @Autowired
    private final SongsAlbumsRepository songsAlbumsRepository;

    @Autowired
    private final ArtistsAssembler artistsAssembler;

    @Autowired
    private final SongPartialDTOAssembler songPartialDTOAssembler;

    public ArtistService(ArtistRepository artistRepository, SongsAlbumsRepository songsAlbumsRepository, ArtistsAssembler artistsAssembler, SongPartialDTOAssembler songPartialDTOAssembler) {
        this.artistRepository = artistRepository;
        this.songsAlbumsRepository = songsAlbumsRepository;
        this.artistsAssembler = artistsAssembler;
        this.songPartialDTOAssembler = songPartialDTOAssembler;
    }

//=============================================================================================================
//======================================== GET ================================================================
//=============================================================================================================

    @Override
    public CollectionModel<EntityModel<Artist>> getAllArtists(Optional<Pageable> pageable) {
        List<Artist> artists = pageable.isPresent() ? artistRepository.findAll(pageable.get()) : (List<Artist>) artistRepository.findAll();
        if (artists.isEmpty())
            throw new ArtistNotFound("There's no data available for the chosen search parameters.", null);

        List<EntityModel<Artist>> artistList = artists.stream()
                .map(artistsAssembler::toModel)
                .collect(Collectors.toList());

        return CollectionModel.of(artistList, linkTo(methodOn(ArtistController.class).getComplexSearch(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), Optional.empty(), Optional.empty(), Optional.empty())).withSelfRel());
    }

    @Override
    public EntityModel<ArtistWithSongsDTO> getArtistById(String uuid) {
        Artist artist = artistRepository.findById(uuid)
                .orElseThrow(() -> new ArtistNotFound(String.format("Artist not found: invalid argument %s : %s", "id", uuid), linkTo(methodOn(ArtistController.class).getComplexSearch(Optional.empty(),Optional.empty(),Optional.empty(),Optional.empty(),Optional.empty())).toString()));

        ArtistWithSongsDTO dto = new ArtistWithSongsDTO(uuid, artist.getName());
        List<Song> listSongs = songsAlbumsRepository.findSongsAlbumsByArtistsUuid(uuid);
        if (!listSongs.isEmpty()) {
            List<EntityModel<SongPartialDTO>> songs = listSongs.stream().
                    map(song -> new SongPartialDTO(song.getId(), song.getName(), Song.Genre.valueOf(song.getGenre()), Song.Type.valueOf(song.getType()))).
                    map(songPartialDTOAssembler::toModel).collect(Collectors.toList());

            dto.setSongs(songs);
            return EntityModel.of(dto,
                    linkTo(methodOn(ArtistController.class).getArtistById(uuid)).withSelfRel(),
                    linkTo(methodOn(ArtistController.class).getComplexSearch(Optional.empty(),Optional.empty(),Optional.empty(),Optional.empty(),Optional.empty())).withRel("parent"));
        } else throw new SongsAlbumsNotFound("This artist does not have any songs!", linkTo(methodOn(SongController.class).getByQuery(Optional.empty(),Optional.empty(),Optional.empty(),Optional.empty(),Optional.empty(),Optional.empty(),Optional.empty())).toString());
    }

    @Override
    public CollectionModel<EntityModel<Song>> getByArtistId(String artistId) {
        List<Song> list = songsAlbumsRepository.findSongsAlbumsByArtistsUuid(artistId);
        if (list.size() != 0) {
            List<EntityModel<Song>> musicListArtistId = list.stream()
                    .map(song -> EntityModel.of(song,
                            linkTo(methodOn(SongController.class).getSongById(song.getId())).withSelfRel(),
                            linkTo(methodOn(SongController.class).getByQuery(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).withRel("parent")))
                    .collect(Collectors.toList());

            return CollectionModel.of(musicListArtistId,
                    linkTo(methodOn(ArtistController.class).getSongsByArtistId(artistId)).withSelfRel(),
                    linkTo(methodOn(ArtistController.class).getArtistById(artistId)).withRel("parent"));
        } else throw new SongsAlbumsNotFound("This artist does not have any songs!", linkTo(methodOn(SongController.class).getByQuery(Optional.empty(),Optional.empty(),Optional.empty(),Optional.empty(),Optional.empty(),Optional.empty(),Optional.empty())).toString());
    }

    @Override
    public CollectionModel<EntityModel<Artist>> getArtistByNameAndActivity(String name, String match, Boolean isActive, Optional<Pageable> pageable) {
        List<Artist> artists = pageable.isPresent() ? artistRepository.findComplexSearch(name, isActive, pageable.get()) : artistRepository.findComplexSearch(name, isActive);

        if (artists.size() != 0) {
            List<EntityModel<Artist>> artistsList = artists.stream()
                    .map(song -> EntityModel.of(song,
                            linkTo(methodOn(ArtistController.class).getComplexSearch(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), name.describeConstable(), match.describeConstable(), Optional.of(String.valueOf(isActive)))).withSelfRel(),
                            linkTo(methodOn(ArtistController.class).getComplexSearch(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).withRel("parent")))
                    .collect(Collectors.toList());

            return CollectionModel.of(artistsList,
                    linkTo(methodOn(ArtistController.class).getComplexSearch(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), name.describeConstable(), match.describeConstable(), Optional.of(String.valueOf(isActive)))).withSelfRel());}
        else throw new ArtistNotFound("There's no data available for the chosen search parameters. ", linkTo(methodOn(ArtistController.class).getComplexSearch(Optional.empty(),Optional.empty(),Optional.empty(),Optional.empty(),Optional.empty())).toString());

    }

    @Override
    public CollectionModel<EntityModel<Artist>> getArtistByName(String name, String match, Optional<Pageable> pageable) {
        List<Artist> artists = pageable.isPresent() ? artistRepository.findComplexSearch(name, null, pageable.get()) : artistRepository.findComplexSearch(name, null);

        if (artists.size() != 0) {
            List<EntityModel<Artist>> artistsList = artists.stream()
                    .map(song -> EntityModel.of(song,
                            linkTo(methodOn(ArtistController.class).getComplexSearch(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), name.describeConstable(), match.describeConstable(), Optional.empty())).withSelfRel(),
                            linkTo(methodOn(ArtistController.class).getComplexSearch(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).withRel("parent")))
                    .collect(Collectors.toList());

            return CollectionModel.of(artistsList, linkTo(methodOn(ArtistController.class).getComplexSearch(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), name.describeConstable(), match.describeConstable(), Optional.empty())).withSelfRel());
        } else throw new ArtistNotFound("There's no data available for the chosen search parameters. ", linkTo(methodOn(ArtistController.class).getComplexSearch(Optional.empty(),Optional.empty(),Optional.empty(),Optional.empty(),Optional.empty())).toString());

    }

    @Override
    public CollectionModel<EntityModel<Artist>> getArtistByNameLikeAndActivity(String name, Boolean isActive, Optional<Pageable> pageable) {
        List<Artist> artists = pageable.isPresent() ? artistRepository.findComplexSearch("%" + name + "%", isActive, pageable.get()) : artistRepository.findComplexSearch("%" + name + "%", isActive);
        if (artists.size() != 0) {
            List<EntityModel<Artist>> artistsList = artists.stream()
                    .map(song -> EntityModel.of(song,
                            linkTo(methodOn(ArtistController.class).getComplexSearch(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), name.describeConstable(), Optional.empty(), Optional.of(String.valueOf(isActive)))).withSelfRel(),
                            linkTo(methodOn(ArtistController.class).getComplexSearch(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).withRel("parent")))
                    .collect(Collectors.toList());

            return CollectionModel.of(artistsList, linkTo(methodOn(ArtistController.class).getComplexSearch(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), name.describeConstable(), Optional.empty(), Optional.of(String.valueOf(isActive)))).withSelfRel());
        } else throw new ArtistNotFound("There's no data available for the chosen search parameters. ", linkTo(methodOn(ArtistController.class).getComplexSearch(Optional.empty(),Optional.empty(),Optional.empty(),Optional.empty(),Optional.empty())).toString());

    }

    @Override
    public CollectionModel<EntityModel<Artist>> getArtistByNameLike(String name, Optional<Pageable> pageable) {
        List<Artist> artists = pageable.isPresent() ? artistRepository.findComplexSearch("%" + name + "%", null, pageable.get()) : artistRepository.findComplexSearch("%" + name + "%", null);
        if (artists.size() != 0) {
            List<EntityModel<Artist>> artistsList = artists.stream()
                    .map(song -> EntityModel.of(song,
                            linkTo(methodOn(ArtistController.class).getComplexSearch(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), name.describeConstable(), Optional.empty(), Optional.empty())).withSelfRel(),
                            linkTo(methodOn(ArtistController.class).getComplexSearch(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).withRel("parent")))
                    .collect(Collectors.toList());

            return CollectionModel.of(artistsList, linkTo(methodOn(ArtistController.class).getComplexSearch(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), name.describeConstable(), Optional.empty(), Optional.empty())).withSelfRel());
        } else throw new ArtistNotFound("There's no data available for the chosen search parameters. ", linkTo(methodOn(ArtistController.class).getComplexSearch(Optional.empty(),Optional.empty(),Optional.empty(),Optional.empty(),Optional.empty())).toString());

    }

    @Override
    public CollectionModel<EntityModel<Artist>> getArtistByActivity(Boolean isActive, Optional<Pageable> pageable) {
        List<Artist> artists = pageable.isPresent() ? artistRepository.findComplexSearch(null, isActive, pageable.get()) : artistRepository.findComplexSearch(null, isActive);
        if (artists.size() != 0) {
            List<EntityModel<Artist>> artistsList = artists.stream()
                    .map(song -> EntityModel.of(song,
                            linkTo(methodOn(ArtistController.class).getComplexSearch(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), Optional.empty(), Optional.empty(), Optional.of(String.valueOf(isActive)))).withSelfRel(),
                            linkTo(methodOn(ArtistController.class).getComplexSearch(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).withRel("parent")))
                    .collect(Collectors.toList());

            return CollectionModel.of(artistsList, linkTo(methodOn(ArtistController.class).getComplexSearch(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), Optional.empty(), Optional.empty(), Optional.of(String.valueOf(isActive)))).withSelfRel());
        } else throw new ArtistNotFound("There's no data available for the chosen search parameters. ", linkTo(methodOn(ArtistController.class).getComplexSearch(Optional.empty(),Optional.empty(),Optional.empty(),Optional.empty(),Optional.empty())).toString());

    }

    @Override
    public CollectionModel<EntityModel<Artist>> getByComplexSearch(Optional<String> name, Optional<String> match, Optional<Boolean> isActive, Optional<Pageable> pageable) {
        int choice = 0;
        choice += name.isPresent() ? 100 : 0;
        choice += match.filter(s -> s.equals("exact")).map(s -> 10).orElse(0);
        choice += isActive.isPresent() ? 1 : 0;
        switch (choice) {
            case 111 -> {
                return getArtistByNameAndActivity(name.get(), match.get(), isActive.get(), pageable);
            }
            case 110 -> {
                return getArtistByName(name.get(), match.get(), pageable);
            }
            case 101 -> {
                return getArtistByNameLikeAndActivity(name.get(), isActive.get(), pageable);
            }
            case 100 -> {
                return getArtistByNameLike(name.get(), pageable);
            }
            case 1 -> {
                return getArtistByActivity(isActive.get(), pageable);
            }
            default -> {
                return getAllArtists(pageable);
            }
        }
    }


//=============================================================================================================
//======================================== PUT ================================================================
//=============================================================================================================

    @Override
    public ResponseEntity<?> replaceArtist(Artist artist, String uuid) {

        AtomicBoolean created = new AtomicBoolean(true);

        if (artist.getUuid() == null)
            throw new ArtistParameterException("ID is missing. Please enter an ID, give the artist a personality");
        else if (artist.getUuid().length() > 13) // abis, asta e un CNP
            throw new ArtistParameterException("ID format is wrong. Length must be maximum 13 characters long.");
        else {
            if (!Pattern.compile("^[0-9]+$")
                    .matcher(artist.getUuid())
                    .matches())
                throw new ArtistParameterException("ID must be a numeric value!");
        }



        if (!(artist.getIsActive().toString().equals("true") ||
                artist.getIsActive().toString().equals("false")))
        {
            throw new ArtistParameterException("Please give a valid value: true, false or null");
        }

        Artist updatedArtist = artistRepository.findById(uuid)
                .map(updated -> {
                    created.set(false);
                    updated.setName(artist.getName());
                    updated.setIsActive(artist.getIsActive());
                    try {
                        return artistRepository.save(updated);
                    } catch (DataAccessException ex) {
                        throw new JPAException(Objects.requireNonNull(ex.getRootCause()).toString());
                    }
                })
                .orElseGet(() -> {
                    artist.setUuid(uuid);
                    try {
                        return artistRepository.save(artist);
                    } catch (DataAccessException ex) {
                        throw new JPAException(Objects.requireNonNull(ex.getRootCause()).toString());
                    }
                });
        if (created.get()) {
            EntityModel<Artist> entityModel = artistsAssembler.toModel(updatedArtist);

            return ResponseEntity
                    .created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                    .body(entityModel);
        } else {
            return ResponseEntity.noContent().build();
        }
    }

//==============================================================================================================
//======================================== DELETE ==============================================================
//==============================================================================================================

    @Override
    public EntityModel<Artist> deleteArtist(String uuid) {
        Optional<Artist> toBeDeleted = artistRepository.findById(uuid);
        if (toBeDeleted.isPresent()) {
            artistRepository.deleteById(uuid);
            return artistsAssembler.toModel(toBeDeleted.get());
        } else
            throw new ArtistNotFound(String.format("Artist not found: invalid argument (%s) : (%s)", "id", uuid), linkTo(methodOn(ArtistController.class).getComplexSearch(Optional.empty(),Optional.empty(),Optional.empty(),Optional.empty(),Optional.empty())).toString());

    }

}
