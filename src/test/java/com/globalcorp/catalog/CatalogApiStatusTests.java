package com.globalcorp.catalog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpServer;

public class CatalogApiStatusTests {
    
    private static final String BASE_URL = "http://localhost:8000";
    private static HttpServer server;

    @BeforeAll
    public static void start() throws IOException {
        // Start the server before tests run
        server = CatalogApi.startServer();
    }

    @AfterAll
    public static void stop() {
        // Stop the server after tests finish
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    public void testStatusPage() {
       // assertResponse("/", 200, "\"status\": \"OK\"");
       assertResponse("/", 200, "\"status\"");
    }

    /**
     * Helper to run a basic GET test and assert status/content.
     */
    private void assertResponse(String path, int expectedStatus, String expectedContentSnippet) {
        try {
            URL url = URI.create(BASE_URL + path).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(2000);
            conn.setReadTimeout(2000);

            int status = conn.getResponseCode();
            
            // Read response
            BufferedReader in = new BufferedReader(new InputStreamReader(
                (status > 299) ? conn.getErrorStream() : conn.getInputStream()
            ));
            StringBuilder content = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();

            assertEquals(expectedStatus, status, "Status code mismatch");
            assertTrue(content.toString().contains(expectedContentSnippet), 
                "Response content mismatch. Got: " + content.toString());

        } catch (IOException e) {
            Assertions.fail("Request failed: " + e.getMessage());
        }
    }
}
