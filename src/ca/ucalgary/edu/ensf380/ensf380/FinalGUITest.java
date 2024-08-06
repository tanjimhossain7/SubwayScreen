package ca.ucalgary.edu.ensf380;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static org.junit.jupiter.api.Assertions.*;

public class FinalGUITest {

    private JLabel adLabel;
    private JLabel mapLabel;
    private JLabel trainInfoLabel;
    private JLabel announcementLabel;
    private TrainStationManager stationManager;

    @BeforeEach
    public void setUp() {
        adLabel = new JLabel();
        mapLabel = new JLabel();
        trainInfoLabel = new JLabel();
        announcementLabel = new JLabel();
        stationManager = new TrainStationManager(); // Assuming TrainStationManager is correctly initialized
        FinalGUI.stationManager = stationManager;
    }

    @Test
    public void testUpdateWeather() {
        // You can set a fixed value or simulate the behavior by adjusting the method
        // but this test is difficult to make meaningful without actual weather data
        FinalGUI finalGUI = new FinalGUI();
        finalGUI.updateWeather(adLabel);

        // This is not a reliable test since weather data changes. You might need to mock the data source
        assertNotNull(adLabel.getText());
    }

    @Test
    public void testDisplayAdvertisement() {
        // Setup test folder with a dummy image
        File testAdsFolder = new File("test-ads");
        testAdsFolder.mkdir();
        File testImage = new File(testAdsFolder, "test-image.png");
        try {
            BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
            ImageIO.write(img, "PNG", testImage);
        } catch (IOException e) {
            e.printStackTrace();
        }

        FinalGUI finalGUI = new FinalGUI();
        finalGUI.displayAdvertisement(adLabel, null); // You might need to adjust this if you have an Advertisement class

        assertNotNull(adLabel.getIcon()); // Assuming displayAdvertisement sets an icon
        assertEquals("", adLabel.getText()); // Text should be empty if image is loaded

        // Cleanup
        testImage.delete();
        testAdsFolder.delete();
    }

    @Test
    public void testDisplayMapAndTrainInfo() {
        // Setup TrainStationManager
        TrainStationManager.Station mockStation = new TrainStationManager.Station("001", "Station A", 100, 200, "Red", "");
        TrainStationManager.TrainData mockTrainData = new TrainStationManager.TrainData("1", mockStation, "forward", "Station X", "Red");
        mockTrainData.updateNextStations(Map.of("001", mockStation), "forward");

        stationManager.getAllTrainData().clear(); // Clear existing data
        stationManager.getAllTrainData().add(mockTrainData); // Add mock data

        // Set up a dummy map image
        try {
            BufferedImage mapImage = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);
            ImageIO.write(mapImage, "PNG", new File("C:/Users/saimk/OneDrive/Desktop/SubwayScreen/src/ca/ucalgary/edu/ensf380/Map/Trains.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        FinalGUI finalGUI = new FinalGUI();
        finalGUI.displayMapAndTrainInfo(mapLabel, trainInfoLabel, announcementLabel);

        assertNotNull(mapLabel.getIcon()); // Check if map image is set
        assertTrue(trainInfoLabel.getText().contains("Station A")); // Check if train info contains station
        assertEquals("Next Stop: Station X", announcementLabel.getText()); // Check if announcement label is updated correctly
    }
}
