package br.com.versalius.iking.model;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by jn18 on 13/01/2017.
 */

public class User implements Serializable {

    private long id;
    private String password;
    private String avatar;
    private String name;
    private String email;
    private String birthday;
    private int gender_id;
    private String phone;
    private String rg;
    private String passport;
    private String shipping_agent;
    private String nationality;
    private String street;
    private String number;
    private String zip_code;
    private int city_id;
    private String profession;
    private String neighborhood;
    private String cpf;
    private String key;

    public User(JSONObject json) {
        if (json != null) {
            try {
                this.id = json.getJSONObject("userData").optLong("id");
                this.password = json.getJSONObject("userData").optString("password", "");
                this.avatar = json.getJSONObject("userData").optString("avatar", "");
                this.name = json.getJSONObject("userData").optString("name", "");
                this.email = json.getJSONObject("userData").optString("email", "");
                this.birthday = json.getJSONObject("userData").optString("birthday", "");
                this.email = json.getJSONObject("userData").optString("email", "");
                this.gender_id = json.getJSONObject("userData").optInt("gender_id");
                this.phone = json.getJSONObject("userData").optString("phone");
                this.rg = json.getJSONObject("userData").optString("rg");
                this.passport = json.getJSONObject("userData").optString("passport");
                this.shipping_agent = json.getJSONObject("userData").optString("shipping_agent");
                this.nationality = json.getJSONObject("userData").optString("nationality");
                this.street = json.getJSONObject("userData").optString("street");
                this.number = json.getJSONObject("userData").optString("number");
                this.zip_code = json.getJSONObject("userData").optString("zipcode");
                this.city_id = json.getJSONObject("userData").optInt("city_id");
                this.profession = json.getJSONObject("userData").optString("profession");
                this.neighborhood = json.getJSONObject("userData").optString("neighborhood");
                this.cpf = json.getJSONObject("userData").optString("cpf");
                this.key = json.optString("key");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            this.key = json.optString("key");
        }
    }

    public long getId() {
        return this.id;
    }

    public String getPassword() {
        return password;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {return email;}

    public String getBirthday() {return birthday;}

    public int getGender_id() {
        return gender_id;
    }

    public String getPhone() {
        return phone;
    }

    public String getRg() {return rg;}

    public String getPassport() {
        return passport;
    }

    public String getShipping_agent() {
        return shipping_agent;
    }

    public String getNationality() {
        return nationality;
    }

    public String getStreet() {
        return street;
    }
    public String getNumber(){ return number;}

    public String getZip_code() {
        return zip_code;
    }

    public int getCity_id() {
        return city_id;
    }

    public String getProfession() {
        return profession;
    }

    public String getNeighborhood() {
        return neighborhood;
    }

    public String getCpf() {
        return cpf;
    }

    public String getKey() {
        return key;
    }

}
