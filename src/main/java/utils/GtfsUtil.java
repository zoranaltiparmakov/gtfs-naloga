package utils;

import trip.models.Calendar;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;

public final class GtfsUtil {

    public static String generateGtfsFilePath(String gtfsResource) {
        return "gtfs/" + gtfsResource + ".txt";
    }

    /**
     * Parses GTFS HH:mm:ss (or H:mm:ss) into ZonedDateTime and handling > 24h correctly.
     * According to the GTFS reference (https://gtfs.org/documentation/schedule/reference),
     * time is in HH:MM:SS format, but H:MM:SS is also accepted.
     * The time is measured from "noon minus 12h" of the service day (effectively midnight except for days on which daylight savings time changes occur)
     * For times occurring after midnight on the service day, enter the time as a value greater than 24:00:00 in HH:MM:SS.
     * Example: 14:30:00 for 2:30PM or 25:35:00 for 1:35AM on the next day.
     */
    public static ZonedDateTime parseGtfsTime(String timeStr, ZonedDateTime serviceDate) {
        String[] parts = timeStr.split(":");
        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);
        int second = Integer.parseInt(parts[2]);

        // If hour >= 24, increment the date
        LocalDate date = serviceDate.toLocalDate().plusDays(hour / 24);
        LocalTime time = LocalTime.of(hour % 24, minute, second);

        return ZonedDateTime.of(date, time, serviceDate.getZone());
    }

    public static boolean runsToday(Calendar cal, LocalDate date) {
        if (date.isBefore(cal.start()) || date.isAfter(cal.end())) {
            return false;
        }
        int dow = date.getDayOfWeek().getValue(); // 1=Mon

        return cal.days()[dow - 1];
    }
}
