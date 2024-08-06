package ca.ucalgary.edu.ensf380;

import javax.swing.*;
import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * CityHallAds handles the initialization, insertion, and retrieval of advertisements from a SQLite database.
 */
public class CityHallAds {
    private static final String DB_URL = "jdbc:sqlite:./CityHallAds.db";

    /**
     * The main method to start the database operations.
     * @param args command-line arguments.
     */
    public static void main(String[] args) {
        initializeDatabase();
        clearExistingData();
        insertAdsFromFolder();
    }

    /**
     * Initializes the database by creating necessary tables if they do not exist.
     */
    private static void initializeDatabase() {
        String createAdvertisementsTable = "CREATE TABLE IF NOT EXISTS Advertisements (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "title TEXT NOT NULL," +
                "description TEXT," +
                "media_id INTEGER," +
                "display_order INTEGER," +
                "FOREIGN KEY (media_id) REFERENCES MediaFiles(id))";
        
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

    /**
     * Clears existing data from the Advertisements and MediaFiles tables.
     */
    private static void clearExistingData() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM Advertisements");
            stmt.execute("DELETE FROM MediaFiles");
            stmt.execute("DELETE FROM sqlite_sequence WHERE name='Advertisements' OR name='MediaFiles'");
            System.out.println("Existing data cleared successfully.");
        } catch (SQLException e) {
            System.out.println("Failed to clear existing data!");
            e.printStackTrace();
        }
    }

    /**
     * Inserts advertisements from the specified folder into the database.
     */
    private static void insertAdsFromFolder() {
        String insertAdvertisement = "INSERT INTO Advertisements (title, description, media_id, display_order) VALUES (?, ?, ?, ?)";
        String insertMediaFile = "INSERT INTO MediaFiles (file_name, file_type, file_path) VALUES (?, ?, ?)";

        File adsFolder = new File("C:/Users/saimk/OneDrive/Desktop/SubwayScreen/src/ca/ucalgary/edu/ensf380/Ads");
        if (!adsFolder.exists() || !adsFolder.isDirectory()) {
            System.out.println("Ads folder not found.");
            return;
        }

        File[] adFiles = adsFolder.listFiles((dir, name) -> {
            String lowerName = name.toLowerCase();
            return lowerName.endsWith(".png") || lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") || lowerName.endsWith(".bmp");
        });

        if (adFiles == null || adFiles.length == 0) {
            System.out.println("No advertisement files found in the Ads folder.");
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement adStmt = conn.prepareStatement(insertAdvertisement);
             PreparedStatement mediaStmt = conn.prepareStatement(insertMediaFile)) {

            for (int i = 0; i < adFiles.length; i++) {
                File adFile = adFiles[i];
                String fileName = adFile.getName();
                String fileType = getFileExtension(fileName).toUpperCase();
                String filePath = adFile.getAbsolutePath();

                // Insert media file
                mediaStmt.setString(1, fileName);
                mediaStmt.setString(2, fileType);
                mediaStmt.setString(3, filePath);
                mediaStmt.executeUpdate();

                // Insert advertisement
                adStmt.setString(1, "Ad " + (i + 1));
                adStmt.setString(2, "Description for " + fileName);
                adStmt.setInt(3, i + 1); // Assuming this is the ID of the media file we just inserted
                adStmt.setInt(4, i + 1);
                adStmt.executeUpdate();
            }

            System.out.println("Ads from folder inserted successfully.");

        } catch (SQLException e) {
            System.out.println("Failed to insert ads from folder!");
            e.printStackTrace();
        }
    }

    /**
     * Returns the file extension of the specified file name.
     * @param fileName the name of the file.
     * @return the file extension as a string.
     */
    private static String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);
    }

    /**
     * Fetches advertisements from the database.
     * @return a list of advertisements.
     */
    public static List<Advertisement> fetchAdvertisements() {
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

    /**
     * Displays an advertisement on the provided label.
     * @param label the JLabel where the advertisement will be displayed.
     * @param ad the Advertisement object containing details about the ad.
     */
    public static void displayAdvertisement(JLabel label, Advertisement ad) {
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

    /**
     * Retrieves a list of map images from the Map folder.
     * @return a list of map image files.
     */
    public static List<File> getMapImages() {
        List<File> mapImages = new ArrayList<>();
        File mapFolder = new File("Map");
        if (mapFolder.exists() && mapFolder.isDirectory()) {
            File[] files = mapFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".png"));
            if (files != null) {
                for (File file : files) {
                    mapImages.add(file);
                }
            }
        }
        System.out.println("Found " + mapImages.size() + " map images");
        return mapImages;
    }
}
