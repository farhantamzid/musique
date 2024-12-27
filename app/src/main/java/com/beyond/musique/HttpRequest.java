package com.beyond.musique;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Utility class for making HTTP requests.
 */
public class HttpRequest {

    /**
     * Executes a GET request to the specified URL.
     * @param targetURL The URL to send the GET request to.
     * @return The response from the server as a String.
     */
    public static String executeGet(String targetURL) {
        URL url;
        HttpURLConnection connection = null;

        try {
            // Create a URL object from the target URL
            url = new URL(targetURL);
            // Open a connection to the URL
            connection = (HttpURLConnection) url.openConnection();
            // Set the request method to GET
            connection.setRequestMethod("GET");
            // Set the User-Agent property to mimic a web browser
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");

            // Get the response code from the server
            int status = connection.getResponseCode();
            Log.d("API Status", "Response Code: " + status);

            InputStream is;
            // Check if the response code is not OK (200)
            if (status != HttpURLConnection.HTTP_OK) {
                // Get the error stream if the response code is not OK
                is = connection.getErrorStream();
                Log.d("API Error", "Error Response: " + status);
            } else {
                // Get the input stream if the response code is OK
                is = connection.getInputStream();
            }

            // Read the response from the input stream
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuffer response = new StringBuffer();

            // Append each line of the response to the StringBuffer
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            // Return the response as a String
            return response.toString();
        } catch (Exception e) {
            // Print the stack trace if an exception occurs
            e.printStackTrace();
            return null;
        } finally {
            // Disconnect the connection if it is not null
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}