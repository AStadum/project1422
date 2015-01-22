package com.addressbook.thorrism.addressbook;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
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


public class BookScreen extends Activity {

    private SearchView mSearch;
    private TextView mEmptyView;
    private AddressBook mBook;
    private View mActiveContact;
    private String mBookId;
    private List<Contact> mContacts;
    private ProgressBar mContactSpinner;

    //ExpandableListView items
    private ExpandableListView mExpandableView;
    private ContactExpandableAdapter mAdapter;
    private List<String> mContactHeaders;
    private HashMap<String, Contact> mContactData;
    private Vibrator mVibrator;

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
        mContactHeaders = new ArrayList<String>();
        mContactData    = new HashMap<String, Contact>();
        mVibrator       = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);


        //Set the action bar's icon to be the logo.
        ActionBar actionBar = getActionBar();
        actionBar.setIcon(R.drawable.ic_logo);

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
    public Comparator<Contact> getComparator(int compare){
        Comparator<Contact> result;
        //By zipcode, we check if zipcodes are equal if so, compare the names, if not just return zip codes
        if(compare == 0) {

            Log.e("Zipsort", "Zipsort");
            result = new Comparator<Contact>() {
                @Override
                public int compare(Contact lhs, Contact rhs) {
                    int val = lhs.getZipcode() - rhs.getZipcode();
                    if (val == 0)
                        return (lhs.getFirstName() + lhs.getLastName()).compareTo(rhs.getFirstName() + rhs.getLastName());
                    else return val;
                }
            };
        }
        //By last name sort, just compare last names
        else if(compare == 1) {
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
        else{
            Log.e("Default", "Default");
            result = new Comparator<Contact>() {
                @Override
                public int compare(Contact lhs, Contact rhs) {
                    int val = lhs.getFirstName().compareTo(rhs.getFirstName());
                    if(val == 0)
                        return (lhs.getFirstName() + lhs.getLastName()).compareTo((rhs.getFirstName() + rhs.getLastName()));
                    else return val;
                }
            };
        }
        return result;
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
            ParseQuery<AddressBook> bookQuery = ParseQuery.getQuery(AddressBook.class);
            bookQuery.whereEqualTo("objectId", params[0]);

            try{
                List<AddressBook> books = bookQuery.find();
                mBook = books.get(0);
                mContacts = mBook.getEntries();
                mContactHeaders.clear();
                mContactData.clear();

                //Fetch the contacts, must do to have a value for the contact
                for(Contact contact: mContacts){
                    contact.fetchIfNeeded();
                }

                //Attempt to sort the contacts list by first name
                Collections.sort(mContacts, getComparator(0));
                mBook.setEntries(mContacts);

                //For each contact, add their names to a new header
                Contact contact;
                for(int i=0; i<mContacts.size(); ++i){
                    if(mContactData.get(mContacts.get(i).getFirstName() + " " + mContacts.get(i).getLastName() ) == null) {
                        contact = mContacts.get(i);
                        mContactHeaders.add(contact.getFirstName() + " " + contact.getLastName());
                        mContactData.put(mContactHeaders.get(mContactData.size()), contact);
                    }
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
                    //displayContacts();
                    displayList();
                    mEmptyView.setVisibility(View.INVISIBLE);
                }
            }
            mContactSpinner.setVisibility(View.GONE);
        }
    }

    /**
     * Create a new custom adapter for our ExpandableListView and populate the adapter with
     * our contacts and headers containing the contact's names.
     */
    public void displayList(){
        mAdapter = new ContactExpandableAdapter(this, mContactHeaders, mContactData);
        mExpandableView.setAdapter(mAdapter);
        addHeaderListeners();
    }

    /**
     * Clear the active contacts icons when we select another contact. If the current active contact
     * is null, the function just returns.
     */
    public void clearActiveContact(){
        //Check if null, otherwise hide the ImageViews from the active contact selected
        if(mActiveContact == null) return;
        ImageView removeIcon = (ImageView) mActiveContact.findViewById(R.id.contactRemoveBtn);
        ImageView editIcon   = (ImageView) mActiveContact.findViewById(R.id.contactEditBtn);
        removeIcon.setVisibility(View.GONE);
        editIcon.setVisibility(View.GONE);
    }

    /**
     * Add the listeners for the contact's which are headers in the ExpandableListView. When a group
     * is expanded, clear the current contact's icons for edit / remove if they are visible.
     */
    public void addHeaderListeners(){

        //When a group is expanded, hide the icons from the previously current contact selected
        mExpandableView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
                clearActiveContact();
                mActiveContact = mExpandableView.getChildAt(groupPosition);
            }
        });

        //On long click for a contact, make the edit / remove icons visible.
        mExpandableView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                clearActiveContact();
                mActiveContact = mExpandableView.getChildAt(position);
                mVibrator.vibrate(100);

                final ImageView removeIcon = (ImageView) mActiveContact.findViewById(R.id.contactRemoveBtn);
                final ImageView editIcon   = (ImageView) mActiveContact.findViewById(R.id.contactEditBtn);
                removeIcon.setVisibility(View.VISIBLE);
                editIcon.setVisibility(View.VISIBLE);

                removeIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        removeBookDialog(position, removeIcon, editIcon);
                    }
                });
                return true;
            }
        });
    }

    /**
     * Opens a dialog box confirming if a user truly wants to delete a contact (confirmation)
     * @param position
     * @param exitIcon
     * @param editIcon
     */
    public void removeBookDialog(final int position, final ImageView exitIcon, final ImageView editIcon) {
        LayoutInflater inflater = LayoutInflater.from(this);

        final View modifyBookView = inflater.inflate(R.layout.remove_book, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(modifyBookView);

        builder.setCancelable(false)
                .setNegativeButton("No", null)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface i, int id) {
                        removeContact(position);
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
                exitIcon.setVisibility(View.GONE);
                editIcon.setVisibility(View.GONE);
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
    public void removeContact(int position){
        Contact contact = mContacts.get(position);
        mBook.removeEntry(position);
        mContactHeaders.remove(position);
        mBook.saveInBackground();

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
     * Add a listener for the search view when a user queries by inputting a contact name.
     */
    public void addSearchListener(){
        mSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }
        });
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
            case R.id.action_addBook:
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.home_screen, menu);

        //Acquire the search view and manager for the search view, and add a search listener.
        mSearch  = (SearchView) menu.findItem(R.id.action_search).getActionView();
        addSearchListener();

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
            new FetchBookTask().execute(mBookId);
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
        return;
    }
}
