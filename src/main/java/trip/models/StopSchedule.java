package trip.models;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public record StopSchedule(
        String stopName,
        Map<Route, List<Arrival>> arrivals
) {
    public StopSchedule {
        stopName = Objects.requireNonNull(stopName, "stopName cannot be null");
        if (arrivals == null) arrivals = Map.of();
    }
}
