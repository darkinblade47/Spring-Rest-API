package com.sPOSify.POSpotify.api.assemblers;

import com.sPOSify.POSpotify.api.controllers.ArtistController;
import com.sPOSify.POSpotify.jpa.song_related.Artist;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ArtistsAssembler implements RepresentationModelAssembler<Artist, EntityModel<Artist>> {

    @Override
    public EntityModel<Artist> toModel(Artist artist) {

        return EntityModel.of(artist,
                WebMvcLinkBuilder.linkTo(methodOn(ArtistController.class).getArtistById(artist.getUuid())).withSelfRel(),
                linkTo(methodOn(ArtistController.class).getComplexSearch(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).withRel("parent"));
    }
}