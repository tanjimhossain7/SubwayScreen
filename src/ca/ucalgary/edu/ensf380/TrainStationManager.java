package ca.ucalgary.edu.ensf380;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class TrainStationManager {
    private final Map<String, TrainData> trainDataMap = new HashMap<>();
    private final Map<String, Station> stations = new HashMap<>();
    private final String outputDirectory = "C:\\Users\\saimk\\OneDrive\\Desktop\\SubwayScreen\\out";
    private final String subwayFilePath = "C:\\Users\\saimk\\OneDrive\\Desktop\\SubwayScreen\\src\\ca\\ucalgary\\edu\\ensf380\\Map\\Map.csv";

    public TrainStationManager() {
        loadStationData();
        monitorOutputFiles();
    }

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

    private void deleteOutputFiles() {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(outputDirectory), "*.csv")) {
            for (Path entry : stream) {
                Files.delete(entry);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public TrainData getTrainData(String trainNumber) {
        return trainDataMap.get(trainNumber);
    }

    public Collection<TrainData> getAllTrainData() {
        return trainDataMap.values();
    }

    public static class TrainData {
        private final String trainNumber;
        private final Station station;
        private final String direction;
        private final String destination;
        private final String lineColor;
        private List<String> nextStations;

        public TrainData(String trainNumber, Station station, String direction, String destination, String lineColor) {
            this.trainNumber = trainNumber;
            this.station = station;
            this.direction = direction;
            this.destination = destination;
            this.lineColor = lineColor;
            this.nextStations = new ArrayList<>();
        }

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

        public String getTrainNumber() {
            return trainNumber;
        }

        public Station getStation() {
            return station;
        }

        public String getDirection() {
            return direction;
        }

        public String getDestination() {
            return destination;
        }

        public String getLineColor() {
            return lineColor;
        }

        public List<String> getNextStations() {
            return nextStations;
        }
    }

    public static class Station {
        private final String stationCode;
        private final String stationName;
        private final double x;
        private final double y;
        private final String lineCode;
        private final String commonStations;

        public Station(String stationCode, String stationName, double x, double y, String lineCode, String commonStations) {
            this.stationCode = stationCode;
            this.stationName = stationName;
            this.x = x;
            this.y = y;
            this.lineCode = lineCode;
            this.commonStations = commonStations;
        }

        public String getStationCode() {
            return stationCode;
        }

        public String getStationName() {
            return stationName;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public String getLineCode() {
            return lineCode;
        }

        public String getCommonStations() {
            return commonStations;
        }
    }
}





