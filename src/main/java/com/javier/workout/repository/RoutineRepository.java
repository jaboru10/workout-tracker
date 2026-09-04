package com.javier.workout.repository;

import com.javier.workout.model.Routine;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface RoutineRepository extends MongoRepository<Routine, String> {

    // Rutinas del usuario (no predefinidas), sin archivar
    List<Routine> findByUserIdAndArchivedFalse(String userId);

    // Biblioteca de predefinidas (globales)
    List<Routine> findByPresetTrueAndArchivedFalse();

    // La rutina activa del usuario (solo debería haber una)
    Optional<Routine> findByUserIdAndActiveTrueAndArchivedFalse(String userId);
}
