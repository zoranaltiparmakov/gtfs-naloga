package trip.models;

import java.util.List;

public record StopSchedule(
        String stopName,
        List<Arrival> arrivals
) {}
