import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

import com.sun.net.httpserver.HttpServer;

/**
 * A simple test suite for the CatalogApi.
 * * Usage:
 * 1. Compile: javac CatalogApiTests.java CatalogApi.java
 * 2. Run:     java CatalogApiTests
 */
public class CatalogApiTests {

    private static final String BASE_URL = "http://localhost:8000";

    public static void main(String[] args) {
        System.out.println("Running CatalogApiTests...");
        HttpServer server = null;
        int passed = 0;
        int failed = 0;

        try {
            // 1. Start the server programmatically
            System.out.println("Starting server for testing...");
            server = CatalogApi.startServer();

            // 2. Run Tests
            
            // Test 1: Root Status Page
            if (runTest("Status Page", "/", 200, "\"status\": \"OK\"")) passed++; else failed++;

            // Test 2: Get Valid Item (Item 1)
            if (runTest("Get Item 1", "/items/1", 200, "\"id\": 1")) passed++; else failed++;

            // Test 3: Get Valid Item (Item 50)
            if (runTest("Get Item 50", "/items/50", 200, "\"id\": 50")) passed++; else failed++;

            // Test 4: Get Invalid Item (Item 999)
            if (runTest("Get Non-Existent Item", "/items/999", 404, "\"error\": \"Item not found\"")) passed++; else failed++;

            // Test 5: Get Items List Warning
            if (runTest("Get Items List", "/items", 200, "Use /items/{id}")) passed++; else failed++;

            // Test 6: Method Not Allowed
            // (We simulate this manually since the helper does GETs)
            if (runMethodNotAllowedTest()) passed++; else failed++;

            // 3. Summary
            System.out.println("\n--------------------------------------------------");
            System.out.println("Test Summary: " + passed + " Passed, " + failed + " Failed.");
            System.out.println("--------------------------------------------------");

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("FATAL: Test suite crashed.");
        } finally {
            // 4. Cleanup
            if (server != null) {
                server.stop(0);
                System.out.println("Server stopped.");
            }
        }
    }

    /**
     * Helper to run a basic GET test.
     */
    private static boolean runTest(String testName, String path, int expectedStatus, String expectedContentSnippet) {
        System.out.printf("TEST: %-25s ", testName);
        try {
            URL url = URI.create(BASE_URL + path).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(2000);
            conn.setReadTimeout(2000);

            int status = conn.getResponseCode();
            
            // Read response (from error stream if status > 299)
            BufferedReader in = new BufferedReader(new InputStreamReader(
                (status > 299) ? conn.getErrorStream() : conn.getInputStream()
            ));
            StringBuilder content = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();

            // Assertions
            if (status != expectedStatus) {
                System.out.println("[FAIL] - Expected Status " + expectedStatus + " but got " + status);
                return false;
            }

            if (!content.toString().contains(expectedContentSnippet)) {
                System.out.println("[FAIL] - Content mismatch. Got: " + content.toString());
                return false;
            }

            System.out.println("[PASS]");
            return true;

        } catch (IOException e) {
            System.out.println("[FAIL] - Exception: " + e.getMessage());
            return false;
        }
    }

    /**
     * Specific test for POST method on GET-only endpoint.
     */
    private static boolean runMethodNotAllowedTest() {
        System.out.printf("TEST: %-25s ", "Method Not Allowed");
        try {
            URL url = URI.create(BASE_URL + "/items/1").toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST"); // Should fail
            
            int status = conn.getResponseCode();
            if (status == 405) {
                System.out.println("[PASS]");
                return true;
            } else {
                System.out.println("[FAIL] - Expected 405 but got " + status);
                return false;
            }
        } catch (IOException e) {
            System.out.println("[FAIL] - Exception: " + e.getMessage());
            return false;
        }
    }
}