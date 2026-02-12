package trip.views;

import trip.models.StopSchedule;
import trip.TimeFormatEnum;

import java.time.Clock;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// Normally would be on UI side to present data in different format, but for purposes of this task
// I will make a mapper that will map Stop Schedule model into Stop Schedule View.
public class StopScheduleMapper {

    public static StopScheduleView toView(
            StopSchedule schedule,
            TimeFormatEnum timeFormat
    ) {
        Map<String, List<ArrivalView>> mapped =
                schedule.arrivals().entrySet().stream()
                        .collect(Collectors.toMap(
                                entry -> entry.getKey().routeId(), // or shortName(), your call
                                entry -> entry.getValue().stream()
                                        .map(arrival ->
                                                new ArrivalView(
                                                        mapDateTimeToRepresentation(
                                                                arrival.dateTime(),
                                                                timeFormat
                                                        )
                                                )
                                        )
                                        .toList()
                        ));

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
