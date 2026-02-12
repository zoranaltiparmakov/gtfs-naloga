package trip.views;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public record StopScheduleView(
        String stopName,
        Map<String, List<ArrivalView>> arrivals
) {
    public StopScheduleView {
        stopName = Objects.requireNonNull(stopName, "stopName cannot be null");
        if (arrivals == null) arrivals = Map.of();
    }
}
