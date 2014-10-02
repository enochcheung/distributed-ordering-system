package com.enochc.software648.hw1;

import java.io.Serializable;

import org.json.simple.JSONObject;

public class CustomerAddress implements Serializable {
    private static final long serialVersionUID = 14832030L;

    private String firstname;
    private String lastname;
    private String street;
    private String city;
    private String state;
    private String zipcode;

    public CustomerAddress(String firstname, String lastname,
                        String street, String city, String state, String zipcode) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.street = street;
        this.city = city;
        this.state = state;
        this.zipcode = zipcode;
    }

    public CustomerAddress(JSONObject jsonObject) {

        this.firstname = (String) jsonObject.get("firstname");
        this.lastname = (String) jsonObject.get("lastname");
        this.street = (String) jsonObject.get("street");
        this.city = (String) jsonObject.get("city");
        this.state = (String) jsonObject.get("state");
        this.zipcode = (String) jsonObject.get("zipcode");

    }

    public JSONObject toJsonObject() {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("firstname", firstname);
        jsonObject.put("lastname", lastname);
        jsonObject.put("street", street);
        jsonObject.put("city", city);
        jsonObject.put("state", state);
        jsonObject.put("zipcode", zipcode);

        return jsonObject;
    }

    public String toString() {
        return String.format("%s %s%n%s%n%s, %s %s", firstname, lastname,
                street, city, state, zipcode);
    }
}
