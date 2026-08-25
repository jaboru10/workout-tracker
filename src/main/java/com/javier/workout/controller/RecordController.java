package com.javier.workout.controller;

import com.javier.workout.config.SecurityUtils;
import com.javier.workout.service.RecordService;
import com.javier.workout.service.RecordService.RecordResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/records")
public class RecordController {

    private final RecordService recordService;

    public RecordController(RecordService recordService) {
        this.recordService = recordService;
    }

    /**
     * GET /api/records/{exerciseId}?windowMonths=6&position=1
     * windowMonths: 0 = histórico. position: opcional (contexto de fatiga).
     */
    @GetMapping("/{exerciseId}")
    public List<RecordResult> records(
            @PathVariable String exerciseId,
            @RequestParam(defaultValue = "6") int windowMonths,
            @RequestParam(required = false) Integer position) {
        return recordService.computeRecords(
                SecurityUtils.currentUserId(), exerciseId, windowMonths, position);
    }
}
