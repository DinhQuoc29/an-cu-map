package vn.ancu.amenity;

import jakarta.persistence.*;
import org.locationtech.jts.geom.Point;

import java.time.Instant;

@Entity
@Table(name = "amenity")
public class Amenity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "osm_id")
    private Long osmId;

    @Column(name = "category", nullable = false, length = 32)
    private String category;

    @Column(name = "subcategory", length = 64)
    private String subcategory;

    @Column(name = "name", length = 255)
    private String name;

    @Column(name = "geom", nullable = false, columnDefinition = "geography(Point,4326)")
    private Point geom;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public Long getId() { return id; }
    public Long getOsmId() { return osmId; }
    public String getCategory() { return category; }
    public String getSubcategory() { return subcategory; }
    public String getName() { return name; }
    public Point getGeom() { return geom; }
    public Instant getCreatedAt() { return createdAt; }
}

