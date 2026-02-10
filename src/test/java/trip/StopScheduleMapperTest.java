package trip;

import org.junit.jupiter.api.Test;
import trip.models.Arrival;
import trip.models.StopSchedule;
import trip.views.StopScheduleMapper;
import trip.views.StopScheduleView;

import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class StopScheduleMapperTest {

    @Test
    public void testTimeInRelativeFormat() {
        String relativeTimeFormat = "\\d+ min";

        StopSchedule stopSchedule = new StopSchedule("STOP 1", List.of(
                new Arrival("Route 1", ZonedDateTime.now()),
                new Arrival("Route 1", ZonedDateTime.now())
        ));

        StopScheduleView stopScheduleView = StopScheduleMapper.toView(stopSchedule, TimeFormatEnum.RELATIVE);

        for (var arrival : stopScheduleView.arrivals()) {
            assertTrue(arrival.time().matches(relativeTimeFormat), "Should be in '%l min' format");
        }
    }

    @Test
    public void testTimeInAbsoluteFormat() {
        String relativeTimeFormat = "^([01]\\d|2[0-3]):([0-5]\\d)$";

        StopSchedule stopSchedule = new StopSchedule("STOP 1", List.of(
                new Arrival("Route 1", ZonedDateTime.now()),
                new Arrival("Route 1", ZonedDateTime.now())
        ));

        StopScheduleView stopScheduleView = StopScheduleMapper.toView(stopSchedule, TimeFormatEnum.ABSOLUTE);

        for (var arrival : stopScheduleView.arrivals()) {
            assertTrue(arrival.time().matches(relativeTimeFormat));
        }
    }
}