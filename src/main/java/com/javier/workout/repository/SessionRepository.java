package com.javier.workout.repository;

import com.javier.workout.model.Session;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.time.LocalDate;
import java.util.List;

public interface SessionRepository extends MongoRepository<Session, String> {
    List<Session> findByUserIdOrderByDateDesc(String userId);
    List<Session> findByUserIdAndDateBetween(String userId, LocalDate from, LocalDate to);
    List<Session> findByUserIdAndExercisesExerciseId(String userId, String exerciseId);
}
