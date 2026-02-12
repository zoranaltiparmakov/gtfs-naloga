package trip.models;

import java.time.LocalDate;

public record Calendar(
        String serviceId,
        Boolean[] days, // [Mon..Sun]
        LocalDate start,
        LocalDate end
) {}
