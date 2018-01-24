package br.com.versalius.e_stokrootsilver.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Calendar;

/**
 * Created by Giovanne on 23/10/2017.
 */

public class User implements Serializable{

    private long id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;

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
}
