package com.addressbook.thorrism.addressbook;

import com.parse.ParseClassName;
import com.parse.ParseObject;

import org.json.JSONArray;

import java.util.List;

/**
 * Created by Lucas Crawford on 1/9/2015.
 */

@ParseClassName("AddressBook")
public class AddressBook extends ParseObject {

    public String getUserID() {
        return getString("userID");
    }

    public void setUserID(String userID) {
        put("userID", userID);
    }

    public String getBookID() {
        return getObjectId();
    }

    public String getBookName() {
        return getString("bookName");
    }

    public void setBookName(String bookName) {
        put("bookName", bookName);
    }

    public List<Contact> getEntries() {
        return getList("entries");
    }

    public void initEntries(List<Contact> entries){
        put("entries", entries);
    }

    public void setEntries(List<Contact> entries){
        put("entries", entries);
    }

    public void addEntry(Contact contact) {
        List<Contact> entries = getEntries();
        entries.add(contact);
        put("entries", entries);
    }

    /**
     * Remove an entry from the address book. This wil require
     * querying for a contact and identifying it's index to do so...
     * @param index contact desired to be removed.
     */
    public void removeEntry(int index){
        List<Contact> entries = getEntries();
        entries.remove(index);
        put("entries", entries);
    }
}
