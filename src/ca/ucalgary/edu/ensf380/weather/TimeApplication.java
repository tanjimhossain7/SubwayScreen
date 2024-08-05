package weather;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;

public class TimeApplication extends Application {

    private static String timezone = "America/Edmonton";  // Default to Calgary's timezone

    public static void main(String[] args) {
        if (args.length > 0) {
            timezone = args[0];  // Use timezone provided in command line argument
        }
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Label timeLabel = new Label();
        timeLabel.setFont(new Font("Arial", 48));  // Big, bold letters

        StackPane root = new StackPane();
        root.getChildren().add(timeLabel);
        Scene scene = new Scene(root, 500, 300);
        
        primaryStage.setTitle("Time Viewer");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Update the time every second
        Thread timeThread = new Thread(() -> {
            while (true) {
                String currentTime = fetchCurrentTime(timezone);
                javafx.application.Platform.runLater(() -> timeLabel.setText(currentTime));
                try {
                    Thread.sleep(1000);  // Update every second
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        timeThread.setDaemon(true);
        timeThread.start();
    }

    private String fetchCurrentTime(String timezone) {
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
