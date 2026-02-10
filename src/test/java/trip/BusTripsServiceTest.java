package trip;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BusTripsServiceTest {
    private final BusTripsService busTripsService = new BusTripsLocalService(Clock.systemDefaultZone());

    @Test
    public void testLimit() {
        StopSchedule stopSchedule = busTripsService.stopSchedule(1, 2);

        assertEquals(2, stopSchedule.arrivals().size());
    }

    @Test
    public void testStationExists() {
        StopSchedule stopSchedule = busTripsService.stopSchedule(1, 2);

        assertNotNull(stopSchedule.stopName());
    }

    @Test
    public void testStationNotExists() {
        StopSchedule stopSchedule = busTripsService.stopSchedule(0, 2);

        assertNull(stopSchedule.stopName());
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
        List<StopSchedule.Arrival> arrivals = List.of(
                new StopSchedule.Arrival("", ZonedDateTime.now()),
                new StopSchedule.Arrival("", ZonedDateTime.now())
        );
        StopSchedule stopSchedule = busTripsService.stopSchedule(1, 10);

        assertIterableEquals(arrivals, stopSchedule.arrivals());
    }
}