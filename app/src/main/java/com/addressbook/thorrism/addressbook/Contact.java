package com.addressbook.thorrism.addressbook;

import com.parse.ParseClassName;
import com.parse.ParseObject;

import java.util.List;
import java.util.Map;

/**
 * Created by Lucas Crawford on 1/6/2015.
 */

@ParseClassName("Contact")
public class Contact extends ParseObject{

    /**
     * Default constructor
     */
    public Contact(){
    }

    /**
     * Getters and setters for all the data fields.
     */
    public String getFirstName() {
        return getString("firstName");
    }

    public void setFirstName(String firstName) {
        put("firstName", firstName);
    }

    public String getLastName() {
        return getString("lastName");
    }

    public void setLastName(String lastName) {
        put("lastName", lastName);
    }

    public String getAddress() {
        return getString("address");
    }

    public void setAddress(String address) {
        put("address", address);
    }

    public String getZipcode() {
        return getString("zipcode");
    }

    public void setZipcode(String zipcode) {
        put("zipcode", zipcode);
    }

    public String getCity() {
        return getString("city");
    }

    public void setCity(String city) {
        put("city", city);
    }

    public String getState() {
       return getString("state");
    }

    public void setState(String state) {
       put("state", state);
    }

    public void setNumber(String number){
        put("number", number);
    }

    public String getNumber(){
        return getString("number");
    }

    public void setEmail(String email){
        put("email", email);
    }

    public String getEmail(){
        return getString("email");
    }

    public List<String> getExtras(){
        return getList("extras");
    }

    public void setExtras(List<String> extras){
        put("extras", extras);
    }
}
