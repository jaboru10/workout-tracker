package com.javier.workout.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Copia completa de los datos de la PWA (v3, local-first) de un usuario.
 *
 * La PWA guarda todo en el propio móvil (IndexedDB) y sube aquí una copia
 * para no perder datos y poder restaurarlos en otro dispositivo. Se guarda
 * como JSON en texto para no depender de la forma interna del documento
 * (las claves con '.' o '$' no serían válidas como subdocumento de Mongo).
 *
 * Un documento por usuario. `version` se incrementa en cada subida.
 */
@Document(collection = "snapshots")
public class Snapshot {

    @Id
    private String id;

    private String userId;
    private String json;        // datos de la PWA tal cual
    private Instant updatedAt;  // hora del servidor en la última subida
    private String deviceId;    // dispositivo que hizo la última subida
    private long version;
    private long sizeBytes;

    public Snapshot() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getJson() { return json; }
    public void setJson(String json) { this.json = json; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }

    public long getSizeBytes() { return sizeBytes; }
    public void setSizeBytes(long sizeBytes) { this.sizeBytes = sizeBytes; }
}
