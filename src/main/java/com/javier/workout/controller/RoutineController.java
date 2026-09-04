package com.javier.workout.controller;

import com.javier.workout.config.SecurityUtils;
import com.javier.workout.model.Routine;
import com.javier.workout.repository.RoutineRepository;
import com.javier.workout.service.RoutineService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/routines")
public class RoutineController {

    private final RoutineRepository repo;
    private final RoutineService service;

    public RoutineController(RoutineRepository repo, RoutineService service) {
        this.repo = repo;
        this.service = service;
    }

    /** Mis rutinas (copias personales + propias), no las predefinidas. */
    @GetMapping
    public List<Routine> list() {
        return repo.findByUserIdAndArchivedFalse(SecurityUtils.currentUserId());
    }

    /** La rutina activa del usuario (204 si no tiene ninguna). */
    @GetMapping("/active")
    public ResponseEntity<Routine> active() {
        return repo.findByUserIdAndActiveTrueAndArchivedFalse(SecurityUtils.currentUserId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    /** Biblioteca de predefinidas (solo lectura). */
    @GetMapping("/presets")
    public List<Routine> presets() {
        return repo.findByPresetTrueAndArchivedFalse();
    }

    /** Crea una rutina vacía desde cero. */
    @PostMapping
    public Routine create(@RequestBody Routine body) {
        return service.createEmpty(
                SecurityUtils.currentUserId(), body.getName(), body.getLevel(), body.getType());
    }

    /** Usa una predefinida: crea una copia personal modificable y la activa. */
    @PostMapping("/from-preset/{presetId}")
    public ResponseEntity<Routine> fromPreset(@PathVariable String presetId) {
        return repo.findById(presetId)
                .filter(Routine::isPreset)
                .map(preset -> ResponseEntity.ok(
                        service.copyFromPreset(SecurityUtils.currentUserId(), preset)))
                .orElse(ResponseEntity.notFound().build());
    }

    /** Renombrar / cambiar categoría. */
    @PutMapping("/{id}")
    public ResponseEntity<Routine> update(@PathVariable String id, @RequestBody Routine body) {
        String userId = SecurityUtils.currentUserId();
        return repo.findById(id)
                .filter(r -> userId.equals(r.getUserId()))
                .map(existing -> {
                    existing.setName(body.getName());
                    existing.setLevel(body.getLevel());
                    existing.setType(body.getType());
                    return ResponseEntity.ok(repo.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /** Activar una rutina (desactiva el resto). */
    @PutMapping("/{id}/activate")
    public ResponseEntity<Routine> activate(@PathVariable String id) {
        String userId = SecurityUtils.currentUserId();
        return repo.findById(id)
                .filter(r -> userId.equals(r.getUserId()) && !r.isArchived())
                .map(r -> ResponseEntity.ok(service.activate(userId, r)))
                .orElse(ResponseEntity.notFound().build());
    }

    /** Archivar (soft delete). No borra sus días ni el historial. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        String userId = SecurityUtils.currentUserId();
        return repo.findById(id)
                .filter(r -> userId.equals(r.getUserId()))
                .map(r -> {
                    r.setArchived(true);
                    r.setActive(false);
                    repo.save(r);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
