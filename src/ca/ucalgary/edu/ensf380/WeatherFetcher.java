package ca.ucalgary.edu.ensf380;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import org.json.JSONObject;

public class WeatherFetcher {
    private static final String API_KEY = "your_api_key";
    private static final String BASE_URL = "http://api.openweathermap.org/data/2.5/weather?q=";

    public static String fetchWeather(String city) throws IOException {
        String urlString = BASE_URL + city + "&appid=" + API_KEY;
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            Scanner scanner = new Scanner(url.openStream());
            StringBuilder inline = new StringBuilder();
            while (scanner.hasNext()) {
                inline.append(scanner.nextLine());
            }
            scanner.close();
            return inline.toString();
        } else {
            throw new IOException("Error fetching weather data: " + responseCode);
        }
    }

    public static String parseWeatherData(String jsonData) {
        JSONObject obj = new JSONObject(jsonData);
        JSONObject main = obj.getJSONObject("main");
        double temp = main.getDouble("temp") - 273.15; // Convert Kelvin to Celsius
        return "Temperature: " + String.format("%.2f", temp) + "°C";
    }
}
