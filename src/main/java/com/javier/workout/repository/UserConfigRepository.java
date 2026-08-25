package com.javier.workout.repository;

import com.javier.workout.model.UserConfig;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface UserConfigRepository extends MongoRepository<UserConfig, String> {
    Optional<UserConfig> findByUserId(String userId);
}
