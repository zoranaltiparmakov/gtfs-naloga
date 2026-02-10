package trip.views;

import java.util.List;

public record StopScheduleView(
        String stopName,
        List<ArrivalView> arrivals
) {
    public record ArrivalView(
            String route,
            String time
    ) {}
}
