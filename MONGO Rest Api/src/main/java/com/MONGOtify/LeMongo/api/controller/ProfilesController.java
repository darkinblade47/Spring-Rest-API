package com.MONGOtify.LeMongo.api.controller;

import com.MONGOtify.LeMongo.entity.dto.OutputProfileDTO;
import com.MONGOtify.LeMongo.entity.dto.PlaylistDTO;
import com.MONGOtify.LeMongo.entity.dto.ProfileDTO;
import com.MONGOtify.LeMongo.exceptions.ForbiddenException;
import com.MONGOtify.LeMongo.exceptions.UnprocessableEntityException;
import com.MONGOtify.LeMongo.services.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Optional;

@CrossOrigin
@RestController
public class ProfilesController {

    @Autowired
    private final ProfileService profileService;


    public ProfilesController(ProfileService profileService) {
        this.profileService = profileService;
    }

//=============================================================================================================
//=================================== GET MAPPING =============================================================
//=============================================================================================================


    @GetMapping({"/api/profiles/", "/api/profiles"})
    public ResponseEntity<CollectionModel<EntityModel<OutputProfileDTO>>> getAllProfiles(@RequestHeader("Authorization") Optional<String> token) {
        //No one is allowed to see all the profiles
        throw new ForbiddenException("You are not authorised to perform this operation");
    }

    @GetMapping({"/api/profiles/{idProfile}", "/api/profiles/{idProfile}/"})
    public ResponseEntity<EntityModel<OutputProfileDTO>> getProfileById(@PathVariable String idProfile, @RequestHeader("Authorization") Optional<String> token) {
        String[] claims = profileService.SoapAuthorize(token);
        Boolean allow = claims.length == 2 && Arrays.asList(claims[1].substring(2, claims[1].length() - 2).split("', '")).contains("client");
        //if the request is made by the admin, give him the right to do whatever he wants
        return ResponseEntity.ok(profileService.getProfileById(idProfile, claims[0], allow));
    }

    @GetMapping({"/api/profiles/{idProfile}/playlists", "/api/profiles/{idProfile}/playlists/"})
    public ResponseEntity<CollectionModel<EntityModel<PlaylistDTO>>> getPlaylistsByProfileId(@PathVariable String idProfile, @RequestHeader("Authorization") Optional<String> token) {
        String[] claims = profileService.SoapAuthorize(token);
        Boolean allow = claims.length == 2 && Arrays.asList(claims[1].substring(2, claims[1].length() - 2).split("', '")).contains("client");
        return ResponseEntity.ok(profileService.getPlaylistsByProfileId(idProfile, claims[0], allow));
    }

    @GetMapping({"/api/profiles/{idProfile}/playlists/{idPlaylist}", "/api/profiles/{idProfile}/playlists/{idPlaylist}/"})
    public ResponseEntity<EntityModel<PlaylistDTO>> getPlaylistById(@PathVariable String idProfile,
                                                                    @PathVariable String idPlaylist,
                                                                    @RequestHeader("Authorization") Optional<String> token) {
        String[] claims = profileService.SoapAuthorize(token);
        Boolean allow = claims.length == 2 && Arrays.asList(claims[1].substring(2, claims[1].length() - 2).split("', '")).contains("client");
        return ResponseEntity.ok(profileService.getPlaylistById(idProfile, idPlaylist, claims[0], allow));
    }

    @GetMapping({"/api/users/{idUser}/profile/playlists", "/api/users/{idUser}/profile/playlists/"})
    public ResponseEntity<CollectionModel<EntityModel<PlaylistDTO>>> getPlaylistsByIdUser(@PathVariable String idUser,
                                                                                          @RequestHeader("Authorization") Optional<String> token) {
        System.out.println(token);
        String[] claims = profileService.SoapAuthorize(token);
        System.out.println("aici ajung oare?");

        Boolean allow = claims.length == 2 && Arrays.asList(claims[1].substring(2, claims[1].length() - 2).split("', '")).contains("client");
        return ResponseEntity.ok(profileService.getPlaylistsByIdUser(idUser, claims[0], allow));
    }

    @GetMapping({"/api/users/{idUser}/profile/playlists/{idPlaylist}", "/api/users/{idUser}/profile/playlists/{idPlaylist}/"})
    public ResponseEntity<EntityModel<PlaylistDTO>> getPlaylistByIdFromIdUser(@PathVariable String idUser,
                                                                              @PathVariable String idPlaylist,
                                                                              @RequestHeader("Authorization") Optional<String> token) {
        String[] claims = profileService.SoapAuthorize(token);
        Boolean allow = claims.length == 2 && Arrays.asList(claims[1].substring(2, claims[1].length() - 2).split("', '")).contains("client");
        return ResponseEntity.ok(profileService.getPlaylistByIdFromIdUser(idUser, idPlaylist, claims[0], allow));
    }

//=============================================================================================================
//==================================== POST MAPPING ===========================================================
//=============================================================================================================


    @PostMapping({"/api/profiles/", "/api/profiles"})
    public ResponseEntity<?> createProfile(@RequestBody ProfileDTO profile, @RequestHeader("Authorization") Optional<String> token) {
        String[] claims = profileService.SoapAuthorize(token);
        profile.setIdUser(claims[0]);
        if (claims.length == 2 && Arrays.asList(claims[1].substring(2, claims[1].length() - 2).split("', '")).contains("client"))
            return profileService.createNewProfile(profile);
        else throw new ForbiddenException("You are not allowed to perform this action");
    }

    @PostMapping({"/api/profiles/{idProfile}/playlists", "/api/profiles/{idProfile}/playlists/"})
    public ResponseEntity<?> createPlaylist(@PathVariable String idProfile, @RequestBody PlaylistDTO playlist, @RequestHeader("Authorization") Optional<String> token) {
        String[] claims = profileService.SoapAuthorize(token);
        if (claims.length == 2 && Arrays.asList(claims[1].substring(2, claims[1].length() - 2).split("', '")).contains("client"))
            return profileService.createNewPlaylist(idProfile, playlist, claims[0]);
        else throw new ForbiddenException("You are not allowed to perform this action");
    }

    @PostMapping({"/api/users/{idUser}/profile/playlists", "/api/users/{idUser}/profile/playlists/"})
    public ResponseEntity<?> createPlaylistByUser(@PathVariable String idUser, @RequestBody PlaylistDTO playlist, @RequestHeader("Authorization") Optional<String> token) {
        String[] claims = profileService.SoapAuthorize(token);
        if (claims.length == 2 && Arrays.asList(claims[1].substring(2, claims[1].length() - 2).split("', '")).contains("client"))
            return profileService.createPlaylistByUser(idUser, playlist, claims[0]);
        else throw new ForbiddenException("You are not allowed to perform this action");
    }


//=============================================================================================================
//==================================== PUT MAPPING=============================================================
//=============================================================================================================

    @PutMapping({"/api/profiles/{idProfile}", "/api/profiles/{idProfile}/"})
    public ResponseEntity<?> updateProfile(@PathVariable String idProfile,
                                           @RequestBody ProfileDTO profileDTO,
                                           @RequestParam Optional<String> action,
                                           @RequestHeader("Authorization") Optional<String> token) {
        if (action.isPresent()) {
            if (action.get().equals("update")) {
                String[] claims = profileService.SoapAuthorize(token);
                if (claims.length == 2 && Arrays.asList(claims[1].substring(2, claims[1].length() - 2).split("', '")).contains("client"))
                    return profileService.updateProfileInformation(idProfile, profileDTO, claims[0]);
                else throw new ForbiddenException("You are not allowed to perform this action");
            }
            throw new UnprocessableEntityException("Query parameter unknown. Available parameters: \"update\".");
        } else
            throw new UnprocessableEntityException("The query parameter action is missing. Available parameters: \"update\".");
    }

    @PutMapping({"/api/profiles/{idProfile}/playlists/{idPlaylist}", "/api/profiles/{idProfile}/playlists/{idPlaylist}/"})
    public ResponseEntity<?> updateProfilePlaylist(@PathVariable String idProfile,
                                                   @PathVariable String idPlaylist,
                                                   @RequestBody PlaylistDTO playlist,
                                                   @RequestParam Optional<String> action,
                                                   @RequestHeader("Authorization") Optional<String> token) {
        if (action.isPresent()) {
            if (action.get().equals("update")) {
                String[] claims = profileService.SoapAuthorize(token);
                if (claims.length == 2 && Arrays.asList(claims[1].substring(2, claims[1].length() - 2).split("', '")).contains("client"))
                    return profileService.updateProfilePlaylists(idProfile, idPlaylist, playlist, claims[0]);
                else throw new ForbiddenException("You are not allowed to perform this action");
            } else
                throw new UnprocessableEntityException("Query parameter unknown. Please pick one of the following: \"update\".");
        } else
            throw new UnprocessableEntityException("The query parameter action is missing. Please pick one of the following: \"update\".");
    }

//=============================================================================================================
//==================================== PATCH MAPPING ==========================================================
//=============================================================================================================

    @PatchMapping({"/api/profiles/{idProfile}/likedMusic/{idLikedMusic}", "/api/profiles/{idProfile}/likedMusic/{idLikedMusic}/"})
    public ResponseEntity<?> patchProfileLikedMusic(@PathVariable String idProfile,
                                                    @PathVariable String idLikedMusic,
                                                    @RequestParam Optional<String> action,
                                                    @RequestHeader("Authorization") Optional<String> token) {
        if (action.isPresent()) {
            if (action.get().equals("add") || action.get().equals("remove")) {
                String[] claims = profileService.SoapAuthorize(token);
                if (claims.length == 2 && Arrays.asList(claims[1].substring(2, claims[1].length() - 2).split("', '")).contains("client"))
                    return profileService.patchProfileLikedMusic(idProfile, idLikedMusic, action.get(), claims[0]);
                else throw new ForbiddenException("You are not allowed to perform this action");
            } else
                throw new UnprocessableEntityException("Query parameter unknown. Please pick one of the following: \"add\" or \"remove\".");
        } else
            throw new UnprocessableEntityException("The query parameter action is missing. Please pick one of the following: \"add\" or \"remove\".");
    }

    @PatchMapping({"/api/profiles/{idProfile}/playlists/{idPlaylist}/songs/{idSong}", "/api/profiles/{idProfile}/playlists/{idPlaylist}/songs/{idSong}/"})
    public ResponseEntity<?> patchProfilePlaylistSongs(@PathVariable String idProfile,
                                                       @PathVariable String idPlaylist,
                                                       @PathVariable String idSong,
                                                       @RequestParam Optional<String> action,
                                                       @RequestHeader("Authorization") Optional<String> token) {
        if (action.isPresent()) {
            if (action.get().equals("add") || action.get().equals("remove")) {
                String[] claims = profileService.SoapAuthorize(token);
                if (claims.length == 2 && Arrays.asList(claims[1].substring(2, claims[1].length() - 2).split("', '")).contains("client"))
                    return profileService.patchProfilePlaylistSongs(idProfile, idPlaylist, idSong, action.get(), claims[0]);
                else throw new ForbiddenException("You are not allowed to perform this action");
            } else
                throw new UnprocessableEntityException("Query parameter unknown. Please pick one of the following: \"add\" or \"remove\".");
        } else
            throw new UnprocessableEntityException("The query parameter action is missing. Please pick one of the following: \"add\" or \"remove\".");
    }

//=============================================================================================================
//==================================== DELETE MAPPING =========================================================
//=============================================================================================================

    @DeleteMapping({"/api/profiles/{idProfile}", "/api/profiles/{idProfile}/"})
    public ResponseEntity<?> deleteProfile(@PathVariable String idProfile,
                                           @RequestHeader("Authorization") Optional<String> token) {
        String[] claims = profileService.SoapAuthorize(token);
        if (claims.length == 2 && Arrays.asList(claims[1].substring(2, claims[1].length() - 2).split("', '")).contains("client")) {
            EntityModel<OutputProfileDTO> deleted = profileService.deleteProfileById(idProfile, claims[0]);
            return ResponseEntity.ok(deleted);
        } else throw new ForbiddenException("You are not allowed to perform this action");
    }

    @DeleteMapping({"/api/profiles/{idProfile}/playlists/{idPlaylist}", "/api/profiles/{idProfile}/playlists/{idPlaylist}/"})
    public ResponseEntity<?> deleteProfilePlaylist(@PathVariable String idProfile, @PathVariable String idPlaylist,
                                                   @RequestHeader("Authorization") Optional<String> token) {
        String[] claims = profileService.SoapAuthorize(token);
        if (claims.length == 2 && Arrays.asList(claims[1].substring(2, claims[1].length() - 2).split("', '")).contains("client")) {
            EntityModel<PlaylistDTO> deleted = profileService.deleteProfilePlaylistById(idProfile, idPlaylist, claims[0]);
            return ResponseEntity.ok(deleted);
        } else throw new ForbiddenException("You are not allowed to perform this action");
    }
}
