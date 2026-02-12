package trip;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import trip.models.*;
import utils.GtfsDataLoader;
import utils.IOUtil;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.*;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;
import static trip.GtfsDataMocker.*;

@ExtendWith(MockitoExtension.class)
class BusTripsServiceTest {
    private static final ZoneId zoneId = ZoneId.of("Europe/Ljubljana");

    @Mock
    private GtfsDataLoader gtfsDataLoader;
    @Mock
    private Clock clock;

    private BusTripsService busTripsService;

    @BeforeEach
    public void reset() {
        busTripsService = new BusTripsLocalService(clock, gtfsDataLoader);

        doReturn(stopsData).when(gtfsDataLoader).loadStops();
        doReturn(calendarsData).when(gtfsDataLoader).loadCalendars();
        doReturn(routesData).when(gtfsDataLoader).loadRoutes();
        doReturn(tripsData).when(gtfsDataLoader).loadTrips();
    }

    @Test
    @DisplayName("Test that limit will be applied and it will return at maximum <b>numBusesPerLine</b> items for each route.")
    public void testNumOfBusesPerLineLimit() {
        setAgencyAndClock(zoneId, LocalDateTime.of(2026, 2, 12, 5, 0));

        StopSchedule stopSchedule = runSchedule(801011, 2);

        assertFalse(stopSchedule.arrivals().isEmpty());
        assertAll("All routes should have at max 2 arrivals",
                stopSchedule.arrivals()
                        .entrySet()
                        .stream()
                        .map(entry -> () -> assertTrue(
                                entry.getValue().size() <= 2,
                                String.format("Route ID %s should have at max 2 arrivals", entry.getKey().routeId()))));
        List<ZonedDateTime> arrivalTimes = stopSchedule.arrivals().values().stream()
                .flatMap(List::stream)
                .map(Arrival::dateTime)
                .sorted()
                .toList();
        assertEquals(2, arrivalTimes.size());

    }

    @Test
    @DisplayName("Test that user/system using different timezone will get the next arrivals right by converting them from GTFS source timezone to the user/system one.")
    public void testDifferentTimeZoneIsHandled() {
        // Agency in Athens timezone (CET+2)
        // Client in Ljubljana timezone (CET+1)
        setAgencyAndClock(ZoneId.of("Europe/Athens"), LocalDateTime.of(2026, 2, 12, 5, 0));

        StopSchedule stopSchedule = runSchedule(801011, 2);

        assertFalse(stopSchedule.arrivals().isEmpty());
        List<ZonedDateTime> arrivalTimes = stopSchedule.arrivals().values().stream()
                .flatMap(List::stream)
                .map(Arrival::dateTime)
                .sorted()
                .toList();
        assertEquals(4, arrivalTimes.size());
    }

    @Test
    public void testStationExists() {
        StopSchedule stopSchedule = busTripsService.stopSchedule(801011, 2);

        assertNotNull(stopSchedule.stopName());
    }

    @Test
    public void testStationNotExistsExceptionThrown() {
        assertThrows(RuntimeException.class, () -> busTripsService.stopSchedule(0, 2));
    }

    @Test
    @DisplayName("Test that all scheduled arrivals are within the next 2h window.")
    public void testSchedulesWithinNext2h() {
        setAgencyAndClock(zoneId, LocalDateTime.of(2026, 2, 12, 5, 0));

        ZonedDateTime windowStart = clock.instant().atZone(zoneId);
        ZonedDateTime windowEnd = windowStart.plusHours(2);

        StopSchedule stopSchedule = runSchedule(801011, 4);

        List<ZonedDateTime> arrivalTimes = stopSchedule.arrivals().values().stream()
                .flatMap(List::stream)
                .map(Arrival::dateTime)
                .toList();

        assertFalse(arrivalTimes.isEmpty());

        assertAll("Every arrival must be within [now, now+2h]",
                arrivalTimes.stream()
                        .map(arrival -> () -> {
                            assertFalse(arrival.toInstant().isBefore(clock.instant()),
                                    "Arrival %s should not be before now (%s)"
                                            .formatted(arrival, windowStart));
                            assertFalse(arrival.toInstant().isAfter(windowEnd.toInstant()),
                                    "Arrival %s should not be after now+2h (%s)"
                                            .formatted(arrival, windowEnd));
                        }));
    }

    @Test
    @DisplayName("Test that arrivals per route are sorted in ascending order")
    public void testArrivalsAreSortedAsc() {
        setAgencyAndClock(zoneId, LocalDateTime.of(2026, 2, 12, 5, 0));

        StopSchedule stopSchedule = runSchedule(801011, 4);

        assertAll("Arrivals must be sorted ascending per route",
                stopSchedule.arrivals().entrySet().stream()
                        .map(entry -> () -> {
                            List<Arrival> arrivals = entry.getValue();
                            for (int i = 1; i < arrivals.size(); i++) {
                                assertTrue(
                                        !arrivals.get(i).dateTime().isBefore(arrivals.get(i - 1).dateTime()),
                                        "Route %s: arrival[%d] (%s) should not be before arrival[%d] (%s)"
                                                .formatted(entry.getKey().routeId(),
                                                        i, arrivals.get(i).dateTime(),
                                                        i - 1, arrivals.get(i - 1).dateTime()));
                            }
                        }));
    }

    private StopSchedule runSchedule(int stationId, int numBusesPerLine) {
        try (MockedStatic<Files> filesMock = mockStatic(Files.class);
             MockedStatic<IOUtil> utilMock = mockStatic(IOUtil.class)) {

            utilMock.when(() -> IOUtil.getResourcePathFromUrl(any(String.class)))
                    .thenReturn(Path.of("dummy"));
            filesMock.when(() -> Files.lines(any(Path.class), eq(StandardCharsets.UTF_8))).thenReturn(stopTimes.stream());

            return busTripsService.stopSchedule(stationId, numBusesPerLine);
        }
    }

    private void setAgencyAndClock(ZoneId agencyZoneId, LocalDateTime localDateTime) {
        doReturn(Map.of("LPP", new Agency(agencyZoneId)))
                .when(gtfsDataLoader).loadAgencies();

        doReturn(zoneId).when(clock).getZone();
        var instant = localDateTime
                .atZone(zoneId)
                .toInstant();
        doReturn(instant).when(clock).instant();
    }
}