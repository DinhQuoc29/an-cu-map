package vn.ancu.airquality;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

/**
 * REST controller cho air quality.
 * GET /api/air-quality
 */
@RestController
@RequestMapping("/api/air-quality")
@Validated
public class AirQualityController {

    private final OpenMeteoClient client;

    public AirQualityController(OpenMeteoClient client) {
        this.client = client;
    }

    @GetMapping
    public ResponseEntity<AirQualityResponseDto> getAirQuality(
            @RequestParam @DecimalMin("-90.0") @DecimalMax("90.0") double lat,
            @RequestParam @DecimalMin("-180.0") @DecimalMax("180.0") double lng
    ) {
        AirQualityResponseDto result = client.fetchAvgPm25(lat, lng).block();
        if (result == null) {
            return ResponseEntity.ok(null);
        }
        return ResponseEntity.ok(result);
    }
}

