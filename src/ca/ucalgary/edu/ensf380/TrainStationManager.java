package ca.ucalgary.edu.ensf380;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

public class TrainStationManager {
    private final Map<String, List<TrainData>> trainPositions;
    private final Map<String, String> stationCodeToName;
    private final String outputDirectory = "C:\\Users\\saimk\\OneDrive\\Desktop\\SubwayScreen\\out";
    private final String simulatorPath = "C:\\Users\\saimk\\OneDrive\\Desktop\\SubwayScreen\\exe\\SubwaySimulator.jar";
    private final String subwayFilePath = "C:\\Users\\saimk\\OneDrive\\Desktop\\SubwayScreen\\data\\subway.csv";
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private Process simulatorProcess;

    public TrainStationManager() {
        this.trainPositions = new HashMap<>();
        this.stationCodeToName = new HashMap<>();
        loadStationData();
        startSimulator();
        monitorOutputFiles();
    }

    private void loadStationData() {
        try (BufferedReader br = new BufferedReader(new FileReader(subwayFilePath))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue; // Skip the header line
                }
                String[] parts = line.split(",");
                if (parts.length >= 8) {
                    String stationCode = parts[3].trim();
                    String stationName = parts[4].trim();

                    stationCodeToName.put(stationCode, stationName);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startSimulator() {
        try {
            ProcessBuilder pb = new ProcessBuilder("java", "-jar", simulatorPath, "--in", subwayFilePath, "--out", outputDirectory);
            simulatorProcess = pb.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void monitorOutputFiles() {
        Runnable task = () -> {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(outputDirectory), "*.csv")) {
                Path latestFile = null;
                for (Path entry : stream) {
                    if (latestFile == null || Files.getLastModifiedTime(entry).toMillis() > Files.getLastModifiedTime(latestFile).toMillis()) {
                        latestFile = entry;
                    }
                }
                if (latestFile != null) {
                    updateTrainPositions(latestFile);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        scheduler.scheduleWithFixedDelay(task, 0, 15, TimeUnit.SECONDS);
    }

    private void updateTrainPositions(Path filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath.toFile()))) {
            String line;
            Map<String, List<TrainData>> newPositions = new HashMap<>();
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 5) {
                    String lineColor = parts[0].trim();
                    String trainNumber = parts[1].trim();
                    String stationCode = parts[2].trim();
                    String direction = parts[3].trim();
                    String destination = parts[4].trim();

                    String stationName = stationCodeToName.getOrDefault(stationCode, stationCode);
                    String destinationName = stationCodeToName.getOrDefault(destination, destination);

                    newPositions.putIfAbsent(lineColor, new ArrayList<>());

                    TrainData data = new TrainData(trainNumber, stationName, direction, destinationName);
                    newPositions.get(lineColor).add(data);
                }
            }
            synchronized (trainPositions) {
                trainPositions.clear();
                trainPositions.putAll(newPositions);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<TrainData> getTrainPositions(String lineColor) {
        synchronized (trainPositions) {
            return trainPositions.getOrDefault(lineColor, new ArrayList<>());
        }
    }

    public void shutdown() {
        scheduler.shutdown();
        try {
            if (simulatorProcess != null) {
                simulatorProcess.destroy();
            }
            Files.walk(Paths.get(outputDirectory))
                    .filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class TrainData {
        private final String trainNumber;
        private final String stationName;
        private final String direction;
        private final String destination;
        private final List<String> commonStations;

        public TrainData(String trainNumber, String stationName, String direction, String destination) {
            this.trainNumber = trainNumber;
            this.stationName = stationName;
            this.direction = direction;
            this.destination = destination;
            this.commonStations = new ArrayList<>();
        }

        public String getTrainNumber() {
            return trainNumber;
        }

        public String getStationName() {
            return stationName;
        }

        public String getDirection() {
            return direction;
        }

        public String getDestination() {
            return destination;
        }

        public void addCommonStation(String station) {
            commonStations.add(station);
        }

        public List<String> getCommonStations() {
            return commonStations;
        }

        public List<String> getSurroundingStations() {
            // Mock method to get the previous, current, and next stations for a train
            // Replace this with the actual logic to get the correct stations based on train data
            return Arrays.asList("PrevStation", stationName, "NextStation1", "NextStation2", "NextStation3");
        }
    }
}














