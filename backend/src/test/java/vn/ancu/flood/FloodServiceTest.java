package vn.ancu.flood;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit test FloodService.computeScore — không cần Spring context, không cần Docker.
 */
class FloodServiceTest {

    private final FloodPointRepository repository = mock(FloodPointRepository.class);
    private final FloodService service = new FloodService(repository);

    @Test
    void emptyList_returns_1() {
        assertThat(service.computeScore(List.of())).isEqualTo(1);
    }

    @ParameterizedTest(name = "severity={0}, conf={1} → score={2}")
    @CsvSource({
            "1, 1.0, 1",   // weightedSum=1 → ceil(0.5)=1
            "2, 1.0, 1",   // weightedSum=2 → ceil(1.0)=1
            "3, 1.0, 2",   // weightedSum=3 → ceil(1.5)=2
            "4, 1.0, 2",   // weightedSum=4 → ceil(2.0)=2
            "5, 1.0, 3",   // weightedSum=5 → ceil(2.5)=3
            "5, 0.5, 2",   // weightedSum=2.5 → ceil(1.25)=2
    })
    void singlePoint_computeScore(int severity, double confidence, int expected) {
        var row = makeRow(severity, confidence);
        assertThat(service.computeScore(List.of(row))).isEqualTo(expected);
    }

    @Test
    void multipleHighSeverity_capped_at_5() {
        // 5 điểm severity=5, conf=1.0 → weightedSum=25 → ceil(12.5)=13 → capped to 5
        var rows = List.of(
                makeRow(5, 1.0), makeRow(5, 1.0), makeRow(5, 1.0),
                makeRow(5, 1.0), makeRow(5, 1.0)
        );
        assertThat(service.computeScore(rows)).isEqualTo(5);
    }

    @Test
    void nullConfidence_treatedAs_0_5() {
        var row = makeRow(4, null);
        // severity=4 × conf=0.5 = 2.0 → ceil(1.0) = 1
        assertThat(service.computeScore(List.of(row))).isEqualTo(1);
    }

    @Test
    void calcRisk_calls_repository_with_correct_params() {
        when(repository.findActiveNearby(21.0, 105.8, 500.0))
                .thenReturn(List.of(makeRow(3, 0.6)));

        var result = service.calcRisk(21.0, 105.8);

        assertThat(result.nearbyPointsCount()).isEqualTo(1);
        assertThat(result.riskScore().value()).isGreaterThanOrEqualTo(1);
        assertThat(result.riskScore().value()).isLessThanOrEqualTo(5);
        verify(repository).findActiveNearby(21.0, 105.8, 500.0);
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private FloodScoreRow makeRow(int severity, Double confidence) {
        return new FloodScoreRow() {
            @Override public Integer getSeverity() { return severity; }
            @Override public Double getConfidence() { return confidence; }
        };
    }
}

