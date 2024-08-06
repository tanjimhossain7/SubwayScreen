package ca.ucalgary.edu.ensf380;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class WeatherApplicationTest {

    @Test
    void testFetchWeatherDataValidCity() {
        String weatherData = WeatherApplication.fetchWeatherData("Calgary");
        assertNotNull(weatherData, "Weather data should not be null");
        assertTrue(weatherData.contains("Weather:"), "Weather data should contain 'Weather:' field");
        assertTrue(weatherData.contains("Temperature:"), "Weather data should contain 'Temperature:' field");
        assertTrue(weatherData.contains("Wind:"), "Weather data should contain 'Wind:' field");
        assertTrue(weatherData.contains("Humidity:"), "Weather data should contain 'Humidity:' field");
        assertTrue(weatherData.contains("Precipitation:"), "Weather data should contain 'Precipitation:' field");
        assertTrue(weatherData.contains("Pressure:"), "Weather data should contain 'Pressure:' field");
    }

    @Test
    void testFetchWeatherDataInvalidCity() {
        String weatherData = WeatherApplication.fetchWeatherData("InvalidCityName");
        assertTrue(weatherData.startsWith("Failed to fetch weather data"), 
                   "Should return error message for invalid city name");
    }

    @Test
    void testFetchWeatherDataEmptyCity() {
        String weatherData = WeatherApplication.fetchWeatherData("");
        assertTrue(weatherData.startsWith("Failed to fetch weather data"), 
                   "Should return error message for empty city name");
    }

    @Test
    void testFetchWeatherDataNullCity() {
        String weatherData = WeatherApplication.fetchWeatherData(null);
        assertTrue(weatherData.startsWith("Failed to fetch weather data"), 
                   "Should return error message for null city name");
    }

    @Test
    void testFetchWeatherDataSpecialCharacters() {
        String weatherData = WeatherApplication.fetchWeatherData("New York");
        assertNotNull(weatherData, "Weather data should not be null for city name with space");
        assertTrue(weatherData.contains("Weather:"), "Weather data should contain 'Weather:' field");
    }
}
