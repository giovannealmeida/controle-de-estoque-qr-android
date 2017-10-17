package br.com.versalius.e_stokrootsilver.model;

/**
 * Created by Giovanne on 17/10/2017.
 */

public class Product {
    private int id;
    private String name;
    private String weight;
    private double price;

    public Product(int id, String name, String weight, double price) {
        this.id = id;
        this.name = name;
        this.weight = weight;
        this.price = price;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getWeight() {
        return weight;
    }

    public double getPrice() {
        return price;
    }
}
