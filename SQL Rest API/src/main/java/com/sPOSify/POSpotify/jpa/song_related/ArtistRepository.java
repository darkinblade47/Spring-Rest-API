package com.sPOSify.POSpotify.jpa.song_related;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ArtistRepository extends CrudRepository<Artist, String> {
    Artist findByName(String name);

    Artist findByIsActive(Boolean isActive);

    List<Artist> findArtistsBySongsId(Integer id);

    List<Artist> findByNameLike(String pattern);

    @Query(value = "SELECT * FROM artists WHERE" +
            "(?1 is null or name like ?1) and (?2 is null or is_active=?2)", nativeQuery = true)
    List<Artist> findComplexSearch(String pattern, Boolean isActive);

    @Query(value = "SELECT * FROM artists WHERE" +
            "(?1 is null or name like ?1) and (?2 is null or is_active=?2)",
            countQuery = "SELECT count(*) from artists",
            nativeQuery = true)
    List<Artist> findComplexSearch(String pattern, Boolean isActive, Pageable pageable);

    List<Artist> findAll(Pageable pageable);
}