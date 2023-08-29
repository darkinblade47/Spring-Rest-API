package com.sPOSify.POSpotify.errorHandling.customExceptions;

public class ArtistParameterException extends RuntimeException {
    public ArtistParameterException(String text) {
        super(text);
    }
}
