package ca.ucalgary.edu.ensf380;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * TrainStationManager manages the data related to train stations and trains, including real-time updates.
 */
public class TrainStationManager {
    private final Map<String, TrainData> trainDataMap = new HashMap<>();
    private final Map<String, Station> stations = new HashMap<>();
    private final String outputDirectory = "D:\\SubwayScreen\\out";
    private final String subwayFilePath = "D:\\Map.csv";

    /**
     * Constructs a TrainStationManager and starts monitoring output files for updates.
     */
    public TrainStationManager() {
        loadStationData();
        monitorOutputFiles();
    }

    /**
     * Loads station data from the specified CSV file.
     */
    private void loadStationData() {
        try (BufferedReader br = new BufferedReader(new FileReader(subwayFilePath))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue; // Skip header
                }
                String[] parts = line.split(",");
                if (parts.length >= 7) {
                    String lineCode = parts[1].trim();
                    String stationCode = parts[3].trim();
                    String stationName = parts[4].trim();
                    double x = Double.parseDouble(parts[5].trim());
                    double y = Double.parseDouble(parts[6].trim());
                    String commonStations = parts.length == 8 ? parts[7].trim() : "";

                    Station station = new Station(stationCode, stationName, x, y, lineCode, commonStations);
                    stations.put(stationCode, station);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Monitors the output directory for new files and processes them for train data updates.
     */
    private void monitorOutputFiles() {
        Runnable task = () -> {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(outputDirectory), "*.csv")) {
                Path firstFile = null;
                for (Path entry : stream) {
                    if (firstFile == null || Files.getLastModifiedTime(entry).toMillis() < Files.getLastModifiedTime(firstFile).toMillis()) {
                        firstFile = entry;
                    }
                }
                if (firstFile != null) {
                    updateTrainData(firstFile);
                    deleteOutputFiles(); // Delete files after processing
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        // Adjusting the timer to check every 7.5 seconds
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                task.run();
            }
        }, 0, 15000);
    }

    /**
     * Updates the train data based on the information in the specified file.
     * @param filePath the path to the file containing train data updates.
     */
    private void updateTrainData(Path filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath.toFile()))) {
            String line;
            trainDataMap.clear();
            while ((line = br.readLine()) != null) {
                if (line.startsWith("LineName")) continue; // Skip header
                String[] parts = line.split(",");
                if (parts.length >= 5) {
                    String lineColor = parts[0].trim();
                    String trainNumber = parts[1].trim();
                    String stationCode = parts[2].trim();
                    String direction = parts[3].trim();
                    String destination = parts[4].trim();

                    Station station = stations.get(stationCode);
                    if (station != null) {
                        TrainData trainData = new TrainData(trainNumber, station, direction, destination, lineColor);
                        trainData.updateNextStations(stations, direction);
                        trainDataMap.put(trainNumber, trainData);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Deletes all output files in the monitored directory.
     */
    private void deleteOutputFiles() {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(outputDirectory), "*.csv")) {
            for (Path entry : stream) {
                Files.delete(entry);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the train data for a specific train number.
     * @param trainNumber the train number to look up.
     * @return the TrainData object associated with the train number.
     */
    public TrainData getTrainData(String trainNumber) {
        return trainDataMap.get(trainNumber);
    }

    /**
     * Gets all train data currently loaded.
     * @return a collection of TrainData objects.
     */
    public Collection<TrainData> getAllTrainData() {
        return trainDataMap.values();
    }

    /**
     * The TrainData class represents the data associated with a train, including its current station and next stops.
     */
    public static class TrainData {
        private final String trainNumber;
        private final Station station;
        private final String direction;
        private final String destination;
        private final String lineColor;
        private List<String> nextStations;

        /**
         * Constructs a TrainData object with the specified parameters.
         * @param trainNumber the train number.
         * @param station the current station of the train.
         * @param direction the direction the train is traveling.
         * @param destination the final destination of the train.
         * @param lineColor the color code of the train line.
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
         */
        public void updateNextStations(Map<String, Station> stations, String direction) {
            List<Station> allStations = new ArrayList<>();
            for (Station st : stations.values()) {
                if (st.getLineCode().equals(this.lineColor)) {
                    allStations.add(st);
                }
            }
            allStations.sort(Comparator.comparing(Station::getStationCode));

            int currentIndex = -1;
            for (int i = 0; i < allStations.size(); i++) {
                if (allStations.get(i).getStationCode().equals(this.station.getStationCode())) {
                    currentIndex = i;
                    break;
                }
            }

            nextStations.clear();
            if (currentIndex != -1) {
                if ("forward".equals(direction)) {
                    for (int i = 1; i <= 4; i++) {
                        if (currentIndex + i < allStations.size()) {
                            nextStations.add(allStations.get(currentIndex + i).getStationName());
                        } else {
                            nextStations.add("End of Line");
                        }
                    }
                } else {
                    for (int i = 1; i <= 4; i++) {
                        if (currentIndex - i >= 0) {
                            nextStations.add(allStations.get(currentIndex - i).getStationName());
                        } else {
                            nextStations.add("Start of Line");
                        }
                    }
                }
            }
        }

        /**
         * Gets the train number.
         * @return the train number.
         */
        public String getTrainNumber() {
            return trainNumber;
        }

        /**
         * Gets the current station of the train.
         * @return the current station.
         */
        public Station getStation() {
            return station;
        }

        /**
         * Gets the direction the train is traveling.
         * @return the direction of the train.
         */
        public String getDirection() {
            return direction;
        }

        /**
         * Gets the final destination of the train.
         * @return the destination of the train.
         */
        public String getDestination() {
            return destination;
        }

        /**
         * Gets the color code of the train line.
         * @return the line color.
         */
        public String getLineColor() {
            return lineColor;
        }

        /**
         * Gets the list of next stations the train will stop at.
         * @return a list of next station names.
         */
        public List<String> getNextStations() {
            return nextStations;
        }
    }

    /**
     * The Station class represents a subway station, including its location and line information.
     */
    public static class Station {
        private final String stationCode;
        private final String stationName;
        private final double x;
        private final double y;
        private final String lineCode;
        private final String commonStations;

        /**
         * Constructs a Station object with the specified parameters.
         * @param stationCode the code of the station.
         * @param stationName the name of the station.
         * @param x the x-coordinate of the station.
         * @param y the y-coordinate of the station.
         * @param lineCode the code of the train line the station is on.
         * @param commonStations a comma-separated list of common stations.
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
         */
        public String getStationCode() {
            return stationCode;
        }

        /**
         * Gets the station name.
         * @return the station name.
         */
        public String getStationName() {
            return stationName;
        }

        /**
         * Gets the x-coordinate of the station.
         * @return the x-coordinate of the station.
         */
        public double getX() {
            return x;
        }

        /**
         * Gets the y-coordinate of the station.
         * @return the y-coordinate of the station.
         */
        public double getY() {
            return y;
        }

        /**
         * Gets the code of the train line the station is on.
         * @return the line code.
         */
        public String getLineCode() {
            return lineCode;
        }

        /**
         * Gets the common stations associated with this station.
         * @return a comma-separated list of common stations.
         */
        public String getCommonStations() {
            return commonStations;
        }
    }
}
