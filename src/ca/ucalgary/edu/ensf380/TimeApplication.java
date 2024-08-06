package ca.ucalgary.edu.ensf380;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;

/**
 * The TimeApplication class provides methods to get the current time for a specified timezone.
 */
public class TimeApplication {

    /**
     * Gets the current time for the specified timezone.
     * @param timezone the timezone identifier, e.g., "America/Edmonton".
     * @return the current time as a formatted string.
     */
    public static String getCurrentTime(String timezone) {
        try {
            ZoneId zoneId = ZoneId.of(timezone);
            ZonedDateTime zonedDateTime = ZonedDateTime.now(zoneId);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            return zonedDateTime.format(formatter);
        } catch (Exception e) {
            return "Invalid timezone.";
        }
    }
}


