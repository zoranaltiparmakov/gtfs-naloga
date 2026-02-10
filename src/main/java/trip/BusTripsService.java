package trip;

import trip.models.StopSchedule;

public interface BusTripsService {

    StopSchedule stopSchedule(int stationId, int numBusesPerLine);
}
