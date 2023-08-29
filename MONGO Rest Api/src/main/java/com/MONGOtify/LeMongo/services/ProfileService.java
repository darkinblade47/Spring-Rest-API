package com.MONGOtify.LeMongo.services;

import com.MONGOtify.LeMongo.api.assembler.PlaylistAssembler;
import com.MONGOtify.LeMongo.api.assembler.ProfileAssembler;
import com.MONGOtify.LeMongo.api.controller.ProfilesController;
import com.MONGOtify.LeMongo.api.soap.SoapConfig;
import com.MONGOtify.LeMongo.entity.Profile;
import com.MONGOtify.LeMongo.entity.dto.OutputProfileDTO;
import com.MONGOtify.LeMongo.entity.dto.PlaylistDTO;
import com.MONGOtify.LeMongo.entity.dto.ProfileDTO;
import com.MONGOtify.LeMongo.entity.dto.SongDTO;
import com.MONGOtify.LeMongo.exceptions.*;
import com.MONGOtify.LeMongo.repo.ProfileRepository;
import com.google.gson.Gson;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.xml.bind.JAXBException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Service
public class ProfileService implements IProfile {
    private final RestTemplate restTemplate;
    @Autowired
    ProfileRepository profileRepository;

    @Autowired
    ProfileAssembler profileAssembler;

    @Autowired
    PlaylistAssembler playlistAssembler;

    @Autowired
    private SoapConfig soapConfig;

    public ProfileService() {
        this.restTemplate = new RestTemplate();
    }

//=============================================================================================================
//======================================== SOAP ===============================================================
//=============================================================================================================

    public String[] SoapAuthorize(Optional<String> token) {
        String authorizationString;
        if (token.isPresent())
            try {
                authorizationString = soapConfig.soapClient(soapConfig.marshaller()).AuthorizeUser(token.get().split("\\s+")[1]);
                if (authorizationString.startsWith("Error:Signature has expired"))
                    throw new UnauthorizedException("Session expired.Please log in again.");
                else if (authorizationString.startsWith("Error"))
                    throw new UnauthorizedException("Something occurred.Please log in again.");
                System.out.println(authorizationString);
            } catch (JAXBException e) {
                throw new UnauthorizedException("Something occurred.Please log in again.");
            }
        else throw new UnauthorizedException("You are not authorized.Please log in.");
        Pattern pattern = Pattern.compile("\\|\\|\\|");
        return pattern.split(authorizationString);
    }

//=============================================================================================================
//======================================== SQL ================================================================
//=============================================================================================================

    public Optional<SongDTO> getMusicById(String id) {
        try {
            ResponseEntity<Object> res = restTemplate.exchange(
                    "http://localhost:8080/api/songcollection/songs/" + id,
                    HttpMethod.GET,
                    new HttpEntity<>(new HttpHeaders()),
                    Object.class);
            JSONObject songObject = new JSONObject(new Gson().toJson(res.getBody()));
            String name = songObject.getString("name");
            String link = songObject.getJSONObject("_links").getJSONObject("self").getString("href");
            return Optional.of(new SongDTO(id, name, link));
        } catch (HttpClientErrorException httpClientErrorException) {
            throw new SongsAlbumsException(String.format("Song not found: invalid argument (%s) : (%s)", "id", id));
        } catch (JSONException e) {
            throw new RuntimeException(e.getMessage());
        }
    }


//=============================================================================================================
//======================================== GET ================================================================
//=============================================================================================================

    @Override
    public CollectionModel<EntityModel<OutputProfileDTO>> getAllProfiles() {
        List<Profile> list = profileRepository.findAll();

        if (list.size() != 0) {
            List<EntityModel<OutputProfileDTO>> profileList = list.stream()
                    .map(profile -> new OutputProfileDTO(profile.get_id(), profile.getNickname(), profile.getLikedMusic(), null))
                    .map(profileAssembler::toModel)
                    .collect(Collectors.toList());

            Set<EntityModel<PlaylistDTO>> hatePlaylist;
            for (EntityModel<OutputProfileDTO> item : profileList) {
                hatePlaylist = new HashSet<>();
                Profile source = list.stream().filter(p -> p.get_id().equals(item.getContent().getId())).findFirst().orElse(null);
                if (source != null) {
                    for (PlaylistDTO playlist : source.getPlaylists()) {
                        hatePlaylist.add(playlistAssembler.toModel(item.getContent(), playlist));
                    }
                    item.getContent().setPlaylists(hatePlaylist);
                }
            }
            return CollectionModel.of(profileList, linkTo(methodOn(ProfilesController.class).getAllProfiles(Optional.empty())).withSelfRel());
        } else throw new ProfileNotFound("There's no data available for the chosen search parameters.");
    }

    @Override
    public EntityModel<OutputProfileDTO> getProfileById(String idProfile, String idUser, Boolean allow) {
        Optional<Profile> profile = profileRepository.findProfileBy_id(idProfile);
        if (profile.isPresent()) {
            if (profile.get().getIdUser().equals(idUser) && allow) {
                Set<EntityModel<PlaylistDTO>> hatePlaylist = new HashSet<>();
                for (PlaylistDTO playlist : profile.get().getPlaylists()) {
                    hatePlaylist.add(playlistAssembler.toModel(profile.get(), playlist));
                }
                OutputProfileDTO outputProfile = new OutputProfileDTO(profile.get().get_id(), profile.get().getNickname(), profile.get().getLikedMusic(), hatePlaylist);
                return profileAssembler.toModel(outputProfile);
            } else throw new ForbiddenException("You do not have the rights to access this profile!");
        } else
            throw new ProfileNotFound(String.format("Profile not found: invalid argument (%s) : (%s)", "id", idProfile));
    }

    @Override
    public CollectionModel<EntityModel<PlaylistDTO>> getPlaylistsByProfileId(String idProfile, String idUser, Boolean allow) {
        Optional<Profile> profile = profileRepository.findProfileBy_id(idProfile);
        if (profile.isPresent()) {
            if (profile.get().getIdUser().equals(idUser) && allow) {
                if (!profile.get().getPlaylists().isEmpty()) {
                    List<EntityModel<PlaylistDTO>> playlistList = profile.get().getPlaylists().stream()
                            .map(playlist -> playlistAssembler.toModel(profile.get(), playlist))
                            .collect(Collectors.toList());
                    return CollectionModel.of(playlistList, linkTo(methodOn(ProfilesController.class).getPlaylistsByProfileId(profile.get().get_id(), Optional.empty())).withSelfRel());
                } else throw new PlaylistNotFound("This profile doesn't have any playlist.");
            } else throw new ForbiddenException("You do not have the rights to access this profile!");
        } else
            throw new ProfileNotFound(String.format("Profile not found: invalid argument (%s) : (%s)", "id", idProfile));

    }

    @Override
    public EntityModel<PlaylistDTO> getPlaylistById(String idProfile, String idPlaylist, String idUser, Boolean allow) {

        Optional<Profile> profile = profileRepository.findProfileBy_id(idProfile);
        if (profile.isPresent()) {
            if (profile.get().getIdUser().equals(idUser) && allow) {
                PlaylistDTO playlist = profile.get().getPlaylists().stream().filter(p -> p.get_id().equals(idPlaylist)).findFirst().orElse(null);
                if (playlist != null) {
                    return playlistAssembler.toModel(profile.get(), playlist);
                } else
                    throw new PlaylistNotFound(String.format("Playlist not found: invalid argument (%s) : (%s)", "id", idPlaylist));
            } else throw new ForbiddenException("You do not have the rights to access this profile!");
        } else
            throw new ProfileNotFound(String.format("Profile not found: invalid argument (%s) : (%s)", "id", idProfile));

    }

    @Override
    public EntityModel<PlaylistDTO> getPlaylistByIdFromIdUser(String idUser, String idPlaylist, String tokenIdUser, Boolean allow) {
        if (Objects.equals(idUser, tokenIdUser) && allow) {
            Optional<Profile> profile = profileRepository.findProfileByIdUser(idUser);
            if (profile.isPresent()) {
                PlaylistDTO playlist = profile.get().getPlaylists().stream().filter(p -> p.get_id().equals(idPlaylist)).findFirst().orElse(null);
                if (playlist != null) {
                    return playlistAssembler.toModel(profile.get(), playlist);
                } else
                    throw new PlaylistNotFound(String.format("Playlist not found: invalid argument (%s) : (%s)", "id", idPlaylist));
            } else
                throw new ProfileNotFound(String.format("Profile not found: invalid argument (%s)", "user id"));
        } else throw new ForbiddenException("You do not have the rights to access this profile!");
    }

    @Override
    public CollectionModel<EntityModel<PlaylistDTO>> getPlaylistsByIdUser(String idUser, String tokenIdUser, Boolean allow) {
        if (Objects.equals(idUser, tokenIdUser) && allow) {
            Optional<Profile> profile = profileRepository.findProfileByIdUser(idUser);
            if (profile.isPresent()) {
                if (!profile.get().getPlaylists().isEmpty()) {
                    List<EntityModel<PlaylistDTO>> playlistList = profile.get().getPlaylists().stream()
                            .map(playlist -> playlistAssembler.toModel(profile.get(), playlist))
                            .collect(Collectors.toList());
                    return CollectionModel.of(playlistList, linkTo(methodOn(ProfilesController.class).getPlaylistsByIdUser(idUser, Optional.empty())).withSelfRel());
                } else throw new PlaylistNotFound("This profile doesn't have any playlist.");
            } else
                throw new ProfileNotFound(String.format("Profile not found: invalid argument (%s)", "user id"));
        } else throw new ForbiddenException("You do not have the rights to access this profile!");

    }

//==============================================================================================================
//========================================== POST ==============================================================
//==============================================================================================================

    @Override
    public ResponseEntity<?> createNewProfile(ProfileDTO profile) {
        if (profile.getIdUser() == null) {
            throw new UnprocessableEntityException("Can't fulfill this request: id is missing.");
        }
        Optional<Profile> foundProfile = profileRepository.findProfileByIdUser(profile.getIdUser());
        if (foundProfile.isEmpty()) {
            if (!Pattern.compile("^(.+)@(\\S+)$")
                    .matcher(profile.getEmail())
                    .matches())
                throw new UnprocessableEntityException("Profile information invalid. Please insert a valid email address.");

            if (profileRepository.findProfileByEmail(profile.getEmail()).isPresent())
                throw new ProfileDuplicate("This email is already in use.");

            if (!Pattern.compile("\\w+")
                    .matcher(profile.getNickname())
                    .matches())
                throw new UnprocessableEntityException("Profile information invalid. Please don't use special characters, use just alphanumeric characters.");

            if (profileRepository.findProfileByNickname(profile.getNickname()).isPresent())
                throw new ProfileDuplicate("This nickname is already taken.");

            if (profile.getLikedMusic() != null && profile.getLikedMusic().size() != 0) {
                throw new UnprocessableEntityException("Can't create a profile that already has liked music, are you coming from the future or what?");
            }

            Profile newProfile = new Profile(profile.getIdUser(), profile.getNickname(), profile.getEmail());
            profileRepository.save(newProfile);

            Set<EntityModel<PlaylistDTO>> hatePlaylist = new HashSet<>();
            OutputProfileDTO outputProfile = new OutputProfileDTO(newProfile.get_id(), newProfile.getNickname(), newProfile.getLikedMusic(), hatePlaylist);
            EntityModel<OutputProfileDTO> entityModel = profileAssembler.toModel(outputProfile);
            return ResponseEntity
                    .created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                    .body(entityModel);
        } else throw new UnprocessableEntityException("Profile already exists.");
    }

    @Override
    public ResponseEntity<?> createNewPlaylist(String idProfile, PlaylistDTO playlist, String idUser) {
        if (idProfile == null) {
            throw new UnprocessableEntityException("Can't fulfill this request: profile's id is missing.");
        }
        Optional<Profile> foundProfile = profileRepository.findProfileBy_id(idProfile);
        if (foundProfile.isPresent()) {
            if (foundProfile.get().getIdUser().equals(idUser)) {
                foundProfile.get().getPlaylists().add(playlist);
                profileRepository.save(foundProfile.get());
                EntityModel<PlaylistDTO> entityModel = playlistAssembler.toModel(foundProfile.get(), playlist);
                return ResponseEntity
                        .created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                        .body(entityModel);
            } else
                throw new ForbiddenException("You do not have the rights to create playlists for this profile! Go get your own!");
        } else
            throw new ProfileNotFound(String.format("Profile not found: invalid argument (%s) : (%s)", "id", idProfile));

    }

    @Override
    public ResponseEntity<?> createPlaylistByUser(String idUser, PlaylistDTO playlist, String idUserToken) {
        if (idUser == null) {
            throw new UnprocessableEntityException("Can't fulfill this request: user's id is missing.");
        }
        if (Objects.equals(idUser, idUserToken)) {
            Optional<Profile> foundProfile = profileRepository.findProfileByIdUser(idUser);
            if (foundProfile.isPresent()) {
                foundProfile.get().getPlaylists().add(playlist);
                profileRepository.save(foundProfile.get());
                EntityModel<PlaylistDTO> entityModel = playlistAssembler.toModel(foundProfile.get(), playlist);
                return ResponseEntity
                        .created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                        .body(entityModel);
            } else
                throw new ProfileNotFound("Profile not found: are you sure you have a profile here?");
        } else
            throw new ForbiddenException("You do not have the rights to create playlists for this profile! Go get your own!");
    }


//==============================================================================================================
//=========================================== PUT ==============================================================
//==============================================================================================================

    @Override
    public ResponseEntity<?> updateProfileInformation(String id, ProfileDTO profile, String idUser) {
        Optional<Profile> oldProfile = profileRepository.findProfileBy_id(id);
        if (oldProfile.isEmpty())
            throw new ProfileNotFound(String.format("Profile not found: invalid argument (%s) : (%s)", "id", id));
        else if (!oldProfile.get().getIdUser().equals(idUser))
            throw new ForbiddenException("You do not have the rights to update this profile! Go get your own!");

        if (profile.getIdUser().equals(""))
            throw new UnprocessableEntityException("ID User is missing, can't update something if I don't know where to look for it.");

        if (!Pattern.compile("^(.+)@(\\S+)$")
                .matcher(profile.getEmail())
                .matches())
            throw new UnprocessableEntityException("Profile information invalid. Please insert a valid email address.");

        if (profileRepository.findProfileByEmail(profile.getEmail()).isPresent())
            throw new ProfileDuplicate("This email is already in use.");

        if (!Pattern.compile("\\w+")
                .matcher(profile.getNickname())
                .matches())
            throw new UnprocessableEntityException("Profile information invalid. Please don't use special characters, use just alphanumeric characters.");

        if (profileRepository.findProfileByNickname(profile.getNickname()).isPresent())
            throw new ProfileDuplicate("This nickname is already taken.");


        oldProfile = Optional.of(new Profile(profile));

        profileRepository.save(oldProfile.get());

        return ResponseEntity.noContent().build();

    }

    @Override
    public ResponseEntity<?> updateProfilePlaylists(String idProfile, String idPlaylist, PlaylistDTO playlist, String idUser) {
        Optional<Profile> oldProfile = profileRepository.findProfileBy_id(idProfile);
        if (oldProfile.isEmpty())
            throw new ProfileNotFound(String.format("Profile not found: invalid argument (%s) : (%s)", "id", idProfile));
        else if (!oldProfile.get().getIdUser().equals(idUser))
            throw new ForbiddenException("You do not have the rights to update this profile! Go get your own!");

        PlaylistDTO oldPlaylist = oldProfile.get().getPlaylists().stream().filter(p -> Objects.equals(p.get_id(), idPlaylist)).findFirst().orElse(null);
        if (oldPlaylist == null) {
            oldProfile.get().getPlaylists().add(playlist);
            profileRepository.save(oldProfile.get());
            EntityModel<PlaylistDTO> entityModel = playlistAssembler.toModel(oldProfile.get(), playlist);
            return ResponseEntity
                    .created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                    .body(entityModel);
        }
        oldProfile.get().getPlaylists().remove(oldPlaylist);
        oldProfile.get().getPlaylists().add(playlist);

        profileRepository.save(oldProfile.get());
        return ResponseEntity.noContent().build();

    }

//==============================================================================================================
//=========================================== PATCH ============================================================
//==============================================================================================================


    @Override
    public ResponseEntity<?> patchProfileLikedMusic(String idProfile, String idSong, String action, String idUser) {
        Optional<Profile> oldProfile = profileRepository.findProfileBy_id(idProfile);
        if (oldProfile.isEmpty())
            throw new ProfileNotFound(String.format("Profile not found: invalid argument (%s) : (%s)", "id", idProfile));
        else if (!oldProfile.get().getIdUser().equals(idUser))
            throw new ForbiddenException("You do not have the rights to update this profile! Go get your own!");

        Optional<SongDTO> likedSong = getMusicById(idSong);
        if (likedSong.isPresent()) {
            if (action.equals("add")) {
                if (!oldProfile.get().getLikedMusic().contains(likedSong.get()))
                    oldProfile.get().getLikedMusic().add(likedSong.get());
                else
                    throw new UnprocessableEntityException("Song is already liked");
            } else if (action.equals("remove")) {
                if (oldProfile.get().getLikedMusic().contains(likedSong.get()))
                    oldProfile.get().getLikedMusic().remove(likedSong.get());
                else
                    throw new SongsAlbumsException(String.format("Song not found: invalid argument (%s) : (%s)", "id", idSong));
            }

        }

        profileRepository.save(oldProfile.get());
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<?> patchProfilePlaylistSongs(String idProfile, String idPlaylist, String idSong, String action, String idUser) {
        Optional<Profile> oldProfile = profileRepository.findProfileBy_id(idProfile);
        if (oldProfile.isEmpty())
            throw new ProfileNotFound(String.format("Profile not found: invalid argument (%s) : (%s)", "id", idProfile));
        else if (!oldProfile.get().getIdUser().equals(idUser))
            throw new ForbiddenException("You do not have the rights to update this profile! Go get your own!");

        PlaylistDTO oldPlaylist = oldProfile.get().getPlaylists().stream().filter(p -> Objects.equals(p.get_id(), idPlaylist)).findFirst().orElse(null);
        if (oldPlaylist == null)
            throw new PlaylistNotFound(String.format("Playlist not found: invalid argument (%s) : (%s)", "id", idPlaylist));

        Optional<SongDTO> newSong = getMusicById(idSong);
        if (newSong.isPresent()) {
            if (action.equals("add")) {
                if (!oldPlaylist.getSongs().contains(newSong.get()))
                    oldPlaylist.getSongs().add(newSong.get());
                else
                    throw new UnprocessableEntityException("Song is already in the playlist");
            } else if (action.equals("remove")) {
                if (oldPlaylist.getSongs().contains(newSong.get()))
                    oldPlaylist.getSongs().remove(newSong.get());
                else
                    throw new SongsAlbumsException(String.format("Song not found: invalid argument (%s) : (%s)", "id", idSong));
            }

        }

        profileRepository.save(oldProfile.get());
        return ResponseEntity.noContent().build();
    }


//==============================================================================================================
//======================================== DELETE ==============================================================
//==============================================================================================================

    @Override
    public EntityModel<OutputProfileDTO> deleteProfileById(String idProfile, String idUser) {
        Optional<Profile> profile = profileRepository.findProfileBy_id(idProfile);
        if (profile.isPresent()) {
            if (profile.get().getIdUser().equals(idUser)) {
                Set<EntityModel<PlaylistDTO>> hatePlaylist = new HashSet<>();
                for (PlaylistDTO playlist : profile.get().getPlaylists()) {
                    hatePlaylist.add(playlistAssembler.toModel(profile.get(), playlist));
                }
                OutputProfileDTO outputProfile = new OutputProfileDTO(profile.get().get_id(), profile.get().getNickname(), profile.get().getLikedMusic(), hatePlaylist);
                profileRepository.delete(profile.get());
                return profileAssembler.toModel(outputProfile);
            } else
                throw new ForbiddenException("You do not have the rights to delete this profile! Go delete your own!");
        } else
            throw new ProfileNotFound(String.format("Profile not found: invalid argument (%s) : (%s)", "id", idProfile));

    }

    @Override
    public EntityModel<PlaylistDTO> deleteProfilePlaylistById(String idProfile, String idPlaylist, String idUser) {
        Optional<Profile> profile = profileRepository.findProfileBy_id(idProfile);
        if (profile.isPresent()) {
            if (profile.get().getIdUser().equals(idUser)) {
                PlaylistDTO oldPlaylist = profile.get().getPlaylists().stream().filter(p -> Objects.equals(p.get_id(), idPlaylist)).findFirst().orElse(null);
                if (oldPlaylist == null)
                    throw new PlaylistNotFound(String.format("Playlist not found: invalid argument (%s) : (%s)", "id", idPlaylist));

                profile.get().getPlaylists().remove(oldPlaylist);
                profileRepository.save(profile.get());

                return playlistAssembler.toModel(profile.get(), oldPlaylist);
            } else
                throw new ForbiddenException("You do not have the rights to delete this playlist! Go delete your own!");
        } else
            throw new ProfileNotFound(String.format("Profile not found: invalid argument (%s) : (%s)", "id", idProfile));

    }


}
