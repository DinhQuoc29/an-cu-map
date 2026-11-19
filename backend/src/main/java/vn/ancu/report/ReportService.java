package vn.ancu.report;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import vn.ancu.amenity.AmenitiesResponseDto;
import vn.ancu.amenity.AmenityService;
import vn.ancu.airquality.AirQualityResponseDto;
import vn.ancu.airquality.OpenMeteoClient;
import vn.ancu.flood.FloodRiskResponseDto;
import vn.ancu.flood.FloodService;
import vn.ancu.routing.RouteResultDto;
import vn.ancu.routing.RoutingService;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Facade tổng hợp báo cáo vị trí.
 *
 * <p>Gọi song song 4 nguồn dữ liệu bằng CompletableFuture.
 * Mỗi nguồn lỗi trả null — không làm sập toàn bộ báo cáo.
 * Timeout tổng: 15 giây.
 */
@Service
public class ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportService.class);
    private static final long TOTAL_TIMEOUT_SECONDS = 15;

    private final AmenityService amenityService;
    private final OpenMeteoClient airQualityClient;
    private final FloodService floodService;
    private final RoutingService routingService;

    public ReportService(AmenityService amenityService,
                         OpenMeteoClient airQualityClient,
                         FloodService floodService,
                         RoutingService routingService) {
        this.amenityService = amenityService;
        this.airQualityClient = airQualityClient;
        this.floodService = floodService;
        this.routingService = routingService;
    }

    /**
     * Tổng hợp báo cáo vị trí.
     *
     * @param lat      vĩ độ điểm cần báo cáo
     * @param lng      kinh độ điểm cần báo cáo
     * @param destLat  vĩ độ điểm đến (nullable — bỏ qua commute nếu null)
     * @param destLng  kinh độ điểm đến (nullable)
     * @param mode     Valhalla costing mode
     */
    public LocationReportDto buildReport(double lat, double lng,
                                          Double destLat, Double destLng,
                                          String mode) {
        // ── Khởi động song song 4 future ──────────────────────────────────────
        var amenitiesFuture = CompletableFuture.supplyAsync(() ->
                safeGet("amenity", () -> amenityService.findNearby(lat, lng)));

        var airFuture = CompletableFuture.supplyAsync(() ->
                safeGet("airQuality", () -> airQualityClient.fetchAvgPm25(lat, lng).block()));

        var floodFuture = CompletableFuture.supplyAsync(() ->
                safeGet("flood", () -> floodService.calcRisk(lat, lng)));

        CompletableFuture<List<RouteResultDto>> commuteFuture;
        if (destLat != null && destLng != null) {
            final double dLat = destLat;
            final double dLng = destLng;
            commuteFuture = CompletableFuture.supplyAsync(() ->
                    safeGet("commute", () -> fetchAllModes(lat, lng, dLat, dLng, mode)));
        } else {
            commuteFuture = CompletableFuture.completedFuture(null);
        }

        // ── Chờ tất cả với timeout tổng ───────────────────────────────────────
        try {
            CompletableFuture.allOf(amenitiesFuture, airFuture, floodFuture, commuteFuture)
                    .get(TOTAL_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (Exception ex) {
            log.warn("Report timeout or error: {}", ex.getClass().getSimpleName());
        }

        return new LocationReportDto(
                lat, lng,
                getNow(amenitiesFuture),
                getNow(airFuture),
                getNow(floodFuture),
                getNow(commuteFuture)
        );
    }

    /** Lấy kết quả routing cho mode được chọn (và tuỳ chọn tất cả modes) */
    private List<RouteResultDto> fetchAllModes(double oLat, double oLng,
                                                double dLat, double dLng,
                                                String primaryMode) {
        // MVP: chỉ tính mode được chọn để tránh 4 lần gọi Valhalla
        RouteResultDto result = routingService.route(oLat, oLng, dLat, dLng, primaryMode);
        return result != null ? List.of(result) : null;
    }

    /** Wrapper bắt mọi exception → trả null + log warning */
    private <T> T safeGet(String source, java.util.function.Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Exception ex) {
            log.warn("Source '{}' error: {}", source, ex.getMessage());
            return null;
        }
    }

    /** Lấy kết quả Future không throw exception */
    private <T> T getNow(CompletableFuture<T> future) {
        try {
            return future.getNow(null);
        } catch (Exception ex) {
            return null;
        }
    }
}

