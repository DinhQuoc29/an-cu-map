package vn.ancu.flood;

import jakarta.persistence.*;
import org.locationtech.jts.geom.Point;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Entity điểm ngập lịch sử.
 * Bảng flood_point (V1__init.sql)
 */
@Entity
@Table(name = "flood_point")
public class FloodPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "geom", nullable = false, columnDefinition = "geography(Point,4326)")
    private Point geom;

    @Column(name = "severity", nullable = false)
    private short severity;   // 1-5 (SMALLINT)

    @Column(name = "source_type", nullable = false, length = 16)
    private String sourceType;

    @Column(name = "source_name", length = 255)
    private String sourceName;

    @Column(name = "source_url")
    private String sourceUrl;

    @Column(name = "event_date")
    private LocalDate eventDate;

    @Column(name = "confidence", precision = 4, scale = 3)
    private BigDecimal confidence;

    @Column(name = "status", nullable = false, length = 16)
    private String status = "ACTIVE";

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    // ─── Getters ─────────────────────────────────────────────────────────────

    public Long getId() { return id; }
    public Point getGeom() { return geom; }
    public int getSeverity() { return severity; }
    public String getSourceType() { return sourceType; }
    public String getSourceName() { return sourceName; }
    public String getSourceUrl() { return sourceUrl; }
    public LocalDate getEventDate() { return eventDate; }
    public Double getConfidence() { return confidence != null ? confidence.doubleValue() : null; }
    public BigDecimal getConfidenceBigDecimal() { return confidence; }
    public String getStatus() { return status; }
    public Instant getExpiresAt() { return expiresAt; }
    public Instant getCreatedAt() { return createdAt; }
}
