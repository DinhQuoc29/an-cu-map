package vn.ancu.airquality;

import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.IntStream;
import static org.assertj.core.api.Assertions.assertThat;

class OpenMeteoClientTest {
    private final OpenMeteoClient client = new OpenMeteoClient(WebClient.builder(), "http://localhost:9999", 1);
    private final LocalDate start = LocalDate.of(2024, 1, 1);
    private final LocalDate end = start.plusDays(364);

    @Test
    void annualMeanIncludesPeriodAndCoverage() {
        var times = IntStream.range(0, 8760).mapToObj(i -> start.atStartOfDay().plusHours(i).toString()).toList();
        var values = new ArrayList<>(Collections.nCopies(8760, 25.0));
        values.set(0, null);
        var result = client.toAnnualAverage(new OpenMeteoClient.OpenMeteoResponse(
                new OpenMeteoClient.Hourly(times, values)), start, end).block();
        assertThat(result).isNotNull();
        assertThat(result.pm25().value()).isEqualTo(25);
        assertThat(result.validHours()).isEqualTo(8759);
        assertThat(result.expectedHours()).isEqualTo(8760);
        assertThat(result.periodStart()).isEqualTo(start);
    }

    @Test
    void emptyOrSparseDataDoesNotBecomeZeroPollution() {
        var times = IntStream.range(0, 24).mapToObj(i -> start.atStartOfDay().plusHours(i).toString()).toList();
        var sparse = new OpenMeteoClient.OpenMeteoResponse(new OpenMeteoClient.Hourly(times, Collections.nCopies(24, 0.0)));
        assertThat(client.toAnnualAverage(sparse, start, end).block()).isNull();
        assertThat(client.toAnnualAverage(new OpenMeteoClient.OpenMeteoResponse(null), start, end).block()).isNull();
    }

    @Test
    void repeatedHoursDoNotFabricateAnnualCoverage() {
        var repeated = new OpenMeteoClient.OpenMeteoResponse(new OpenMeteoClient.Hourly(
                Collections.nCopies(8760, "2024-01-01T00:00"), Collections.nCopies(8760, 10.0)));
        assertThat(client.toAnnualAverage(repeated, start, end).block()).isNull();
    }
}
