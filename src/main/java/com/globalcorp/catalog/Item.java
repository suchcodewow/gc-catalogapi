package com.globalcorp.catalog;

/**
 * Standard Class (Java 11 Compatible)
 * Represents an item in the catalog.
 */
public class Item {
    private final int id;
    private final String name;
    private final String description;
    private final double price;

    public Item(int id, String name, String description, double price) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
    }

    public String toJson() {
        return String.format(
            "{\n" +
            "    \"id\": %d,\n" +
            "    \"name\": \"%s\",\n" +
            "    \"description\": \"%s\",\n" +
            "    \"price\": %.2f\n" +
            "}", id, name, description, price);
    }
}