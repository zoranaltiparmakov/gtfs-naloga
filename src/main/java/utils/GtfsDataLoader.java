package utils;

import trip.models.*;

import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public final class GtfsDataLoader {

    public Map<String, Trip> loadTrips() {
        Map<String, Trip> trips = new HashMap<>();

        String urlPath = GtfsUtil.generateGtfsFilePath("trips");
        try (Stream<String> lines = Files.lines(IOUtil.getResourcePathFromUrl(urlPath))) {
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

    public Map<Integer, Stop> loadStops() {
        Map<Integer, Stop> stops = new HashMap<>();

        String urlPath = GtfsUtil.generateGtfsFilePath("stops");
        try (Stream<String> lines = Files.lines(IOUtil.getResourcePathFromUrl(urlPath))) {
            lines.skip(1)
                    .map(l -> l.split(",", -1))
                    .forEach(cols -> {
                        Integer stopId = Integer.parseInt(cols[0]);
                        String name = cols[2];
                        String timezone = cols[11].isEmpty() ? null : cols[11];
                        ZoneId zone = timezone != null ? ZoneId.of(timezone) : null;
                        stops.put(stopId, new Stop(stopId, name, zone));
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return stops;
    }

    public Map<String, Route> loadRoutes() {
        Map<String, Route> routes = new HashMap<>();

        String urlPath = GtfsUtil.generateGtfsFilePath("routes");
        try (Stream<String> lines = Files.lines(IOUtil.getResourcePathFromUrl(urlPath))) {
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

    public Map<String, Calendar> loadCalendars() {
        Map<String, Calendar> calendars = new HashMap<>();

        String urlPath = GtfsUtil.generateGtfsFilePath("calendar");
        try (Stream<String> lines = Files.lines(IOUtil.getResourcePathFromUrl(urlPath))) {
            lines
                    .skip(1)
                    .map(line -> line.split(",", -1))
                    .forEach(cols -> {
                        String serviceId = cols[0];
                        Boolean[] days = new Boolean[7];
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

    public Map<String, Agency> loadAgencies() {
        Map<String, Agency> agencies = new HashMap<>();

        String urlPath = GtfsUtil.generateGtfsFilePath("agency");
        try (Stream<String> lines = Files.lines(IOUtil.getResourcePathFromUrl(urlPath))) {
            lines
                    .skip(1)
                    .map(line -> line.split(",", -1))
                    .forEach(cols -> {
                        String agencyId = cols[0];
                        String timeZone = cols[3];
                        agencies.put(agencyId, new Agency(ZoneId.of(timeZone)));
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return agencies;
    }
}
