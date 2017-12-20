package br.com.versalius.e_stokrootsilver.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by Giovanne on 17/10/2017.
 */

public class Product implements Serializable {
    private int id;
    private String code;
    private String name;
    private String description;
    private int stockQuantity;
    private double retailPrice;
    private double wholesalePrice;
    private boolean status;

    /*TODO: remover já já*/
    public Product(int id, String name, String sas, double retailPrice) {
        this.id = id;
        this.name = name;
        this.retailPrice = retailPrice;
    }

    public Product(JSONObject json) {
        try {
            if (json.has("id")) {
                this.id = json.getInt("id");
            }
            if (json.has("code")) {
                this.code = json.getString("code");
            }
            if (json.has("product_name")) {
                this.name = json.getString("product_name");
            }
            if (json.has("description")) {
                this.description = json.getString("description");
            }
            if (json.has("quantity_in_stock")) {
                this.stockQuantity = json.getInt("quantity_in_stock");
            }
            if (json.has("wholesale_value")) {
                this.wholesalePrice = json.getDouble("wholesale_value");
            }
            if (json.has("retail_value")) {
                this.retailPrice = json.getDouble("retail_value");
            }
            if (json.has("status")) {
                this.status = json.getBoolean("status");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public int getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public double getRetailPrice() {
        return retailPrice;
    }

    public double getWholesalePrice() {
        return wholesalePrice;
    }

    public boolean isStatus() {
        return status;
    }
}
