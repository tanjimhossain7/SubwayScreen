package ca.ucalgary.edu.ensf380;

import java.io.*;  // Importing classes for input and output operations
import java.nio.file.*;  // Importing classes for handling file paths and file system operations
import java.util.*;  // Importing utility classes for data structures and collections

/**
 * TrainStationManager manages the data related to train stations and trains, including real-time updates.
 * This class is responsible for loading train station data, monitoring updates to train data, and providing access to this data.
 */
public class TrainStationManager {
    // A map that stores train data, keyed by train number
    private final Map<String, TrainData> trainDataMap = new HashMap<>();
    // A map that stores station data, keyed by station code
    private final Map<String, Station> stations = new HashMap<>();
    // Directory to monitor for train data output files
    private final String outputDirectory = "D:\\SubwayScreen\\out";
    // Path to the file containing subway station data
    private final String subwayFilePath = "D:\\Map.csv";

    /**
     * Constructs a TrainStationManager and starts monitoring output files for updates.
     * This constructor initializes the manager by loading station data and setting up file monitoring.
     */
    public TrainStationManager() {
        loadStationData();  // Load station data from the subway file
        monitorOutputFiles();  // Start monitoring the output directory for new files
    }

    /**
     * Loads station data from the specified CSV file.
     * This method reads a CSV file containing station information and populates the stations map.
     */
    private void loadStationData() {
        try (BufferedReader br = new BufferedReader(new FileReader(subwayFilePath))) {  // Open the CSV file for reading
            String line;
            boolean firstLine = true;  // Flag to skip the header line
            while ((line = br.readLine()) != null) {  // Read each line from the CSV file
                if (firstLine) {
                    firstLine = false;  // Skip the header line
                    continue;
                }
                String[] parts = line.split(",");  // Split the line by commas to extract station data
                if (parts.length >= 7) {  // Check if the line has the expected number of columns
                    String lineCode = parts[1].trim();  // Extract and trim the line code
                    String stationCode = parts[3].trim();  // Extract and trim the station code
                    String stationName = parts[4].trim();  // Extract and trim the station name
                    double x = Double.parseDouble(parts[5].trim());  // Parse and trim the x-coordinate
                    double y = Double.parseDouble(parts[6].trim());  // Parse and trim the y-coordinate
                    String commonStations = parts.length == 8 ? parts[7].trim() : "";  // Extract and trim common stations, if available

                    // Create a new Station object and add it to the stations map
                    Station station = new Station(stationCode, stationName, x, y, lineCode, commonStations);
                    stations.put(stationCode, station);
                }
            }
        } catch (IOException e) {  // Handle exceptions related to file I/O
            e.printStackTrace();  // Print the stack trace for debugging
        }
    }

    /**
     * Monitors the output directory for new files and processes them for train data updates.
     * This method sets up a task to periodically check the output directory for new train data files.
     */
    private void monitorOutputFiles() {
        Runnable task = () -> {  // Define a task to be executed periodically
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(outputDirectory), "*.csv")) {  // Open the output directory and filter for CSV files
                Path firstFile = null;  // Initialize a variable to store the first file found
                for (Path entry : stream) {  // Iterate over each file in the directory
                    if (firstFile == null || Files.getLastModifiedTime(entry).toMillis() < Files.getLastModifiedTime(firstFile).toMillis()) {
                        firstFile = entry;  // Update firstFile to the oldest file in the directory
                    }
                }
                if (firstFile != null) {  // If a file was found
                    updateTrainData(firstFile);  // Update train data using the file
                    deleteOutputFiles();  // Delete the processed output files
                }
            } catch (IOException e) {  // Handle exceptions related to file I/O
                e.printStackTrace();  // Print the stack trace for debugging
            }
        };
        // Set up a timer to run the task every 15 seconds
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                task.run();  // Execute the task
            }
        }, 0, 15000);  // Start immediately, and repeat every 15 seconds
    }

    /**
     * Updates the train data based on the information in the specified file.
     * @param filePath the path to the file containing train data updates.
     * This method reads a file containing train data and updates the trainDataMap accordingly.
     */
    private void updateTrainData(Path filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath.toFile()))) {  // Open the file for reading
            String line;
            trainDataMap.clear();  // Clear the current train data map
            while ((line = br.readLine()) != null) {  // Read each line from the file
                if (line.startsWith("LineName")) continue;  // Skip the header line
                String[] parts = line.split(",");  // Split the line by commas to extract train data
                if (parts.length >= 5) {  // Check if the line has the expected number of columns
                    String lineColor = parts[0].trim();  // Extract and trim the line color
                    String trainNumber = parts[1].trim();  // Extract and trim the train number
                    String stationCode = parts[2].trim();  // Extract and trim the station code
                    String direction = parts[3].trim();  // Extract and trim the train direction
                    String destination = parts[4].trim();  // Extract and trim the destination

                    // Get the station object from the stations map using the station code
                    Station station = stations.get(stationCode);
                    if (station != null) {
                        // Create a new TrainData object and add it to the trainDataMap
                        TrainData trainData = new TrainData(trainNumber, station, direction, destination, lineColor);
                        trainData.updateNextStations(stations, direction);  // Update the list of next stations for the train
                        trainDataMap.put(trainNumber, trainData);  // Add the train data to the map
                    }
                }
            }
        } catch (IOException e) {  // Handle exceptions related to file I/O
            e.printStackTrace();  // Print the stack trace for debugging
        }
    }

    /**
     * Deletes all output files in the monitored directory.
     * This method is called after processing train data files to clean up the output directory.
     */
    private void deleteOutputFiles() {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(outputDirectory), "*.csv")) {  // Open the output directory and filter for CSV files
            for (Path entry : stream) {  // Iterate over each file in the directory
                Files.delete(entry);  // Delete the file
            }
        } catch (IOException e) {  // Handle exceptions related to file I/O
            e.printStackTrace();  // Print the stack trace for debugging
        }
    }

    /**
     * Gets the train data for a specific train number.
     * @param trainNumber the train number to look up.
     * @return the TrainData object associated with the train number.
     * This method retrieves train data for a specific train number from the trainDataMap.
     */
    public TrainData getTrainData(String trainNumber) {
        return trainDataMap.get(trainNumber);
    }

    /**
     * Gets all train data currently loaded.
     * @return a collection of TrainData objects.
     * This method returns a collection of all train data currently stored in the trainDataMap.
     */
    public Collection<TrainData> getAllTrainData() {
        return trainDataMap.values();
    }

    /**
     * The TrainData class represents the data associated with a train, including its current station and next stops.
     * This class encapsulates the properties and methods related to an individual train's data.
     */
    public static class TrainData {
        private final String trainNumber;  // The unique identifier for the train
        private final Station station;  // The current station where the train is located
        private final String direction;  // The direction the train is traveling
        private final String destination;  // The final destination of the train
        private final String lineColor;  // The color code of the train line
        private List<String> nextStations;  // A list of stations the train will stop at next

        /**
         * Constructs a TrainData object with the specified parameters.
         * @param trainNumber the train number.
         * @param station the current station of the train.
         * @param direction the direction the train is traveling.
         * @param destination the final destination of the train.
         * @param lineColor the color code of the train line.
         * This constructor initializes the properties of the train data object.
         */
        public TrainData(String trainNumber, Station station, String direction, String destination, String lineColor) {
            this.trainNumber = trainNumber;
            this.station = station;
            this.direction = direction;
            this.destination = destination;
            this.lineColor = lineColor;
            this.nextStations = new ArrayList<>();
        }

        /**
         * Updates the list of next stations for the train based on its current direction.
         * @param stations a map of all stations.
         * @param direction the direction the train is traveling.
         * This method calculates and updates the list of stations the train will stop at next, depending on its direction.
         */
        public void updateNextStations(Map<String, Station> stations, String direction) {
            List<Station> allStations = new ArrayList<>();  // Initialize a list to store stations on the same line
            for (Station st : stations.values()) {  // Iterate over each station
                if (st.getLineCode().equals(this.lineColor)) {  // Check if the station is on the same line as the train
                    allStations.add(st);  // Add the station to the list
                }
            }
            allStations.sort(Comparator.comparing(Station::getStationCode));  // Sort stations by station code

            int currentIndex = -1;  // Initialize the index of the current station
            for (int i = 0; i < allStations.size(); i++) {  // Iterate over the list of stations
                if (allStations.get(i).getStationCode().equals(this.station.getStationCode())) {  // Find the index of the current station
                    currentIndex = i;
                    break;
                }
            }

            nextStations.clear();  // Clear the current list of next stations
            if (currentIndex != -1) {  // If the current station was found
                if ("forward".equals(direction)) {  // If the train is moving forward
                    for (int i = 1; i <= 4; i++) {  // Calculate the next 4 stations
                        if (currentIndex + i < allStations.size()) {  // Check if the index is within bounds
                            nextStations.add(allStations.get(currentIndex + i).getStationName());  // Add the next station to the list
                        } else {
                            nextStations.add("End of Line");  // Add "End of Line" if there are no more stations
                        }
                    }
                } else {  // If the train is moving backward
                    for (int i = 1; i <= 4; i++) {  // Calculate the previous 4 stations
                        if (currentIndex - i >= 0) {  // Check if the index is within bounds
                            nextStations.add(allStations.get(currentIndex - i).getStationName());  // Add the previous station to the list
                        } else {
                            nextStations.add("Start of Line");  // Add "Start of Line" if there are no more stations
                        }
                    }
                }
            }
        }

        /**
         * Gets the train number.
         * @return the train number.
         * This method returns the unique identifier for the train.
         */
        public String getTrainNumber() {
            return trainNumber;
        }

        /**
         * Gets the current station of the train.
         * @return the current station.
         * This method returns the station where the train is currently located.
         */
        public Station getStation() {
            return station;
        }

        /**
         * Gets the direction the train is traveling.
         * @return the direction of the train.
         * This method returns the direction the train is traveling.
         */
        public String getDirection() {
            return direction;
        }

        /**
         * Gets the final destination of the train.
         * @return the destination of the train.
         * This method returns the final destination of the train.
         */
        public String getDestination() {
            return destination;
        }

        /**
         * Gets the color code of the train line.
         * @return the line color.
         * This method returns the color code of the train line.
         */
        public String getLineColor() {
            return lineColor;
        }

        /**
         * Gets the list of next stations the train will stop at.
         * @return a list of next station names.
         * This method returns the list of stations the train will stop at next.
         */
        public List<String> getNextStations() {
            return nextStations;
        }
    }

    /**
     * The Station class represents a subway station, including its location and line information.
     * This class encapsulates the properties and methods related to an individual station.
     */
    public static class Station {
        private final String stationCode;  // The unique code of the station
        private final String stationName;  // The name of the station
        private final double x;  // The x-coordinate of the station on a map
        private final double y;  // The y-coordinate of the station on a map
        private final String lineCode;  // The code of the train line the station is on
        private final String commonStations;  // A comma-separated list of stations shared by multiple lines

        /**
         * Constructs a Station object with the specified parameters.
         * @param stationCode the code of the station.
         * @param stationName the name of the station.
         * @param x the x-coordinate of the station.
         * @param y the y-coordinate of the station.
         * @param lineCode the code of the train line the station is on.
         * @param commonStations a comma-separated list of common stations.
         * This constructor initializes the properties of the station object.
         */
        public Station(String stationCode, String stationName, double x, double y, String lineCode, String commonStations) {
            this.stationCode = stationCode;
            this.stationName = stationName;
            this.x = x;
            this.y = y;
            this.lineCode = lineCode;
            this.commonStations = commonStations;
        }

        /**
         * Gets the station code.
         * @return the station code.
         * This method returns the unique code of the station.
         */
        public String getStationCode() {
            return stationCode;
        }

        /**
         * Gets the station name.
         * @return the station name.
         * This method returns the name of the station.
         */
        public String getStationName() {
            return stationName;
        }

        /**
         * Gets the x-coordinate of the station.
         * @return the x-coordinate of the station.
         * This method returns the x-coordinate of the station on a map.
         */
        public double getX() {
            return x;
        }

        /**
         * Gets the y-coordinate of the station.
         * @return the y-coordinate of the station.
         * This method returns the y-coordinate of the station on a map.
         */
        public double getY() {
            return y;
        }

        /**
         * Gets the code of the train line the station is on.
         * @return the line code.
         * This method returns the code of the train line the station is on.
         */
        public String getLineCode() {
            return lineCode;
        }

        /**
         * Gets the common stations associated with this station.
         * @return a comma-separated list of common stations.
         * This method returns a list of stations shared by multiple lines.
         */
        public String getCommonStations() {
            return commonStations;
        }
    }
}

