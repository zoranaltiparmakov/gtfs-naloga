package trip.models;

import java.time.ZoneId;

public record Stop(
        Integer stopId,
        String name,
        ZoneId timezone
) {}
