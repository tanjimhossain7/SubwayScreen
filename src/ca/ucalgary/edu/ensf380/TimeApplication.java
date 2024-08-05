package ca.ucalgary.edu.ensf380;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;

public class TimeApplication {

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


