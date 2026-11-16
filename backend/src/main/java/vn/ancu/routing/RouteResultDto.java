package vn.ancu.routing;

/** DTO kết quả routing — gửi lên frontend */
public record RouteResultDto(
        String mode,
        int durationS,
        int distanceM,
        /** Luôn true — Valhalla không tính kẹt xe */
        boolean idealOnly
) {
    public RouteResultDto(String mode, int durationS, int distanceM) {
        this(mode, durationS, distanceM, true);
    }
}

