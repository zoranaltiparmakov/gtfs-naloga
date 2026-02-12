package utils;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class IOUtil {

    public static Path getResourcePathFromUrl(String urlPath) {
        URL resource = GtfsUtil.class.getClassLoader().getResource(urlPath);
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
