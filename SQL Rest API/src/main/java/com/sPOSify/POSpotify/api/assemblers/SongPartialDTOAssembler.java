package com.sPOSify.POSpotify.api.assemblers;

import com.sPOSify.POSpotify.api.controllers.SongController;
import com.sPOSify.POSpotify.jpa.dto.SongPartialDTO;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class SongPartialDTOAssembler implements RepresentationModelAssembler<SongPartialDTO, EntityModel<SongPartialDTO>> {

    @Override
    public EntityModel<SongPartialDTO> toModel(SongPartialDTO song) {

        return EntityModel.of(song,
                WebMvcLinkBuilder.linkTo(methodOn(SongController.class).getSongById(song.getId())).withSelfRel(),
                linkTo(methodOn(SongController.class).getByQuery(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).withRel("parent"));
    }
}
