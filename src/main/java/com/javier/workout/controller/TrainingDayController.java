package com.javier.workout.controller;

import com.javier.workout.config.SecurityUtils;
import com.javier.workout.model.TrainingDay;
import com.javier.workout.repository.TrainingDayRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/training-days")
public class TrainingDayController {

    private final TrainingDayRepository repo;

    public TrainingDayController(TrainingDayRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<TrainingDay> list() {
        return repo.findByUserIdAndActiveTrueOrderByOrderAsc(SecurityUtils.currentUserId());
    }

    @PostMapping
    public TrainingDay create(@RequestBody TrainingDay day) {
        day.setId(null);
        day.setUserId(SecurityUtils.currentUserId());
        day.setActive(true);
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
}
