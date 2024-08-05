package ca.ucalgary.edu.ensf380;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class FinalGUI {
    private static final String TARGET_TRAIN = "1"; // or whichever value you need
    private static TrainStationManager stationManager;

    public static void main(String[] args) {
        // Run the simulator
        startSimulator();

        // Initialize and start the GUI
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Final GUI");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1200, 800);
            frame.setLayout(new BorderLayout());

            // Main panels
            JPanel topPanel = new JPanel(new GridLayout(1, 2));
            JPanel bottomPanel = new JPanel(new GridLayout(2, 1));

            // Add the top and bottom panels to the frame
            frame.add(topPanel, BorderLayout.NORTH);
            frame.add(bottomPanel, BorderLayout.CENTER);

            // Time panel (Right Side of Top Panel)
            JPanel weatherTimePanel = new JPanel(new GridLayout(2, 1));
            JLabel timeLabel = new JLabel("Loading time...", SwingConstants.CENTER);
            timeLabel.setFont(new Font("Serif", Font.BOLD, 24));
            weatherTimePanel.add(timeLabel);
            topPanel.add(weatherTimePanel);

            // Ads and Map panel
            JPanel adsMapPanel = new JPanel(new BorderLayout());
            JLabel adLabel = new JLabel("", SwingConstants.CENTER);
            adLabel.setFont(new Font("Serif", Font.BOLD, 24));
            adsMapPanel.add(adLabel, BorderLayout.NORTH);

            JLabel mapLabel = new JLabel("", SwingConstants.CENTER);
            adsMapPanel.add(mapLabel, BorderLayout.CENTER);
            bottomPanel.add(adsMapPanel);

            // Train info panel
            JPanel trainInfoPanel = new JPanel(new GridLayout(1, 1));
            JLabel trainInfoLabel = new JLabel("Loading train data...", SwingConstants.CENTER);
            trainInfoLabel.setFont(new Font("Serif", Font.PLAIN, 18));
            trainInfoPanel.add(trainInfoLabel);
            bottomPanel.add(trainInfoPanel);

            frame.setVisible(true);

            // Start fetching data
            updateTime(timeLabel);
            startAdDisplay(adLabel, mapLabel, trainInfoLabel);
        });
    }

    private static void startSimulator() {
        try {
            ProcessBuilder pb = new ProcessBuilder("java", "-jar", "C:\\Users\\saimk\\OneDrive\\Desktop\\SubwayScreen\\exe\\SubwaySimulator.jar");
            pb.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void updateTime(JLabel timeLabel) {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                String currentTime = TimeApplication.getCurrentTime("America/Edmonton");
                SwingUtilities.invokeLater(() -> timeLabel.setText("Current Time: " + currentTime));
            }
        }, 0, 1000); // Update every second
    }

    private static void startAdDisplay(JLabel adLabel, JLabel mapLabel, JLabel trainInfoLabel) {
        stationManager = new TrainStationManager();
        CityHallAds cityHallAds = new CityHallAds();
        List<Advertisement> ads = cityHallAds.fetchAdvertisements();

        Timer adTimer = new Timer();
        adTimer.scheduleAtFixedRate(new TimerTask() {
            int adIndex = 0;

            @Override
            public void run() {
                if (ads != null && !ads.isEmpty()) {
                    displayAdvertisement(adLabel, ads.get(adIndex));
                    adIndex = (adIndex + 1) % ads.size();
                }
            }
        }, 0, 10000); // Switch ad every 10 seconds

        Timer mapTimer = new Timer();
        mapTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                displayMapAndTrainInfo(mapLabel, trainInfoLabel);
            }
        }, 5000, 15000); // Display map 5 seconds after ad, every 15 seconds
    }

    private static void displayAdvertisement(JLabel label, Advertisement ad) {
        String filePath = ad.getFilePath();
        String fileType = ad.getFileType().toUpperCase();

        switch (fileType) {
            case "JPEG":
            case "JPG":
            case "PNG":
            case "BMP":
                try {
                    BufferedImage img = ImageIO.read(new File(filePath));
                    label.setIcon(new ImageIcon(img));
                } catch (IOException e) {
                    e.printStackTrace();
                    label.setText("Failed to load advertisement image.");
                }
                break;
            default:
                label.setText("Unsupported media type: " + fileType);
                break;
        }
    }

    private static void displayMapAndTrainInfo(JLabel mapLabel, JLabel trainInfoLabel) {
        BufferedImage mapImage;
        try {
            mapImage = ImageIO.read(new File("C:\\Users\\saimk\\OneDrive\\Desktop\\SubwayScreen\\src\\ca\\ucalgary\\edu\\ensf380\\Map\\Trains.png"));
        } catch (IOException e) {
            e.printStackTrace();
            mapLabel.setText("Failed to load map image.");
            return;
        }

        Graphics2D g2d = mapImage.createGraphics();
        g2d.setColor(Color.GRAY);

        for (TrainStationManager.TrainData data : stationManager.getAllTrainData()) {
            int x = (int) data.getStation().getX();
            int y = (int) data.getStation().getY();
            g2d.fillOval(x, y, 10, 10); // Draw grey dot for each train

            if (TARGET_TRAIN.equals(data.getTrainNumber())) {
                g2d.setColor(Color.GREEN); // Green dot for target train
                g2d.fillOval(x, y, 10, 10);
                g2d.setColor(Color.GRAY); // Reset color for other trains
            }
        }

        g2d.dispose();
        mapLabel.setIcon(new ImageIcon(mapImage));

        TrainStationManager.TrainData targetData = stationManager.getTrainData(TARGET_TRAIN);
        if (targetData != null) {
            List<String> nextStations = targetData.getNextStations();
            StringBuilder infoText = new StringBuilder("<html>");
            infoText.append("Prev: ").append(nextStations.get(0)).append("<br>");
            infoText.append("Current: ").append(targetData.getStation().getStationName()).append("<br>");
            infoText.append("Next: ").append(nextStations.get(1)).append("<br>");
            infoText.append("Second: ").append(nextStations.get(2)).append("<br>");
            infoText.append("Third: ").append(nextStations.get(3)).append("<br>");
            infoText.append("</html>");

            trainInfoLabel.setText(infoText.toString());
        }
    }
}