package com.javier.workout.controller;

import com.javier.workout.config.SecurityUtils;
import com.javier.workout.model.Exercise;
import com.javier.workout.repository.ExerciseRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exercises")
public class ExerciseController {

    private final ExerciseRepository repo;

    public ExerciseController(ExerciseRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Exercise> list() {
        return repo.findByUserIdAndActiveTrue(SecurityUtils.currentUserId());
    }

    @PostMapping
    public Exercise create(@RequestBody Exercise ex) {
        ex.setId(null);
        ex.setUserId(SecurityUtils.currentUserId());
        ex.setActive(true);
        return repo.save(ex);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Exercise> update(@PathVariable String id, @RequestBody Exercise ex) {
        return repo.findById(id)
                .filter(e -> e.getUserId().equals(SecurityUtils.currentUserId()))
                .map(existing -> {
                    existing.setName(ex.getName());
                    existing.setMuscleGroup(ex.getMuscleGroup());
                    existing.setBodyweight(ex.isBodyweight());
                    return ResponseEntity.ok(repo.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Soft delete: nunca borramos de verdad
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        return repo.findById(id)
                .filter(e -> e.getUserId().equals(SecurityUtils.currentUserId()))
                .map(e -> {
                    e.setActive(false);
                    repo.save(e);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
