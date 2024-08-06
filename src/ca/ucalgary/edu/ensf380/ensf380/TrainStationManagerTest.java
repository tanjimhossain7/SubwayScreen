package ca.ucalgary.edu.ensf380;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class TrainStationManagerTest {

    private TrainStationManager manager;
    private final String testOutputDirectory = "test-output";
    private final String testSubwayFilePath = "test-subway.csv";

    @BeforeEach
    public void setUp() throws IOException {
        // Create a test directory and files
        Files.createDirectories(Paths.get(testOutputDirectory));
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(testSubwayFilePath))) {
            writer.write("LineCode,LineName,StationCode,StationName,X,Y,CommonStations\n");
            writer.write("Red,Red Line,001,Station A,0.0,0.0,\n");
            writer.write("Red,Red Line,002,Station B,1.0,1.0,\n");
        }
        manager = new TrainStationManager() {
            @Override
            protected void loadStationData() {
                // Load test data
                try (BufferedReader br = new BufferedReader(new FileReader(testSubwayFilePath))) {
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
                            String stationCode = parts[2].trim();
                            String stationName = parts[3].trim();
                            double x = Double.parseDouble(parts[4].trim());
                            double y = Double.parseDouble(parts[5].trim());
                            String commonStations = parts.length == 8 ? parts[6].trim() : "";

                            Station station = new Station(stationCode, stationName, x, y, lineCode, commonStations);
                            stations.put(stationCode, station);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    @Test
    public void testLoadStationData() {
        // Test loading station data
        Station station = manager.getStation("001");
        assertNotNull(station);
        assertEquals("Station A", station.getStationName());
        assertEquals("Red", station.getLineCode());
    }

    @Test
    public void testUpdateTrainData() throws IOException {
        // Create a test CSV file for train data
        String testTrainFilePath = "test-train-data.csv";
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(testTrainFilePath))) {
            writer.write("LineName,TrainNumber,StationCode,Direction,Destination\n");
            writer.write("Red,123,001,forward,Station B\n");
        }

        // Call the method to update train data
        manager.updateTrainData(Paths.get(testTrainFilePath));

        // Verify the train data
        TrainData trainData = manager.getTrainData("123");
        assertNotNull(trainData);
        assertEquals("123", trainData.getTrainNumber());
        assertEquals("Station A", trainData.getStation().getStationName());
        assertTrue(trainData.getNextStations().contains("Station B"));
    }

    @Test
    public void testDeleteOutputFiles() throws IOException {
        // Create a test file to be deleted
        Path testFile = Paths.get(testOutputDirectory, "test-file.csv");
        Files.createFile(testFile);

        // Call the method to delete output files
        manager.deleteOutputFiles();

        // Verify the file is deleted
        assertFalse(Files.exists(testFile));
    }

    @AfterEach
    public void tearDown() throws IOException {
        // Clean up test files and directories
        Files.deleteIfExists(Paths.get(testSubwayFilePath));
        Files.deleteIfExists(Paths.get(testOutputDirectory));
    }
}
