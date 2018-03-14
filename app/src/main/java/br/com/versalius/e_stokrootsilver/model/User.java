package br.com.versalius.e_stokrootsilver.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by Giovanne on 23/10/2017.
 */

public class User implements Serializable{

    private long id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private int levelId; //1 - ADM, 2 - vendedor
    private int typeSaleId; //1 - Atacado, 2 - Varejo, 3 - Ambos

    public User(JSONObject json) {
        try {
            if(json.has("id")) {
                this.id = json.getInt("id");
            }

            String[] name = json.getString("name").split(" ");
            this.firstName = name[0];
            this.lastName = name[1];

            if(json.has("email")) {
                this.email = json.optString("email", "");
            }

            if(json.has("password")) {
                this.password = json.optString("password", "");
            }

            if(json.has("level_id")) {
                this.levelId = json.getInt("level_id");
            }

            if(json.has("type_sale_id")) {
                this.typeSaleId = json.getInt("type_sale_id");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public int getLevelId() {
        return levelId;
    }

    public int getTypeSaleId() {
        return typeSaleId;
    }
}
