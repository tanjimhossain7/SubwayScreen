package ca.ucalgary.edu.ensf380;

import javafx.application.Application;  // Importing Application class for JavaFX applications
import javafx.scene.Scene;  // Importing Scene class to create scenes in JavaFX
import javafx.scene.control.TextArea;  // Importing TextArea class for text display
import javafx.scene.layout.VBox;  // Importing VBox layout for arranging nodes vertically
import javafx.stage.Stage;  // Importing Stage class for the primary stage in JavaFX
import java.io.BufferedReader;  // Importing BufferedReader for reading input streams
import java.io.InputStreamReader;  // Importing InputStreamReader to read bytes and decode them into characters
import java.net.HttpURLConnection;  // Importing HttpURLConnection for HTTP-specific operations
import java.net.URL;  // Importing URL class to handle URLs
import java.net.URLEncoder;  // Importing URLEncoder for encoding URL parameters
import java.nio.charset.StandardCharsets;  // Importing StandardCharsets to handle character encodings

/**
 * WeatherApplication is a JavaFX application that displays weather information for a specified city.
 * This class uses the wttr.in service to fetch weather data and display it in a JavaFX GUI.
 */
public class WeatherApplication extends Application {

    private static String city = "Calgary";  // Default city, can be changed via command line

    /**
     * The main method that launches the JavaFX application.
     * @param args command-line arguments.
     * This method checks for command-line arguments to set the city and launches the JavaFX application.
     */
    public static void main(String[] args) {
        if (args.length > 0) {
            city = args[0];  // Use city provided in command line argument
        }
        launch(args);  // Launch the JavaFX application
    }

    /**
     * Fetches weather data for the specified location.
     * @param location the location to fetch weather data for.
     * @return the weather data as a formatted string.
     * This method sends a request to wttr.in with the specified location and returns the formatted weather data.
     */
    public static String fetchWeatherData(String location) {
        try {
            // Format string for the weather data, specifying the format for each weather component
            String formatString = "Weather: %C%n Temperature: %t%nWind: %w%n Humidity: %h%n Precipitation: %p%n Pressure: %P%n";
            String encodedFormat = URLEncoder.encode(formatString, StandardCharsets.UTF_8.toString());  // Encode the format string for URL usage

            // Construct the full URL with the encoded location and format
            URL url = new URL("https://wttr.in/" + location + "?format=" + encodedFormat);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();  // Open a connection to the URL
            connection.setRequestMethod("GET");  // Set the HTTP request method to GET
            connection.setFollowRedirects(true);  // Allow the connection to follow redirects

            StringBuilder response = new StringBuilder();  // Initialize a StringBuilder to accumulate the response
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {  // Read the input stream from the connection
                String line;
                while ((line = reader.readLine()) != null) {  // Read each line of the response
                    response.append(line).append("\n");  // Append the line to the response
                }
            } finally {
                connection.disconnect();  // Close the connection
            }
            return response.toString();  // Return the accumulated response as a string
        } catch (Exception e) {  // Handle exceptions
            return "Failed to fetch weather data. Error: " + e.getMessage();  // Return an error message if an exception occurs
        }
    }

    /**
     * Starts the JavaFX application and displays the weather information.
     * @param primaryStage the primary stage for this application.
     * This method sets up the JavaFX scene, fetches weather data, and displays it in a text area.
     */
    @Override
    public void start(Stage primaryStage) {
        String weatherData = fetchWeatherData(city);  // Fetch weather data for the specified city

        TextArea textArea = new TextArea(weatherData);  // Create a TextArea to display the weather data
        textArea.setWrapText(true);  // Enable text wrapping in the TextArea

        VBox root = new VBox(textArea);  // Create a VBox layout and add the TextArea to it
        Scene scene = new Scene(root, 400, 300);  // Create a Scene with the VBox layout

        primaryStage.setTitle("Weather Application");  // Set the title of the primary stage
        primaryStage.setScene(scene);  // Set the scene on the primary stage
        primaryStage.show();  // Show the primary stage
    }
}
