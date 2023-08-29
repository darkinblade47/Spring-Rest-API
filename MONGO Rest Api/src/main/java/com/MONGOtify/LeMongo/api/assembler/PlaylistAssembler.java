package com.MONGOtify.LeMongo.api.assembler;

import com.MONGOtify.LeMongo.api.controller.ProfilesController;
import com.MONGOtify.LeMongo.entity.Profile;
import com.MONGOtify.LeMongo.entity.dto.OutputProfileDTO;
import com.MONGOtify.LeMongo.entity.dto.PlaylistDTO;
import com.MONGOtify.LeMongo.entity.dto.ProfileDTO;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class PlaylistAssembler implements RepresentationModelAssembler<ProfileDTO, EntityModel<PlaylistDTO>> {


    public EntityModel<PlaylistDTO> toModel(Profile profile, PlaylistDTO playlist) {
        return EntityModel.of(playlist,
                linkTo(methodOn(ProfilesController.class).getPlaylistById(profile.get_id(), playlist.get_id(), Optional.empty())).withSelfRel(),
                linkTo(methodOn(ProfilesController.class).getPlaylistsByProfileId(profile.get_id(), Optional.empty())).withRel("parent"));
    }

    @Override
    public EntityModel<PlaylistDTO> toModel(ProfileDTO entity) {
        return null;
    }

    public EntityModel<PlaylistDTO> toModel(OutputProfileDTO profile, PlaylistDTO playlist) {
        return EntityModel.of(playlist,
                linkTo(methodOn(ProfilesController.class).getPlaylistById(profile.getId(), playlist.get_id(), Optional.empty())).withSelfRel(),
                linkTo(methodOn(ProfilesController.class).getPlaylistsByProfileId(profile.getId(), Optional.empty())).withRel("parent"));
    }
}