package com.sPOSify.POSpotify.errorHandling.customExceptions;

import lombok.Getter;
import lombok.Setter;

public class ArtistNotFound extends RuntimeException {
    @Getter
    @Setter
    private String parent;
    public ArtistNotFound(String text,String parent) {
        super(text);
        this.parent = parent;
    }

}
