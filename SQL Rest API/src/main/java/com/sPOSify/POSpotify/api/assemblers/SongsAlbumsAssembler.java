package com.sPOSify.POSpotify.api.assemblers;

import com.sPOSify.POSpotify.api.controllers.SongController;
import com.sPOSify.POSpotify.jpa.song_related.Song;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public
class SongsAlbumsAssembler implements RepresentationModelAssembler<Song, EntityModel<Song>> {

    @Override
    public EntityModel<Song> toModel(Song music) {

        return EntityModel.of(music,
                WebMvcLinkBuilder.linkTo(methodOn(SongController.class).getSongById(music.getId())).withSelfRel(),
                linkTo(methodOn(SongController.class).getByQuery(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).withRel("parent"));
    }
}