package weather;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class WeatherApplication extends Application {

    private static String city = "Calgary";  // Default city, can be changed via command line

    public static void main(String[] args) {
        if (args.length > 0) {
            city = args[0];  // Use city provided in command line argument
        }
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        TextArea weatherInfo = new TextArea();
        weatherInfo.setEditable(false);
        weatherInfo.setStyle("-fx-font-family: monospace; -fx-font-size: 14px; -fx-padding: 10;");

        VBox root = new VBox();
        root.getChildren().add(weatherInfo);
        Scene scene = new Scene(root, 400, 200);
        
        primaryStage.setTitle("Weather Viewer");
        primaryStage.setScene(scene);
        primaryStage.show();

        new Thread(() -> {
            String weather = fetchWeatherData(city);
            javafx.application.Platform.runLater(() -> weatherInfo.setText(weather));
        }).start();
    }

    private String fetchWeatherData(String location) {
        try {
            String formatString = "Weather: %C%n Temperature: %t%nWind: %w%n Humidity: %h%n Precipitation: %p%n Pressure: %P%n";
            String encodedFormat = URLEncoder.encode(formatString, StandardCharsets.UTF_8.toString());

            URL url = new URL("https://wttr.in/" + location + "?format=" + encodedFormat);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setFollowRedirects(true);

            StringBuilder response = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line).append("\n");
                }
            } finally {
                connection.disconnect();
            }
            return response.toString();
        } catch (Exception e) {
            return "Failed to fetch weather data. Error: " + e.getMessage();
        }
    }
}

