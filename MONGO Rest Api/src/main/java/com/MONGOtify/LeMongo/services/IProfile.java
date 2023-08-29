package com.MONGOtify.LeMongo.services;

import com.MONGOtify.LeMongo.entity.dto.OutputProfileDTO;
import com.MONGOtify.LeMongo.entity.dto.PlaylistDTO;
import com.MONGOtify.LeMongo.entity.dto.ProfileDTO;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;

public interface IProfile {

    CollectionModel<EntityModel<OutputProfileDTO>> getAllProfiles();


    EntityModel<OutputProfileDTO> getProfileById(String idProfile, String idUser, Boolean allow);


    CollectionModel<EntityModel<PlaylistDTO>> getPlaylistsByProfileId(String idProfile, String idUser, Boolean allow);


    EntityModel<PlaylistDTO> getPlaylistById(String idProfile, String idPlaylist, String idUser, Boolean allow);

    EntityModel<PlaylistDTO> getPlaylistByIdFromIdUser(String idUser, String idPlaylist, String tokenIdUser, Boolean allow);

    CollectionModel<EntityModel<PlaylistDTO>> getPlaylistsByIdUser(String idUser, String tokenIdUser, Boolean allow);

    ResponseEntity<?> createNewProfile(ProfileDTO profile);


    ResponseEntity<?> createNewPlaylist(String idProfile, PlaylistDTO playlist, String idUser);


    ResponseEntity<?> createPlaylistByUser(String idUser, PlaylistDTO playlist, String idUserToken);

    ResponseEntity<?> updateProfileInformation(String id, ProfileDTO profile, String idUser);


    ResponseEntity<?> updateProfilePlaylists(String idProfile, String idPlaylist, PlaylistDTO playlist, String idUser);


    ResponseEntity<?> patchProfileLikedMusic(String idProfile, String idSong, String action, String idUser);


    ResponseEntity<?> patchProfilePlaylistSongs(String idProfile, String idPlaylist, String idSong, String action, String idUser);


    EntityModel<OutputProfileDTO> deleteProfileById(String idProfile, String idUser);


    EntityModel<PlaylistDTO> deleteProfilePlaylistById(String idProfile, String idPlaylist, String idUser);
}
