package trip;

import org.junit.jupiter.api.Test;
import trip.models.Arrival;
import trip.models.Route;
import trip.models.StopSchedule;
import trip.views.StopScheduleMapper;
import trip.views.StopScheduleView;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

class StopScheduleMapperTest {

    private final StopSchedule stopSchedule = new StopSchedule("STOP 1", Map.of(
            new Route("1000", "7", "LPP001"),
            List.of(new Arrival(ZonedDateTime.now()),
                    new Arrival(ZonedDateTime.now().plusHours(1))
            )
    ));

    @Test
    public void testTimeInRelativeFormat() {
        String relativeTimeFormat = "\\d+ min";

        StopScheduleView stopScheduleView = StopScheduleMapper.toView(stopSchedule, TimeFormatEnum.RELATIVE);

        for (var arrival : stopScheduleView.arrivals().entrySet()) {
            arrival.getValue()
                    .forEach(arrivalView ->
                            assertTrue(arrivalView.time().matches(relativeTimeFormat), "Should be in 'X min' format"));
        }
    }

    @Test
    public void testTimeInAbsoluteFormat() {
        String absoluteTimeFormat = "^([01]\\d|2[0-3]):([0-5]\\d)$";

        StopScheduleView stopScheduleView = StopScheduleMapper.toView(stopSchedule, TimeFormatEnum.ABSOLUTE);

        for (var arrival : stopScheduleView.arrivals().entrySet()) {
            arrival.getValue()
                    .forEach(arrivalView ->
                            assertTrue(arrivalView.time().matches(absoluteTimeFormat), "Should be in 'HH:mm' format"));
        }
    }
}