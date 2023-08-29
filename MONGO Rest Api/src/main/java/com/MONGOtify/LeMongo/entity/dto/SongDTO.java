package com.MONGOtify.LeMongo.entity.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

@Data
public class SongDTO {

    @Id
    @Getter
    @Setter
    private String _id;
    @Getter
    @Setter
    private String name;
    @Getter
    @Setter
    private String link;

    public SongDTO(String _id, String name, String link) {
        this._id = _id;
        this.name = name;
        this.link = link;
    }
}
