package vn.ancu.report;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller báo cáo tổng hợp vị trí.
 * GET /api/report
 */
@RestController
@RequestMapping("/api/report")
@Validated
public class ReportController {

    private final ReportService service;

    public ReportController(ReportService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<LocationReportDto> getReport(
            @RequestParam @DecimalMin("-90.0") @DecimalMax("90.0") double lat,
            @RequestParam @DecimalMin("-180.0") @DecimalMax("180.0") double lng,
            @RequestParam(required = false) @DecimalMin("-90.0") @DecimalMax("90.0") Double destLat,
            @RequestParam(required = false) @DecimalMin("-180.0") @DecimalMax("180.0") Double destLng,
            @RequestParam(defaultValue = "motor_scooter") @Pattern(regexp = "motor_scooter|auto|bicycle|pedestrian") String mode
    ) {
        if ((destLat == null) != (destLng == null)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Cần đủ destLat và destLng");
        }
        return ResponseEntity.ok(service.buildReport(lat, lng, destLat, destLng, mode));
    }
}

