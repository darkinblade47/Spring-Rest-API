package com.sPOSify.POSpotify.jpa.song_related;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Set;

public interface SongsAlbumsRepository extends CrudRepository<Song, Integer> {
    //NON PAGEABLE
    List<Song> findByName(String name);

    List<Song> findByNameLike(String pattern);

    List<Song> findByType(Song.Type type);

    List<Song> findByGenre(Song.Genre genre);

    List<Song> findByYear(Integer year);

    @Query(value = "SELECT * FROM songs_and_albums as sa, songs_and_albums as sa2 where sa.parent = sa2.id and sa.parent=?1", nativeQuery = true)
    Set<Song> cautaAlbumele(Integer album);


    List<Song> findSongsAlbumsByArtistsUuid(String uuid);

    List<Song> findByNameLikeAndGenreAndYearAndType(String pattern, Song.Genre genre, Integer year, Song.Type type);

    List<Song> findByNameLikeAndGenreAndYear(String pattern, Song.Genre genre, Integer year);

    List<Song> findByNameLikeAndYearAndType(String name, Integer year, Song.Type type);

    List<Song> findByNameLikeAndGenreAndType(String name, Song.Genre genre, Song.Type type);

    List<Song> findByNameLikeAndType(String name, Song.Type type);

    List<Song> findByNameLikeAndGenre(String name, Song.Genre genre);

    List<Song> findByNameLikeAndYear(String name, Integer year);

    List<Song> findByNameAndGenreAndYearAndType(String name, Song.Genre genre, Integer year, Song.Type type);

    List<Song> findByNameAndGenreAndYear(String pattern, Song.Genre genre, Integer year);

    List<Song> findByNameAndGenreAndType(String pattern, Song.Genre genre, Song.Type type);

    List<Song> findByNameAndYearAndType(String pattern, Integer year, Song.Type type);

    List<Song> findByNameAndGenre(String pattern, Song.Genre genre);

    List<Song> findByNameAndYear(String pattern, Integer year);

    List<Song> findByNameAndType(String pattern, Song.Type type);

    List<Song> findByGenreAndYearAndType(Song.Genre genre, Integer year, Song.Type type);

    List<Song> findByYearAndType(Integer year, Song.Type type);

    List<Song> findByGenreAndYear(Song.Genre genre, Integer year);

    List<Song> findByGenreAndType(Song.Genre genre, Song.Type type);

    //PAGEABLE
    List<Song> findAll(Pageable page);

    List<Song> findByName(String name, Pageable pageable);

    List<Song> findByNameLike(String pattern, Pageable pageable);

    List<Song> findByType(Song.Type type, Pageable pageable);

    List<Song> findByGenre(Song.Genre genre, Pageable pageable);

    List<Song> findByYear(Integer year, Pageable pageable);

    List<Song> findSongsAlbumsByArtistsUuid(String uuid, Pageable pageable);

    List<Song> findByNameLikeAndGenreAndYearAndType(String pattern, Song.Genre genre, Integer year, Song.Type type, Pageable pageable);

    List<Song> findByNameLikeAndGenreAndYear(String pattern, Song.Genre genre, Integer year, Pageable pageable);

    List<Song> findByNameLikeAndYearAndType(String name, Integer year, Song.Type type, Pageable pageable);

    List<Song> findByNameLikeAndGenreAndType(String name, Song.Genre genre, Song.Type type, Pageable pageable);

    List<Song> findByNameLikeAndType(String name, Song.Type type, Pageable pageable);

    List<Song> findByNameLikeAndGenre(String name, Song.Genre genre, Pageable pageable);

    List<Song> findByNameLikeAndYear(String name, Integer year, Pageable pageable);

    List<Song> findByNameAndGenreAndYearAndType(String name, Song.Genre genre, Integer year, Song.Type type, Pageable pageable);

    List<Song> findByNameAndGenreAndYear(String pattern, Song.Genre genre, Integer year, Pageable pageable);

    List<Song> findByNameAndGenreAndType(String pattern, Song.Genre genre, Song.Type type, Pageable pageable);

    List<Song> findByNameAndYearAndType(String pattern, Integer year, Song.Type type, Pageable pageable);

    List<Song> findByNameAndGenre(String pattern, Song.Genre genre, Pageable pageable);

    List<Song> findByNameAndYear(String pattern, Integer year, Pageable pageable);

    List<Song> findByNameAndType(String pattern, Song.Type type, Pageable pageable);

    List<Song> findByGenreAndYearAndType(Song.Genre genre, Integer year, Song.Type type, Pageable pageable);

    List<Song> findByYearAndType(Integer year, Song.Type type, Pageable pageable);

    List<Song> findByGenreAndYear(Song.Genre genre, Integer year, Pageable pageable);

    List<Song> findByGenreAndType(Song.Genre genre, Song.Type type, Pageable pageable);
}