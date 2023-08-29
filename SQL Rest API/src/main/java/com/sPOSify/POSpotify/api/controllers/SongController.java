package com.sPOSify.POSpotify.api.controllers;

import com.sPOSify.POSpotify.api.services.SongsAndAlbumsService;
import com.sPOSify.POSpotify.api.soap.SoapAuthorizer;
import com.sPOSify.POSpotify.errorHandling.customExceptions.ForbiddenException;
import com.sPOSify.POSpotify.jpa.dto.InsertMusicDTO;
import com.sPOSify.POSpotify.jpa.song_related.Song;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Optional;

@CrossOrigin
@RestController
public class SongController {

    @Autowired
    private final SongsAndAlbumsService songService;

    private final int DEFAULT_PAGE_SIZE = 10;

    public SongController(SongsAndAlbumsService songService) {
        this.songService = songService;
    }

//=============================================================================================================
//====================================GET MAPPING==============================================================
//=============================================================================================================

    @GetMapping({"/api/songcollection/songs/{id}", "/api/songcollection/songs/{id}/"})
    public ResponseEntity<?> getSongById(@PathVariable Integer id) {
        return ResponseEntity.ok(songService.getSongById(id));
    }

    @GetMapping({"/api/songcollection/songs/", "/api/songcollection/songs"})
    public ResponseEntity<CollectionModel<EntityModel<Song>>> getByQuery(@RequestParam Optional<Integer> page,
                                                                         @RequestParam Optional<Integer> items_per_page,
                                                                         @RequestParam Optional<String> name,
                                                                         @RequestParam Optional<String> match,
                                                                         @RequestParam Optional<String> genre,
                                                                         @RequestParam Optional<Integer> year,
                                                                         @RequestParam Optional<String> type) {
        Optional<Pageable> pag;
        pag = page.map(value -> items_per_page.map(integer -> PageRequest.of(value, integer))
                .orElseGet(() -> PageRequest.of(value, DEFAULT_PAGE_SIZE)));
        return ResponseEntity.ok(songService.complexSearch(name, match, genre, year, type, pag));
    }

//=============================================================================================================
//====================================POST MAPPING=============================================================
//=============================================================================================================

    @PostMapping({"/api/songcollection/songs", "/api/songcollection/songs/"})
    public ResponseEntity<?> newMusic(@RequestBody InsertMusicDTO newMusic, @RequestHeader("Authorization") Optional<String> token) {
        String[] claims = SoapAuthorizer.SoapAuthorize(token);
        if (claims.length == 2 && Arrays.asList(claims[1].substring(2, claims[1].length() - 2).split("', '")).contains("content_manager")) {
            EntityModel<Song> music = songService.newMusic(newMusic);
            return ResponseEntity
                    .created(music.getRequiredLink(IanaLinkRelations.SELF).toUri())
                    .body(music);
        } else throw new ForbiddenException("You are not authorised to perform this action");

    }

//==============================================================================================================
//====================================PUT MAPPING===============================================================
//==============================================================================================================

    @PutMapping({"/api/songcollection/songs/{id}", "/api/songcollection/songs/{id}"})
    public ResponseEntity<?> replaceSong(@RequestBody Song music, @PathVariable Integer id, @RequestHeader("Authorization") Optional<String> token) {
        String[] claims = SoapAuthorizer.SoapAuthorize(token);
        if (claims.length == 2 && Arrays.asList(claims[1].substring(2, claims[1].length() - 2).split("', '")).contains("content_manager")) {
            return songService.replaceSong(music, id);
        } else throw new ForbiddenException("You are not authorised to perform this action");
    }

//==============================================================================================================
//====================================DELETE MAPPING============================================================
//==============================================================================================================

    @DeleteMapping({"/api/songcollection/songs/{id}", "/api/songcollection/songs/{id}"})
    public ResponseEntity<?> deleteById(@PathVariable Integer id, @RequestHeader("Authorization") Optional<String> token) {
        String[] claims = SoapAuthorizer.SoapAuthorize(token);
        if (claims.length == 2 && Arrays.asList(claims[1].substring(2, claims[1].length() - 2).split("', '")).contains("content_manager")) {
            EntityModel<Song> deleted = songService.deleteSong(id);
            return ResponseEntity.ok(deleted);
        } else throw new ForbiddenException("You are not authorised to perform this action");
    }

}
