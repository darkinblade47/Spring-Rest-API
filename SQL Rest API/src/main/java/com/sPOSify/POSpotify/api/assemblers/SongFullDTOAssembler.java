package com.sPOSify.POSpotify.api.assemblers;

import com.sPOSify.POSpotify.api.controllers.SongController;
import com.sPOSify.POSpotify.jpa.dto.SongFullDTO;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class SongFullDTOAssembler implements RepresentationModelAssembler<SongFullDTO, EntityModel<SongFullDTO>> {

    @Override
    public CollectionModel<EntityModel<SongFullDTO>> toCollectionModel(Iterable<? extends SongFullDTO> entities) {
        return RepresentationModelAssembler.super.toCollectionModel(entities);
    }

    @Override
    public EntityModel<SongFullDTO> toModel(SongFullDTO song) {

        return EntityModel.of(song,
                WebMvcLinkBuilder.linkTo(methodOn(SongController.class).getSongById(song.getId())).withSelfRel(),
                linkTo(methodOn(SongController.class).getByQuery(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).withRel("parent"));
    }
}
