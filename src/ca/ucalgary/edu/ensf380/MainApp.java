package ca.ucalgary.edu.ensf380;

import java.io.IOException;

public class MainApp {
    public static void main(String[] args) {
        try {
            String weatherData = WeatherFetcher.fetchWeather("Calgary");
            String weatherInfo = WeatherFetcher.parseWeatherData(weatherData);
            System.out.println(weatherInfo);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
