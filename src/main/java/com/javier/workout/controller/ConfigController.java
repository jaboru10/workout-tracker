package com.javier.workout.controller;

import com.javier.workout.config.SecurityUtils;
import com.javier.workout.model.UserConfig;
import com.javier.workout.repository.UserConfigRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/config")
public class ConfigController {

    private final UserConfigRepository repo;

    public ConfigController(UserConfigRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public UserConfig get() {
        String userId = SecurityUtils.currentUserId();
        return repo.findByUserId(userId).orElseGet(() -> {
            UserConfig c = new UserConfig();
            c.setUserId(userId);
            c.getRepRanges().add(new UserConfig.RepRange("Fuerza", 1, 5));
            c.getRepRanges().add(new UserConfig.RepRange("Hipertrofia", 6, 12));
            c.getRepRanges().add(new UserConfig.RepRange("Resistencia", 13, 20));
            return repo.save(c);
        });
    }

    @PutMapping
    public UserConfig update(@RequestBody UserConfig config) {
        String userId = SecurityUtils.currentUserId();
        UserConfig existing = repo.findByUserId(userId).orElseGet(UserConfig::new);
        existing.setUserId(userId);
        existing.setRepRanges(config.getRepRanges());
        existing.setDefaultRecordWindowMonths(config.getDefaultRecordWindowMonths());
        existing.setRecordWindowsMonths(config.getRecordWindowsMonths());
        return repo.save(existing);
    }
}
