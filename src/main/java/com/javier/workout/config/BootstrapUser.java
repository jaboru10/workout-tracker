package com.javier.workout.config;

import com.javier.workout.model.User;
import com.javier.workout.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * El registro público está bloqueado (KAN-9). Para no tener que crear el
 * usuario a mano en Atlas en una base nueva (o en CI), si se definen
 * APP_BOOTSTRAP_USERNAME y APP_BOOTSTRAP_PASSWORD se crea ese usuario al
 * arrancar, solo si no existe. Nunca modifica un usuario existente y nunca
 * tumba el arranque.
 */
@Component
public class BootstrapUser implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(BootstrapUser.class);

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final String username;
    private final String password;

    public BootstrapUser(UserRepository users,
                         PasswordEncoder encoder,
                         @Value("${app.bootstrap.username:}") String username,
                         @Value("${app.bootstrap.password:}") String password) {
        this.users = users;
        this.encoder = encoder;
        this.username = username == null ? "" : username.trim();
        this.password = password == null ? "" : password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (username.isEmpty() || password.isEmpty()) return;
        try {
            if (users.existsByUsername(username)) return;
            users.save(new User(username, encoder.encode(password)));
            log.info("Usuario inicial '{}' creado (APP_BOOTSTRAP_USERNAME)", username);
        } catch (Exception e) {
            log.warn("No se pudo crear el usuario inicial: {}", e.getMessage());
        }
    }
}
