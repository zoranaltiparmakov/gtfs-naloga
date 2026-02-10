package trip.models;

import java.time.ZonedDateTime;

public record Arrival(
        String route,
        ZonedDateTime dateTime
) {}
