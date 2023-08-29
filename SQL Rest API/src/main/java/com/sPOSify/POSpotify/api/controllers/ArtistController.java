package com.sPOSify.POSpotify.api.controllers;

import com.sPOSify.POSpotify.api.services.ArtistService;
import com.sPOSify.POSpotify.api.soap.SoapAuthorizer;
import com.sPOSify.POSpotify.errorHandling.customExceptions.ForbiddenException;
import com.sPOSify.POSpotify.jpa.dto.ArtistWithSongsDTO;
import com.sPOSify.POSpotify.jpa.song_related.Artist;
import com.sPOSify.POSpotify.jpa.song_related.Song;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Optional;

@CrossOrigin
@RestController
public class ArtistController {

    @Autowired
    private final ArtistService artistService;

    private final int DEFAULT_PAGE_SIZE = 5;

    public ArtistController(ArtistService artistService) {
        this.artistService = artistService;
    }

//=============================================================================================================
//====================================GET MAPPING==============================================================
//=============================================================================================================

    @GetMapping({"/api/songcollection/artists/{uuid}", "/api/songcollection/artists/{uuid}/"})
    public ResponseEntity<EntityModel<ArtistWithSongsDTO>> getArtistById(@PathVariable String uuid) {
        return ResponseEntity.ok(artistService.getArtistById(uuid));
    }

    @GetMapping({"/api/songcollection/artists/{id}/songs", "/api/songcollection/artists/{id}/songs/"})
    public ResponseEntity<CollectionModel<EntityModel<Song>>> getSongsByArtistId(@PathVariable String id) {
        return ResponseEntity.ok(artistService.getByArtistId(id));
    }

    @GetMapping({"/api/songcollection/artists/", "/api/songcollection/artists"})
    public ResponseEntity<CollectionModel<EntityModel<Artist>>> getComplexSearch(@RequestParam Optional<Integer> page,
                                                                                 @RequestParam Optional<Integer> items_per_page,
                                                                                 @RequestParam Optional<String> name,
                                                                                 @RequestParam Optional<String> match,
                                                                                 @RequestParam Optional<String> isActive) {
        Optional<Pageable> pag;
        pag = page.map(value -> items_per_page.map(integer -> PageRequest.of(value, integer))
                .orElseGet(() -> PageRequest.of(value, DEFAULT_PAGE_SIZE)));

        Optional <String> matchOpt = Optional.empty();
        if (match.isPresent()) {
            if(match.get().equals("null")){
                matchOpt = Optional.empty();
            }
        }

        Optional <Boolean> isActiveOpt = Optional.empty();
        if (isActive.isPresent()) {
            if(isActive.get().equals("true")){
                isActiveOpt = Optional.of(true);
            }
        }

        return ResponseEntity.ok(artistService.getByComplexSearch(name, matchOpt, isActiveOpt, pag));
    }


//=============================================================================================================
//====================================POST MAPPING=============================================================
//=============================================================================================================

    //there's nothing here

//==============================================================================================================
//====================================PUT MAPPING===============================================================
//==============================================================================================================

    @PutMapping({"/api/songcollection/artists/{id}", "/api/songcollection/artists/{id}/"})
    ResponseEntity<?> replaceArtist(@RequestBody Artist artist, @PathVariable String id, @RequestHeader("Authorization") Optional<String> token) {
        String[] claims = SoapAuthorizer.SoapAuthorize(token);
        if (claims.length == 2 && Arrays.asList(claims[1].substring(2, claims[1].length() - 2).split("', '")).contains("content_manager")) {
            return artistService.replaceArtist(artist, id);
        } else throw new ForbiddenException("You are not authorised to perform this action");
    }

//==============================================================================================================
//====================================DELETE MAPPING============================================================
//==============================================================================================================

    @DeleteMapping({"/api/songcollection/artists/{id}", "/api/songcollection/artists/{id}/"})
    public ResponseEntity<?> deleteById(@PathVariable String id, @RequestHeader("Authorization") Optional<String> token) {
        String[] claims = SoapAuthorizer.SoapAuthorize(token);
        if (claims.length == 2 && Arrays.asList(claims[1].substring(2, claims[1].length() - 2).split("', '")).contains("content_manager")) {
            EntityModel<Artist> deleted = artistService.deleteArtist(id);
            return ResponseEntity.ok(deleted);
        } else throw new ForbiddenException("You are not authorised to perform this action");
    }

}