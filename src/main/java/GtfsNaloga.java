import tools.jackson.databind.ObjectMapper;
import trip.*;

import java.time.Clock;

public class GtfsNaloga {

    private static final BusTripsService busTripsService = new BusTripsLocalService(Clock.systemDefaultZone());

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("No arguments found. See help.");
            printHelpAndExit();
        }

        String cmd = args[0];

        switch (cmd) {
            case "busTrips" -> {
                if (args.length != 4) {
                    printHelpAndExit();
                }

                int stationId = Integer.parseInt(args[1]);
                int numBusesPerLine = Integer.parseInt(args[2]);
                TimeFormatEnum timeFormat = TimeFormatEnum.value(args[3]);

                StopSchedule stopSchedule = busTripsService.stopSchedule(stationId, numBusesPerLine);

                // Use view to format data differently (e.g., display absolute or relative time).
                StopScheduleView stopScheduleView = StopScheduleMapper.toView(stopSchedule, timeFormat);

                // Using Jackson ObjectMapper to print more friendly readable JSON in the console,
                // using default pretty printer.
                String friendlyJson = new ObjectMapper()
                        .writerWithDefaultPrettyPrinter()
                        .writeValueAsString(stopScheduleView);

                System.out.println(friendlyJson);
            }
            case "help" -> printHelpAndExit();
            default -> {
                System.err.println("Unknown command: " + cmd);
                printHelpAndExit();
            }
        }
    }

    private static void printHelpAndExit() {
        System.out.println("""
            
                Usage:
                    1. busTrips <station_id> <num_buses_per_line> <relative|absolute>
                    2. help
            
                Example: busTrips 1 4 relative
            
            """);
        System.exit(5);
    }
}
