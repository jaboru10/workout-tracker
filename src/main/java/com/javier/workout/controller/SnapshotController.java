package com.javier.workout.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javier.workout.config.SecurityUtils;
import com.javier.workout.model.Snapshot;
import com.javier.workout.repository.SnapshotRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Copia de seguridad / sincronización de la PWA local-first (v3).
 *
 *  GET  /api/snapshot       → 200 { updatedAt, deviceId, version, sizeBytes, data } · 204 si no hay copia
 *  GET  /api/snapshot/meta  → igual pero sin `data` (para saber si hay algo más nuevo)
 *  PUT  /api/snapshot       → body { data, deviceId, baseUpdatedAt, force }
 *       - Si el servidor tiene una copia más nueva que `baseUpdatedAt` (otro
 *         dispositivo subió después) responde 409 con la meta del servidor,
 *         salvo que `force` sea true.
 *
 * También sirve como exportación completa para herramientas externas
 * (por ejemplo, un MCP que lea el entrenamiento de fuerza).
 */
@RestController
@RequestMapping("/api/snapshot")
public class SnapshotController {

    private final SnapshotRepository repo;
    private final ObjectMapper mapper;

    public SnapshotController(SnapshotRepository repo, ObjectMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    public record SnapshotRequest(JsonNode data, String deviceId, Instant baseUpdatedAt, Boolean force) {}

    @GetMapping
    public ResponseEntity<?> get() {
        return repo.findByUserId(SecurityUtils.currentUserId())
                .<ResponseEntity<?>>map(s -> ResponseEntity.ok(toBody(s, true)))
                .orElse(ResponseEntity.noContent().build());
    }

    @GetMapping("/meta")
    public ResponseEntity<?> meta() {
        return repo.findByUserId(SecurityUtils.currentUserId())
                .<ResponseEntity<?>>map(s -> ResponseEntity.ok(toBody(s, false)))
                .orElse(ResponseEntity.noContent().build());
    }

    @PutMapping
    public ResponseEntity<?> put(@RequestBody SnapshotRequest req) {
        if (req == null || req.data() == null || !req.data().isObject()) {
            return ResponseEntity.badRequest().body("Falta el objeto 'data'");
        }
        String userId = SecurityUtils.currentUserId();
        Snapshot snap = repo.findByUserId(userId).orElse(null);

        boolean force = Boolean.TRUE.equals(req.force());
        if (snap != null && !force && req.baseUpdatedAt() != null && snap.getUpdatedAt() != null
                && snap.getUpdatedAt().isAfter(req.baseUpdatedAt())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(toBody(snap, false));
        }

        String json;
        try {
            json = mapper.writeValueAsString(req.data());
        } catch (JsonProcessingException e) {
            return ResponseEntity.badRequest().body("JSON no válido");
        }

        if (snap == null) {
            snap = new Snapshot();
            snap.setUserId(userId);
        }
        snap.setJson(json);
        snap.setSizeBytes(json.getBytes(StandardCharsets.UTF_8).length);
        snap.setDeviceId(req.deviceId());
        snap.setUpdatedAt(Instant.now());
        snap.setVersion(snap.getVersion() + 1);
        snap = repo.save(snap);

        return ResponseEntity.ok(toBody(snap, false));
    }

    private Map<String, Object> toBody(Snapshot s, boolean withData) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("updatedAt", s.getUpdatedAt());
        body.put("deviceId", s.getDeviceId());
        body.put("version", s.getVersion());
        body.put("sizeBytes", s.getSizeBytes());
        if (withData) {
            try {
                body.put("data", s.getJson() == null ? null : mapper.readTree(s.getJson()));
            } catch (JsonProcessingException e) {
                body.put("data", null);
            }
        }
        return body;
    }
}
