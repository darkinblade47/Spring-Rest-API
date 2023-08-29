package com.MONGOtify.LeMongo.api.assembler;

import com.MONGOtify.LeMongo.api.controller.ProfilesController;
import com.MONGOtify.LeMongo.entity.Profile;
import com.MONGOtify.LeMongo.entity.dto.OutputProfileDTO;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ProfileAssembler implements RepresentationModelAssembler<Profile, EntityModel<Profile>> {
    @Override
    public EntityModel<Profile> toModel(Profile profile) {
        return EntityModel.of(profile,
                linkTo(methodOn(ProfilesController.class).getProfileById(profile.get_id(), Optional.empty())).withSelfRel(),
                linkTo(methodOn(ProfilesController.class).getAllProfiles(Optional.empty())).withRel("parent"));
    }

    public EntityModel<OutputProfileDTO> toModel(OutputProfileDTO outputProfile) {
        return EntityModel.of(outputProfile,
                linkTo(methodOn(ProfilesController.class).getProfileById(outputProfile.getId(), Optional.empty())).withSelfRel(),
                linkTo(methodOn(ProfilesController.class).getAllProfiles(Optional.empty())).withRel("parent"));
    }
}