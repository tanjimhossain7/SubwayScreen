package ca.ucalgary.edu.ensf380;

import java.sql.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class CityHallAds {
	private static final String DB_URL = "jdbc:sqlite:C:/Users/saimk/OneDrive/Desktop/SubwayScreen/CityHallAds.db";
    private static TrainStationManager stationManager;
    private static final String TARGET_TRAIN = "1"; // or whichever value you need

    public static void main(String[] args) {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return;
        }
        
     // Start the simulator
        try {
            ProcessBuilder pb = new ProcessBuilder("java", "-jar", "C:\\Users\\saimk\\OneDrive\\Desktop\\SubwayScreen\\exe\\SubwaySimulator.jar --in \"..\\data\\subway.csv\" --out \"..\\out");
            pb.start();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        stationManager = new TrainStationManager();

        initializeDatabase();
        insertSampleData();

        stationManager = new TrainStationManager();

        initializeDatabase();
        insertSampleData();

        List<Advertisement> ads = fetchAdvertisements();

        if (ads.isEmpty()) {
            System.out.println("No advertisements found in the database.");
            return;
        }

        JFrame frame = new JFrame("City Hall Advertisements");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 500);

        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel();
        label.setHorizontalAlignment(JLabel.CENTER);
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new GridLayout(1, 1)); // Adjust layout to match the hardcoded train output

        panel.add(label, BorderLayout.CENTER);
        frame.setLayout(new BorderLayout());
        frame.add(panel, BorderLayout.CENTER);
        frame.add(infoPanel, BorderLayout.SOUTH);
        frame.setVisible(true);

        int index = 0;
        while (true) {
            try {
                Advertisement ad = ads.get(index);
                displayAdvertisement(label, ad);
                Thread.sleep(10000); // Show each ad for 10 seconds
                displayMapAndTrainInfo(label, infoPanel);
                Thread.sleep(5000); // Show map for 5 seconds
                index = (index + 1) % ads.size();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void initializeDatabase() {
        String createAdvertisementsTable = "CREATE TABLE IF NOT EXISTS Advertisements (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "title TEXT NOT NULL," +
                "description TEXT," +
                "media_id INTEGER," +
                "display_order INTEGER)";

        String createMediaFilesTable = "CREATE TABLE IF NOT EXISTS MediaFiles (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "file_name TEXT NOT NULL," +
                "file_type TEXT NOT NULL," +
                "file_path TEXT NOT NULL)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            stmt.execute(createAdvertisementsTable);
            stmt.execute(createMediaFilesTable);
            System.out.println("Database initialized successfully.");

        } catch (SQLException e) {
            System.out.println("Database initialization failed!");
            e.printStackTrace();
        }
    }

    private static void insertSampleData() {
        String insertAdvertisement = "INSERT INTO Advertisements (title, description, media_id, display_order) VALUES (?, ?, ?, ?)";
        String insertMediaFile = "INSERT INTO MediaFiles (file_name, file_type, file_path) VALUES (?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement adStmt = conn.prepareStatement(insertAdvertisement);
             PreparedStatement mediaStmt = conn.prepareStatement(insertMediaFile)) {

            // Insert a sample media file
            mediaStmt.setString(1, "sample_ad.jpg");
            mediaStmt.setString(2, "JPEG");
            mediaStmt.setString(3, "path/to/sample_ad.jpg");
            mediaStmt.executeUpdate();

            // Insert a sample advertisement
            adStmt.setString(1, "Sample Ad");
            adStmt.setString(2, "This is a sample advertisement");
            adStmt.setInt(3, 1); // Assuming this is the ID of the media file we just inserted
            adStmt.setInt(4, 1);
            adStmt.executeUpdate();

            System.out.println("Sample data inserted successfully.");

        } catch (SQLException e) {
            System.out.println("Failed to insert sample data!");
            e.printStackTrace();
        }
    }

    private static List<Advertisement> fetchAdvertisements() {
        List<Advertisement> ads = new ArrayList<>();
        try {
            // Load the SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");
            System.out.println("SQLite JDBC Driver Registered!");

            try (Connection conn = DriverManager.getConnection(DB_URL);
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT a.id, a.title, a.description, m.file_name, m.file_type, m.file_path " +
                         "FROM Advertisements a JOIN MediaFiles m ON a.media_id = m.id ORDER BY a.display_order")) {

                System.out.println("Database connection established");
                while (rs.next()) {
                    Advertisement ad = new Advertisement(
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getString("description"),
                            rs.getString("file_name"),
                            rs.getString("file_type"),
                            rs.getString("file_path")
                    );
                    ads.add(ad);
                    System.out.println("Added advertisement: " + ad.getTitle());
                }
            }
        } catch (ClassNotFoundException e) {
            System.out.println("SQLite JDBC Driver not found. Include it in your library path!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("Database connection failed!");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("An unexpected error occurred!");
            e.printStackTrace();
        }
        System.out.println("Total advertisements fetched: " + ads.size());
        return ads;
    }

    private static void displayAdvertisement(JLabel label, Advertisement ad) {
        String filePath = ad.getFilePath();
        String fileType = ad.getFileType();
        switch (fileType.toUpperCase()) {
            case "JPEG":
            case "JPG":
            case "PNG":
            case "BMP":
                label.setIcon(new ImageIcon(filePath));
                break;
            case "PDF":
                // Handle PDF display
                break;
            case "MPG":
                // Handle MPG display
                break;
        }
    }

    private static void displayMapAndTrainInfo(JLabel label, JPanel infoPanel) {
        BufferedImage mapImage;
        try {
            mapImage = ImageIO.read(new File("C:\\Users\\saimk\\OneDrive\\Desktop\\SubwayScreen\\src\\ca\\ucalgary\\edu\\ensf380\\Map\\Trains.png"));
        } catch (Exception e) {
            e.printStackTrace();
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

        // Add the legend
        int legendX = 20;
        int legendY = mapImage.getHeight() - 100;
        g2d.setColor(Color.BLACK);
        g2d.drawString("Legend:", legendX, legendY);
        g2d.setColor(Color.RED);
        g2d.fillRect(legendX, legendY + 10, 10, 10);
        g2d.drawString("Red Line", legendX + 15, legendY + 20);
        g2d.setColor(Color.BLUE);
        g2d.fillRect(legendX, legendY + 30, 10, 10);
        g2d.drawString("Blue Line", legendX + 15, legendY + 40);
        g2d.setColor(Color.GREEN);
        g2d.fillRect(legendX, legendY + 50, 10, 10);
        g2d.drawString("Green Line", legendX + 15, legendY + 60);

        g2d.dispose();
        label.setIcon(new ImageIcon(mapImage));

        // Display the train information for the hardcoded train
        TrainStationManager.TrainData targetData = stationManager.getTrainData(TARGET_TRAIN);
        if (targetData != null) {
            infoPanel.removeAll();
            JPanel trainInfoPanel = new JPanel();
            trainInfoPanel.setLayout(new GridLayout(1, 4));

            List<String> nextStations = targetData.getNextStations();
            JLabel prevStationLabel = new JLabel("<html><div style='padding: 10px;'>Prev: " + nextStations.get(0) + "</div></html>", JLabel.CENTER);
            JLabel currentStationLabel = new JLabel("<html><div style='padding: 10px;'>Current: <span style='background-color:" + getLineColorHex(targetData.getLineColor()) + "'>" 
                                                    + targetData.getStation().getStationName() + "</span></div></html>", JLabel.CENTER);
            currentStationLabel.setOpaque(true);
            JLabel nextStationLabel = new JLabel("<html><div style='padding: 10px;'>Next: " + nextStations.get(1) + "</div></html>", JLabel.CENTER);
            JLabel afterNextStationLabel = new JLabel("<html><div style='padding: 10px;'>Second: " + nextStations.get(2) + "</div></html>", JLabel.CENTER);
            JLabel ThirdStationLabel = new JLabel("<html><div style='padding: 10px;'>Third: " + nextStations.get(3) + "</div></html>", JLabel.CENTER);

            // Add the station information to the train info panel
            trainInfoPanel.add(prevStationLabel);
            trainInfoPanel.add(currentStationLabel);
            trainInfoPanel.add(nextStationLabel);
            trainInfoPanel.add(afterNextStationLabel);
            trainInfoPanel.add(ThirdStationLabel);

         // Create a panel for announcements
            JPanel announcementPanel = new JPanel(new GridLayout(1, 1));
            JLabel announcementLabel = new JLabel("<html><div style='padding: 10px;'>Next Station: " + nextStations.get(1) + "</div></html>", JLabel.CENTER);
            announcementPanel.add(announcementLabel);

            // Add the train info panel and the announcement panel to the info panel
            infoPanel.add(trainInfoPanel);
            infoPanel.add(announcementPanel);

            infoPanel.revalidate();
            infoPanel.repaint();
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