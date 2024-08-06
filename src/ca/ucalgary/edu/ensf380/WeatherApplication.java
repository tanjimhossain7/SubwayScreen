package ca.ucalgary.edu.ensf380;

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

/**
 * WeatherApplication is a JavaFX application that displays weather information for a specified city.
 */
public class WeatherApplication extends Application {

    private static String city = "Calgary";  // Default city, can be changed via command line

    /**
     * The main method that launches the JavaFX application.
     * @param args command-line arguments.
     */
    public static void main(String[] args) {
        if (args.length > 0) {
            city = args[0];  // Use city provided in command line argument
        }
        launch(args);
    }

    /**
     * Fetches weather data for the specified location.
     * @param location the location to fetch weather data for.
     * @return the weather data as a formatted string.
     */
    public static String fetchWeatherData(String location) {
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

    /**
     * Starts the JavaFX application and displays the weather information.
     * @param primaryStage the primary stage for this application.
     */
    @Override
    public void start(Stage primaryStage) {
        String weatherData = fetchWeatherData(city);

        TextArea textArea = new TextArea(weatherData);
        textArea.setWrapText(true);

        VBox root = new VBox(textArea);
        Scene scene = new Scene(root, 400, 300);

        primaryStage.setTitle("Weather Application");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}

