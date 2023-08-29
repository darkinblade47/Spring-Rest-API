package com.sPOSify.POSpotify.errorHandling;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.sPOSify.POSpotify.errorHandling.customExceptions.AlbumParentException;
import com.sPOSify.POSpotify.errorHandling.customExceptions.ArtistNotFound;
import com.sPOSify.POSpotify.errorHandling.customExceptions.ArtistParameterException;
import com.sPOSify.POSpotify.errorHandling.customExceptions.EnumException;
import com.sPOSify.POSpotify.errorHandling.customExceptions.ForbiddenException;
import com.sPOSify.POSpotify.errorHandling.customExceptions.JPAException;
import com.sPOSify.POSpotify.errorHandling.customExceptions.SongsAlbumsNotFound;
import com.sPOSify.POSpotify.errorHandling.customExceptions.UnauthorizedException;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Component
@ControllerAdvice
public class GlobalRestExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = {EnumException.class})
    public ResponseEntity<Object> handleEnum(EnumException ex, WebRequest request) {
        ApiErrorResponse apiResponse = new ApiErrorResponse
                .ApiErrorResponseBuilder()
                .withDetail(ex.getMessage())
                .withMessage("Bad parameter")
                .withError_code("422")
                .withStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                .atTime(LocalDateTime.now(ZoneOffset.UTC))
                .build();
        return new ResponseEntity<>(apiResponse, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(value = {AlbumParentException.class})
    public ResponseEntity<Object> handleAlbumWithParent(AlbumParentException ex, WebRequest request) {
        ApiErrorResponse apiResponse = new ApiErrorResponse
                .ApiErrorResponseBuilder()
                .withDetail(ex.getMessage())
                .withMessage("Bad parameter")
                .withError_code("422")
                .withStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                .atTime(LocalDateTime.now(ZoneOffset.UTC))
                .build();
        return new ResponseEntity<>(apiResponse, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(value = {SongsAlbumsNotFound.class, ArtistNotFound.class})
    public ResponseEntity<Object> handleNotFound(RuntimeException ex, WebRequest request) {
        SongsAlbumsNotFound excS=null;
        ArtistNotFound excA=null;
        if(ex.getClass().getSimpleName().equals("ArtistNotFound"))
            excA = (ArtistNotFound) ex;
        if(ex.getClass().getSimpleName().equals("SongsAlbumsNotFound"))
            excS = (SongsAlbumsNotFound) ex;

        ApiErrorResponse apiResponse = new ApiErrorResponse
                .ApiErrorResponseBuilder()
                .withDetail(ex.getMessage())
                .withMessage("Where " + ((ex.getClass().getSimpleName().equals("SongsAlbumsNotFound")) ? "song?" : "artist?"))
                .withError_code("404")
                .withParent(excS !=null ? excS.getParent() : excA.getParent())
                .withStatus(HttpStatus.NOT_FOUND)
                .atTime(LocalDateTime.now(ZoneOffset.UTC))
                .build();
        return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = {JPAException.class})
    public ResponseEntity<Object> handleJpaException(JPAException ex, WebRequest request) {
        ApiErrorResponse apiResponse = new ApiErrorResponse
                .ApiErrorResponseBuilder()
                .withDetail(ex.getMessage())
                .withMessage("Not acceptable")
                .withError_code("406")
                .withStatus(HttpStatus.NOT_ACCEPTABLE)
                .atTime(LocalDateTime.now(ZoneOffset.UTC))
                .build();
        return new ResponseEntity<>(apiResponse, HttpStatus.NOT_ACCEPTABLE);
    }

    @ExceptionHandler(value = {ArtistParameterException.class})
    public ResponseEntity<Object> handleArtistParameter(ArtistParameterException ex, WebRequest request) {
        ApiErrorResponse apiResponse = new ApiErrorResponse
                .ApiErrorResponseBuilder()
                .withDetail(ex.getMessage())
                .withMessage("Bad parameter")
                .withError_code("422")
                .withStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                .atTime(LocalDateTime.now(ZoneOffset.UTC))
                .build();
        return new ResponseEntity<>(apiResponse, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(value = {UnauthorizedException.class})
    public ResponseEntity<Object> handleNotAuthorized(UnauthorizedException ex, WebRequest request) {
        ApiErrorResponse apiResponse = new ApiErrorResponse
                .ApiErrorResponseBuilder()
                .withDetail(ex.getMessage())
                .withMessage("Unauthorized")
                .withError_code("401")
                .withStatus(HttpStatus.UNAUTHORIZED)
                .atTime(LocalDateTime.now(ZoneOffset.UTC))
                .build();
        return new ResponseEntity<>(apiResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(value = {ForbiddenException.class})
    public ResponseEntity<Object> handleForbidden(ForbiddenException ex, WebRequest request) {
        ApiErrorResponse apiResponse = new ApiErrorResponse
                .ApiErrorResponseBuilder()
                .withDetail(ex.getMessage())
                .withMessage("Forbidden")
                .withError_code("403")
                .withStatus(HttpStatus.FORBIDDEN)
                .atTime(LocalDateTime.now(ZoneOffset.UTC))
                .build();
        return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
    }


    @ExceptionHandler(value = {RuntimeException.class})
    public ResponseEntity<Object> handleAnyException(RuntimeException ex, WebRequest request) {
        ApiErrorResponse apiResponse = new ApiErrorResponse
                .ApiErrorResponseBuilder()
                .withDetail("Something occurred!")
                .withMessage("I'm bamboozled")
                .withError_code("422")
                .withStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                .atTime(LocalDateTime.now(ZoneOffset.UTC))
                .build();
        return new ResponseEntity<>(apiResponse, HttpStatus.UNPROCESSABLE_ENTITY);
    }

}