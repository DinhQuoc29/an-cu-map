package vn.ancu.routing;

import ch.hsr.geohash.GeoHash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.Optional;

/**
 * Service routing — cache kết quả Valhalla theo geohash ô lưới ~500 m (precision 6).
 *
 * <p>Cache lookup:
 * <ol>
 *   <li>Tính originCell, destCell từ geohash precision 6 (~610 × 610 m)</li>
 *   <li>departBucket = giờ hiện tại (0-23) — đơn giản hóa cho MVP</li>
 *   <li>Tìm trong route_cache → hit: trả DB; miss: gọi ValhallaClient → lưu DB</li>
 * </ol>
 */
@Service
public class RoutingService {

    private static final Logger log = LoggerFactory.getLogger(RoutingService.class);
    private static final int GEOHASH_PRECISION = 6; // ~610 × 610 m

    private final ValhallaClient valhallaClient;
    private final RouteCacheRepository cacheRepository;

    public RoutingService(ValhallaClient valhallaClient, RouteCacheRepository cacheRepository) {
        this.valhallaClient = valhallaClient;
        this.cacheRepository = cacheRepository;
    }

    /**
     * Lấy kết quả route — từ cache hoặc gọi Valhalla.
     *
     * @return RouteResultDto hoặc null nếu Valhalla không khả dụng
     */
    public RouteResultDto route(double oLat, double oLng,
                                double dLat, double dLng,
                                String mode) {
        String originCell = GeoHash.geoHashStringWithCharacterPrecision(oLat, oLng, GEOHASH_PRECISION);
        String destCell   = GeoHash.geoHashStringWithCharacterPrecision(dLat, dLng, GEOHASH_PRECISION);
        short  bucket     = (short) LocalTime.now().getHour();

        // 1. Tìm cache
        Optional<RouteCache> cached = cacheRepository
                .findByOriginCellAndDestCellAndModeAndDepartBucket(originCell, destCell, mode, bucket);

        if (cached.isPresent()) {
            RouteCache c = cached.get();
            log.debug("Route cache hit: {} → {} mode={}", originCell, destCell, mode);
            return new RouteResultDto(mode, c.getDurationS(), c.getDistanceM());
        }

        // 2. Cache miss → gọi Valhalla (blocking từ Mono)
        RouteResultDto result = valhallaClient.route(oLat, oLng, dLat, dLng, mode).block();
        if (result == null) return null;

        // 3. Lưu cache (ignore conflict — unique constraint)
        try {
            cacheRepository.save(new RouteCache(
                    originCell, destCell, mode, bucket,
                    result.durationS(), result.distanceM()
            ));
        } catch (DataIntegrityViolationException ex) {
            log.debug("Route cache conflict (race condition), ignoring: {}", ex.getMessage());
        }

        return result;
    }
}

