package com.javier.workout.controller;

import com.javier.workout.config.JwtService;
import com.javier.workout.dto.AuthDtos.*;
import com.javier.workout.model.User;
import com.javier.workout.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;

    public AuthController(UserRepository userRepo, PasswordEncoder encoder, JwtService jwtService) {
        this.userRepo = userRepo;
        this.encoder = encoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        if (userRepo.existsByUsername(req.username())) {
            return ResponseEntity.badRequest().body("Username already taken");
        }
        User user = new User(req.username(), encoder.encode(req.password()));
        user = userRepo.save(user);
        String token = jwtService.generateToken(user.getId(), user.getUsername());
        return ResponseEntity.ok(new AuthResponse(token, user.getId(), user.getUsername()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        User user = userRepo.findByUsername(req.username()).orElse(null);
        if (user == null || !encoder.matches(req.password(), user.getPassword())) {
            return ResponseEntity.status(401).body("Invalid credentials");
        }
        String token = jwtService.generateToken(user.getId(), user.getUsername());
        return ResponseEntity.ok(new AuthResponse(token, user.getId(), user.getUsername()));
    }
}
