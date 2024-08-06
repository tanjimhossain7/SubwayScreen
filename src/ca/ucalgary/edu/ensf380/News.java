package ca.ucalgary.edu.ensf380;

import java.io.BufferedReader;  // Importing BufferedReader for reading input streams
import java.io.InputStreamReader;  // Importing InputStreamReader to read bytes and decode them into characters
import java.net.HttpURLConnection;  // Importing HttpURLConnection for HTTP-specific operations
import java.net.URI;  // Importing URI class to handle Uniform Resource Identifiers
import java.net.URLEncoder;  // Importing URLEncoder for encoding URL parameters
import java.nio.charset.StandardCharsets;  // Importing StandardCharsets to handle character encodings
import org.json.JSONArray;  // Importing JSONArray to handle JSON arrays
import org.json.JSONObject;  // Importing JSONObject to handle JSON objects

/**
 * The News class provides methods to fetch news articles from an online API.
 * This class interacts with a third-party API to retrieve news articles based on a given search query.
 */
public class News {
    // API key for authentication with the news API
    private static final String API_KEY = "3BbUZoh-7fAr5m3wSxWbtRB-2yP6mVUD2DptrUWXC2FQcrWV";
    // Base URL for the news API
    private static final String BASE_URL = "https://api.currentsapi.services/v1/search";

    /**
     * Fetches news articles based on the specified query.
     * @param query the search query for fetching news.
     * @return an array of news articles in string format.
     * @throws Exception if an error occurs while fetching the news.
     * This method sends a request to the API with the search query, retrieves the response, and parses the news articles.
     */
    public static String[] fetchNews(String query) throws Exception {
        System.out.println("Fetching news for query: " + query);  // Log the search query

        // Encode the search query for URL usage
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        // Construct the full URL with the encoded query and API key
        String urlStr = BASE_URL + "?keywords=" + encodedQuery + "&apiKey=" + API_KEY;

        URI uri = URI.create(urlStr);  // Create a URI from the constructed URL
        HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();  // Open a connection to the URL
        conn.setRequestMethod("GET");  // Set the HTTP request method to GET

        int responseCode = conn.getResponseCode();  // Get the HTTP response code
        if (responseCode != 200) {  // Check if the response code indicates an error
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));  // Read the error stream
            StringBuilder errorResponse = new StringBuilder();  // Initialize a StringBuilder to accumulate the error message
            String line;
            while ((line = errorReader.readLine()) != null) {  // Read each line of the error response
                errorResponse.append(line);  // Append the line to the error response
            }
            errorReader.close();  // Close the BufferedReader
            throw new RuntimeException("Failed : HTTP error code : " + responseCode + "\nError response: " + errorResponse.toString());  // Throw an exception with the error message
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));  // Read the input stream from the connection
        StringBuilder response = new StringBuilder();  // Initialize a StringBuilder to accumulate the response
        String output;
        while ((output = br.readLine()) != null) {  // Read each line of the response
            response.append(output);  // Append the line to the response
        }

        conn.disconnect();  // Close the connection

        JSONObject jsonResponse = new JSONObject(response.toString());  // Parse the response into a JSONObject
        JSONArray newsArray = jsonResponse.getJSONArray("news");  // Extract the news array from the JSON response
        
        // Initialize an array to store the news articles as strings
        String[] articles = new String[newsArray.length()];
        for (int i = 0; i < newsArray.length(); i++) {  // Iterate over each news article in the array
            JSONObject article = newsArray.getJSONObject(i);  // Get the current news article as a JSONObject
            articles[i] = String.format("Title: %s\nPublished At: %s\nDescription: %s\nURL: %s\n",
                    article.optString("title", "N/A"),  // Get the title of the article, or "N/A" if not available
                    article.optString("published", "N/A"),  // Get the publication date, or "N/A" if not available
                    article.optString("description", "N/A"),  // Get the description of the article, or "N/A" if not available
                    article.optString("url", "N/A"));  // Get the URL of the article, or "N/A" if not available
        }

        return articles;  // Return the array of news articles
    }
}
