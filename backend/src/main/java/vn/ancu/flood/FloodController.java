package vn.ancu.flood;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

/**
 * REST controller cho flood risk.
 * GET /api/flood/risk
 */
@RestController
@RequestMapping("/api/flood")
@Validated
public class FloodController {

    private final FloodService service;

    public FloodController(FloodService service) {
        this.service = service;
    }

    @GetMapping("/risk")
    public ResponseEntity<FloodRiskResponseDto> getRisk(
            @RequestParam @DecimalMin("-90.0") @DecimalMax("90.0") double lat,
            @RequestParam @DecimalMin("-180.0") @DecimalMax("180.0") double lng
    ) {
        return ResponseEntity.ok(service.calcRisk(lat, lng));
    }
}

