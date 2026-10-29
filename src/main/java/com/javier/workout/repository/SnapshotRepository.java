package com.javier.workout.repository;

import com.javier.workout.model.Snapshot;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface SnapshotRepository extends MongoRepository<Snapshot, String> {
    Optional<Snapshot> findByUserId(String userId);
}
