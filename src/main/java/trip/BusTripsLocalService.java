package trip;

import trip.models.*;
import trip.models.Calendar;
import utils.GtfsDataLoader;
import utils.GtfsUtil;
import utils.IOUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BusTripsLocalService implements BusTripsService {

    private final Clock clock;
    private final GtfsDataLoader dataLoader;

    // Pass clock in the constructor to make stop schedule testable and predictable
    public BusTripsLocalService(Clock clock, GtfsDataLoader dataLoader) {
        this.clock = clock;
        this.dataLoader = dataLoader;
    }

    @Override
    public StopSchedule stopSchedule(int stationId, int numBusesPerLine) {
        Duration intervalDuration = Duration.of(2, ChronoUnit.HOURS);
        String stopName;
        Map<Route, PriorityQueue<Arrival>> arrivalsByRoute = new HashMap<>();
        // Comparator to compare arrivals by datetime
        Comparator<Arrival> arrivalComparator = Comparator.comparing(Arrival::dateTime);
        // Streaming from stop times file.
        String urlPath = GtfsUtil.generateGtfsFilePath("stop_times");
        Path path = IOUtil.getResourcePathFromUrl(urlPath);

        // Using Java Streams API because of simplicity and elegance, BufferedReader may be a bit faster
        try(Stream<String> lines = Files.lines(path, StandardCharsets.UTF_8)) {
            // If stop times file is opened, load other data in memory to make connection when reading schedule.
            Map<String, Agency> agencies = dataLoader.loadAgencies();
            Map<String, Calendar> calendars = dataLoader.loadCalendars();
            Map<String, Route> routes = dataLoader.loadRoutes();
            Map<String, Trip> trips = dataLoader.loadTrips();

            Map<Integer, Stop> stops = dataLoader.loadStops();
            Stop stop = stops.get(stationId);
            if (stop == null) {
                throw new IllegalArgumentException("Unknown stop: " + stationId);
            }
            stopName = stop.name();

            lines
                    .skip(1L)
                    .map(line -> line.split(",", -1))
                    .filter(cols -> Integer.parseInt(cols[3]) == stationId)
                    .forEach(cols -> {
                        String tripId = cols[0];
                        Trip trip = trips.get(tripId);
                        if (trip == null) {
                            String msg = "No trip found for Trip ID: %s. Will continue with others";
                            System.out.printf((msg) + "%n", tripId);
                            return;
                        }

                        Route route = routes.get(trip.routeId());
                        if (route == null) {
                            String msg = "No route with ID: %s for Trip with ID: %s. Will continue with others";
                            System.out.printf((msg) + "%n", trip.routeId(), tripId);
                            return;
                        }

                        Agency agency = agencies.get(route.agencyId());
                        if (agency == null) {
                            String msg = "Agency with ID: %s not found in agencies data.";
                            System.out.printf((msg) + "%n", route.agencyId());
                            return;
                        }
                        // If stop has its own timezone, override agency timezone (https://gtfs.org/documentation/schedule/reference/)
                        ZoneId zoneId = stop.timezone() != null ? stop.timezone() : agencies.get(route.agencyId()).zoneId();

                        ZonedDateTime providerZonedDateTime = ZonedDateTime.now(clock).withZoneSameInstant(zoneId);

                        Calendar cal = calendars.get(trip.serviceId());
                        if (cal == null || !GtfsUtil.runsToday(cal, providerZonedDateTime.toLocalDate())) {
                            String msg = "Trip ID %s does not run on %s. Will continue with others";
                            System.out.printf((msg) + "%n", tripId, providerZonedDateTime);
                            return;
                        }

                        ZonedDateTime arrival = GtfsUtil.parseGtfsTime(cols[1], providerZonedDateTime);

                        ZonedDateTime limit = providerZonedDateTime.plus(intervalDuration);
                        if (arrival.isBefore(providerZonedDateTime) || arrival.isAfter(limit)) {
                            String msg = "Arrival (Trip %s) is in the past or more than %s minutes from now.";
                            System.out.printf((msg) + "%n", tripId, intervalDuration.toMinutes());
                            return;
                        }

                        System.out.printf(("Arrival (Trip %s) matches at %s for station %d.") + "%n", tripId, arrival, stationId);

                        // Compare arrivals by datetime in descending order, where latest arrival is first on the max-heap
                        arrivalsByRoute
                                .computeIfAbsent(route,_ -> new PriorityQueue<>(arrivalComparator.reversed()))
                                .add(new Arrival(arrival));

                        PriorityQueue<Arrival> pq = arrivalsByRoute.get(route);

                        if (pq.size() > numBusesPerLine) {
                            pq.poll(); // Remove latest (worst) arrival
                        }
                    });
        } catch (IOException e) {
            System.err.println("Error reading stop_times files...");
            throw new RuntimeException(e);
        }

        Map<Route, List<Arrival>> result = arrivalsByRoute.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .sorted(arrivalComparator)
                                .toList()
                ));

        return new StopSchedule(stopName, result);
    }
}
