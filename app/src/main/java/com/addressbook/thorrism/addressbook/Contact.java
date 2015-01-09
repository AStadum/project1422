package com.addressbook.thorrism.addressbook;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by Lucas Crawford on 1/6/2015.
 */

@ParseClassName("Contact")
public class Contact extends ParseObject{
    private String firstName;
    private String lastName;
    private String address;
    private String city;
    private String state;
    private int zipcode;


    /**
     * Default constructor
     */
    public Contact(){
        firstName = "John";
        lastName  = "Doe";
        address   = "1337 13th st.";
    }

    /**
     * Getters and setters for all the data fields
     */
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getZipcode() {
        return zipcode;
    }

    public void setZipcode(int zipcode) {
        this.zipcode = zipcode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

}
