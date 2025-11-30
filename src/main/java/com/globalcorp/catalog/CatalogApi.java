package com.globalcorp.catalog;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * A simple, zero-dependency Java HTTP API for an item catalog.
 * Refactored for Java 20+ features (Records, Text Blocks, var).
 * * Usage:
 * 1. Build:   mvn clean package
 * 2. Run:     java -jar target/catalog-api-1.0.0-SNAPSHOT.jar
 * 3. Test:    Open browser or curl to http://localhost:8000/items/1
 */
public class CatalogApi {

    // In-memory database
    private static final Map<Integer, Item> catalog = new HashMap<>();
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
        populateCatalog();

        // 2. Create Server
        var server = HttpServer.create(new InetSocketAddress(PORT), 0);
        
        // 3. Define Contexts (Endpoints)
        server.createContext("/", new StatusHandler());
        server.createContext("/items", new ItemHandler());

        // 4. Start Server
        server.setExecutor(null); 
        server.start();
        return server;
    }

    /**
     * Handler for HTTP requests to /items
     */
    static class ItemHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Only allow GET methods
            if (!"GET".equals(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, """
                    {"error": "Method Not Allowed"}
                """);
                return;
            }

            var path = exchange.getRequestURI().getPath();
            // Expected path: /items/{id}
            var parts = path.split("/");

            // If requesting the list (just /items)
            if (parts.length == 2) {
                sendResponse(exchange, 200, """
                    {"message": "Use /items/{id} to get details for IDs 1-50"}""");
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
                        sendResponse(exchange, 404, """
                            {"error": "Item not found"}""");
                    }
                } catch (NumberFormatException e) {
                    sendResponse(exchange, 400, """
                        {"error": "Invalid ID format"}""");
                }
                return;
            }

            sendResponse(exchange, 400, """
                {"error": "Bad Request"}""");
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

    /**
     * Handler for HTTP requests to the root path /
     */
    static class StatusHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Only allow GET methods
            if (!"GET".equals(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, """
                    {"error": "Method Not Allowed"}""");
                return;
            }

            // Check if the path is exactly "/" (HttpServer maps "/" to all unmapped paths)
            if (!exchange.getRequestURI().getPath().equals("/")) {
                sendResponse(exchange, 404, """
                    {"error": "Not Found"}""");
                return;
            }

            // Get configuration from environment variables
            var version = System.getenv().getOrDefault("APP_VERSION", "0.1");
            var animalEnv = System.getenv().getOrDefault("APP_ANIMAL", "unknown");

            // Process ASCII art for JSON safety
            var rawArt = getAnimalArt(animalEnv);
            var escapedArt = rawArt
                .replace("\\", "\\\\")  // Escape backslashes for JSON
                .replace("\"", "\\\"");  // Escape quotes for JSON
                // .replace("\n", "\\n");  // Escape newlines for JSON

            var response = """
                {
                    "status": "OK",
                    "service": "CatalogApi",
                    "version": "%s",
                    "itemsLoaded": %d,
                    "mascot": "%s"
                }""".formatted(version, catalog.size(), escapedArt);
                
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
    }
    /**
     * Returns the ASCII art for the requested animal.
     */
    private static String getAnimalArt(String animal) {
        return switch (animal.toLowerCase()) {
            case "monkey" -> """
                            __,__
                .--.  .-"     "-.  .--.
                / .. \\/  .-. .-.  \\/ .. \\
                | |  '|  /   Y   \\  |'  | |
                | \\   \\  \\ 0 | 0 /  /   / |
                \\ '- ,\\.-"`` ``"-./, -' /
                `'-' /_   ^ ^   _\\ '-'`
                    |  \\\\._   _./  |
                    \\   \\ `~` /   /
                     '._ '-=-' _.'
                        '~---~'
                """;
            case "canary" -> """
                    .-"-.
                    / 4 4 \\
                    \\_ v _/
                    //   \\      
                ((     ))
            =======""===""=======
                    |||
                    '|'
                """;
            case "dog" -> """
                    /^-^\\
                   / o o \\
                  /   Y   \\
                  V \\ v / V
                    / - \\
                   /    |
             (    /     |
              ===/___) ||
                """;
            case "cat" -> """
            |\\---/|
            | ,_, |
             \\_`_/-..----.
          ___/ `   ' ,""+ \\  
         (__...'   __\\    |`.___.';
           (_,...'(_,.`__)/'.....+
                """;
            case "rabbit" -> """
                    ,
                   /|      __
                  / |   ,-~ /
                 Y :|  //  /
                 | jj /( .^
                 >-"~"-v"
                /       Y
                jo  o    |
               ( ~T~     j
                >._-' _./
              /   "~"  |
             Y     _,  |
            /| ;-"~ _  l
           / l/ ,-"~    \\
           \\//\\/      .- \\
            Y        /    Y
            l       I     !
            ]\\      _\\    /"\\
           (" ~----( ~   Y.  )
                """;
            case "tiger" -> """
                 ('__')
                 ( oo )
                 (_)_)
                """;
            case "dragon" -> """
                                _ ___                /^^\\ /^\\  /^^\\_
                    _          _@)@) \\            ,,/ '` ~ `'~~ ', `\\.
                _/o\\_ _ _ _/~`.`...'~\\        ./~~..,'`','',.,' '  ~:
                / `,'.~,~.~  .   , . , ~|,   ,/ .,' , ,. .. ,,.   `,  ~\\_
                ( ' _' _ '_` _  '  .    , `\\_/ .' ..' '  `  `   `..  `,   \\_
                ~V~ V~ V~ V~ ~\\ `   ' .  '    , ' .,.,''`.,.''`.,.``. ',   \\_
                _/\\ /\\ /\\ /\\_/, . ' ,   `_/~\\_ .' .,. ,, , _/~\\_ `. `. '.,  \\_
                < ~ ~ '~`'~'`, .,  .   `_: ::: \\_ '      `_/ ::: \\_ `.,' . ',  \\_
                \\ ' `_  '`_    _    ',/ _::_::_ \\ _    _/ _::_::_ \\   `.,'.,`., \\-,-,-,_,_,
                `'~~ `'~~ `'~~ `'~~  \\(_)(_)(_)/  `~~' \\(_)(_)(_)/ ~'`\\_.._,._,'_;_;_;_;_;
                """;
            default -> "No mascot selected. Set APP_ANIMAL environment variable.";
        };
    }

    /**
     * Generates 50 mock items.
     */
    private static void populateCatalog() {
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

    record Item(int id, String name, String description, double price) {
        public String toJson() {
            return """
                {
                    "id": %d,
                    "name": "%s",
                    "description": "%s",
                    "price": %.2f
                }""".formatted(id, name, description, price);
        }
    }
}