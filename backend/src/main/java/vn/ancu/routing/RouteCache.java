package vn.ancu.routing;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Entity cache kết quả routing theo ô lưới geohash (~500 m).
 * Schema: route_cache (V1__init.sql)
 */
@Entity
@Table(name = "route_cache")
public class RouteCache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "origin_cell", nullable = false, length = 16)
    private String originCell;

    @Column(name = "dest_cell", nullable = false, length = 16)
    private String destCell;

    @Column(name = "mode", nullable = false, length = 32)
    private String mode;

    @Column(name = "depart_bucket", nullable = false)
    private short departBucket;

    @Column(name = "duration_s", nullable = false)
    private int durationS;

    @Column(name = "distance_m", nullable = false)
    private int distanceM;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public RouteCache() {}

    public RouteCache(String originCell, String destCell, String mode,
                      short departBucket, int durationS, int distanceM) {
        this.originCell = originCell;
        this.destCell = destCell;
        this.mode = mode;
        this.departBucket = departBucket;
        this.durationS = durationS;
        this.distanceM = distanceM;
    }

    // ─── Getters ─────────────────────────────────────────────────────────────

    public Long getId() { return id; }
    public String getOriginCell() { return originCell; }
    public String getDestCell() { return destCell; }
    public String getMode() { return mode; }
    public short getDepartBucket() { return departBucket; }
    public int getDurationS() { return durationS; }
    public int getDistanceM() { return distanceM; }
    public Instant getCreatedAt() { return createdAt; }
}

