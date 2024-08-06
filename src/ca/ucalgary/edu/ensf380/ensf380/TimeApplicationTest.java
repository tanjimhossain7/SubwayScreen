package ca.ucalgary.edu.ensf380;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

class TimeApplicationTest {

    @Test
    void testGetCurrentTimeValidTimezone() {
        String time = TimeApplication.getCurrentTime("America/New_York");
        assertNotNull(time, "Time should not be null");
        assertTrue(time.matches("\\d{2}:\\d{2}:\\d{2}"), "Time should be in HH:mm:ss format");
    }

    @Test
    void testGetCurrentTimeInvalidTimezone() {
        String time = TimeApplication.getCurrentTime("Invalid/Timezone");
        assertEquals("Invalid timezone.", time, "Should return error message for invalid timezone");
    }

    @Test
    void testGetCurrentTimeAccuracy() {
        String actualTime = TimeApplication.getCurrentTime("UTC");
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));
        String expectedTime = now.format(DateTimeFormatter.ofPattern("HH:mm"));
        
        assertTrue(actualTime.startsWith(expectedTime), 
                   "The returned time should be close to the current time (allowing for a few seconds difference)");
    }
}
