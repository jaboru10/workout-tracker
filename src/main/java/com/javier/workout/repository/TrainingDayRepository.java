package com.javier.workout.repository;

import com.javier.workout.model.TrainingDay;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface TrainingDayRepository extends MongoRepository<TrainingDay, String> {
    List<TrainingDay> findByUserIdAndActiveTrueOrderByOrderAsc(String userId);

    // Días de una rutina concreta (IL-004)
    List<TrainingDay> findByUserIdAndRoutineIdAndActiveTrueOrderByOrderAsc(String userId, String routineId);

    // Días legacy sin rutina (datos previos a IL-004)
    List<TrainingDay> findByUserIdAndRoutineIdIsNullAndActiveTrueOrderByOrderAsc(String userId);
}
