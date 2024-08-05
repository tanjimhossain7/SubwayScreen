package ca.ucalgary.edu.ensf380;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.json.JSONArray;
import org.json.JSONObject;

public class NewsApp {
    private static final String API_KEY = "your_api_key_here";
    private static final String BASE_URL = "https://api.currentsapi.services/v1/search";

    public static void main(String[] args) {
        System.out.println("Starting application...");
        
        if (args.length == 0) {
            System.out.println("Please provide a search query.");
            return;
        }

        String query = args[0];
        try {
            String[] articles = fetchNews(query);
            displayNews(articles);
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
            e.printStackTrace(); // Print the full stack trace for debugging
        }
    }

    private static String[] fetchNews(String query) throws Exception {
        System.out.println("Fetching news for query: " + query);

        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String urlStr = BASE_URL + "?keywords=" + encodedQuery + "&apiKey=" + API_KEY;

        URI uri = URI.create(urlStr);
        HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
        conn.setRequestMethod("GET");

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new RuntimeException("Failed : HTTP error code : " + responseCode);
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String output;
        while ((output = br.readLine()) != null) {
            response.append(output);
        }

        conn.disconnect();

        JSONObject jsonResponse = new JSONObject(response.toString());
        JSONArray newsArray = jsonResponse.getJSONArray("news");

        String[] articles = new String[newsArray.length()];
        for (int i = 0; i < newsArray.length(); i++) {
            JSONObject article = newsArray.getJSONObject(i);
            articles[i] = String.format("Title: %s\nPublished At: %s\nDescription: %s\nURL: %s\n",
                    article.optString("title", "N/A"),
                    article.optString("published", "N/A"),
                    article.optString("description", "N/A"),
                    article.optString("url", "N/A"));
        }

        return articles;
    }

    private static void displayNews(String[] articles) throws InterruptedException {
        System.out.println("Displaying " + articles.length + " articles");

        for (String article : articles) {
            clearConsole();
            System.out.println(article);
            Thread.sleep(5000); // Wait for 5 seconds before showing the next article
        }
    }

    private static void clearConsole() {
        try {
            final String os = System.getProperty("os.name");
            if (os.contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception e) {
            for (int i = 0; i < 50; i++) System.out.println();
        }
    }
}