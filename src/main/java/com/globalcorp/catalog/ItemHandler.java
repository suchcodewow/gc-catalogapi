package com.globalcorp.catalog;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * Handler for HTTP requests to /items
 */
public class ItemHandler implements HttpHandler {

    private final Map<Integer, Item> catalog;

    public ItemHandler(Map<Integer, Item> catalog) {
        this.catalog = catalog;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Only allow GET methods
        if (!"GET".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "{\"error\": \"Method Not Allowed\"}");
            return;
        }

        var path = exchange.getRequestURI().getPath();
        // Expected path: /items/{id}
        var parts = path.split("/");

        // If requesting the list (just /items)
        if (parts.length == 2) {
            sendResponse(exchange, 200, "{\"message\": \"Use /items/{id} to get details for IDs 1-50\"}");
            return;
        }

        // If requesting a specific ID
        if (parts.length == 3) {
            try {
                var id = Integer.parseInt(parts[2]);
                if (catalog.containsKey(id)) {
                    var item = catalog.get(id);
                    sendResponse(exchange, 200, item.toJson());
                } else {
                    sendResponse(exchange, 404, "{\"error\": \"Item not found\"}");
                }
            } catch (NumberFormatException e) {
                sendResponse(exchange, 400, "{\"error\": \"Invalid ID format\"}");
            }
            return;
        }

        sendResponse(exchange, 400, "{\"error\": \"Bad Request\"}");
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        var responseBytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (var os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
}