package ca.ucalgary.edu.ensf380;

import org.junit.Test;
import static org.junit.Assert.*;

public class AdvertisementTest {

    @Test
    public void testConstructorAndGetters() {
        Advertisement ad = new Advertisement(1, "Test Ad", "Description", "test.jpg", "jpg", "/path/to/test.jpg");
        
        assertEquals(1, ad.getId());
        assertEquals("Test Ad", ad.getTitle());
        assertEquals("Description", ad.getDescription());
        assertEquals("test.jpg", ad.getFileName());
        assertEquals("jpg", ad.getFileType());
        assertEquals("/path/to/test.jpg", ad.getFilePath());
    }

    @Test
    public void testSetters() {
        Advertisement ad = new Advertisement(1, "Test Ad", "Description", "test.jpg", "jpg", "/path/to/test.jpg");
        
        ad.setId(2);
        ad.setTitle("Updated Ad");
        ad.setDescription("New Description");
        ad.setFileName("new.png");
        ad.setFileType("png");
        ad.setFilePath("/new/path/new.png");
        
        assertEquals(2, ad.getId());
        assertEquals("Updated Ad", ad.getTitle());
        assertEquals("New Description", ad.getDescription());
        assertEquals("new.png", ad.getFileName());
        assertEquals("png", ad.getFileType());
        assertEquals("/new/path/new.png", ad.getFilePath());
    }
}
