package com.addressbook.thorrism.addressbook;

/**
 * Created by Lucas Crawford on 1/6/2015.
 */
public class Contact {
    private String firstName;
    private String lastName;
    private String number;
    private String address;
    private String email;


    /**
     * Default constructor
     */
    public Contact(){
        firstName = "John";
        lastName  = "Doe";
        number    = "867-5309";
        address   = "1337 13th st.";
        email     = "lol@gmail.com";
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

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}
