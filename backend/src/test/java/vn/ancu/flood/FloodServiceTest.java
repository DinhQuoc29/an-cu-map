package vn.ancu.flood;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.time.LocalDate;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class FloodServiceTest {
    private final FloodPointRepository repository = mock(FloodPointRepository.class);
    private final FloodService service = new FloodService(repository);

    @Test
    void noEvidenceDoesNotMeanLowRisk() {
        when(repository.findActiveNearby(21, 105.8, 500)).thenReturn(List.of());
        var result = service.calcRisk(21, 105.8);
        assertThat(result.riskScore()).isNull();
        assertThat(result.assessmentStatus()).isEqualTo("INSUFFICIENT_DATA");
        assertThat(result.evidence()).isEmpty();
    }

    @Test
    void historicalEvidenceRetainsItsSourceWithoutInventingAProbability() {
        var row = mock(FloodEvidenceRow.class);
        when(row.getId()).thenReturn(1L);
        when(row.getLocationLabel()).thenReturn("Intersection");
        when(row.getEventDate()).thenReturn(LocalDate.of(2024, 8, 13));
        when(row.getSourceName()).thenReturn("Source");
        when(row.getSourceUrl()).thenReturn("https://example.com/event");
        when(row.getDistanceM()).thenReturn(100.0);
        when(repository.findActiveNearby(21, 105.8, 500)).thenReturn(List.of(row));
        var result = service.calcRisk(21, 105.8);
        assertThat(result.riskScore()).isNull();
        assertThat(result.evidence().getFirst().sourceUrl()).isEqualTo("https://example.com/event");
        assertThat(result.nearbyPointsCount()).isEqualTo(1);
        assertThat(result.assessmentStatus()).isEqualTo("HISTORICAL_EVIDENCE");
    }
}
