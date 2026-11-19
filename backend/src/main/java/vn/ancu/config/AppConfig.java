package vn.ancu.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import reactor.netty.http.client.HttpClient;
import io.netty.resolver.DefaultAddressResolverGroup;
import io.netty.channel.ChannelOption;

import java.time.Duration;

/**
 * Cấu hình WebClient, Cache (Caffeine).
 * CORS được giải quyết ở tầng nginx (prod); chỉ cần CORS cho dev nếu frontend chạy port khác.
 */
@Configuration
@EnableCaching
public class AppConfig {

    /**
     * WebClient dùng chung — mỗi service cần baseUrl riêng sẽ clone từ builder này.
     */
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create()
                        .resolver(DefaultAddressResolverGroup.INSTANCE)
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)))
                .codecs(config -> config.defaultCodecs().maxInMemorySize(2 * 1024 * 1024)); // 2 MB
    }

    /**
     * Cache manager dùng Caffeine.
     * Các cache name: "routeCache", "airQualityCache", "amenityCache", "geocodeCache"
     */
    @Bean
    public CacheManager cacheManager() {
        var manager = new CaffeineCacheManager();
        manager.setAsyncCacheMode(true); // Spring caches Mono values asynchronously.
        // geocodeCache: TTL 1 giờ — giảm truy vấn lặp đến geocoder
        manager.registerCustomCache("geocodeCache",
                Caffeine.newBuilder()
                        .maximumSize(500)
                        .expireAfterWrite(Duration.ofHours(1))
                        .recordStats()
                        .build());
        // Cache chung cho routing, air quality, amenity
        manager.setCaffeine(
                Caffeine.newBuilder()
                        .maximumSize(2000)
                        .expireAfterWrite(Duration.ofMinutes(15))
                        .recordStats()
        );
        return manager;
    }
}

