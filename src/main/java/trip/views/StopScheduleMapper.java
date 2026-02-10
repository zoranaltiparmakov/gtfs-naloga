package trip.views;

import trip.models.StopSchedule;
import trip.TimeFormatEnum;

import java.time.Clock;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

// Normally would be on UI side to present data in different format, but for purposes of this task
// I will make a mapper that will map Stop Schedule model into Stop Schedule View.
public class StopScheduleMapper {

    public static StopScheduleView toView(
            StopSchedule schedule,
            TimeFormatEnum timeFormat
    ) {
        List<StopScheduleView.ArrivalView> mapped = schedule.arrivals()
                .stream()
                .map(arrival -> new StopScheduleView.ArrivalView(arrival.route(), mapDateTimeToRepresentation(arrival.dateTime(), timeFormat)))
                .toList();

        return new StopScheduleView(schedule.stopName(), mapped);
    }

    private static String mapDateTimeToRepresentation(ZonedDateTime zonedDateTime, TimeFormatEnum timeFormat) {
        String timeRepresentation;
        if (TimeFormatEnum.RELATIVE.equals(timeFormat)) {
            Duration duration = Duration.between(Clock.systemDefaultZone().instant(), zonedDateTime);
            timeRepresentation = String.format("%d min", duration.toMinutes());
        } else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            timeRepresentation = zonedDateTime.format(formatter);
        }

        return timeRepresentation;
    }
}
