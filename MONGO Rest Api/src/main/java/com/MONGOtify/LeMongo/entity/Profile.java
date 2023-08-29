package com.MONGOtify.LeMongo.entity;

import com.MONGOtify.LeMongo.entity.dto.PlaylistDTO;
import com.MONGOtify.LeMongo.entity.dto.ProfileDTO;
import com.MONGOtify.LeMongo.entity.dto.SongDTO;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@Document("profile")
public class Profile {

    @Id
    private String _id;
    private String idUser;
    private String nickname;
    private String email;
    private List<SongDTO> likedMusic = new ArrayList<>();
    private Set<PlaylistDTO> playlists = new HashSet<>();

    public Profile(String idUser, String nickname, String email) {
        this.idUser = idUser;
        this.nickname = nickname;
        this.email = email;
        this.likedMusic = new ArrayList<>();
        this.playlists = new HashSet<>();
    }

    public Profile(ProfileDTO dto) {
        super();
        this._id = dto.get_id();
        this.idUser = dto.getIdUser();
        this.nickname = dto.getNickname();
        this.email = dto.getEmail();
        this.likedMusic = dto.getLikedMusic();
        this.playlists = dto.getPlaylists();
    }


}
