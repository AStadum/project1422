package com.addressbook.thorrism.addressbook;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.DeleteCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;


public class ContactsScreen extends Activity {
    private TextView mEmptyView;
    private AddressBook mBook;
    private View mActiveContact;
    private int mCurrentGroup;
    private String mBookId;
    private List<Contact> mContacts;
    private ProgressBar mContactSpinner;

    //ExpandableListView items
    private ExpandableListView mExpandableView;
    private ContactExpandableAdapter mAdapter;
    private List<String> mContactHeaders;
    private HashMap<String, Contact> mContactData;
    private Vibrator mVibrator;
    private Comparator<Contact> mComparator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.book_screen);

        //Acquire the XML objects
        mEmptyView      = (TextView) findViewById(R.id.currentBookView);
        mContactSpinner = (ProgressBar) findViewById(R.id.contactsSpinner);
        mContacts       = new ArrayList<Contact>();
        mBookId         = getIntent().getExtras().getString("BookId");

        //ExpandleListView items
        mExpandableView = (ExpandableListView) findViewById(R.id.contactsExpandableView);
        addHeaderListeners();
        mContactHeaders = new ArrayList<String>();
        mContactData    = new HashMap<String, Contact>();
        mVibrator       = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        mCurrentGroup = -1;
        setComparator(2);

        //Set the action bar's icon to be the logo.
        ActionBar actionBar = getActionBar();
        actionBar.setIcon(R.drawable.ic_logo);
        DroidBook.setFontRoboto(mEmptyView, this);

        //Initialize the Parse instance and fetch the user's contacts
        initParse();
        new FetchBookTask().execute(mBookId);
    }

    /**
     * Initialize Parse to be used. Requires the context, App Id, and Client Id. Also,
     * the classes, or ParseObjects used, are registered for use.
     */
    public void initParse(){
        ParseObject.registerSubclass(AddressBook.class);
        ParseObject.registerSubclass(Contact.class);
        Parse.initialize(this, "kpVXSqTA4cCxBYcDlcz1gGJKPZvMeofiKlWKzcV3", "T4FqPFp0ufX4qs8rIUDL8EX8RSluB0wGX51ZpL12" );
    }

    /**
     * The purpose of this function is to return a custom comparator based on which option the
     * user has selected.
     * @param compare - the type of sort the user wants to perform
     * @return custom comparator returned to sort the contacts based on the parameter chosen.
     */
    public void setComparator(int compare){
        Comparator<Contact> result = null;

        //By zipcode, we check if zipcodes are equal if so, compare the names, if not just return zip codes
        if(compare == 0) {
            result = new Comparator<Contact>() {
                @Override
                public int compare(Contact lhs, Contact rhs) {
                    int val = lhs.getZipcode().compareTo(rhs.getZipcode());
                    if (val == 0)
                        return (lhs.getFirstName() + lhs.getLastName()).compareTo(rhs.getFirstName() + rhs.getLastName());
                    else return val;
                }
            };
        }

        //By last name sort, just compare last names
        if(compare == 1) {
            result = new Comparator<Contact>() {
                @Override
                public int compare(Contact lhs, Contact rhs) {
                    int val = lhs.getLastName().compareTo(rhs.getLastName());
                    if (val == 0)
                        return (lhs.getFirstName() + lhs.getLastName()).compareTo((rhs.getFirstName() + rhs.getLastName()));
                    else return val;
                }
            };
        }

        //By last name sort, just compare last names
        if(compare == 2) {
            result = new Comparator<Contact>() {
                @Override
                public int compare(Contact lhs, Contact rhs) {
                    int val = lhs.getFirstName().compareTo(rhs.getFirstName());
                    if (val == 0)
                        return (lhs.getFirstName() + lhs.getLastName()).compareTo((rhs.getFirstName() + rhs.getLastName()));
                    else return val;
                }
            };
        }

        mComparator = result;
    }

    /**
     * AsyncTask used for a two network operations. We query the database for all the contacts (a list)
     * for the book we are currently interested in selected by the user. We must fetch the contacts
     * that are within the list from the database too, since we only fetch a list when we get the
     * Address Book's entries.
     */
    private class FetchBookTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected void onPreExecute(){
            mContactSpinner.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(String... params){
            ParseQuery<AddressBook> bookQuery = ParseQuery.getQuery(AddressBook.class).fromLocalDatastore();

            if(bookQuery == null)
                DroidBook.getInstance().close();
            else
                bookQuery.whereEqualTo("objectId", params[0]);

            try{
                List<AddressBook> books = bookQuery.find();
                ParseObject.pinAll(books);
                mBook = books.get(0);
                mContacts = mBook.getEntries();
                mContactHeaders.clear();
                mContactData.clear();

                //Fetch the contacts, must do to have a value for the contact
                for(Contact contact: mContacts){
                    try{
                        contact.fetchFromLocalDatastore();
                    }catch(ParseException e){
                        contact.fetchIfNeeded();
                    }
                }

                ParseObject.pinAll(mContacts);

                //Attempt to sort the contacts list by first name
                Collections.sort(mContacts, mComparator);
                mBook.setEntries(mContacts);

                //For each contact, add their names to a new header
                Contact contact;
                for(int i=0; i<mContacts.size(); ++i){
                    contact = mContacts.get(i);
                    if(contact == null)
                        Log.e(DroidBook.TAG, "*****NULL****");
                    mContactHeaders.add(contact.getFirstName() + " " + contact.getLastName());
                    mContactData.put(mContactHeaders.get(mContactData.size()), contact);
                }
                return true;
            }catch(ParseException e){
                e.printStackTrace();
                Log.e(DroidBook.TAG, "Error: " + e.getMessage());
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean result){
            if(result){
                if(mContacts == null || mContacts.size() == 0){
                    mEmptyView.setVisibility(View.VISIBLE);
                }else{
                    displayList();
                    mEmptyView.setVisibility(View.INVISIBLE);
                }
            }
            mContactSpinner.setVisibility(View.GONE);
        }
    }

    private class SortContactsTask extends AsyncTask<Void, Void, Void>{

        @Override
        public void onPreExecute(){
            mContactSpinner.setVisibility(View.VISIBLE);
        }

        @Override
        public Void doInBackground(Void... params){
            mContactHeaders.clear();
            mContactData.clear();

            //Attempt to sort the contacts list by first name
            Collections.sort(mContacts, mComparator);
            mBook.setEntries(mContacts);

            //For each contact, add their names to a new header
            Contact contact;
            for(int i=0; i<mContacts.size(); ++i){
                contact = mContacts.get(i);
                mContactHeaders.add(contact.getFirstName() + " " + contact.getLastName());
                mContactData.put(mContactHeaders.get(mContactData.size()), contact);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result){
            mAdapter.notifyDataSetChanged();
            mContactSpinner.setVisibility(View.GONE);
        }
    }

    private class UpdateContactTask extends AsyncTask<String, Void, Void>{

        @Override
        public void onPreExecute(){
            mContactSpinner.setVisibility(View.VISIBLE);
        }

        @Override
        public Void doInBackground(String... params){
            Contact contact = null;
            Contact result = null;

            //Find the updated contact that matches our parameter objectID.
            for(int i=0; i<mContacts.size(); ++i){
                contact = mContacts.get(i);
                if(contact == null) return null;
                if(contact.getObjectId().equals(params[0])){
                    try{
                        try{
                            contact.fetchFromLocalDatastore();
                            result = contact;
                        }catch(ParseException e){
                            result = contact.fetch();
                        }
                        mContactHeaders.set(i, result.getFirstName() + " " + result.getLastName());
                        mContactData.put(mContactHeaders.get(i), result);
                        return null;
                    }catch(ParseException e){
                        e.printStackTrace();
                        Log.e(DroidBook.TAG, "Failed to update contact! Check your network status.");
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result){
            displayList();
            mContactSpinner.setVisibility(View.GONE);
        }
    }

    /**
     * Create a new custom adapter for our ExpandableListView and populate the adapter with
     * our contacts and headers containing the contact's names.
     */
    public void displayList(){
        if(checkEmptyContacts()) mEmptyView.setVisibility(View.VISIBLE);
        mAdapter = new ContactExpandableAdapter(this, mContactHeaders, mContactData);
        mExpandableView.setAdapter(mAdapter);
    }

    /**
     * Add the listeners for the contact's which are headers in the ExpandableListView. When a group
     * is expanded, clear the current contact's icons for edit / remove if they are visible.
     */
    public void addHeaderListeners(){
        mExpandableView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                long packedPosition = mExpandableView.getExpandableListPosition(position);

                int itemType        = ExpandableListView.getPackedPositionType(packedPosition);
                int groupPosition   = ExpandableListView.getPackedPositionGroup(packedPosition);
                int childPosition   = ExpandableListView.getPackedPositionChild(packedPosition);

                if(itemType == ExpandableListView.PACKED_POSITION_TYPE_GROUP){
                    Contact contact = (Contact) mAdapter.getChild(groupPosition, 0);
                    contactDialog(contact, groupPosition);
                    mVibrator.vibrate(100);
                }

                return false;
            }
        });
    }

    public void editContact(Contact contact){
        Intent intent = new Intent(this, ContactEditScreen.class);
        intent.putExtra("ContactID", contact.getObjectId());
        intent.putExtra("FirstName", contact.getFirstName());
        intent.putExtra("LastName", contact.getLastName());
        intent.putExtra("ZipCode", contact.getZipcode());
        intent.putExtra("Address", contact.getAddress());
        intent.putExtra("CityName", contact.getCity());
        intent.putExtra("StateName", contact.getState());
        intent.putExtra("Number", contact.getNumber());
        intent.putExtra("Email", contact.getEmail());
        startActivity(intent);
    }

    public void contactDialog(final Contact contact, final int position){
        LayoutInflater inflater = LayoutInflater.from(this);

        final View contactViewOptions          = inflater.inflate(R.layout.contact_options, null);
        final LinearLayout editContactLayout   = (LinearLayout) contactViewOptions.findViewById(R.id.editContactLayout);
        final LinearLayout deleteContactLayout = (LinearLayout) contactViewOptions.findViewById(R.id.deleteContactLayout);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(contactViewOptions);

        builder.setCancelable(true)
                .setTitle(contact.getFirstName() + " " + contact.getLastName());

        //Set the icon for the dialog window to the app's icon
        builder.setIcon(R.drawable.ic_launcher);

        //Build the dialog and create custom listeners for buttons
        final AlertDialog dialog = builder.create();

        //Remove the icons for the contact edit / remove to be visible
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
            }
        });

        //Add click listeners to the two layouts
        editContactLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editContact(contact);
                dialog.dismiss();
            }
        });

        deleteContactLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeContactDialog(contact, position);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    /**
     * Opens a dialog box confirming if a user truly wants to delete a contact (confirmation)
     * @param
     */
    public void removeContactDialog(final Contact contact, final int position) {
        LayoutInflater inflater = LayoutInflater.from(this);

        final View modifyBookView = inflater.inflate(R.layout.remove_contact, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(modifyBookView);

        builder.setCancelable(true)
                .setNegativeButton("No", null)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface i, int id) {
                        removeContact(contact, position);
                    }
                })
                .setTitle("Remove Contact");

        //Set the icon for the dialog window to the app's icon
        builder.setIcon(R.drawable.ic_launcher);

        //Build the dialog and create custom listeners for buttons
        final AlertDialog dialog = builder.create();

        //Remove the icons for the contact edit / remove to be visible
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
            }
        });

        dialog.show();
    }

    /**
     * When the user clickes the remove button, and confirms it, we delete the contact from
     * the database in the background, update the associated Address book and save it, and
     * then we finish up by alerting the user the contact was deleted with a Toast.
     * @param position
     */
    public void removeContact(Contact contact, int position){
        mBook.removeEntry(position);
        mContactHeaders.remove(position);
        mBook.saveEventually();

        contact.deleteInBackground(new DeleteCallback() {

            @Override
            public void done(ParseException e) {
                if(e==null){
                    createToast("Deleted Contact");
                }else{
                    e.printStackTrace();
                    createToast("Failed to delete.");
                }
            }
        });

        //Update the display to show contact removed
        displayList();
    }

    /**
     * A toast method for testing purposes. Easier than typing each time lol
     */
    public void createToast(String s){
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }

    /**
     * When a user selects logout from the options drop down menu reset
     * the DroidBook's instance of username / user and return the user to
     * the start screen.
     * @param item - the Logout item from the dropdown menu for options
     */
    public void logoutUser(MenuItem item){
        ParseUser.logOut();
        DroidBook.getInstance().username = "";
        DroidBook.getInstance().user     = null;
        startActivity(new Intent(getApplicationContext(), StartScreen.class));
    }


    /**
     * +
     * When a user selects "Choose new Address Book" they return to the previous screen
     * and can select another address book to be active
     */
    public void returnUserToBooks(MenuItem item){
        this.finish();
    }

    /**
     * Menu tools control. Currently just listens for Add button to be pressed.
     * @param item - the item the user has selected
     * @return boolean to return if the selection was successful or not.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_addContact:
                Intent intent = new Intent(this, CreateContactScreen.class);
                intent.putExtra("BookId", mBookId);
                startActivity(intent);
                return true;
            case R.id.action_options:
                return true;
            default:

                return super.onOptionsItemSelected(item);
        }
    }

    public boolean checkEmptyContacts(){
        if(mContacts.size() == 0)
            return true;
        else
            return false;
    }

    /**
     * OnClick methods used by the MenuItems under settings to sort the user's contacts
     * based on what they chose.
     * @param i
     */
    public void sortByLast(MenuItem i){
        if(checkEmptyContacts()) createToast("No contacts found!");
        else {
            setComparator(1);
            new SortContactsTask().execute();
        }
    }

    public void sortByZip(MenuItem i){
        if(checkEmptyContacts()) createToast("No contacts found!");
        else {
            setComparator(0);
            new SortContactsTask().execute();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.contact_screen, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Purpose is to check if the spinner is active when the app is paused or destroyed. If so,
     * we set it to gone because otherwise it stays spinning forever until an add action is
     * performed.
     */
    public void checkSpinnerStatus(){
        if(mContactSpinner.getVisibility() == View.VISIBLE){
            mContactSpinner.setVisibility(View.GONE);
        }
    }

    /**
     * Deal with the Android lifecycle.
     */
    @Override
    public void onResume(){
        super.onResume();
        if(mBook != null && mContacts != null){
            SharedPreferences prefs = getApplicationContext().getSharedPreferences("ADD_STATE", MODE_PRIVATE);

            //Check if we need to update the contacts list from the user adding a new contact
            if(prefs.getString("STATE", "").equals("NEW")) {
                new FetchBookTask().execute(mBookId);
                prefs.edit().putString("STATE", "UPDATED").apply();
            }

            //If we just updated, update the list view to display our changes.
            if(prefs.getString("STATE", "").equals("MODIFIED")) {
                prefs.edit().putString("STATE", "UPDATED").apply();
                new UpdateContactTask().execute(prefs.getString("CONTACT", ""));
            }
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        checkSpinnerStatus();
    }

    @Override
    public void onStart(){
        super.onStart();
        DroidBook.getInstance().contactScreenActivity = this;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        checkSpinnerStatus();
        DroidBook.getInstance().contactScreenActivity = null;
    }

    /*Prevent the user from returning to the splash screen (it is done)*/
    @Override
    public void onBackPressed(){
        super.onBackPressed();
    }
}
