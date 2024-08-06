package Newsapp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.json.JSONArray;
import org.json.JSONObject;

public class News {
    private static final String API_KEY = "3BbUZoh-7fAr5m3wSxWbtRB-2yP6mVUD2DptrUWXC2FQcrWV";
    private static final String BASE_URL = "https://api.currentsapi.services/v1/search";

    public static String[] fetchNews(String query) throws Exception {
        System.out.println("Fetching news for query: " + query);

        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String urlStr = BASE_URL + "?keywords=" + encodedQuery + "&apiKey=" + API_KEY;

        URI uri = URI.create(urlStr);
        HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
        conn.setRequestMethod("GET");

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            StringBuilder errorResponse = new StringBuilder();
            String line;
            while ((line = errorReader.readLine()) != null) {
                errorResponse.append(line);
            }
            errorReader.close();
            throw new RuntimeException("Failed : HTTP error code : " + responseCode + "\nError response: " + errorResponse.toString());
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
}
