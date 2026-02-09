# Requirements
#### 1. Display next buses arriving at a given stop
#### 2. Only consider buses arriving within the next 2h from the time of the query
#### 3. Efficient memory usage
#### 4. Input: busTrips <station_id> <num_buses_per_line> <relative|absolute>
##### Parameters:
- station_id (int): ID of the stop (e.g., 12345)
- num_buses_per_line (int): Maximum num of upcoming arrivals to show per route
- relative|absolute (Enum): Format of the arrival times:
  - absolute: arrival time (e.g., 12:10)
  - relative: time until arrival (e.g., 10 min)
#### 5. Output:
- Name of the stop
- For each route, up to N upcoming arrivals within the next 2h
- Display times in the format selected by the user (absolute or relative)

# Assumptions
#### 1. Data
    There can be 10.000+ routes with millions of stop time combined (millions of rows in stop_times file).
    Feed size (all GTFS files) can be 50-500+ MB in size
#### 2. App
    GTFS files are present locally on disk.
    Application is a console app, using CLI to call busTrips method to get list of next bus stops.
    For time of the schedules, respect user/system timezone.