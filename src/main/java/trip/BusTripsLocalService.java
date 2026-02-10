package trip;

import trip.models.*;
import trip.models.Calendar;
import utils.Util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Stream;

public class BusTripsLocalService implements BusTripsService {

    private final Clock clock;

    // Pass clock in the constructor to make stop schedule testable and predictable
    public BusTripsLocalService(Clock clock) {
        this.clock = clock;
    }

    @Override
    public StopSchedule stopSchedule(int stationId, int numBusesPerLine) {
        Instant now = Instant.now(clock);
        Path path = Util.getGtfsResourcePath("stop_times");

        Map<String, PriorityQueue<Arrival>> arrivalsByRoute = new HashMap<>();
        List<StopTime> stopTimes;
        // Streaming from files.
        // Using Java Streams API because of simplicity and elegancy, BufferedReader may be a bit faster
        try(Stream<String> lines = Files.lines(path, StandardCharsets.UTF_8)) {
            // If stop_times file is opened, load other data in memory to make connection when reading schedule.
            Map<String, ZoneId> agencies = loadAgencies();
            Map<String, Calendar> calendars = loadCalendars();
            Map<String, Route> routes = loadRoutes();

            Map<Integer, Stop> stops = loadStops();
            Stop stop = stops.get(stationId);
            if (stop == null) {
                throw new IllegalArgumentException("Unknown stop: " + stationId);
            }

            Map<String, Trip> trips = loadTrips();

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

                        // If stop has its own timezone, override agency timezone (https://gtfs.org/documentation/schedule/reference/)
                        ZoneId zoneId = stop.timezone() != null ? stop.timezone() : agencies.get(route.agencyId());

                        Calendar cal = calendars.get(trip.serviceId());
                        if (cal == null || !runsToday(cal, now.atZone(zoneId).toLocalDate())) {
                            String msg = "Trip ID %s does not run on %s. Will continue with others";
                            System.out.printf((msg) + "%n", tripId, now.toString());
                            return;
                        }

                        ZonedDateTime arrival = parseGtfsTime(cols[1], now.atZone(zoneId).toLocalDate(), zoneId);

                        Instant twoHoursLater = now.plus(2, ChronoUnit.HOURS);
                        if (arrival.isBefore(now.atZone(zoneId)) || arrival.isAfter(twoHoursLater.atZone(zoneId))) {
                            String msg = "Arrival (Trip %s) is in the past or more than 2h from now.";
                            System.out.printf((msg) + "%n", tripId);
                            return;
                        }

                        System.out.printf(("Arrival (Trip %s) matches at %s for station %d.") + "%n", tripId, arrival, stationId);

                        arrivalsByRoute
                                .computeIfAbsent(route.routeId(),
                                        _ -> new PriorityQueue<>(Comparator.comparing(Arrival::dateTime)))
                                .add(new Arrival(tripId, arrival));

                        PriorityQueue<Arrival> pq =
                                arrivalsByRoute.get(route.routeId());

                        if (pq.size() > numBusesPerLine) {
                            pq.poll(); // drop farthest
                        }

                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        List<Arrival> arrivals = arrivalsByRoute.values().stream()
                .flatMap(PriorityQueue::stream)
                .sorted(Comparator.comparing(a -> a.dateTime()))
                .toList();
        return new StopSchedule("", arrivals);
    }

    private boolean runsToday(Calendar cal, LocalDate date) {
        if (date.isBefore(cal.start()) || date.isAfter(cal.end())) {
            return false;
        }
        int dow = date.getDayOfWeek().getValue(); // 1=Mon
        return cal.days()[dow - 1];
    }

    private Map<String, Trip> loadTrips() {
        Map<String, Trip> trips = new HashMap<>();
        try (Stream<String> lines = Files.lines(Util.getGtfsResourcePath("trips"))) {
            lines.skip(1)
                    .map(l -> l.split(",", -1))
                    .forEach(cols -> {
                        String tripId = cols[2]; // trip_id
                        String routeId = cols[0]; // route_id
                        String serviceId = cols[1]; // service_id
                        trips.put(tripId, new Trip(tripId, routeId, serviceId));
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return trips;
    }

    private Map<Integer, Stop> loadStops() {
        Map<Integer, Stop> stops = new HashMap<>();
        try (Stream<String> lines = Files.lines(Util.getGtfsResourcePath("stops"))) {
            lines.skip(1)
                    .map(l -> l.split(",", -1))
                    .forEach(cols -> {
                        int stopId = Integer.parseInt(cols[0]);
                        String name = cols[2];
                        String tzStr = cols[11].isEmpty() ? null : cols[11];
                        ZoneId zone = tzStr != null ? ZoneId.of(tzStr) : null;
                        stops.put(stopId, new Stop(stopId, name, zone));
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return stops;
    }

    private Map<String, Route> loadRoutes() {
        Map<String, Route> routes = new HashMap<>();
        try (Stream<String> lines = Files.lines(Util.getGtfsResourcePath("routes"))) {
            lines.skip(1)
                    .map(l -> l.split(",", -1))
                    .forEach(cols -> {
                        String routeId = cols[0];
                        String agencyId = cols[1];
                        String shortName = cols[2]; // route_short_name
                        routes.put(routeId, new Route(routeId, shortName, agencyId));
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return routes;
    }

    private Map<String, Calendar> loadCalendars() {
        Map<String, Calendar> calendars = new HashMap<>();
        try (Stream<String> lines = Files.lines(Util.getGtfsResourcePath("calendar"))) {
            lines
                    .skip(1)
                    .map(line -> line.split(",", -1))
                    .forEach(cols -> {
                        String serviceId = cols[0];
                        boolean[] days = new boolean[7];
                        for (int i = 0; i < 7; i++) {
                            days[i] = "1".equals(cols[i+1]);
                        }
                        LocalDate start = LocalDate.parse(cols[8], DateTimeFormatter.BASIC_ISO_DATE);
                        LocalDate end = LocalDate.parse(cols[9], DateTimeFormatter.BASIC_ISO_DATE);
                        calendars.put(serviceId, new Calendar(serviceId, days, start, end));
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return calendars;
    }

    private Map<String, ZoneId> loadAgencies() {
        Map<String, ZoneId> agencies = new HashMap<>();

        try (Stream<String> lines = Files.lines(Util.getGtfsResourcePath("agency"))) {
            lines
                    .skip(1)
                    .map(line -> line.split(",", -1))
                    .forEach(cols -> {
                        String agencyId = cols[0];
                        String timeZone = cols[3];
                        agencies.put(agencyId, ZoneId.of(timeZone));
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return agencies;
    }

    /**
     * Parses GTFS HH:mm:ss (or H:mm:ss) into ZonedDateTime and handling > 24h correctly.
     * According to the GTFS reference (https://gtfs.org/documentation/schedule/reference),
     * time is in HH:MM:SS format, but H:MM:SS is also accepted.
     * The time is measured from "noon minus 12h" of the service day (effectively midnight except for days on which daylight savings time changes occur)
     * For times occurring after midnight on the service day, enter the time as a value greater than 24:00:00 in HH:MM:SS.
     * Example: 14:30:00 for 2:30PM or 25:35:00 for 1:35AM on the next day.
     */
    private static ZonedDateTime parseGtfsTime(String timeStr, LocalDate serviceDate, ZoneId zoneId) {
        String[] parts = timeStr.split(":");
        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);
        int second = Integer.parseInt(parts[2]);

        // If hour >= 24, increment the date
        LocalDate date = serviceDate.plusDays(hour / 24);
        LocalTime time = LocalTime.of(hour % 24, minute, second);

        // In the requirements, agency dataset was not mentioned, and system time was mentioned, therefore I think using
        // system/user timezone is ok.
        return ZonedDateTime.of(date, time, zoneId);
    }

    private ServiceDay parseCalendar(String line) {
        String[] cols = line.split(",");
        String serviceId = cols[0];

        Set<DayOfWeek> days = new HashSet<>();
        if ("1".equals(cols[1])) days.add(DayOfWeek.MONDAY);
        if ("1".equals(cols[2])) days.add(DayOfWeek.TUESDAY);
        if ("1".equals(cols[3])) days.add(DayOfWeek.WEDNESDAY);
        if ("1".equals(cols[4])) days.add(DayOfWeek.THURSDAY);
        if ("1".equals(cols[5])) days.add(DayOfWeek.FRIDAY);
        if ("1".equals(cols[6])) days.add(DayOfWeek.SATURDAY);
        if ("1".equals(cols[7])) days.add(DayOfWeek.SUNDAY);

        LocalDate startDate = LocalDate.parse(cols[8], DateTimeFormatter.ofPattern("yyyyMMdd"));
        LocalDate endDate = LocalDate.parse(cols[9], DateTimeFormatter.ofPattern("yyyyMMdd"));

        return new ServiceDay(serviceId, days, startDate, endDate);
    }
}
