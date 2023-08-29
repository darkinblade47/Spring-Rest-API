package com.sPOSify.POSpotify.api.services;

import com.sPOSify.POSpotify.api.assemblers.ArtistsAssembler;
import com.sPOSify.POSpotify.api.assemblers.SongPartialDTOAssembler;
import com.sPOSify.POSpotify.api.assemblers.SongWithAlbumSongsDTOAssembler;
import com.sPOSify.POSpotify.api.assemblers.SongsAlbumsAssembler;
import com.sPOSify.POSpotify.api.controllers.ArtistController;
import com.sPOSify.POSpotify.api.controllers.SongController;
import com.sPOSify.POSpotify.errorHandling.*;
import com.sPOSify.POSpotify.errorHandling.customExceptions.AlbumParentException;
import com.sPOSify.POSpotify.errorHandling.customExceptions.ArtistNotFound;
import com.sPOSify.POSpotify.errorHandling.customExceptions.ArtistParameterException;
import com.sPOSify.POSpotify.errorHandling.customExceptions.EnumException;
import com.sPOSify.POSpotify.errorHandling.customExceptions.JPAException;
import com.sPOSify.POSpotify.errorHandling.customExceptions.SongsAlbumsNotFound;
import com.sPOSify.POSpotify.jpa.dto.BaldSongDTO;
import com.sPOSify.POSpotify.jpa.dto.InsertMusicDTO;
import com.sPOSify.POSpotify.jpa.dto.SongPartialDTO;
import com.sPOSify.POSpotify.jpa.dto.SongWithAlbumSongsDTO;
import com.sPOSify.POSpotify.jpa.song_related.Artist;
import com.sPOSify.POSpotify.jpa.song_related.ArtistRepository;
import com.sPOSify.POSpotify.jpa.song_related.Song;
import com.sPOSify.POSpotify.jpa.song_related.SongsAlbumsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Service
public class SongsAndAlbumsService implements ISongsAndAlbums {

    @Autowired
    private final SongsAlbumsRepository songsAlbumsRepository;

    @Autowired
    private final SongsAlbumsAssembler songsAssembler;

    @Autowired
    private final SongPartialDTOAssembler songPartialDTOAssembler;

    @Autowired
    private final SongWithAlbumSongsDTOAssembler songWithAlbumSongsDTOAssembler;

    @Autowired
    private final ArtistsAssembler artistAssembler;
    private final ArtistRepository artistRepository;


    public SongsAndAlbumsService(SongsAlbumsRepository songsAlbumsRepository, SongsAlbumsAssembler songsAssembler, SongPartialDTOAssembler songPartialDTOAssembler, SongWithAlbumSongsDTOAssembler songWithAlbumSongsDTOAssembler, ArtistsAssembler artistAssembler,
                                 ArtistRepository artistRepository) {
        this.songsAlbumsRepository = songsAlbumsRepository;
        this.songsAssembler = songsAssembler;
        this.songPartialDTOAssembler = songPartialDTOAssembler;
        this.songWithAlbumSongsDTOAssembler = songWithAlbumSongsDTOAssembler;
        this.artistAssembler = artistAssembler;
        this.artistRepository = artistRepository;
    }

//=============================================================================================================
//======================================== GET ================================================================
//=============================================================================================================


    @Override
    public CollectionModel<EntityModel<Song>> getAllSongs(Optional<Pageable> pageable) {
        List<Song> list = pageable.isPresent() ? songsAlbumsRepository.findAll(pageable.get()) : (List<Song>) songsAlbumsRepository.findAll();

        if (list.size() != 0) {
            List<EntityModel<Song>> musicList = list.stream()
                    .map(songsAssembler::toModel)
                    .collect(Collectors.toList());
            return CollectionModel.of(musicList, linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).withSelfRel(),linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).withRel("parent"));
        } else throw new SongsAlbumsNotFound("There's no data available for the chosen search parameters. ", linkTo(methodOn(SongController.class).getByQuery(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).toString());

    }

    @Override
    public EntityModel<?> getSongById(Integer id) {
        Song music = songsAlbumsRepository.findById(id)
                .orElseThrow(() -> new SongsAlbumsNotFound(String.format("Song not found: invalid argument (%s) : (%s)", "id", id),linkTo(methodOn(SongController.class).getByQuery(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).withSelfRel().toString()));
            if(Objects.equals(music.getType(), Song.Type.ALBUM.name())) {
                music.setContainingSongs(songsAlbumsRepository.cautaAlbumele(id));

                SongWithAlbumSongsDTO dto = new SongWithAlbumSongsDTO(music);

                List<EntityModel<BaldSongDTO>> containingSongs = new ArrayList<>();
                Set<Song> albSongs = music.getAlbumSongs();
                for (Song s : albSongs) {
                    BaldSongDTO song = new BaldSongDTO(s.getName());
                    EntityModel<BaldSongDTO> entity = EntityModel.of(song,
                            WebMvcLinkBuilder.linkTo(methodOn(SongController.class).getSongById(s.getId())).withSelfRel(),
                            linkTo(methodOn(SongController.class).getByQuery(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).withRel("parent"));
                    containingSongs.add(entity);
                }
                containingSongs.stream().collect(Collectors.toList());

                if (!music.getArtists().isEmpty()) {
                    for (Artist artist : music.getArtists()) {
                        dto.addArtist(artistAssembler.toModel(artist));
                    }
                }

                dto.setAlbumSongs(containingSongs);
                return songWithAlbumSongsDTOAssembler.toModel(dto);
            }

        return songsAssembler.toModel(music);
    }

    @Override
    public CollectionModel<EntityModel<Song>> getByName(String name, Optional<Pageable> pageable) {
//----------------------------------------------------EXACT MATCH----------------------------------------------------------------------------
        List<Song> list = pageable.isPresent() ? songsAlbumsRepository.findByName(name, pageable.get()) : songsAlbumsRepository.findByName(name);

        if (list.size() != 0) {
            List<EntityModel<Song>> musicList = list.stream()
                    .map(song -> EntityModel.of(song,
                            linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), name.describeConstable(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).withSelfRel(),
                            linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).withRel("parent")))
                    .collect(Collectors.toList());

            return CollectionModel.of(musicList, linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), name.describeConstable(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).withSelfRel(),linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).withRel("parent"));
        } else throw new SongsAlbumsNotFound("There's no data available for the chosen search parameters. ", linkTo(methodOn(SongController.class).getByQuery(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).toString());
    }

    @Override
    public CollectionModel<EntityModel<Song>> getByNameLike(String name, String pattern, Optional<Pageable> pageable) {
//----------------------------------------------------PARTIAL MATCH----------------------------------------------------------------------------
        List<Song> list = pageable.isPresent() ? songsAlbumsRepository.findByNameLike(pattern, pageable.get()) : songsAlbumsRepository.findByNameLike(pattern);

        if (list.size() != 0) {
            List<EntityModel<Song>> musicList = list.stream()
                    .map(song -> EntityModel.of(song,
                            linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), name.describeConstable(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).withSelfRel(),
                            linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).withRel("parent")))
                    .collect(Collectors.toList());

            return CollectionModel.of(musicList, linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), name.describeConstable(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).withSelfRel(),linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).withRel("parent"));
        } else throw new SongsAlbumsNotFound("There's no data available for the chosen search parameters. ", linkTo(methodOn(SongController.class).getByQuery(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).toString());
    }

    @Override
    public CollectionModel<EntityModel<Song>> getByGenre(String genre, Optional<Pageable> pageable) {
        List<Song> list = pageable.isPresent() ? songsAlbumsRepository.findByGenre(Song.Genre.valueOf(genre.toUpperCase()), pageable.get()) : songsAlbumsRepository.findByGenre(Song.Genre.valueOf(genre.toUpperCase()));

        if (list.size() != 0) {
            List<EntityModel<Song>> musicList = list.stream()
                    .map(song -> EntityModel.of(song,
                            linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), Optional.empty(), Optional.empty(), genre.describeConstable(), Optional.empty(), Optional.empty())).withSelfRel(),
                            linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).withRel("parent")))
                    .collect(Collectors.toList());

            return CollectionModel.of(musicList, linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), Optional.empty(), Optional.empty(), genre.describeConstable(), Optional.empty(), Optional.empty())).withSelfRel(),linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).withRel("parent"));
        } else throw new SongsAlbumsNotFound("There's no data available for the chosen search parameters. ", linkTo(methodOn(SongController.class).getByQuery(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).toString());
    }

    @Override
    public CollectionModel<EntityModel<Song>> getByType(String type, Optional<Pageable> pageable) {
        List<Song> list = pageable.isPresent() ? songsAlbumsRepository.findByType(Song.Type.valueOf(type.toUpperCase()), pageable.get()) : songsAlbumsRepository.findByType(Song.Type.valueOf(type.toUpperCase()));

        if (list.size() != 0) {
            List<EntityModel<Song>> musicList = list.stream()
                    .map(song -> EntityModel.of(song,
                            linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), type.describeConstable())).withSelfRel(),
                            linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).withRel("parent")))
                    .collect(Collectors.toList());

            return CollectionModel.of(musicList, linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), type.describeConstable())).withSelfRel(),linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).withRel("parent"));
        } else throw new SongsAlbumsNotFound("There's no data available for the chosen search parameters. ", linkTo(methodOn(SongController.class).getByQuery(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).toString());

    }

    @Override
    public CollectionModel<EntityModel<Song>> getByYear(Integer year, Optional<Pageable> pageable) {
        List<Song> list = pageable.isPresent() ? songsAlbumsRepository.findByYear(year, pageable.get()) : songsAlbumsRepository.findByYear(year);

        if (list.size() != 0) {
            List<EntityModel<Song>> musicList = list.stream()
                    .map(song -> EntityModel.of(song,
                            linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), Optional.empty(), Optional.empty(), Optional.empty(), year.describeConstable(), Optional.empty())).withSelfRel(),
                            linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).withRel("parent")))
                    .collect(Collectors.toList());

            return CollectionModel.of(musicList, linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), Optional.empty(), Optional.empty(), Optional.empty(), year.describeConstable(), Optional.empty())).withSelfRel(),linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).withRel("parent"));
        } else throw new SongsAlbumsNotFound("There's no data available for the chosen search parameters. ", linkTo(methodOn(SongController.class).getByQuery(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).toString());

    }

//======================================== COMPLEX SEARCH =====================================================

    @Override
    public CollectionModel<EntityModel<Song>> getByExactNameGenreYearType(String name, String match, String genre, Integer year, String type, Optional<Pageable> pageable) {
        List<Song> list = pageable.isPresent() ?
                songsAlbumsRepository.findByNameAndGenreAndYearAndType(name, Song.Genre.valueOf(genre), year, Song.Type.valueOf(type.toUpperCase()), pageable.get())
                : songsAlbumsRepository.findByNameAndGenreAndYearAndType(name, Song.Genre.valueOf(genre), year, Song.Type.valueOf(type.toUpperCase()));

        if (list.size() != 0) {
            List<EntityModel<Song>> musicList = list.stream()
                    .map(song -> EntityModel.of(song,
                            linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), name.describeConstable(), match.describeConstable(), genre.describeConstable(), year.describeConstable(), type.describeConstable())).withSelfRel(),
                            linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).withRel("parent")))
                    .collect(Collectors.toList());

            return CollectionModel.of(musicList, linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), name.describeConstable(), match.describeConstable(), genre.describeConstable(), year.describeConstable(), type.describeConstable())).withSelfRel(),linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).withRel("parent"));
        } else throw new SongsAlbumsNotFound("There's no data available for the chosen search parameters. ", linkTo(methodOn(SongController.class).getByQuery(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).toString());

    }

    @Override
    public CollectionModel<EntityModel<Song>> getByExactNameGenreYear(String name, String match, String genre, Integer year, Optional<Pageable> pageable) {
        List<Song> list = pageable.isPresent() ? songsAlbumsRepository.findByNameAndGenreAndYear(name, Song.Genre.valueOf(genre), year, pageable.get()) : songsAlbumsRepository.findByNameAndGenreAndYear(name, Song.Genre.valueOf(genre), year);

        if (list.size() != 0) {
            List<EntityModel<Song>> musicList = list.stream()
                    .map(song -> EntityModel.of(song,
                            linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), name.describeConstable(), match.describeConstable(), genre.describeConstable(), year.describeConstable(), Optional.empty())).withSelfRel(),
                            linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).withRel("parent")))
                    .collect(Collectors.toList());

            return CollectionModel.of(musicList, linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), name.describeConstable(), match.describeConstable(), genre.describeConstable(), year.describeConstable(), Optional.empty())).withSelfRel(),linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).withRel("parent"));
        } else throw new SongsAlbumsNotFound("There's no data available for the chosen search parameters. ", linkTo(methodOn(SongController.class).getByQuery(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).toString());

    }

    @Override
    public CollectionModel<EntityModel<Song>> getByExactNameGenreType(String name, String match, String genre, String type, Optional<Pageable> pageable) {
        List<Song> list = pageable.isPresent() ? songsAlbumsRepository.findByNameAndGenreAndType(name, Song.Genre.valueOf(genre), Song.Type.valueOf(type.toUpperCase()), pageable.get()) : songsAlbumsRepository.findByNameAndGenreAndType(name, Song.Genre.valueOf(genre), Song.Type.valueOf(type.toUpperCase()));

        if (list.size() != 0) {
            List<EntityModel<Song>> musicList = list.stream()
                    .map(song -> EntityModel.of(song,
                            linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), name.describeConstable(), match.describeConstable(), genre.describeConstable(), Optional.empty(), type.describeConstable())).withSelfRel(),
                            linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).withRel("parent")))
                    .collect(Collectors.toList());

            return CollectionModel.of(musicList, linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), name.describeConstable(), match.describeConstable(), genre.describeConstable(), Optional.empty(), type.describeConstable())).withSelfRel(),linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).withRel("parent"));
        } else throw new SongsAlbumsNotFound("There's no data available for the chosen search parameters. ", linkTo(methodOn(SongController.class).getByQuery(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).toString());

    }

    @Override
    public CollectionModel<EntityModel<Song>> getByExactNameGenre(String name, String match, String genre, Optional<Pageable> pageable) {
        List<Song> list = pageable.isPresent() ? songsAlbumsRepository.findByNameAndGenre(name, Song.Genre.valueOf(genre), pageable.get()) : songsAlbumsRepository.findByNameAndGenre(name, Song.Genre.valueOf(genre));

        if (list.size() != 0) {
            List<EntityModel<Song>> musicList = list.stream()
                    .map(song -> EntityModel.of(song,
                            linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), name.describeConstable(), match.describeConstable(), genre.describeConstable(), Optional.empty(), Optional.empty())).withSelfRel(),
                            linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).withRel("parent")))
                    .collect(Collectors.toList());

            return CollectionModel.of(musicList, linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), name.describeConstable(), match.describeConstable(), genre.describeConstable(), Optional.empty(), Optional.empty())).withSelfRel(),linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).withRel("parent"));
        } else throw new SongsAlbumsNotFound("There's no data available for the chosen search parameters. ", linkTo(methodOn(SongController.class).getByQuery(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).toString());

    }

    @Override
    public CollectionModel<EntityModel<Song>> getByExactNameYearType(String name, Integer year, String type, Optional<Pageable> pageable) {
        List<Song> list = pageable.isPresent() ? songsAlbumsRepository.findByNameAndYearAndType(name, year, Song.Type.valueOf(type.toUpperCase()), pageable.get()) : songsAlbumsRepository.findByNameAndYearAndType(name, year, Song.Type.valueOf(type.toUpperCase()));

        if (list.size() != 0) {
            List<EntityModel<Song>> musicList = list.stream()
                    .map(song -> EntityModel.of(song,
                            linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), name.describeConstable(), Optional.empty(), Optional.empty(), year.describeConstable(), type.describeConstable())).withSelfRel(),
                            linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).withRel("parent")))
                    .collect(Collectors.toList());

            return CollectionModel.of(musicList, linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), name.describeConstable(), Optional.empty(), Optional.empty(), year.describeConstable(), type.describeConstable())).withSelfRel(),linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).withRel("parent"));
        } else throw new SongsAlbumsNotFound("There's no data available for the chosen search parameters. ", linkTo(methodOn(SongController.class).getByQuery(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).toString());

    }

    @Override
    public CollectionModel<EntityModel<Song>> getByExactNameYear(String name, Integer year, Optional<Pageable> pageable) {
        List<Song> list = pageable.isPresent() ? songsAlbumsRepository.findByNameAndYear(name, year, pageable.get()) : songsAlbumsRepository.findByNameAndYear(name, year);

        if (list.size() != 0) {
            List<EntityModel<Song>> musicList = list.stream()
                    .map(song -> EntityModel.of(song,
                            linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), name.describeConstable(), Optional.empty(), Optional.empty(), year.describeConstable(), Optional.empty())).withSelfRel(),
                            linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).withRel("parent")))
                    .collect(Collectors.toList());

            return CollectionModel.of(musicList, linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), name.describeConstable(), Optional.empty(), Optional.empty(), year.describeConstable(), Optional.empty())).withSelfRel(),linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).withRel("parent"));
        } else throw new SongsAlbumsNotFound("There's no data available for the chosen search parameters. ", linkTo(methodOn(SongController.class).getByQuery(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).toString());

    }

    @Override
    public CollectionModel<EntityModel<Song>> getByExactNameType(String name, String type, Optional<Pageable> pageable) {
        List<Song> list = pageable.isPresent() ? songsAlbumsRepository.findByNameAndType(name, Song.Type.valueOf(type.toUpperCase()), pageable.get()) : songsAlbumsRepository.findByNameAndType(name, Song.Type.valueOf(type.toUpperCase()));

        if (list.size() != 0) {
            List<EntityModel<Song>> musicList = list.stream()
                    .map(song -> EntityModel.of(song,
                            linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), name.describeConstable(), Optional.empty(), Optional.empty(), Optional.empty(), type.describeConstable())).withSelfRel(),
                            linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).withRel("parent")))
                    .collect(Collectors.toList());

            return CollectionModel.of(musicList, linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), name.describeConstable(), Optional.empty(), Optional.empty(), Optional.empty(), type.describeConstable())).withSelfRel(),linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).withRel("parent"));
        } else throw new SongsAlbumsNotFound("There's no data available for the chosen search parameters. ", linkTo(methodOn(SongController.class).getByQuery(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).toString());

    }

    @Override
    public CollectionModel<EntityModel<Song>> getByNameLikeGenreYearType(String name, String genre, Integer year, String type, Optional<Pageable> pageable) {
        String pattern = "%" + name + "%";
        List<Song> list = pageable.isPresent() ? songsAlbumsRepository.findByNameLikeAndGenreAndYearAndType(pattern, Song.Genre.valueOf(genre), year, Song.Type.valueOf(type.toUpperCase()), pageable.get()) : songsAlbumsRepository.findByNameLikeAndGenreAndYearAndType(pattern, Song.Genre.valueOf(genre), year, Song.Type.valueOf(type.toUpperCase()));

        if (list.size() != 0) {
            List<EntityModel<Song>> musicList = list.stream()
                    .map(song -> EntityModel.of(song,
                            linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), name.describeConstable(), Optional.empty(), genre.describeConstable(), year.describeConstable(), type.describeConstable())).withSelfRel(),
                            linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).withRel("parent")))
                    .collect(Collectors.toList());

            return CollectionModel.of(musicList, linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), name.describeConstable(), Optional.empty(), genre.describeConstable(), year.describeConstable(), type.describeConstable())).withSelfRel(),linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).withRel("parent"));
        } else throw new SongsAlbumsNotFound("There's no data available for the chosen search parameters. ", linkTo(methodOn(SongController.class).getByQuery(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).toString());

    }

    @Override
    public CollectionModel<EntityModel<Song>> getByNameLikeGenreYear(String name, String genre, Integer year, Optional<Pageable> pageable) {
        String pattern = "%" + name + "%";
        List<Song> list = pageable.isPresent() ? songsAlbumsRepository.findByNameLikeAndGenreAndYear(pattern, Song.Genre.valueOf(genre), year, pageable.get()) : songsAlbumsRepository.findByNameLikeAndGenreAndYear(pattern, Song.Genre.valueOf(genre), year);

        if (list.size() != 0) {
            List<EntityModel<Song>> musicList = list.stream()
                    .map(song -> EntityModel.of(song,
                            linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), name.describeConstable(), Optional.empty(), genre.describeConstable(), year.describeConstable(), Optional.empty())).withSelfRel(),
                            linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).withRel("parent")))
                    .collect(Collectors.toList());

            return CollectionModel.of(musicList, linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), name.describeConstable(), Optional.empty(), genre.describeConstable(), year.describeConstable(), Optional.empty())).withSelfRel(),linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).withRel("parent"));
        } else throw new SongsAlbumsNotFound("There's no data available for the chosen search parameters. ", linkTo(methodOn(SongController.class).getByQuery(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).toString());

    }

    @Override
    public CollectionModel<EntityModel<Song>> getByNameLikeGenreType(String name, String genre, String type, Optional<Pageable> pageable) {
        String pattern = "%" + name + "%";
        List<Song> list = pageable.isPresent() ? songsAlbumsRepository.findByNameLikeAndGenreAndType(pattern, Song.Genre.valueOf(genre), Song.Type.valueOf(type.toUpperCase()), pageable.get()) : songsAlbumsRepository.findByNameLikeAndGenreAndType(pattern, Song.Genre.valueOf(genre), Song.Type.valueOf(type.toUpperCase()));

        if (list.size() != 0) {
            List<EntityModel<Song>> musicList = list.stream()
                    .map(song -> EntityModel.of(song,
                            linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), name.describeConstable(), Optional.empty(), genre.describeConstable(), Optional.empty(), type.describeConstable())).withSelfRel(),
                            linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).withRel("parent")))
                    .collect(Collectors.toList());

            return CollectionModel.of(musicList, linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), name.describeConstable(), Optional.empty(), genre.describeConstable(), Optional.empty(), type.describeConstable())).withSelfRel(),linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).withRel("parent"));
        } else throw new SongsAlbumsNotFound("There's no data available for the chosen search parameters. ", linkTo(methodOn(SongController.class).getByQuery(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).toString());

    }

    @Override
    public CollectionModel<EntityModel<Song>> getByNameLikeGenre(String name, String genre, Optional<Pageable> pageable) {
        String pattern = "%" + name + "%";
        List<Song> list = pageable.isPresent() ? songsAlbumsRepository.findByNameLikeAndGenre(pattern, Song.Genre.valueOf(genre), pageable.get()) : songsAlbumsRepository.findByNameLikeAndGenre(pattern, Song.Genre.valueOf(genre));

        if (list.size() != 0) {
            List<EntityModel<Song>> musicList = list.stream()
                    .map(song -> EntityModel.of(song,
                            linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), name.describeConstable(), Optional.empty(), genre.describeConstable(), Optional.empty(), Optional.empty())).withSelfRel(),
                            linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).withRel("parent")))
                    .collect(Collectors.toList());

            return CollectionModel.of(musicList, linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), name.describeConstable(), Optional.empty(), genre.describeConstable(), Optional.empty(), Optional.empty())).withSelfRel(),linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).withRel("parent"));
        } else throw new SongsAlbumsNotFound("There's no data available for the chosen search parameters. ", linkTo(methodOn(SongController.class).getByQuery(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).toString());

    }

    @Override
    public CollectionModel<EntityModel<Song>> getByNameLikeYearType(String name, Integer year, String type, Optional<Pageable> pageable) {
        String pattern = "%" + name + "%";
        List<Song> list = pageable.isPresent() ? songsAlbumsRepository.findByNameLikeAndYearAndType(pattern, year, Song.Type.valueOf(type.toUpperCase()), pageable.get()) : songsAlbumsRepository.findByNameLikeAndYearAndType(pattern, year, Song.Type.valueOf(type.toUpperCase()));

        if (list.size() != 0) {
            List<EntityModel<Song>> musicList = list.stream()
                    .map(song -> EntityModel.of(song,
                            linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), name.describeConstable(), Optional.empty(), Optional.empty(), year.describeConstable(), type.describeConstable())).withSelfRel(),
                            linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).withRel("parent")))
                    .collect(Collectors.toList());

            return CollectionModel.of(musicList, linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), name.describeConstable(), Optional.empty(), Optional.empty(), year.describeConstable(), type.describeConstable())).withSelfRel(),linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).withRel("parent"));
        } else throw new SongsAlbumsNotFound("There's no data available for the chosen search parameters. ", linkTo(methodOn(SongController.class).getByQuery(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).toString());

    }

    @Override
    public CollectionModel<EntityModel<Song>> getByNameLikeYear(String name, Integer year, Optional<Pageable> pageable) {
        String pattern = "%" + name + "%";
        List<Song> list = pageable.isPresent() ? songsAlbumsRepository.findByNameLikeAndYear(pattern, year, pageable.get()) : songsAlbumsRepository.findByNameLikeAndYear(pattern, year);

        if (list.size() != 0) {
            List<EntityModel<Song>> musicList = list.stream()
                    .map(song -> EntityModel.of(song,
                            linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), name.describeConstable(), Optional.empty(), Optional.empty(), year.describeConstable(), Optional.empty())).withSelfRel(),
                            linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).withRel("parent")))
                    .collect(Collectors.toList());

            return CollectionModel.of(musicList, linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), name.describeConstable(), Optional.empty(), Optional.empty(), year.describeConstable(), Optional.empty())).withSelfRel(),linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).withRel("parent"));
        } else throw new SongsAlbumsNotFound("There's no data available for the chosen search parameters. ", linkTo(methodOn(SongController.class).getByQuery(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).toString());

    }

    @Override
    public CollectionModel<EntityModel<Song>> getByNameLikeType(String name, String type, Optional<Pageable> pageable) {
        String pattern = "%" + name + "%";
        List<Song> list = pageable.isPresent() ? songsAlbumsRepository.findByNameLikeAndType(pattern, Song.Type.valueOf(type.toUpperCase()), pageable.get()) : songsAlbumsRepository.findByNameLikeAndType(pattern, Song.Type.valueOf(type.toUpperCase()));

        if (list.size() != 0) {
            List<EntityModel<Song>> musicList = list.stream()
                    .map(song -> EntityModel.of(song,
                            linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), name.describeConstable(), Optional.empty(), Optional.empty(), Optional.empty(), type.describeConstable())).withSelfRel(),
                            linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).withRel("parent")))
                    .collect(Collectors.toList());

            return CollectionModel.of(musicList, linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), name.describeConstable(), Optional.empty(), Optional.empty(), Optional.empty(), type.describeConstable())).withSelfRel(),linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).withRel("parent"));
        } else throw new SongsAlbumsNotFound("There's no data available for the chosen search parameters. ", linkTo(methodOn(SongController.class).getByQuery(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).toString());

    }

    @Override
    public CollectionModel<EntityModel<Song>> getByGenreYearType(String genre, Integer year, String type, Optional<Pageable> pageable) {
        List<Song> list = pageable.isPresent() ? songsAlbumsRepository.findByGenreAndYearAndType(Song.Genre.valueOf(genre), year, Song.Type.valueOf(type.toUpperCase()), pageable.get()) : songsAlbumsRepository.findByGenreAndYearAndType(Song.Genre.valueOf(genre), year, Song.Type.valueOf(type.toUpperCase()));

        if (list.size() != 0) {
            List<EntityModel<Song>> musicList = list.stream()
                    .map(song -> EntityModel.of(song,
                            linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), Optional.empty(), Optional.empty(), genre.describeConstable(), year.describeConstable(), type.describeConstable())).withSelfRel(),
                            linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).withRel("parent")))
                    .collect(Collectors.toList());

            return CollectionModel.of(musicList, linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), Optional.empty(), Optional.empty(), genre.describeConstable(), year.describeConstable(), type.describeConstable())).withSelfRel(),linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).withRel("parent"));
        } else throw new SongsAlbumsNotFound("There's no data available for the chosen search parameters. ", linkTo(methodOn(SongController.class).getByQuery(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).toString());

    }

    @Override
    public CollectionModel<EntityModel<Song>> getByGenreYear(String genre, Integer year, Optional<Pageable> pageable) {
        List<Song> list = pageable.isPresent() ? songsAlbumsRepository.findByGenreAndYear(Song.Genre.valueOf(genre), year, pageable.get()) : songsAlbumsRepository.findByGenreAndYear(Song.Genre.valueOf(genre), year);

        if (list.size() != 0) {
            List<EntityModel<Song>> musicList = list.stream()
                    .map(song -> EntityModel.of(song,
                            linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), Optional.empty(), Optional.empty(), genre.describeConstable(), year.describeConstable(), Optional.empty())).withSelfRel(),
                            linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).withRel("parent")))
                    .collect(Collectors.toList());

            return CollectionModel.of(musicList, linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), Optional.empty(), Optional.empty(), genre.describeConstable(), year.describeConstable(), Optional.empty())).withSelfRel(),linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).withRel("parent"));
        } else throw new SongsAlbumsNotFound("There's no data available for the chosen search parameters. ", linkTo(methodOn(SongController.class).getByQuery(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).toString());

    }

    @Override
    public CollectionModel<EntityModel<Song>> getByYearType(Integer year, String type, Optional<Pageable> pageable) {
        List<Song> list = pageable.isPresent() ? songsAlbumsRepository.findByYearAndType(year, Song.Type.valueOf(type.toUpperCase()), pageable.get()) : songsAlbumsRepository.findByYearAndType(year, Song.Type.valueOf(type.toUpperCase()));

        if (list.size() != 0) {
            List<EntityModel<Song>> musicList = list.stream()
                    .map(song -> EntityModel.of(song,
                            linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), Optional.empty(), Optional.empty(), Optional.empty(), year.describeConstable(), type.describeConstable())).withSelfRel(),
                            linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).withRel("parent")))
                    .collect(Collectors.toList());

            return CollectionModel.of(musicList, linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), Optional.empty(), Optional.empty(), Optional.empty(), year.describeConstable(), type.describeConstable())).withSelfRel(),linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).withRel("parent"));
        } else throw new SongsAlbumsNotFound("There's no data available for the chosen search parameters. ", linkTo(methodOn(SongController.class).getByQuery(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).toString());

    }

    @Override
    public CollectionModel<EntityModel<Song>> getByGenreType(String genre, String type, Optional<Pageable> pageable) {
        List<Song> list = pageable.isPresent() ? songsAlbumsRepository.findByGenreAndType(Song.Genre.valueOf(genre), Song.Type.valueOf(type.toUpperCase()), pageable.get()) : songsAlbumsRepository.findByGenreAndType(Song.Genre.valueOf(genre), Song.Type.valueOf(type.toUpperCase()));

        if (list.size() != 0) {

            List<EntityModel<Song>> musicList = list.stream()
                    .map(song -> EntityModel.of(song,
                            linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), Optional.empty(), Optional.empty(), genre.describeConstable(), Optional.empty(), type.describeConstable())).withSelfRel(),
                            linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).withRel("parent")))
                    .collect(Collectors.toList());

            return CollectionModel.of(musicList, linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), Optional.empty(), Optional.empty(), genre.describeConstable(), Optional.empty(), type.describeConstable())).withSelfRel(),linkTo(methodOn(SongController.class).getByQuery(pageable.map(Pageable::getPageNumber), pageable.map(Pageable::getPageSize), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).withRel("parent"));
        } else throw new SongsAlbumsNotFound("There's no data available for the chosen search parameters. ", linkTo(methodOn(SongController.class).getByQuery(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).toString());

    }


    @Override
    public CollectionModel<EntityModel<Song>> complexSearch(Optional<String> name,
                                                            Optional<String> match,
                                                            Optional<String> genre,
                                                            Optional<Integer> year,
                                                            Optional<String> type,
                                                            Optional<Pageable> pageable
    ) {
        int choice = 0;
        choice += name.isPresent() ? 10000 : 0;
        choice += match.isPresent() ? 1000 : 0;
        choice += genre.isPresent() ? 100 : 0;
        choice += year.isPresent() ? 10 : 0;
        choice += type.isPresent() ? 1 : 0;

        switch (choice) {
            case 11111 -> {
                return getByExactNameGenreYearType(name.get(), match.get(), genre.get(), year.get(), type.get(), pageable);
            }
            case 11110 -> {
                return getByExactNameGenreYear(name.get(), match.get(), genre.get(), year.get(), pageable);
            }
            case 11101 -> {
                return getByExactNameGenreType(name.get(), match.get(), genre.get(), type.get(), pageable);
            }
            case 11100 -> {
                return getByExactNameGenre(name.get(), match.get(), genre.get(), pageable);
            }
            case 11011 -> {
                return getByExactNameYearType(name.get(), year.get(), type.get(), pageable);
            }
            case 11010 -> {
                return getByExactNameYear(name.get(), year.get(), pageable);
            }
            case 11001 -> {
                return getByExactNameType(name.get(), type.get(), pageable);
            }
            case 11000 -> {
                return getByName(name.get(), pageable);
            }
            case 10111 -> {
                return getByNameLikeGenreYearType(name.get(), genre.get(), year.get(), type.get(), pageable);
            }
            case 10110 -> {
                return getByNameLikeGenreYear(name.get(), genre.get(), year.get(), pageable);
            }
            case 10101 -> {
                return getByNameLikeGenreType(name.get(), genre.get(), type.get(), pageable);
            }
            case 10100 -> {
                return getByNameLikeGenre(name.get(), genre.get(), pageable);
            }
            case 10011 -> {
                return getByNameLikeYearType(name.get(), year.get(), type.get(), pageable);
            }
            case 10010 -> {
                return getByNameLikeYear(name.get(), year.get(), pageable);
            }
            case 10001 -> {
                return getByNameLikeType(name.get(), type.get(), pageable);
            }
            case 10000 -> {
                return getByNameLike(name.get(), "%" + name.get() + "%", pageable);
            }
            case 111 -> {
                return getByGenreYearType(genre.get(), year.get(), type.get(), pageable);
            }
            case 110 -> {
                return getByGenreYear(genre.get(), year.get(), pageable);
            }
            case 101 -> {
                return getByGenreType(genre.get(), type.get(), pageable);
            }
            case 100 -> {
                return getByGenre(genre.get(), pageable);
            }
            case 11 -> {
                return getByYearType(year.get(), type.get(), pageable);
            }
            case 10 -> {
                return getByYear(year.get(), pageable);
            }
            case 1 -> {
                return getByType(type.get(), pageable);
            }
            default -> {
                return getAllSongs(pageable);
            }
        }
    }


//=============================================================================================================
//======================================== POST ===============================================================
//=============================================================================================================

    @Override
    public EntityModel<Song> newMusic(InsertMusicDTO newMusic) {
        if (Objects.equals(newMusic.getGenre().name(), Song.Genre.UNKNOWN.name()))
            throw new EnumException(Stream.of(Song.Genre.values()).limit(Song.Genre.values().length - 1).map(Enum::name).collect(Collectors.joining(", ")), "Genre");

        if (Objects.equals(newMusic.getType().name(), Song.Type.UNKNOWN.name()))
            throw new EnumException(Stream.of(Song.Type.values()).limit(Song.Type.values().length - 1).map(Enum::name).collect(Collectors.joining(", ")), "Type");

        if (Objects.equals(newMusic.getType().name(), Song.Type.ALBUM.name())) {
            if (newMusic.getParent() != null)
                throw new AlbumParentException("Albums should not have a parent, parent should be null!");
        }
        if (newMusic.getParent() != null) {
            Optional<Song> foundById = songsAlbumsRepository.findById(newMusic.getParent());
            if (foundById.isPresent()) {
                if (!Objects.equals(foundById.get().getType(), "ALBUM"))
                    throw new AlbumParentException("Only albums should be parents for other songs! Please, don't traumatize the Song children with such mistakes...");
            } else
                throw new SongsAlbumsNotFound(String.format("Song not found: invalid argument (%s) : (%s)", "id", newMusic.getParent()), linkTo(methodOn(SongController.class).getByQuery(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).toString());
        }
        Song song = new Song();
        song.setName(newMusic.getName());
        song.setGenre(newMusic.getGenre().name());
        song.setType(newMusic.getType().name());
        song.setParent(newMusic.getParent());
        song.setYear(newMusic.getYear());

        if (newMusic.getArtistIds()==null || newMusic.getArtistIds().isEmpty())
            throw new ArtistParameterException("New song must have at least one artist!");

        for (String artistId : newMusic.getArtistIds()) {
            Optional<Artist> foundArtist = artistRepository.findById(artistId);
            if (foundArtist.isPresent()) {
                foundArtist.get().addSongs(song);
            } else
                throw new ArtistNotFound(String.format("Artist not found: invalid argument (%s) : (%s)", "id", artistId), linkTo(methodOn(ArtistController.class).getComplexSearch(Optional.empty(),Optional.empty(),Optional.empty(),Optional.empty(),Optional.empty())).toString());
        }

        try {
            return songsAssembler.toModel(songsAlbumsRepository.save(song));
        } catch (DataAccessException ex) {
            throw new JPAException(Objects.requireNonNull(ex.getRootCause()).toString());
        }

    }

//=============================================================================================================
//======================================== PUT ================================================================
//=============================================================================================================

    @Override
    public ResponseEntity<?> replaceSong(Song song, Integer id) {
        if (Objects.equals(song.getGenre(), Song.Genre.UNKNOWN.name()))
            throw new EnumException(Stream.of(Song.Genre.values()).limit(Song.Genre.values().length - 1).map(Enum::name).collect(Collectors.joining(", ")), "Genre");

        if (Objects.equals(song.getType(), Song.Type.UNKNOWN.name()))
            throw new EnumException(Stream.of(Song.Type.values()).limit(Song.Type.values().length - 1).map(Enum::name).collect(Collectors.joining(", ")), "Type");

        if (Objects.equals(song.getType(), Song.Type.ALBUM.name())) {
            if (song.getParent() != null)
                throw new AlbumParentException("Albums should not have a parent, parent should be null!");
        }
        if (song.getParent() != null) {
            Optional<Song> foundById = songsAlbumsRepository.findById(song.getParent());
            if (foundById.isPresent()) {
                if (!Objects.equals(foundById.get().getType(), "ALBUM"))
                    throw new AlbumParentException("Only albums should be parents for other songs! Please, don't traumatize the Song children with such mistakes...");
            } else
                throw new SongsAlbumsNotFound(String.format("Song not found: invalid argument (%s) : (%s)", "id", song.getParent()), linkTo(methodOn(SongController.class).getByQuery(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).toString());
        }
        songsAlbumsRepository.findById(id)
                .map(updated -> {
                    updated.setName(song.getName());
                    updated.setType(song.getType());
                    updated.setGenre(song.getGenre());
                    updated.setYear(song.getYear());
                    updated.setParent(song.getParent());
                    try {
                        return songsAlbumsRepository.save(updated);
                    } catch (DataAccessException ex) {
                        throw new JPAException(Objects.requireNonNull(ex.getRootCause()).toString());
                    }
                })
                .orElseThrow(() -> new SongsAlbumsNotFound(String.format("Song not found: invalid argument (%s) : (%s)", "id", song.getParent()), linkTo(methodOn(SongController.class).getByQuery(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).withSelfRel().toString()));

        return ResponseEntity.noContent().build();
    }

//==============================================================================================================
//======================================== DELETE ==============================================================
//==============================================================================================================

    @Override
    public EntityModel<Song> deleteSong(Integer id) {
        Optional<Song> toBeDeleted = songsAlbumsRepository.findById(id);
        if (toBeDeleted.isPresent()) {
            songsAlbumsRepository.deleteById(id);
            return songsAssembler.toModel(toBeDeleted.get());
        } else
            throw new SongsAlbumsNotFound(String.format("Song not found: invalid argument (%s) : (%s)", "id", id), linkTo(methodOn(SongController.class).getByQuery(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).toString());
    }

}
