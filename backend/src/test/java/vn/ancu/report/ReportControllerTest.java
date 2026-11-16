package vn.ancu.report;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import vn.ancu.amenity.AmenitiesResponseDto;
import vn.ancu.airquality.AirQualityResponseDto;
import vn.ancu.common.DataField;
import vn.ancu.common.SourceType;
import vn.ancu.common.GlobalExceptionHandler;
import vn.ancu.flood.FloodRiskResponseDto;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @WebMvcTest cho ReportController — kiểm tra validate params, response structure.
 * Không cần PostGIS (MockBean inject ReportService).
 */
@WebMvcTest(ReportController.class)
@Import(GlobalExceptionHandler.class)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportService reportService;

    @Test
    void missingLatLng_returns_400() throws Exception {
        mockMvc.perform(get("/api/report"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void invalidLat_returns_400() throws Exception {
        mockMvc.perform(get("/api/report").param("lat", "not-a-number").param("lng", "105.8"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void validRequest_returns_200_with_report_structure() throws Exception {
        // Giả lập FloodRiskResponse
        var floodField = new DataField<>(2, "", SourceType.HISTORICAL, "Test", 0.4, Instant.now());
        var floodRisk = new FloodRiskResponseDto(floodField, 3);
        var amenities = new AmenitiesResponseDto(List.of(), 2000.0);

        when(reportService.buildReport(anyDouble(), anyDouble(), isNull(), isNull(), anyString()))
                .thenReturn(new LocationReportDto(21.028, 105.834, amenities, null, floodRisk, null));

        mockMvc.perform(get("/api/report")
                        .param("lat", "21.028")
                        .param("lng", "105.834"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lat").value(21.028))
                .andExpect(jsonPath("$.lng").value(105.834))
                .andExpect(jsonPath("$.floodRisk.nearbyPointsCount").value(3))
                .andExpect(jsonPath("$.floodRisk.riskScore.value").value(2))
                .andExpect(jsonPath("$.commute").doesNotExist());
    }

    @Test
    void withDestination_passesDestToService() throws Exception {
        when(reportService.buildReport(eq(21.028), eq(105.834), eq(21.044), eq(105.852), eq("auto")))
                .thenReturn(new LocationReportDto(21.028, 105.834, null, null, null, null));

        mockMvc.perform(get("/api/report")
                        .param("lat", "21.028")
                        .param("lng", "105.834")
                        .param("destLat", "21.044")
                        .param("destLng", "105.852")
                        .param("mode", "auto"))
                .andExpect(status().isOk());
    }
}
