package com.javier.workout;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javier.workout.repository.SnapshotRepository;
import com.javier.workout.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de integración contra un MongoDB real (en CI, servicio mongo:7 de
 * GitHub Actions; en local, el de docker compose).
 */
@SpringBootTest(properties = {
        "app.bootstrap.username=ci-user",
        "app.bootstrap.password=ci-password-123",
        "app.jwt.secret=ci-secret-ci-secret-ci-secret-ci-secret-0123456789"
})
@AutoConfigureMockMvc
class ApiIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @Autowired SnapshotRepository snapshots;
    @Autowired UserRepository users;

    private String bearer() throws Exception {
        MvcResult res = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"ci-user\",\"password\":\"ci-password-123\"}"))
                .andExpect(status().isOk())
                .andReturn();
        return "Bearer " + om.readTree(res.getResponse().getContentAsString()).get("token").asText();
    }

    @BeforeEach
    void cleanSnapshots() {
        users.findByUsername("ci-user")
                .flatMap(u -> snapshots.findByUserId(u.getId()))
                .ifPresent(snapshots::delete);
    }

    @Test
    void bootstrapUserCanLogin() throws Exception {
        assertThat(bearer()).startsWith("Bearer ").hasSizeGreaterThan(20);
    }

    @Test
    void wrongPasswordIsRejected() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"ci-user\",\"password\":\"mala\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpointsWithoutValidTokenReturn401() throws Exception {
        mvc.perform(get("/api/snapshot")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/sessions").header(HttpHeaders.AUTHORIZATION, "Bearer token-invalido"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void snapshotRoundTrip() throws Exception {
        String auth = bearer();
        mvc.perform(get("/api/snapshot").header(HttpHeaders.AUTHORIZATION, auth))
                .andExpect(status().isNoContent());

        String body = om.writeValueAsString(Map.of(
                "deviceId", "test-device",
                "data", Map.of(
                        "schemaVersion", 1,
                        "sessions", List.of(Map.of("id", "s1", "date", "2026-10-30", "notes", "a.b $c")))));

        mvc.perform(put("/api/snapshot")
                        .header(HttpHeaders.AUTHORIZATION, auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value(1))
                .andExpect(jsonPath("$.updatedAt").exists())
                .andExpect(jsonPath("$.data").doesNotExist());

        mvc.perform(get("/api/snapshot").header(HttpHeaders.AUTHORIZATION, auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deviceId").value("test-device"))
                .andExpect(jsonPath("$.data.schemaVersion").value(1))
                .andExpect(jsonPath("$.data.sessions[0].id").value("s1"))
                .andExpect(jsonPath("$.data.sessions[0].notes").value("a.b $c"));

        mvc.perform(get("/api/snapshot/meta").header(HttpHeaders.AUTHORIZATION, auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value(1))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void snapshotConflictUnlessForced() throws Exception {
        String auth = bearer();
        String first = om.writeValueAsString(Map.of("deviceId", "movil", "data", Map.of("schemaVersion", 1)));
        mvc.perform(put("/api/snapshot").header(HttpHeaders.AUTHORIZATION, auth)
                        .contentType(MediaType.APPLICATION_JSON).content(first))
                .andExpect(status().isOk());

        String stale = om.writeValueAsString(Map.of(
                "deviceId", "otro", "baseUpdatedAt", "2000-01-01T00:00:00Z", "data", Map.of("schemaVersion", 1)));
        mvc.perform(put("/api/snapshot").header(HttpHeaders.AUTHORIZATION, auth)
                        .contentType(MediaType.APPLICATION_JSON).content(stale))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.deviceId").value("movil"));

        String forced = om.writeValueAsString(Map.of(
                "deviceId", "otro", "baseUpdatedAt", "2000-01-01T00:00:00Z", "force", true,
                "data", Map.of("schemaVersion", 1)));
        mvc.perform(put("/api/snapshot").header(HttpHeaders.AUTHORIZATION, auth)
                        .contentType(MediaType.APPLICATION_JSON).content(forced))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value(2))
                .andExpect(jsonPath("$.deviceId").value("otro"));
    }

    @Test
    void snapshotWithoutDataIsBadRequest() throws Exception {
        mvc.perform(put("/api/snapshot").header(HttpHeaders.AUTHORIZATION, bearer())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"deviceId\":\"x\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void legacyEndpointsStillWork() throws Exception {
        String auth = bearer();
        mvc.perform(get("/api/exercises").header(HttpHeaders.AUTHORIZATION, auth)).andExpect(status().isOk());
        mvc.perform(get("/api/sessions").header(HttpHeaders.AUTHORIZATION, auth)).andExpect(status().isOk());
        mvc.perform(get("/api/routines").header(HttpHeaders.AUTHORIZATION, auth)).andExpect(status().isOk());
        mvc.perform(get("/api/training-days").header(HttpHeaders.AUTHORIZATION, auth)).andExpect(status().isOk());
    }

    @Test
    void corsAllowsGithubPagesOrigin() throws Exception {
        mvc.perform(options("/api/snapshot")
                        .header(HttpHeaders.ORIGIN, "https://jaboru10.github.io")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "PUT")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "authorization,content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "https://jaboru10.github.io"));
    }
}
