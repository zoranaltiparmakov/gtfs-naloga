package trip;

import trip.models.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public final class GtfsDataMocker {
    static final Boolean[] serviceAllDays = List.of(true, true, true, true, true, true, true).toArray(new Boolean[0]);
    static final Boolean[] serviceWeekend = List.of(false, false, false, false, false, true, true).toArray(new Boolean[0]);
    static final Boolean[] serviceWorkdays = List.of(true, true, true, true, true, false, false).toArray(new Boolean[0]);
    static final Boolean[] serviceWorkdaysAndSaturday = List.of(true, true, true, true, true, true, false).toArray(new Boolean[0]);
    static final Boolean[] serviceSaturday = List.of(false, false, false, false, false, true, false).toArray(new Boolean[0]);
    static final LocalDate dateFrom = LocalDate.of(2026, 1, 1);
    static final LocalDate dateTo = LocalDate.of(2026, 12, 1);
    static final Map<String, Calendar> calendarsData = Map.ofEntries(
            Map.entry("1", new Calendar("1", serviceAllDays, dateFrom, dateTo)),
            Map.entry("2", new Calendar("2", serviceWeekend, dateFrom, dateTo)),
            Map.entry("3", new Calendar("3", serviceWorkdays, dateFrom, dateTo)),
            Map.entry("4", new Calendar("4", serviceWorkdaysAndSaturday, dateFrom, dateTo)),
            Map.entry("5", new Calendar("5", serviceSaturday, dateFrom, dateTo))
    );

    static final Map<Integer, Stop> stopsData = Map.ofEntries(
            Map.entry(103201, new Stop(103201, "Litostroj", null)),
            Map.entry(801011, new Stop(801011, "Tivoli", null)),
            Map.entry(600012, new Stop(600012, "Bavarski Dvor", null)),
            Map.entry(504031, new Stop(504031, "Rudnik", null)),
            Map.entry(202051, new Stop(202051, "Zelena Jama", null)),
            Map.entry(300011, new Stop(300011, "Kolordvor", null)),
            Map.entry(203171, new Stop(203171, "Nove Jarše", null)),
            Map.entry(804241, new Stop(804241, "Stanežiče P+R", null)),
            Map.entry(704011, new Stop(704011, "D. Most P+R", null)),
            Map.entry(602061, new Stop(602061, "Križanke", null)),
            Map.entry(803021, new Stop(803021, "Aleja", null)),
            Map.entry(104161, new Stop(104161, "Črnuče", null)),
            Map.entry(103031, new Stop(103031, "AMZS", null)),
            Map.entry(504061, new Stop(504061, "NS Rudnik", null)),
            Map.entry(515013, new Stop(515013, "Škofljica", null)),
            Map.entry(304101, new Stop(304101, "Vevče", null)),
            Map.entry(104221, new Stop(104221, "Gameljne", null))
    );

    static final Map<String, Route> routesData = Map.ofEntries(
            Map.entry("1", new Route("1", "Stanežiče P+R - Dolgi most P+R (ob delavnikih in sobotah)", "LPP")),
            Map.entry("N1", new Route("N1", "Bavarski dvor - Stanežiče P+R - Brod (nočna - vse dni v letu)", "LPP")),
            Map.entry("2", new Route("2", "Zelena jama - Nove Jarše", "LPP")),
            Map.entry("3", new Route("3", "Litostroj - Rudnik", "LPP")),
            Map.entry("N3", new Route("N3", "Bavarski dvor - Rudnik (nočna - vse dni v letu)", "LPP")),
            Map.entry("3B", new Route("3B", "Litostroj - Škofljica", "LPP")),
            Map.entry("6", new Route("6", "Črnuče - Dolgi most (P+R)", "LPP")),
            Map.entry("27", new Route("27", "Vevče - BTC - NS Rudnik (ob delavnikih in sobotah)", "LPP"))
    );

    static final Map<String, Trip> tripsData = Map.ofEntries(
            Map.entry("1_TRIP_100_06:00", new Trip("1_TRIP_100_06:00", "1", "1")),
            Map.entry("1_TRIP_101_06:20", new Trip("1_TRIP_101_06:20", "1", "1")),
            Map.entry("1_TRIP_102_06:50", new Trip("1_TRIP_102_06:50", "1", "1")),
            Map.entry("1_TRIP_103_06:55", new Trip("1_TRIP_103_06:55", "1", "1")),
            Map.entry("1_TRIP_104_09:00", new Trip("1_TRIP_104_09:00", "1", "1")),
            Map.entry("N1_TRIP_200_07:00", new Trip("N1_TRIP_200_07:00", "N1", "1")),
            Map.entry("N1_TRIP_201_07:30", new Trip("N1_TRIP_201_07:30", "N1", "1")),
            Map.entry("2_TRIP_300_06:20", new Trip("2_TRIP_300_06:20", "2", "1")),
            Map.entry("2_TRIP_301_06:40", new Trip("2_TRIP_301_06:40", "2", "1")),
            Map.entry("3_TRIP_400_06:30", new Trip("3_TRIP_400_06:30", "3", "1")),
            Map.entry("3_TRIP_401_07:30", new Trip("3_TRIP_401_07:30", "3", "1")),
            Map.entry("N3_TRIP_500_06:40", new Trip("N3_TRIP_500_06:40", "N3", "1")),
            Map.entry("N3_TRIP_501_06:50", new Trip("N3_TRIP_501_06:50", "N3", "1")),
            Map.entry("N3_TRIP_502_07:20", new Trip("N3_TRIP_502_07:20", "N3", "1")),
            Map.entry("3B_TRIP_600_06:50", new Trip("3B_TRIP_600_06:50", "3B", "1")),
            Map.entry("3B_TRIP_601_06:55", new Trip("3B_TRIP_601_06:55", "3B", "1")),
            Map.entry("6_TRIP_700_07:00", new Trip("6_TRIP_700_07:00", "6", "1")),
            Map.entry("27_TRIP_800_07:10", new Trip("27_TRIP_800_07:10", "27", "1"))
    );

    static final String header = "trip_id,arrival_time,departure_time,stop_id,stop_sequence,stop_headsign,pickup_type,drop_off_type,shape_dist_traveled";
    static String stopTimeLine(String tripId, String arrivalTime, int stopId) {
        return String.join(",", tripId, arrivalTime, arrivalTime, String.valueOf(stopId), "0", "", "", "", "");
    }
    static final List<String> stopTimes = List.of(
            header,
            stopTimeLine("1_TRIP_100_06:00", "06:00:00", 103201),
            stopTimeLine("1_TRIP_100_06:00", "06:10:00", 801011),
            stopTimeLine("1_TRIP_100_06:00", "06:20:00", 600012),
            stopTimeLine("1_TRIP_100_06:00", "06:30:00", 504031),
            stopTimeLine("1_TRIP_101_06:20", "06:20:00", 103201),
            stopTimeLine("1_TRIP_101_06:20", "06:30:00", 801011),
            stopTimeLine("1_TRIP_101_06:20", "06:50:00", 504031),
            stopTimeLine("1_TRIP_102_06:50", "06:50:00", 103201),
            stopTimeLine("1_TRIP_102_06:50", "07:00:00", 801011),
            stopTimeLine("1_TRIP_102_06:50", "07:20:00", 504031),
            stopTimeLine("1_TRIP_103_06:55", "06:55:00", 103201),
            stopTimeLine("1_TRIP_103_06:55", "07:30:00", 504031),
            stopTimeLine("1_TRIP_104_09:00", "09:00:00", 103201),
            stopTimeLine("1_TRIP_104_09:00", "09:10:00", 801011),
            stopTimeLine("1_TRIP_104_09:00", "09:20:00", 600012),
            stopTimeLine("1_TRIP_104_09:00", "09:30:00", 504031),
            stopTimeLine("N1_TRIP_200_07:00", "07:00:00", 600012),
            stopTimeLine("N1_TRIP_200_07:00", "07:20:00", 801011),
            stopTimeLine("N1_TRIP_200_07:00", "07:30:00", 803021),
            stopTimeLine("N1_TRIP_200_07:00", "07:40:00", 104221),
            stopTimeLine("N1_TRIP_201_07:30", "07:30:00", 600012),
            stopTimeLine("N1_TRIP_201_07:30", "07:55:00", 801011),
            stopTimeLine("2_TRIP_300_06:20", "06:20:00", 203171),
            stopTimeLine("2_TRIP_300_06:20", "06:30:00", 600012),
            stopTimeLine("2_TRIP_300_06:20", "06:40:00", 202051),
            stopTimeLine("2_TRIP_301_06:40", "06:40:00", 203171),
            stopTimeLine("2_TRIP_301_06:40", "06:50:00", 202051)
    );
}
