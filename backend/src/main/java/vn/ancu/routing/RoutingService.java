package vn.ancu.routing;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;
import java.time.Duration;

/** Cache exact route inputs briefly in memory; never persist precise location keys. */
@Service
public class RoutingService {
    private final ValhallaClient valhallaClient;
    private final Cache<RouteKey, RouteResultDto> cache = Caffeine.newBuilder()
            .maximumSize(2000).expireAfterWrite(Duration.ofMinutes(15)).build();

    public RoutingService(ValhallaClient valhallaClient) {
        this.valhallaClient = valhallaClient;
    }

    public RouteResultDto route(double oLat, double oLng, double dLat, double dLng, String mode) {
        var key = new RouteKey(oLat, oLng, dLat, dLng, mode);
        // Null results are not cached, so retry can recover as soon as Valhalla is back.
        return cache.get(key, ignored -> valhallaClient.route(oLat, oLng, dLat, dLng, mode).block());
    }

    private record RouteKey(double originLat, double originLng, double destLat, double destLng, String mode) {}
}
