package com.javier.workout.controller;

import com.javier.workout.config.SecurityUtils;
import com.javier.workout.model.Routine;
import com.javier.workout.model.Session;
import com.javier.workout.repository.RoutineRepository;
import com.javier.workout.repository.SessionRepository;
import com.javier.workout.service.RecordService;
import com.javier.workout.service.RecordService.RecordResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    private final SessionRepository repo;
    private final RoutineRepository routineRepo;
    private final RecordService recordService;

    public SessionController(SessionRepository repo, RoutineRepository routineRepo, RecordService recordService) {
        this.repo = repo;
        this.routineRepo = routineRepo;
        this.recordService = recordService;
    }

    @GetMapping
    public List<Session> list() {
        return repo.findByUserIdOrderByDateDesc(SecurityUtils.currentUserId());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Session> get(@PathVariable String id) {
        return repo.findById(id)
                .filter(s -> s.getUserId().equals(SecurityUtils.currentUserId()))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Al crear una sesión, se calculan los récords batidos y se devuelven
     * junto con la sesión guardada.
     */
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Session session) {
        String userId = SecurityUtils.currentUserId();
        session.setId(null);
        session.setUserId(userId);
        // Congela la rutina activa en la sesión (IL-004). Si no hay activa,
        // queda sin rutina (sesión "suelta"), no es error.
        routineRepo.findByUserIdAndActiveTrueAndArchivedFalse(userId).ifPresent(r -> {
            session.setRoutineId(r.getId());
            session.setRoutineName(r.getName());
        });
        Session saved = repo.save(session);

        List<RecordResult> newRecords = recordService.recalcRecords(saved);

        return ResponseEntity.ok(Map.of(
                "session", saved,
                "newRecords", newRecords
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id, @RequestBody Session session) {
        return repo.findById(id)
                .filter(s -> s.getUserId().equals(SecurityUtils.currentUserId()))
                .map(existing -> {
                    session.setId(existing.getId());
                    session.setUserId(existing.getUserId());
                    // La rutina queda CONGELADA en la creación: al editar se
                    // conserva la de la sesión original, no la activa actual.
                    session.setRoutineId(existing.getRoutineId());
                    session.setRoutineName(existing.getRoutineName());
                    Session saved = repo.save(session);
                    List<RecordResult> newRecords = recordService.recalcRecords(saved);
                    return ResponseEntity.ok((Object) Map.of(
                            "session", saved,
                            "newRecords", newRecords));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        String userId = SecurityUtils.currentUserId();
        return repo.findById(id)
                .filter(s -> s.getUserId().equals(userId))
                .map(s -> {
                    repo.delete(s);
                    // recalcula récords si esta sesión era origen de alguno
                    recordService.handleSessionDeletion(userId, id);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
