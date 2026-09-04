package com.javier.workout.controller;

import com.javier.workout.config.SecurityUtils;
import com.javier.workout.model.Routine;
import com.javier.workout.model.TrainingDay;
import com.javier.workout.repository.RoutineRepository;
import com.javier.workout.repository.TrainingDayRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/training-days")
public class TrainingDayController {

    private final TrainingDayRepository repo;
    private final RoutineRepository routineRepo;

    public TrainingDayController(TrainingDayRepository repo, RoutineRepository routineRepo) {
        this.repo = repo;
        this.routineRepo = routineRepo;
    }

    /**
     * Días de una rutina. Sin parámetro devuelve los de la rutina activa.
     * Si el usuario no tiene rutina activa, devuelve los días legacy (sin
     * rutina), para no romper datos previos a IL-004.
     */
    @GetMapping
    public List<TrainingDay> list(@RequestParam(required = false) String routineId) {
        String userId = SecurityUtils.currentUserId();
        String target = routineId != null ? routineId : activeRoutineId(userId);
        if (target == null) {
            return repo.findByUserIdAndRoutineIdIsNullAndActiveTrueOrderByOrderAsc(userId);
        }
        return repo.findByUserIdAndRoutineIdAndActiveTrueOrderByOrderAsc(userId, target);
    }

    @PostMapping
    public TrainingDay create(@RequestBody TrainingDay day) {
        String userId = SecurityUtils.currentUserId();
        day.setId(null);
        day.setUserId(userId);
        day.setActive(true);
        // El día cuelga de la rutina indicada o, por defecto, de la activa.
        if (day.getRoutineId() == null) {
            day.setRoutineId(activeRoutineId(userId));
        }
        return repo.save(day);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TrainingDay> update(@PathVariable String id, @RequestBody TrainingDay day) {
        return repo.findById(id)
                .filter(d -> d.getUserId().equals(SecurityUtils.currentUserId()))
                .map(existing -> {
                    existing.setName(day.getName());
                    existing.setOrder(day.getOrder());
                    existing.setExercises(day.getExercises());
                    return ResponseEntity.ok(repo.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        return repo.findById(id)
                .filter(d -> d.getUserId().equals(SecurityUtils.currentUserId()))
                .map(d -> {
                    d.setActive(false);
                    repo.save(d);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private String activeRoutineId(String userId) {
        return routineRepo.findByUserIdAndActiveTrueAndArchivedFalse(userId)
                .map(Routine::getId)
                .orElse(null);
    }
}
