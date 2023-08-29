package com.MONGOtify.LeMongo.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.hateoas.EntityModel;

import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
public class OutputProfileDTO {

    @Id
    private String id;
    private String nickname;
    private List<SongDTO> likedMusic;
    private Set<EntityModel<PlaylistDTO>> playlists;

}
