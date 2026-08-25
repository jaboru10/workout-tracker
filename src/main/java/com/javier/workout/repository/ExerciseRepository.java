package com.javier.workout.repository;

import com.javier.workout.model.Exercise;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ExerciseRepository extends MongoRepository<Exercise, String> {
    List<Exercise> findByUserIdAndActiveTrue(String userId);
    List<Exercise> findByUserId(String userId);
}
