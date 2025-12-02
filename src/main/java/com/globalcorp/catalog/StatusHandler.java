package com.globalcorp.catalog;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * Handler for HTTP requests to the root path /
 */
public class StatusHandler implements HttpHandler {

    private final Map<Integer, Item> catalog;

    public StatusHandler(Map<Integer, Item> catalog) {
        this.catalog = catalog;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Only allow GET methods
        if (!"GET".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "{\"error\": \"Method Not Allowed\"}");
            return;
        }

        // Check if the path is exactly "/" (HttpServer maps "/" to all unmapped paths)
        if (!exchange.getRequestURI().getPath().equals("/")) {
            sendResponse(exchange, 404, "{\"error\": \"Not Found\"}");
            return;
        }

        // Get configuration from environment variables
        var version = System.getenv().getOrDefault("APP_VERSION", "2.5");
        var animalEnv = System.getenv().getOrDefault("APP_ANIMAL", "unknown");

        // Process ASCII art for JSON safety
        var rawArt = getAnimalArt(animalEnv);
        var escapedArt = rawArt
            .replace("\\", "\\\\")  // Escape backslashes for JSON
            .replace("\"", "\\\"")  // Escape quotes for JSON
            .replace("\n", "\\n");  // Escape newlines for JSON

        var response = String.format(
            "{\n" +
            "    \"status\": \"RDY\",\n" +
            "    \"service\": \"CatalogApi\",\n" +
            "    \"version\": \"%s\",\n" +
            "    \"itemsLoaded\": %d,\n" +
            "    \"mascot\": \"%s\"\n" +
            "}", version, catalog.size(), escapedArt);
            
        sendResponse(exchange, 200, response);
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        var responseBytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (var os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    /**
     * Returns the ASCII art for the requested animal.
     */
    private String getAnimalArt(String animal) {
        switch (animal.toLowerCase()) {
            case "monkey":
                return "  __\n" +
                       " /  \\\n" +
                       "|  o o |\n" +
                       " \\_^_/\n";
            case "canary":
                return "   (\n" +
                       "  ( )\n" +
                       " /   \\\n" +
                       "(___ _)\n";
            case "dog":
                return "   __\n" +
                       " /    \\\n" +
                       "/ ..|\\ \\\n" +
                       "(_\\_|_ )\n";
            case "cat":
                return " /\\_/\\\n" +
                       "( o.o )\n" +
                       " > ^ <\n";
            case "mouse":
                return " _  _\n" +
                       "(o)(o)\n" +
                       " \\../\n";
            case "tiger":
                return " ('__')\n" +
                       " ( oo )\n" +
                       " (_)_) \n";
            case "dragon":
                return "        \\\n" +
                       "       (o>\n" +
                       "   \\\\  //\\\n" +
                       "    \\\\V_/_\n";
            default:
                return "No mascot selected. Set APP_ANIMAL environment variable.";
        }
    }
}