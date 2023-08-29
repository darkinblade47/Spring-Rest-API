package com.MONGOtify.LeMongo.exceptions;

public class PlaylistNotFound extends RuntimeException {
    public PlaylistNotFound(String text) {
        super(text);
    }

}
