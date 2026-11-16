package vn.ancu.amenity;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

/**
 * REST controller cho amenity.
 * GET /api/amenities/nearby — tìm tiện ích gần nhất.
 */
@RestController
@RequestMapping("/api/amenities")
@Validated
public class AmenityController {

    private final AmenityService service;

    public AmenityController(AmenityService service) {
        this.service = service;
    }

    @GetMapping("/nearby")
    public ResponseEntity<AmenitiesResponseDto> nearby(
            @RequestParam @DecimalMin("-90.0") @DecimalMax("90.0") double lat,
            @RequestParam @DecimalMin("-180.0") @DecimalMax("180.0") double lng
    ) {
        return ResponseEntity.ok(service.findNearby(lat, lng));
    }
}

