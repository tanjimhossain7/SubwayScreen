package ca.ucalgary.edu.ensf380;

import javax.swing.*;  // Importing necessary classes for GUI components
import java.io.File;   // Importing File class to handle file operations
import java.sql.*;     // Importing JDBC classes to handle SQLite database operations
import java.util.ArrayList;  // Importing ArrayList to store lists of objects
import java.util.List;       // Importing List interface for list operations

/**
 * CityHallAds handles the initialization, insertion, and retrieval of advertisements from a SQLite database.
 * This class is responsible for managing advertisement data stored in a SQLite database,
 * including creating tables, inserting data, and fetching data.
 */
public class CityHallAds {
    private static final String DB_URL = "jdbc:sqlite:./CityHallAds.db"; // Database URL for SQLite connection

    /**
     * The main method to start the database operations.
     * @param args command-line arguments.
     * This method is the entry point of the application. It initiates database setup and populates it with data.
     */
    public static void main(String[] args) {
        initializeDatabase();  // Create the database tables if they don't exist
        clearExistingData();   // Clear any existing data from the tables
        insertAdsFromFolder(); // Insert advertisements from a specified folder into the database
    }

    /**
     * Initializes the database by creating necessary tables if they do not exist.
     * This method ensures that the database structure is set up before any data operations are performed.
     */
    private static void initializeDatabase() {
        // SQL statement to create the Advertisements table with columns for ID, title, description, media ID, and display order
        String createAdvertisementsTable = "CREATE TABLE IF NOT EXISTS Advertisements (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "title TEXT NOT NULL," +
                "description TEXT," +
                "media_id INTEGER," +
                "display_order INTEGER," +
                "FOREIGN KEY (media_id) REFERENCES MediaFiles(id))";
        
        // SQL statement to create the MediaFiles table with columns for ID, file name, file type, and file path
        String createMediaFilesTable = "CREATE TABLE IF NOT EXISTS MediaFiles (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "file_name TEXT NOT NULL," +
                "file_type TEXT NOT NULL," +
                "file_path TEXT NOT NULL)";

        try (Connection conn = DriverManager.getConnection(DB_URL);  // Establishing a connection to the SQLite database
             Statement stmt = conn.createStatement()) {  // Creating a statement object to execute SQL queries
            
            stmt.execute(createAdvertisementsTable);  // Executing SQL query to create Advertisements table
            stmt.execute(createMediaFilesTable);      // Executing SQL query to create MediaFiles table
            System.out.println("Database initialized successfully.");  // Logging success message
            
        } catch (SQLException e) {  // Handling SQL exceptions
            System.out.println("Database initialization failed!");  // Logging failure message
            e.printStackTrace();  // Printing stack trace for debugging
        }
    }

    /**
     * Clears existing data from the Advertisements and MediaFiles tables.
     * This method is useful for resetting the database before inserting new data,
     * ensuring that no old data interferes with current operations.
     */
    private static void clearExistingData() {
        try (Connection conn = DriverManager.getConnection(DB_URL);  // Establishing a connection to the SQLite database
             Statement stmt = conn.createStatement()) {  // Creating a statement object to execute SQL queries
            stmt.execute("DELETE FROM Advertisements");  // Deleting all records from the Advertisements table
            stmt.execute("DELETE FROM MediaFiles");      // Deleting all records from the MediaFiles table
            stmt.execute("DELETE FROM sqlite_sequence WHERE name='Advertisements' OR name='MediaFiles'");  // Resetting auto-increment IDs
            System.out.println("Existing data cleared successfully.");  // Logging success message
        } catch (SQLException e) {  // Handling SQL exceptions
            System.out.println("Failed to clear existing data!");  // Logging failure message
            e.printStackTrace();  // Printing stack trace for debugging
        }
    }

    /**
     * Inserts advertisements from the specified folder into the database.
     * This method reads advertisement files from a directory, stores them in the database,
     * and associates them with metadata such as title and description.
     */
    private static void insertAdsFromFolder() {
        // SQL statement to insert an advertisement record
        String insertAdvertisement = "INSERT INTO Advertisements (title, description, media_id, display_order) VALUES (?, ?, ?, ?)";
        // SQL statement to insert a media file record
        String insertMediaFile = "INSERT INTO MediaFiles (file_name, file_type, file_path) VALUES (?, ?, ?)";

        // Path to the folder containing advertisement files
        File adsFolder = new File("C:/Users/saimk/OneDrive/Desktop/SubwayScreen/src/ca/ucalgary/edu/ensf380/Ads");
        if (!adsFolder.exists() || !adsFolder.isDirectory()) {  // Check if the folder exists and is a directory
            System.out.println("Ads folder not found.");  // Log error message if folder is not found
            return;  // Exit the method if folder is not found
        }

        // Filter to select only image files (PNG, JPG, JPEG, BMP)
        File[] adFiles = adsFolder.listFiles((dir, name) -> {
            String lowerName = name.toLowerCase();
            return lowerName.endsWith(".png") || lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") || lowerName.endsWith(".bmp");
        });

        if (adFiles == null || adFiles.length == 0) {  // Check if any advertisement files were found
            System.out.println("No advertisement files found in the Ads folder.");  // Log error message if no files were found
            return;  // Exit the method if no files were found
        }

        try (Connection conn = DriverManager.getConnection(DB_URL);  // Establishing a connection to the SQLite database
             PreparedStatement adStmt = conn.prepareStatement(insertAdvertisement);  // Preparing the advertisement insertion statement
             PreparedStatement mediaStmt = conn.prepareStatement(insertMediaFile)) {  // Preparing the media file insertion statement

            for (int i = 0; i < adFiles.length; i++) {  // Iterate over each advertisement file
                File adFile = adFiles[i];  // Get the current file
                String fileName = adFile.getName();  // Get the file name
                String fileType = getFileExtension(fileName).toUpperCase();  // Get the file extension and convert to uppercase
                String filePath = adFile.getAbsolutePath();  // Get the absolute file path

                // Insert media file record
                mediaStmt.setString(1, fileName);
                mediaStmt.setString(2, fileType);
                mediaStmt.setString(3, filePath);
                mediaStmt.executeUpdate();

                // Insert advertisement record
                adStmt.setString(1, "Ad " + (i + 1));  // Set title as "Ad X"
                adStmt.setString(2, "Description for " + fileName);  // Set description
                adStmt.setInt(3, i + 1);  // Assuming this is the ID of the media file we just inserted
                adStmt.setInt(4, i + 1);  // Set display order
                adStmt.executeUpdate();
            }

            System.out.println("Ads from folder inserted successfully.");  // Log success message

        } catch (SQLException e) {  // Handling SQL exceptions
            System.out.println("Failed to insert ads from folder!");  // Log error message
            e.printStackTrace();  // Print stack trace for debugging
        }
    }

    /**
     * Returns the file extension of the specified file name.
     * @param fileName the name of the file.
     * @return the file extension as a string.
     * This utility method helps extract the file extension from a given file name.
     */
    private static String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');  // Find the last occurrence of the dot character
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);  // Return the substring after the dot or an empty string if no dot is found
    }

    /**
     * Fetches advertisements from the database.
     * @return a list of advertisements.
     * This method retrieves advertisement records from the database and stores them in a list.
     */
    public static List<Advertisement> fetchAdvertisements() {
        List<Advertisement> ads = new ArrayList<>();  // Initialize a list to store the fetched advertisements
        try {
            // Load the SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");
            System.out.println("SQLite JDBC Driver Registered!");
            
            try (Connection conn = DriverManager.getConnection(DB_URL);  // Establishing a connection to the SQLite database
                 Statement stmt = conn.createStatement();  // Creating a statement object to execute SQL queries
                 ResultSet rs = stmt.executeQuery("SELECT a.id, a.title, a.description, m.file_name, m.file_type, m.file_path " +
                         "FROM Advertisements a JOIN MediaFiles m ON a.media_id = m.id ORDER BY a.display_order")) {  // Execute a query to retrieve advertisements and their associated media files
                
                System.out.println("Database connection established");  // Log success message
                while (rs.next()) {  // Iterate over the result set
                    Advertisement ad = new Advertisement(
                            rs.getInt("id"),  // Fetch advertisement ID
                            rs.getString("title"),  // Fetch title
                            rs.getString("description"),  // Fetch description
                            rs.getString("file_name"),  // Fetch file name
                            rs.getString("file_type"),  // Fetch file type
                            rs.getString("file_path")  // Fetch file path
                    );
                    ads.add(ad);  // Add the advertisement object to the list
                    System.out.println("Added advertisement: " + ad.getTitle());  // Log each added advertisement
                }
            }
        } catch (ClassNotFoundException e) {  // Handling ClassNotFoundException
            System.out.println("SQLite JDBC Driver not found. Include it in your library path!");  // Log error message
            e.printStackTrace();  // Print stack trace for debugging
        } catch (SQLException e) {  // Handling SQL exceptions
            System.out.println("Database connection failed!");  // Log error message
            e.printStackTrace();  // Print stack trace for debugging
        } catch (Exception e) {  // Handling general exceptions
            System.out.println("An unexpected error occurred!");  // Log error message
            e.printStackTrace();  // Print stack trace for debugging
        }
        System.out.println("Total advertisements fetched: " + ads.size());  // Log the total number of advertisements fetched
        return ads;  // Return the list of advertisements
    }

    /**
     * Displays an advertisement on the provided label.
     * @param label the JLabel where the advertisement will be displayed.
     * @param ad the Advertisement object containing details about the ad.
     * This method updates a JLabel with an advertisement's image.
     */
    public static void displayAdvertisement(JLabel label, Advertisement ad) {
        String filePath = ad.getFilePath();  // Get the file path of the advertisement
        String fileType = ad.getFileType();  // Get the file type of the advertisement
        switch (fileType.toUpperCase()) {  // Handle different file types
            case "JPEG":
            case "JPG":
            case "PNG":
            case "BMP":
                label.setIcon(new ImageIcon(filePath));  // Set the image icon of the JLabel to the advertisement image
                break;
            case "PDF":
                // Handle PDF display (currently not implemented)
                break;
            case "MPG":
                // Handle MPG display (currently not implemented)
                break;
        }
    }

    /**
     * Retrieves a list of map images from the Map folder.
     * @return a list of map image files.
     * This method retrieves image files from a directory that contains maps.
     */
    public static List<File> getMapImages() {
        List<File> mapImages = new ArrayList<>();  // Initialize a list to store map image files
        File mapFolder = new File("Map");  // Specify the folder containing map images
        if (mapFolder.exists() && mapFolder.isDirectory()) {  // Check if the folder exists and is a directory
            File[] files = mapFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".png"));  // Filter for PNG files
            if (files != null) {
                for (File file : files) {  // Iterate over each file
                    mapImages.add(file);  // Add the file to the list
                }
            }
        }
        System.out.println("Found " + mapImages.size() + " map images");  // Log the number of map images found
        return mapImages;  // Return the list of map images
    }
}

