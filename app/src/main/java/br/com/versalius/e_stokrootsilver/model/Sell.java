package br.com.versalius.e_stokrootsilver.model;

import java.io.Serializable;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Giovanne on 17/10/2017.
 */

public class Sell implements Serializable {
    private long id;
    private List<Product> products;
    private String client;
    private String date;

    public Sell(long id, List<Product> products, String client, String date) {
        this.id = id;
        this.products = products;
        this.client = client;
        this.date = date;
    }

    public long getId() {
        return id;
    }

    public List<Product> getProducts() {
        return products;
    }

    public String getClient() {
        return client;
    }

    public String getDate() {
        return date;
    }

    public double getTotalPrice(){
        double total = 0;
        if(products != null && !products.isEmpty()){
            for (Product p: products) {
                total += p.getPrice();
            }
        }
        return total;
    }
}
