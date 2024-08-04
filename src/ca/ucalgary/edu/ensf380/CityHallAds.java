package cityhallads;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import java.io.File;

public class CityHallAds {
    private static final String DB_URL = "jdbc:sqlite:./CityHallAds.db";

    public static void main(String[] args) {
        initializeDatabase();
        insertSampleData();
        
        List<Advertisement> ads = fetchAdvertisements();
        
        if (ads.isEmpty()) {
            System.out.println("No advertisements found in the database.");
            return;
        }

        JFrame frame = new JFrame("City Hall Advertisements");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        JPanel panel = new JPanel();
        JLabel label = new JLabel();
        panel.add(label);
        frame.add(panel);
        frame.setVisible(true);

        int index = 0;
        while (true) {
            try {
                Advertisement ad = ads.get(index);
                displayAdvertisement(label, ad);
                Thread.sleep(10000); // Show each ad for 10 seconds
                displayMap(label);
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
        switch (fileType) {
            case "JPEG":
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
        label.setText("<html><h1>" + ad.getTitle() + "</h1><p>" + ad.getDescription() + "</p></html>");
    }

    private static void displayMap(JLabel label) {
        List<File> mapImages = getMapImages();
        if (!mapImages.isEmpty()) {
            // Randomly select an image from the list
            File randomImage = mapImages.get((int) (Math.random() * mapImages.size()));
            label.setIcon(new ImageIcon(randomImage.getPath()));
            label.setText("<html><h1>Train Positions</h1></html>");
        } else {
            label.setIcon(null);
            label.setText("<html><h1>Train Positions</h1><p>No map images found</p></html>");
        }
    }

    private static List<File> getMapImages() {
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
