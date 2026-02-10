package trip.models;

import java.time.ZonedDateTime;

public record StopTime(
        String tripId,
        ZonedDateTime arrivalTime,
        Integer stopId
) {
}
