package ca.ucalgary.edu.ensf380;

import javax.swing.*;  // Importing Swing components for GUI elements
import java.awt.*;  // Importing AWT classes for graphical components
import java.awt.image.BufferedImage;  // Importing BufferedImage for image handling
import java.io.BufferedReader;  // Importing BufferedReader for reading input streams
import java.io.File;  // Importing File class to handle file operations
import java.io.IOException;  // Importing IOException to handle input/output exceptions
import java.io.InputStreamReader;  // Importing InputStreamReader to read bytes and decode them into characters
import java.net.HttpURLConnection;  // Importing HttpURLConnection for HTTP-specific operations
import java.net.URL;  // Importing URL class to handle URLs
import java.net.URLEncoder;  // Importing URLEncoder for encoding URL parameters
import java.nio.charset.StandardCharsets;  // Importing StandardCharsets to handle character encodings
import java.util.List;  // Importing List interface for list operations
import java.util.Timer;  // Importing Timer for scheduling tasks
import java.util.TimerTask;  // Importing TimerTask to represent tasks scheduled by Timer
import javax.imageio.ImageIO;  // Importing ImageIO for reading and writing image files

/**
 * FinalGUI is the main class for displaying subway information, including advertisements, weather, and train status.
 * This class sets up a GUI that displays real-time subway information and interacts with other classes to fetch data.
 */
public class FinalGUI {
    private static final String TARGET_TRAIN = "1";  // The train number to track and display information for
    private static TrainStationManager stationManager;  // An instance of TrainStationManager to handle train data

    /**
     * The main method to start the GUI and simulator.
     * @param args command-line arguments.
     * This method initializes the GUI and starts the subway simulator.
     */
    public static void main(String[] args) {
        startSimulator();  // Start the subway simulator

        SwingUtilities.invokeLater(() -> {  // Create and show the GUI on the event dispatch thread
            JFrame frame = new JFrame("Final GUI");  // Create a JFrame to hold the GUI components
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  // Set the default close operation to exit the application
            frame.setSize(1200, 800);  // Set the size of the JFrame
            frame.setLayout(new BorderLayout());  // Set the layout manager to BorderLayout

            JPanel topPanel = new JPanel(new GridLayout(1, 2));  // Create a top panel with a grid layout
            JPanel middlePanel = new JPanel(new BorderLayout());  // Create a middle panel with a border layout
            JPanel bottomPanel = new JPanel(new BorderLayout());  // Create a bottom panel with a border layout

            frame.add(topPanel, BorderLayout.NORTH);  // Add the top panel to the north region of the JFrame
            frame.add(middlePanel, BorderLayout.CENTER);  // Add the middle panel to the center region of the JFrame
            frame.add(bottomPanel, BorderLayout.SOUTH);  // Add the bottom panel to the south region of the JFrame

            JPanel adsMapPanel = new JPanel(new BorderLayout());  // Create a panel for displaying ads and maps
            JLabel adLabel = new JLabel("", SwingConstants.CENTER);  // Create a JLabel for displaying advertisements
            adLabel.setFont(new Font("Serif", Font.BOLD, 24));  // Set the font for the ad label
            adsMapPanel.add(adLabel, BorderLayout.NORTH);  // Add the ad label to the north region of the ads/map panel
            JLabel mapLabel = new JLabel("", SwingConstants.CENTER);  // Create a JLabel for displaying the map
            adsMapPanel.add(mapLabel, BorderLayout.CENTER);  // Add the map label to the center region of the ads/map panel
            topPanel.add(adsMapPanel);  // Add the ads/map panel to the top panel

            JPanel weatherTimePanel = new JPanel(new GridLayout(2, 1));  // Create a panel for displaying weather and time information
            JLabel timeLabel = new JLabel("Loading time...", SwingConstants.CENTER);  // Create a JLabel for displaying the current time
            timeLabel.setFont(new Font("Serif", Font.BOLD, 24));  // Set the font for the time label
            weatherTimePanel.add(timeLabel);  // Add the time label to the weather/time panel
            JLabel weatherLabel = new JLabel("Loading weather...", SwingConstants.CENTER);  // Create a JLabel for displaying the weather
            weatherLabel.setFont(new Font("Serif", Font.BOLD, 18));  // Set the font for the weather label
            weatherTimePanel.add(weatherLabel);  // Add the weather label to the weather/time panel
            topPanel.add(weatherTimePanel);  // Add the weather/time panel to the top panel

            JPanel newsPanel = new JPanel(new BorderLayout());  // Create a panel for displaying news
            JLabel newsLabel = new JLabel("Loading news...", SwingConstants.CENTER);  // Create a JLabel for displaying news headlines
            newsLabel.setFont(new Font("Serif", Font.PLAIN, 16));  // Set the font for the news label
            newsPanel.add(newsLabel, BorderLayout.CENTER);  // Add the news label to the center region of the news panel
            middlePanel.add(newsPanel);  // Add the news panel to the middle panel

            JPanel trainInfoPanel = new JPanel(new GridLayout(1, 1));  // Create a panel for displaying train information
            JLabel trainInfoLabel = new JLabel("Loading train data...", SwingConstants.CENTER);  // Create a JLabel for displaying train information
            trainInfoLabel.setFont(new Font("Serif", Font.PLAIN, 18));  // Set the font for the train info label
            trainInfoPanel.add(trainInfoLabel);  // Add the train info label to the train info panel
            bottomPanel.add(trainInfoPanel, BorderLayout.NORTH);  // Add the train info panel to the north region of the bottom panel

            JPanel announcementPanel = new JPanel(new BorderLayout());  // Create a panel for displaying announcements
            JLabel announcementLabel = new JLabel("Next Stop: Loading...", SwingConstants.CENTER);  // Create a JLabel for displaying the next stop
            announcementLabel.setFont(new Font("Serif", Font.BOLD, 18));  // Set the font for the announcement label
            announcementPanel.add(announcementLabel, BorderLayout.CENTER);  // Add the announcement label to the center region of the announcement panel
            bottomPanel.add(announcementPanel, BorderLayout.SOUTH);  // Add the announcement panel to the south region of the bottom panel

            frame.setVisible(true);  // Make the JFrame visible

            updateTime(timeLabel);  // Start updating the time label
            updateWeather(weatherLabel);  // Start updating the weather label
            startAdDisplay(adLabel, mapLabel, trainInfoLabel, announcementLabel);  // Start displaying ads and train information
        });
    }

    /**
     * Updates the weather information on the provided label at regular intervals.
     * @param weatherLabel the JLabel where weather information will be displayed.
     * This method fetches weather data periodically and updates the weather label with the latest information.
     */
    private static void updateWeather(JLabel weatherLabel) {
        Timer weatherTimer = new Timer();  // Create a new Timer to schedule tasks
        weatherTimer.scheduleAtFixedRate(new TimerTask() {  // Schedule a task to run at fixed intervals
            @Override
            public void run() {
                String weatherData = fetchWeatherData("Calgary");  // Fetch the weather data for Calgary
                SwingUtilities.invokeLater(() -> weatherLabel.setText("<html>" + weatherData.replace("\n", "<br>") + "</html>"));  // Update the weather label on the event dispatch thread
            }
        }, 0, 3600000);  // Refresh the weather information every hour
    }

    /**
     * Fetches weather data for the specified location.
     * @param location the location to fetch weather data for.
     * @return the weather data as a formatted string.
     * This method interacts with the wttr.in service to retrieve weather data and return it in a formatted string.
     */
    private static String fetchWeatherData(String location) {
        try {
            // Format string for the weather data, specifying the format for each weather component
            String formatString = "Weather: %C | Temperature: %t | Wind: %w | Humidity: %h | Precipitation: %p | Pressure: %P";
            String encodedFormat = URLEncoder.encode(formatString, StandardCharsets.UTF_8.toString());  // Encode the format string for URL usage
            URL url = new URL("https://wttr.in/" + location + "?format=" + encodedFormat);  // Construct the full URL with the encoded location and format
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();  // Open a connection to the URL
            connection.setRequestMethod("GET");  // Set the HTTP request method to GET
            HttpURLConnection.setFollowRedirects(true);  // Allow the connection to follow redirects

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
            e.printStackTrace();  // Print the stack trace for debugging
            return "Failed to fetch weather data. Error: " + e.getMessage();  // Return an error message if an exception occurs
        }
    }

    /**
     * Starts the subway simulator.
     * This method runs the subway simulator as a separate process.
     */
    private static void startSimulator() {
        try {
            ProcessBuilder pb = new ProcessBuilder("java", "-jar", "D:\\SubwayScreen\\exe\\SubwaySimulator.jar");  // Set up a process builder to run the simulator JAR file
            pb.start();  // Start the simulator process
        } catch (IOException e) {  // Handle exceptions related to process I/O
            e.printStackTrace();  // Print the stack trace for debugging
        }
    }

    /**
     * Updates the time displayed on the provided label at regular intervals.
     * @param timeLabel the JLabel where the current time will be displayed.
     * This method periodically fetches the current time and updates the time label.
     */
    private static void updateTime(JLabel timeLabel) {
        Timer timer = new Timer();  // Create a new Timer to schedule tasks
        timer.scheduleAtFixedRate(new TimerTask() {  // Schedule a task to run at fixed intervals
            @Override
            public void run() {
                String currentTime = TimeApplication.getCurrentTime("America/Edmonton");  // Fetch the current time for the specified time zone
                SwingUtilities.invokeLater(() -> timeLabel.setText("Current Time: " + currentTime));  // Update the time label on the event dispatch thread
            }
        }, 0, 1000);  // Update the time label every second
    }

    /**
     * Starts the advertisement display and alternates between displaying ads and maps.
     * @param adLabel the JLabel where ads will be displayed.
     * @param mapLabel the JLabel where maps will be displayed.
     * @param trainInfoLabel the JLabel where train information will be displayed.
     * @param announcementLabel the JLabel where announcements will be displayed.
     * This method sets up a timer to alternate between displaying advertisements and train information.
     */
    private static void startAdDisplay(JLabel adLabel, JLabel mapLabel, JLabel trainInfoLabel, JLabel announcementLabel) {
        stationManager = new TrainStationManager();  // Initialize the TrainStationManager to handle train data
        List<Advertisement> ads = CityHallAds.fetchAdvertisements();  // Fetch the list of advertisements from the database

        Timer adTimer = new Timer();  // Create a new Timer to schedule tasks
        adTimer.scheduleAtFixedRate(new TimerTask() {  // Schedule a task to run at fixed intervals
            int adIndex = 0;  // Initialize the index for the current ad
            boolean showingAd = true;  // Flag to toggle between showing ads and maps

            @Override
            public void run() {
                if (showingAd) {  // If currently showing ads
                    if (ads != null && !ads.isEmpty()) {  // If there are ads to display
                        displayAdvertisement(adLabel, ads.get(adIndex));  // Display the current ad
                        adIndex = (adIndex + 1) % ads.size();  // Move to the next ad, cycling back to the first ad if at the end
                    }
                } else {  // If currently showing maps
                    displayMapAndTrainInfo(mapLabel, trainInfoLabel, announcementLabel);  // Display the map and train information
                }
                showingAd = !showingAd;  // Toggle between showing ads and maps
            }
        }, 0, 5000);  // Switch between ads and maps every 5 seconds
    }

    /**
     * Displays a randomly selected advertisement from the Ads folder.
     * @param label the JLabel where the advertisement will be displayed.
     * @param ad the Advertisement object containing details about the ad.
     * This method selects an ad from the Ads folder and displays it on the provided label.
     */
    private static void displayAdvertisement(JLabel label, Advertisement ad) {
        String adsFolderPath = "C:/Users/saimk/OneDrive/Desktop/SubwayScreen/src/ca/ucalgary/edu/ensf380/Ads";  // Define the path to the Ads folder
        File adsFolder = new File(adsFolderPath);  // Create a File object for the Ads folder

        if (!adsFolder.exists() || !adsFolder.isDirectory()) {  // Check if the folder exists and is a directory
            label.setText("Ads folder not found: " + adsFolderPath);  // Display an error message if the folder is not found
            return;
        }

        // Filter to select only image files (JPEG, JPG, PNG, BMP)
        File[] adFiles = adsFolder.listFiles((dir, name) -> {
            String lowerName = name.toLowerCase();
            return lowerName.endsWith(".jpeg") || lowerName.endsWith(".jpg") || lowerName.endsWith(".png") || lowerName.endsWith(".bmp");
        });

        if (adFiles == null || adFiles.length == 0) {  // Check if any advertisement files were found
            label.setText("No advertisement images found in: " + adsFolderPath);  // Display an error message if no files were found
            return;
        }

        // Randomly select an advertisement image from the folder
        File file = adFiles[(int) (Math.random() * adFiles.length)];

        try {
            BufferedImage img = ImageIO.read(file);  // Read the selected image file
            if (img != null) {
                label.setIcon(new ImageIcon(img));  // Set the image icon of the JLabel to the advertisement image
                label.setText("");  // Clear any previous error message
            } else {
                label.setText("Unsupported or corrupted image file: " + file.getPath());  // Display an error message if the image is unsupported or corrupted
            }
        } catch (IOException e) {  // Handle exceptions related to file I/O
            e.printStackTrace();  // Print the stack trace for debugging
            label.setText("Failed to load advertisement image.");  // Display an error message if the image cannot be loaded
        }
    }

    /**
     * Displays the map and train information on the provided labels.
     * @param mapLabel the JLabel where the map will be displayed.
     * @param trainInfoLabel the JLabel where train information will be displayed.
     * @param announcementLabel the JLabel where announcements will be displayed.
     * This method draws the map with train locations and updates the train information and announcements.
     */
    private static void displayMapAndTrainInfo(JLabel mapLabel, JLabel trainInfoLabel, JLabel announcementLabel) {
        BufferedImage mapImage;
        try {
            mapImage = ImageIO.read(new File("D:\\Map.csv"));  // Load the map image from a file
        } catch (IOException e) {
            e.printStackTrace();  // Print the stack trace for debugging
            mapLabel.setText("Failed to load map image.");  // Display an error message if the map cannot be loaded
            return;
        }

        Graphics2D g2d = mapImage.createGraphics();  // Create a Graphics2D object to draw on the map image
        g2d.setColor(Color.GRAY);  // Set the drawing color to gray

        for (TrainStationManager.TrainData data : stationManager.getAllTrainData()) {  // Iterate over each train data object
            int x = (int) data.getStation().getX();  // Get the x-coordinate of the train's station
            int y = (int) data.getStation().getY();  // Get the y-coordinate of the train's station
            g2d.fillOval(x, y, 10, 10);  // Draw a filled oval at the train's location

            if (TARGET_TRAIN.equals(data.getTrainNumber())) {  // If the train is the target train
                g2d.setColor(Color.GREEN);  // Set the drawing color to green
                g2d.fillOval(x, y, 10, 10);  // Draw a filled oval at the train's location in green
                g2d.setColor(Color.GRAY);  // Reset the drawing color to gray
            }
        }

        g2d.dispose();  // Dispose of the Graphics2D object
        mapLabel.setIcon(new ImageIcon(mapImage));  // Set the map image as the icon of the map label

        TrainStationManager.TrainData targetData = stationManager.getTrainData(TARGET_TRAIN);  // Get the data for the target train
        if (targetData != null) {  // If data for the target train is available
            List<String> nextStations = targetData.getNextStations();  // Get the list of next stations for the train
            StringBuilder infoText = new StringBuilder("<html>");  // Initialize a StringBuilder to create HTML-formatted text
            infoText.append("Prev: ").append(nextStations.get(0)).append(" ");  // Add the previous station to the text
            infoText.append("Current: <span style='background-color:").append(getLineColorHex(targetData.getLineColor())).append(";'>")
                    .append(targetData.getStation().getStationName()).append("</span> ");  // Add the current station to the text with line color background
            infoText.append("Next: ").append(nextStations.get(1)).append(" ");  // Add the next station to the text
            infoText.append("Second: ").append(nextStations.get(2)).append(" ");  // Add the second next station to the text
            infoText.append("Third: ").append(nextStations.get(3)).append("</html>");  // Add the third next station to the text

            trainInfoLabel.setText(infoText.toString());  // Set the train information label to the formatted text
            announcementLabel.setText("Next Stop: " + nextStations.get(1));  // Update the announcement label with the next stop
        }
    }

    /**
     * Returns the hex color code for the specified line color.
     * @param lineColor the color code of the line.
     * @return the hex color code as a string.
     * This method converts a line color code to its corresponding hex color code.
     */
    private static String getLineColorHex(String lineColor) {
        switch (lineColor) {
            case "R":
                return "#FF0000";  // Red
            case "B":
                return "#0000FF";  // Blue
            case "G":
                return "#00FF00";  // Green
            default:
                return "#000000";  // Black
        }
    }
}


