package vn.ancu.report;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.ancu.amenity.AmenitiesResponseDto;
import vn.ancu.amenity.AmenityService;
import vn.ancu.airquality.AirQualityResponseDto;
import vn.ancu.airquality.OpenMeteoClient;
import vn.ancu.common.DataField;
import vn.ancu.common.SourceType;
import vn.ancu.flood.FloodRiskResponseDto;
import vn.ancu.flood.FloodService;
import vn.ancu.routing.RoutingService;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit test ReportService — kiểm tra parallel fetch và resilience.
 */
@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock AmenityService amenityService;
    @Mock OpenMeteoClient airQualityClient;
    @Mock FloodService floodService;
    @Mock RoutingService routingService;

    @InjectMocks ReportService reportService;

    @Test
    void allSourcesOk_returnsFullReport() {
        // Arrange
        var amenities = new AmenitiesResponseDto(List.of(), 2000.0);
        var airField  = new DataField<>(25.0, "µg/m³", SourceType.MODELED, "Open-Meteo", 0.6, Instant.now());
        var airQual   = new AirQualityResponseDto(airField, null);
        var floodField = new DataField<>(2, "", SourceType.HISTORICAL, "Test", 0.4, Instant.now());
        var flood     = new FloodRiskResponseDto(floodField, 2);

        when(amenityService.findNearby(anyDouble(), anyDouble())).thenReturn(amenities);
        when(airQualityClient.fetchAvgPm25(anyDouble(), anyDouble())).thenReturn(Mono.just(airQual));
        when(floodService.calcRisk(anyDouble(), anyDouble())).thenReturn(flood);

        // Act
        var result = reportService.buildReport(21.028, 105.834, null, null, "motor_scooter");

        // Assert
        assertThat(result.amenities()).isNotNull();
        assertThat(result.airQuality()).isNotNull();
        assertThat(result.floodRisk()).isNotNull();
        assertThat(result.commute()).isNull(); // không có destLat/destLng
    }

    @Test
    void oneSourceThrows_otherSourcesStillPresent() {
        // Arrange: airQuality lỗi, các nguồn khác OK
        var amenities = new AmenitiesResponseDto(List.of(), 2000.0);
        var floodField = new DataField<>(1, "", SourceType.HISTORICAL, "Test", 0.2, Instant.now());
        var flood = new FloodRiskResponseDto(floodField, 0);

        when(amenityService.findNearby(anyDouble(), anyDouble())).thenReturn(amenities);
        when(airQualityClient.fetchAvgPm25(anyDouble(), anyDouble()))
                .thenReturn(Mono.error(new RuntimeException("Open-Meteo down")));
        when(floodService.calcRisk(anyDouble(), anyDouble())).thenReturn(flood);

        // Act — không được throw exception
        var result = reportService.buildReport(21.028, 105.834, null, null, "motor_scooter");

        // Assert: amenity và flood vẫn có, airQuality null
        assertThat(result.amenities()).isNotNull();
        assertThat(result.airQuality()).isNull();
        assertThat(result.floodRisk()).isNotNull();
    }

    @Test
    void withDestination_callsRoutingService() {
        // Arrange
        when(amenityService.findNearby(anyDouble(), anyDouble()))
                .thenReturn(new AmenitiesResponseDto(List.of(), 2000.0));
        when(airQualityClient.fetchAvgPm25(anyDouble(), anyDouble())).thenReturn(Mono.empty());
        when(floodService.calcRisk(anyDouble(), anyDouble()))
                .thenReturn(new FloodRiskResponseDto(
                        new DataField<>(1, "", SourceType.HISTORICAL, "T", 0.2, Instant.now()), 0));
        when(routingService.route(anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyString()))
                .thenReturn(null); // Valhalla không khả dụng

        // Act
        var result = reportService.buildReport(21.028, 105.834, 21.044, 105.852, "auto");

        // Assert: routingService được gọi
        verify(routingService).route(eq(21.028), eq(105.834), eq(21.044), eq(105.852), eq("auto"));
    }
}

