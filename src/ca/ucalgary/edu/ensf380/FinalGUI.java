import cityhallads.CityHallAds;
import cityhallads.Advertisement;
import Newsapp.News;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.io.File;

public class FinalGUI {
    private static final int DISPLAY_TIME_INTERVAL = 5000; // 5 seconds
    private static final int DISPLAY_AD_INTERVAL = 10000;  // 10 seconds
    private static final int DISPLAY_NEWS_INTERVAL = 15000; // 15 seconds

    public static void main(String[] args) {
        JFrame frame = new JFrame("Subway Information System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1024, 768);

        JPanel mainPanel = new JPanel(new CardLayout());
        frame.add(mainPanel);

        JLabel displayLabel = new JLabel();
        displayLabel.setHorizontalAlignment(SwingConstants.CENTER);
        displayLabel.setVerticalAlignment(SwingConstants.CENTER);
        mainPanel.add(displayLabel, "Display");

        frame.setVisible(true);

        // Fetch advertisements
        List<Advertisement> ads = CityHallAds.fetchAdvertisements();

        // Fetch news articles (replace with actual query if needed)
        String[] newsArticles = fetchNews("latest");

        int adIndex = 0;
        int newsIndex = 0;

        while (true) {
            try {
                // Display time
                displayTime(displayLabel);
                Thread.sleep(DISPLAY_TIME_INTERVAL);

                // Display advertisement
                if (!ads.isEmpty()) {
                    Advertisement ad = ads.get(adIndex);
                    CityHallAds.displayAdvertisement(displayLabel, ad);
                    adIndex = (adIndex + 1) % ads.size();
                    Thread.sleep(DISPLAY_AD_INTERVAL);
                }

                // Display map
                displayMap(displayLabel);
                Thread.sleep(DISPLAY_AD_INTERVAL);

                // Display news
                if (newsArticles.length > 0) {
                    displayNews(displayLabel, newsArticles[newsIndex]);
                    newsIndex = (newsIndex + 1) % newsArticles.length;
                    Thread.sleep(DISPLAY_NEWS_INTERVAL);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void displayTime(JLabel label) {
        String currentTime = new SimpleDateFormat("HH:mm:ss").format(new Date());
        label.setText("<html><h1>Current Time</h1><h2>" + currentTime + "</h2></html>");
    }

    private static String[] fetchNews(String query) {
        try {
            return News.fetchNews(query);
        } catch (Exception e) {
            e.printStackTrace();
            return new String[0];
        }
    }

    private static void displayNews(JLabel label, String news) {
        label.setText("<html><h1>Latest News</h1><p>" + news.replace("\n", "<br>") + "</p></html>");
    }

    private static void displayMap(JLabel label) {
        List<File> mapImages = CityHallAds.getMapImages();
        if (!mapImages.isEmpty()) {
            File randomImage = mapImages.get((int) (Math.random() * mapImages.size()));
            label.setIcon(new ImageIcon(randomImage.getPath()));
            label.setText("Train Positions");
        } else {
            label.setIcon(null);
            label.setText("Train Positions: No map images found");
        }
    }
}
