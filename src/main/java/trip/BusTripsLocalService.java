package trip;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class BusTripsLocalService implements BusTripsService {

    private final Clock clock;

    // Inject clock in the constructor to make stop schedule testable and predictable
    public BusTripsLocalService(Clock clock) {
        this.clock = clock;
    }

    @Override
    public StopSchedule stopSchedule(int stationId, int numBusesPerLine) {
        Instant now = Instant.now(clock);
        // Use parameters for X minutes later?
        Instant twoHoursLater = now.plus(2, ChronoUnit.HOURS);

        // TODO: Parse GTFS files and find next stops.

        return new StopSchedule("", List.of());
    }
}
