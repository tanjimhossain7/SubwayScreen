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
            JPanel middlePanel = new JPanel(new BorderLayout());
            JPanel bottomPanel = new JPanel(new BorderLayout());

            // Add the top and bottom panels to the frame
            frame.add(topPanel, BorderLayout.NORTH);
            frame.add(middlePanel, BorderLayout.CENTER);
            frame.add(bottomPanel, BorderLayout.SOUTH);

            // Left side of top panel (Map and Ads)
            JPanel adsMapPanel = new JPanel(new BorderLayout());
            JLabel adLabel = new JLabel("", SwingConstants.CENTER);
            adLabel.setFont(new Font("Serif", Font.BOLD, 24));
            adsMapPanel.add(adLabel, BorderLayout.NORTH);

            JLabel mapLabel = new JLabel("", SwingConstants.CENTER);
            adsMapPanel.add(mapLabel, BorderLayout.CENTER);
            topPanel.add(adsMapPanel);

            // Right side of top panel (Time and Weather)
            JPanel weatherTimePanel = new JPanel(new GridLayout(2, 1));
            JLabel timeLabel = new JLabel("Loading time...", SwingConstants.CENTER);
            timeLabel.setFont(new Font("Serif", Font.BOLD, 24));
            weatherTimePanel.add(timeLabel);

            JLabel weatherLabel = new JLabel("Loading weather...", SwingConstants.CENTER);
            weatherLabel.setFont(new Font("Serif", Font.BOLD, 18));
            weatherTimePanel.add(weatherLabel);
            topPanel.add(weatherTimePanel);

            // Middle panel (News)
            JPanel newsPanel = new JPanel(new BorderLayout());
            JLabel newsLabel = new JLabel("Loading news...", SwingConstants.CENTER);
            newsLabel.setFont(new Font("Serif", Font.PLAIN, 16));
            newsPanel.add(newsLabel, BorderLayout.CENTER);
            middlePanel.add(newsPanel);

            // Bottom panel (Train Info)
            JPanel trainInfoPanel = new JPanel(new GridLayout(1, 1));
            JLabel trainInfoLabel = new JLabel("Loading train data...", SwingConstants.CENTER);
            trainInfoLabel.setFont(new Font("Serif", Font.PLAIN, 18));
            trainInfoPanel.add(trainInfoLabel);
            bottomPanel.add(trainInfoPanel, BorderLayout.NORTH);

            // Announcement section under the train information
            JPanel announcementPanel = new JPanel(new BorderLayout());
            JLabel announcementLabel = new JLabel("Next Stop: Loading...", SwingConstants.CENTER);
            announcementLabel.setFont(new Font("Serif", Font.BOLD, 18));
            announcementPanel.add(announcementLabel, BorderLayout.CENTER);
            bottomPanel.add(announcementPanel, BorderLayout.SOUTH);

            frame.setVisible(true);

            // Start fetching data
            updateTime(timeLabel);
            startAdDisplay(adLabel, mapLabel, trainInfoLabel, announcementLabel);
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

    private static void startAdDisplay(JLabel adLabel, JLabel mapLabel, JLabel trainInfoLabel, JLabel announcementLabel) {
        stationManager = new TrainStationManager();
        CityHallAds cityHallAds = new CityHallAds();
        List<Advertisement> ads = cityHallAds.fetchAdvertisements();

        Timer adTimer = new Timer();
        adTimer.scheduleAtFixedRate(new TimerTask() {
            int adIndex = 0;
            boolean showingAd = true;

            @Override
            public void run() {
                if (showingAd) {
                    if (ads != null && !ads.isEmpty()) {
                        displayAdvertisement(adLabel, ads.get(adIndex));
                        adIndex = (adIndex + 1) % ads.size();
                    }
                } else {
                    displayMapAndTrainInfo(mapLabel, trainInfoLabel, announcementLabel);
                }
                showingAd = !showingAd; // Toggle between ad and map
            }
        }, 0, 5000); // Switch between ad and map every 5 seconds
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

    private static void displayMapAndTrainInfo(JLabel mapLabel, JLabel trainInfoLabel, JLabel announcementLabel) {
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
            infoText.append("Prev: ").append(nextStations.get(0)).append(" ");
            infoText.append("Current: <span style='background-color:").append(getLineColorHex(targetData.getLineColor())).append(";'>")
                    .append(targetData.getStation().getStationName()).append("</span> ");
            infoText.append("Next: ").append(nextStations.get(1)).append(" ");
            infoText.append("Second: ").append(nextStations.get(2)).append(" ");
            infoText.append("Third: ").append(nextStations.get(3)).append("</html>");

            trainInfoLabel.setText(infoText.toString());
            announcementLabel.setText("Next Stop: " + nextStations.get(1));
        }
    }

    private static String getLineColorHex(String lineColor) {
        switch (lineColor) {
            case "R":
                return "#FF0000"; // Red
            case "B":
                return "#0000FF"; // Blue
            case "G":
                return "#00FF00"; // Green
            default:
                return "#000000"; // Black
        }
    }
}
