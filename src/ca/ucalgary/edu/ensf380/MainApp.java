package ca.ucalgary.edu.ensf380;

import java.io.IOException;

public class MainApp {
    public static void main(String[] args) {
        // Fetch and display weather data
        try {
            String weatherData = WeatherFetcher.fetchWeather("Calgary");
            String weatherInfo = WeatherFetcher.parseWeatherData(weatherData);
            System.out.println("Weather Information:");
            System.out.println(weatherInfo);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Fetch and display news data
        try {
            String newsData = NewsFetcher.fetchNews();
            String newsInfo = NewsFetcher.parseNewsData(newsData);
            System.out.println("\nNews Information:");
            System.out.println(newsInfo);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
