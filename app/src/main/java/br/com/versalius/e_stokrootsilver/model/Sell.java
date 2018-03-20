package br.com.versalius.e_stokrootsilver.model;

import java.io.Serializable;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Created by Giovanne on 17/10/2017.
 */

public class Sell implements Serializable {
    public final static int TYPE_WHOLESALE = 1;
    public final static int TYPE_RETAIL = 2;
    public final static int TYPE_WHOLESALE_AND_RETAIL = 3;

    private long id;
    private List<Product> products;
    private String client;
    private Calendar date;
    private int saleType;

    public Sell(long id, List<Product> products, String client, Calendar date, int saleType) {
        this.id = id;
        this.products = products;
        this.client = client;
        this.date = date;
        this.saleType = saleType;
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
                total += p.getValue();
            }
        }

        DecimalFormat df = new DecimalFormat("##.##");
        df.setRoundingMode(RoundingMode.UP);
        return Double.valueOf(df.format(total).replace(",","."));
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

    public void setSaleType(int saleType) {
        this.saleType = saleType;
    }

    public int getSaleType() {
        return saleType;
    }
}
