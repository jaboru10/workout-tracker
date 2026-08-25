package com.javier.workout.repository;

import com.javier.workout.model.TrainingDay;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface TrainingDayRepository extends MongoRepository<TrainingDay, String> {
    List<TrainingDay> findByUserIdAndActiveTrueOrderByOrderAsc(String userId);
}
