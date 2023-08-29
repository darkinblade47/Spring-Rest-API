package com.MONGOtify.LeMongo.repo;

import com.MONGOtify.LeMongo.entity.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ProfileRepository extends MongoRepository<Profile, String> {

    Optional<Profile> findProfileBy_id(String id);

    Optional<Profile> findProfileByIdUser(String id);

    Optional<Profile> findProfileByNickname(String nickname);

    Optional<Profile> findProfileByEmail(String email);

}

