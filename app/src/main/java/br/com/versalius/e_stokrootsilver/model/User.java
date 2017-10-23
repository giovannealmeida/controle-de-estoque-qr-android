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

    public User(JSONObject json) {
        try {
            if(json.has("u_id")) {
                this.id = json.getInt("u_id");
            }

            this.firstName = json.optString("u_first_name", "User");
            this.lastName = json.optString("u_last_name", "");

            if(json.has("u_email")) {
                this.email = json.optString("u_email", "");
            }
            if(json.has("u_password")) {
                this.password = json.optString("u_password", "");
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
