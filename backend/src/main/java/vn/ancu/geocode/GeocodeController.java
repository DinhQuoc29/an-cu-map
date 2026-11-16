package vn.ancu.geocode;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller proxy Nominatim geocoding.
 * GET /api/geocode?q=...
 */
@RestController
@RequestMapping("/api/geocode")
public class GeocodeController {

    private final NominatimClient client;

    public GeocodeController(NominatimClient client) {
        this.client = client;
    }

    @GetMapping
    public ResponseEntity<List<GeocodeResultDto>> search(
            @RequestParam(required = false, defaultValue = "") String q
    ) {
        return ResponseEntity.ok(client.search(q));
    }
}

