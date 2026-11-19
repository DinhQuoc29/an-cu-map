package vn.ancu.routing;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

class RoutingServiceTest {
    private final ValhallaClient client = mock(ValhallaClient.class);
    private final RoutingService service = new RoutingService(client);

    @Test
    void retriesAfterAnUnavailableServiceInsteadOfCachingFailure() {
        var result = new RouteResultDto("auto", 300, 2000);
        when(client.route(21, 105.8, 21.01, 105.81, "auto"))
                .thenReturn(Mono.empty(), Mono.just(result));
        assertThat(service.route(21, 105.8, 21.01, 105.81, "auto")).isNull();
        assertThat(service.route(21, 105.8, 21.01, 105.81, "auto")).isEqualTo(result);
        assertThat(service.route(21, 105.8, 21.01, 105.81, "auto")).isEqualTo(result);
        verify(client, times(2)).route(21, 105.8, 21.01, 105.81, "auto");
    }

    @Test
    void nearbyAddressesAndDifferentModesDoNotShareResults() {
        when(client.route(anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyString()))
                .thenAnswer(call -> Mono.just(new RouteResultDto(call.getArgument(4), 300, 2000)));
        service.route(21, 105.8, 21.01, 105.81, "auto");
        service.route(21.0001, 105.8, 21.01, 105.81, "auto");
        service.route(21, 105.8, 21.01, 105.81, "pedestrian");
        verify(client, times(3)).route(anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyString());
    }
}
