package vn.ancu.routing;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RouteCacheRepository extends JpaRepository<RouteCache, Long> {

    Optional<RouteCache> findByOriginCellAndDestCellAndModeAndDepartBucket(
            String originCell,
            String destCell,
            String mode,
            short departBucket
    );
}

