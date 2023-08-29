package com.MONGOtify.LeMongo.entity.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class ProfileDTO {
    @Id
    private String _id;
    @Getter
    @Setter
    private String idUser;
    @Getter
    @Setter
    private String nickname;
    @Getter
    @Setter
    private String email;
    @Getter
    @Setter
    private List<SongDTO> likedMusic;
    @Getter
    @Setter
    private Set<PlaylistDTO> playlists;

    public ProfileDTO(String _id, String idUser, String nickname, String email, List<SongDTO> likedMusic, Set<PlaylistDTO> playlists) {
        this._id = _id;
        this.idUser = idUser;
        this.nickname = nickname;
        this.email = email;
        this.likedMusic = likedMusic == null ? new ArrayList<>() : likedMusic;
        this.playlists = playlists == null ? new HashSet<>() : playlists;
    }
}
