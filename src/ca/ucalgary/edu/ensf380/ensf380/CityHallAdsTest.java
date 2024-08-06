package ca.ucalgary.edu.ensf380;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

class CityHallAdsTest {

    private static final String TEST_DB_URL = "jdbc:sqlite:./TestCityHallAds.db";

    @BeforeAll
    static void setUpDatabase() {
        CityHallAds.initializeDatabase();
    }

    @AfterAll
    static void cleanUpDatabase() {
        File dbFile = new File("./TestCityHallAds.db");
        if (dbFile.exists()) {
            dbFile.delete();
        }
    }

    @Test
    void testDatabaseInitialization() {
        try (Connection conn = DriverManager.getConnection(TEST_DB_URL);
             Statement stmt = conn.createStatement()) {
            
            ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='Advertisements'");
            assertTrue(rs.next(), "Advertisements table should exist");
            
            rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='MediaFiles'");
            assertTrue(rs.next(), "MediaFiles table should exist");
        } catch (Exception e) {
            fail("Database initialization failed: " + e.getMessage());
        }
    }

    @Test
    void testFetchAdvertisements() {
        List<Advertisement> ads = CityHallAds.fetchAdvertisements();
        assertNotNull(ads, "Fetched advertisements list should not be null");
        // Add more assertions based on expected data
    }

    @Test
    void testGetMapImages() {
        List<File> mapImages = CityHallAds.getMapImages();
        assertNotNull(mapImages, "Map images list should not be null");
        // Add more assertions based on expected map images
    }

    @Test
    void testGetFileExtension() {
        assertEquals("jpg", CityHallAds.getFileExtension("image.jpg"));
        assertEquals("", CityHallAds.getFileExtension("noextension"));
        assertEquals("png", CityHallAds.getFileExtension("file.with.multiple.dots.png"));
    }
}
