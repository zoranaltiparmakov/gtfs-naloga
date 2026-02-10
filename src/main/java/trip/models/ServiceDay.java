package trip.models;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

public record ServiceDay(
        String serviceId,
        Set<DayOfWeek> daysOfWeek,
        LocalDate startDate,
        LocalDate endDate
) {}