package com.MONGOtify.LeMongo.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.bson.codecs.IdGenerator;
import org.bson.codecs.ObjectIdGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class PlaylistDTO {
    private String _id;
    @Setter
    @Getter
    private String idUser;
    @Setter
    @Getter
    private String playlistName;
    @Setter
    @Getter
    private List<SongDTO> songs;

    public PlaylistDTO(String _id, String idUser, String playlistName, List<SongDTO> songs) {
        this._id = _id == null ? UUID.randomUUID().toString() : _id;
        this.idUser = idUser;
        this.playlistName = playlistName;
        this.songs = songs == null ? new ArrayList<SongDTO>() : songs;
    }

}
