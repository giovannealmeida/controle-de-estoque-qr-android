package br.com.versalius.e_stokrootsilver.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by Giovanne on 17/10/2017.
 */

public class Product implements Serializable {
    private Integer id;
    private String code;
    private String name;
    private String description;
    private String weight;
    private double value;
    private String category;
    private int amount;

    /*TODO: remover já já
    * Cria dummy para SellHistoryActivity*/
    public Product(int id, String name, String sas, double value) {
        this.id = id;
        this.name = name;
        this.value = value;
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
            if (json.has("weight")) {
                this.weight = json.getString("weight");
            }
            if (json.has("category")) {
                //O preço vem em String no fomato R$ 0,00
                this.category = json.getString("category");
            }
            if (json.has("amount")) {
                this.amount = json.getInt("amount");
            }
            if (json.has("value")) {
                //O preço vem em String no fomato R$ 0,00
                this.value = Double.valueOf(json.getString("value").substring(3));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Integer getId() {
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

    public String getWeight() {
        return weight;
    }

    public double getValue() {
        return value;
    }

    public String getCategory() {
        return category;
    }

    public int getAmount() {
        return amount;
    }
}
