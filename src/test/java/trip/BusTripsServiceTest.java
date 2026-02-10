package trip;

import org.junit.jupiter.api.Test;
import trip.models.Arrival;
import trip.models.StopSchedule;

import java.time.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BusTripsServiceTest {
    // Suppose right now is 1140 hours in Europe/Ljubljana timezone for testing
    private final ZoneId zoneId = ZoneId.of("Europe/Ljubljana");
    private final ZonedDateTime testZonedDateTime = ZonedDateTime.now(zoneId)
            .withHour(11)
            .withMinute(40)
            .withSecond(0)
            .withNano(0);
    private final Clock clock = Clock.fixed(testZonedDateTime.toInstant(), zoneId);

    private final BusTripsService busTripsService = new BusTripsLocalService(clock);

    @Test
    public void testLimit() {
        StopSchedule stopSchedule = busTripsService.stopSchedule(1, 2);

        assertEquals(1, stopSchedule.arrivals().size());
    }

    @Test
    public void testStationExists() {
        StopSchedule stopSchedule = busTripsService.stopSchedule(2, 2);

        assertNotNull(stopSchedule.stopName());
    }

    @Test
    public void testStationNotExists() {
        assertThrows(RuntimeException.class, () -> busTripsService.stopSchedule(0, 2));
    }

    @Test
    public void testSchedulesWithinNext2h() {
        StopSchedule stopSchedule = busTripsService.stopSchedule(1, 4);

        for (var arrival : stopSchedule.arrivals()) {
            assertTrue(arrival.dateTime().isBefore(Clock.systemDefaultZone().instant().atZone(ZoneId.of("UTC"))));
        }
    }

    @Test
    public void testArrivalsAreSortedAsc() {
        List<Arrival> arrivals = List.of(
                new Arrival("TRIP_1", ZonedDateTime.now(zoneId)
                        .withHour(12)
                        .withMinute(0)
                        .withSecond(0)
                        .withNano(0))
        );
        StopSchedule stopSchedule = busTripsService.stopSchedule(1, 10);

        assertIterableEquals(arrivals, stopSchedule.arrivals());
    }
}