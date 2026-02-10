package utils;

import trip.BusTripsLocalService;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class Util {

    public static Path getGtfsResourcePath(String gtfsResource) {
        URL resource = BusTripsLocalService.class.getClassLoader().getResource("gtfs/" + gtfsResource + ".txt");
        if (resource == null) {
            throw new RuntimeException("Path not found.");
        }

        Path path;
        try {
            path = Paths.get(resource.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException("Error in URI syntax: " + resource);
        }

        if (Files.notExists(path)) {
            throw new IllegalStateException("Failed to find file at path: " + path);
        }

        return path;
    }
}
