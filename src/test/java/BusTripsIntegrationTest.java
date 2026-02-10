import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import trip.StopScheduleView;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

class BusTripsIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testExistingTrips() {
        List<String> args = List.of("busTrips", "1", "2", "relative");

        StopScheduleView stopScheduleView = executeAppAndHijackConsole(args);

        assertFalse(stopScheduleView.arrivals().isEmpty());
    }

    private StopScheduleView executeAppAndHijackConsole(List<String> argsList) {
        // Hijack System.out for duration of the test
        PrintStream originalOut = System.out;

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        // Call the app
        String[] args = argsList.toArray(new String[0]);
        GtfsNaloga.main(args);

        // Reset
        System.setOut(originalOut);

        String output = outContent.toString();
        return objectMapper.readValue(output, StopScheduleView.class);
    }
}