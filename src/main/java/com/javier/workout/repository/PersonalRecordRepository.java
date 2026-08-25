package com.javier.workout.repository;

import com.javier.workout.model.PersonalRecord;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface PersonalRecordRepository extends MongoRepository<PersonalRecord, String> {
    List<PersonalRecord> findByUserIdAndExerciseId(String userId, String exerciseId);
    List<PersonalRecord> findBySourceSessionId(String sourceSessionId);
}
