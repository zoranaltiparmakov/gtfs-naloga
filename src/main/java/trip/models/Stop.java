package trip.models;

import java.time.ZoneId;

public record Stop(
        Integer id,
        String name,
        ZoneId timezone
) {}
