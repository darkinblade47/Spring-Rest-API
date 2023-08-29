package com.sPOSify.POSpotify.errorHandling.customExceptions;

import lombok.Getter;
import lombok.Setter;

public class SongsAlbumsNotFound extends RuntimeException {

    @Getter
    @Setter
    String parent;
    public SongsAlbumsNotFound(String text, String parent) {
        super(text);
        this.parent = parent;
    }

}
