package com.globalcorp.catalog;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.sun.net.httpserver.HttpServer;

/**
 * A simple, zero-dependency Java HTTP API for an item catalog.
 * Refactored for Java 11 compatibility.
 * * Usage:
 * 1. Build:   mvn clean package
 * 2. Run:     java -jar target/catalog-api-1.0.0-SNAPSHOT.jar
 */
public class CatalogApi {

    private static final int PORT = 8000;

    public static void main(String[] args) throws IOException {
        startServer();
        System.out.println("Server started on port " + PORT);
        System.out.println("Try accessing: http://localhost:" + PORT + "/items/5");
    }

    /**
     * Starts the server and returns the instance (useful for testing).
     */
    public static HttpServer startServer() throws IOException {
        // 1. Initialize Data
        Map<Integer, Item> catalog = new HashMap<>();
        populateCatalog(catalog);

        // 2. Create Server
        var server = HttpServer.create(new InetSocketAddress(PORT), 0);
        
        // 3. Define Contexts (Endpoints)
        server.createContext("/", new StatusHandler(catalog));
        server.createContext("/items", new ItemHandler(catalog));

        // 4. Start Server
        server.setExecutor(null); 
        server.start();
        return server;
    }

    /**
     * Generates 50 mock items.
     */
    private static void populateCatalog(Map<Integer, Item> catalog) {
        var adjectives = new String[]{
            "Vintage", "Wireless", "Smart", "Organic", "Ergonomic", 
            "Heavy-Duty", "Portable", "Solar-Powered", "Luxury", "Digital"
        };
        
        var nouns = new String[]{
            "Headphones", "Coffee Maker", "Running Shoes", "Desk Lamp", "Backpack",
            "Wristwatch", "Blender", "Gaming Mouse", "Yoga Mat", "Keyboard"
        };

        var descriptors = new String[]{
            "Perfect for daily use.", "A reliable choice for professionals.", 
            "Limited edition color.", "Top-rated by customers.", "Includes a 2-year warranty."
        };

        var random = new Random();
        var counter = 1;

        // Generate 50 items
        for (int i = 0; i < 5; i++) { 
            for (int j = 0; j < 10; j++) { 
                var adj = adjectives[counter % adjectives.length];
                var noun = nouns[j];
                var name = adj + " " + noun;
                var price = 10.00 + (490.00 * random.nextDouble());
                var desc = descriptors[random.nextInt(descriptors.length)];

                catalog.put(counter, new Item(counter, name, desc, price));
                counter++;
            }
        }
    }
}