package trip;

import java.time.ZonedDateTime;
import java.util.List;

public record StopSchedule(
        String stopName,
        List<Arrival> arrivals
) {
    record Arrival(
            String route,
            ZonedDateTime dateTime
    ) {}
}
