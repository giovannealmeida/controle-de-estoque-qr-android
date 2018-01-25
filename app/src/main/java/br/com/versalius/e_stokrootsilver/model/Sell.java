package br.com.versalius.e_stokrootsilver.model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Giovanne on 17/10/2017.
 */

public class Sell implements Serializable {
    private long id;
    private List<Product> products;
    private String client;
    private Calendar date;

    public Sell(long id, List<Product> products, String client, Calendar date) {
        this.id = id;
        this.products = products;
        this.client = client;
        this.date = date;
    }

    public Sell() {

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

    public Calendar getDate() {
        return date;
    }

    public String getFormattedDate() {
        return new SimpleDateFormat("dd/MM/yyyy").format(date.getTime());
    }

    public double getTotalPrice(){
        double total = 0;
        if(products != null && !products.isEmpty()){
            for (Product p: products) {
                total += p.getRetailPrice();
            }
        }
        return total;
    }

    public void addProduct (Product product){
        if(this.products == null){
            products = new ArrayList<>();
        }
        this.products.add(product);
    }

    public void setDate(Calendar date) {
        this.date = date;
    }

    public void setClient(String client) {
        this.client = client;
    }
}
