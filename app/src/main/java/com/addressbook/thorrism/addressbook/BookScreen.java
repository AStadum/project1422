package com.addressbook.thorrism.addressbook;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

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
    private ListView mContactsView;
    private TextView mEmptyView;
    private AddressBook mBook;
    private String mBookId;
    private List<Contact> mContacts;
    private ProgressBar mContactSpinner;

    //ExpandableListView items
    private ExpandableListView mExpandableView;
    private ContactExpandableAdapter mAdapter;
    private List<String> mContactHeaders;
    private HashMap<String, Contact> mContactData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.book_screen);

        //Acquire the contacts list view
        mContactsView   = (ListView) findViewById(R.id.contactsView);
        mEmptyView      = (TextView) findViewById(R.id.currentBookView);
        mContactSpinner = (ProgressBar) findViewById(R.id.contactsSpinner);
        mContacts       = new ArrayList<Contact>();
        mBookId         = getIntent().getExtras().getString("BookId");

        //ExpandleListView items
        mExpandableView = (ExpandableListView) findViewById(R.id.contactsExpandableView);
        mContactHeaders = new ArrayList<String>();
        mContactData    = new HashMap<String, Contact>();

        //Set the action bar's icon to be the logo.
        ActionBar actionBar = getActionBar();
        actionBar.setIcon(R.drawable.ic_logo);

        //Initialize the Parse instance.
        initParse();
        new FetchBookTask().execute(mBookId);
       // new FetchBookTask().execute(mBookId);
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

//    /**
//     * Fetch the AddressBook from the database with the specific name the user has selected
//     * on the previous screen. Uses a query with two where clauses, one for matching userID
//     * and one to match the name.
//     * @param name - the name of the AddressBook we query from the database.
//     */
//    public void fetchBook(String name){
//        ParseQuery<AddressBook> bookQuery = ParseQuery.getQuery(AddressBook.class);
//        bookQuery.whereEqualTo("objectId", name);
//
//        bookQuery.findInBackground(new FindCallback<AddressBook>() {
//            @Override
//            public void done(List<AddressBook> addressBooks, ParseException e) {
//                if(e == null){
//                    mBook     = addressBooks.get(0);
//                    mContacts = mBook.getEntries();
//                    Log.e(DroidBook.TAG, mBook.getBookName());
//                    if(mContacts == null){
//                        mEmptyView.setVisibility(View.VISIBLE);
//                    }else{
//                        displayContacts();
//                    }
//                }else{
//                    e.printStackTrace();
//                    Log.e(DroidBook.TAG, "Error: " + e.getMessage());
//                }
//            }
//        });
//    }

    public Comparator<Contact> getComparator(int compare){
        Comparator<Contact> result;
        switch(compare){

            //By zipcode
            case 0:
                result = new Comparator<Contact>() {
                    @Override
                    public int compare(Contact lhs, Contact rhs) {
                        int firstZip  = lhs.getZipcode();
                        int secondZip = rhs.getZipcode();
                        Integer.toString(firstZip).toString().compareTo(Integer.toString(firstZip).toString());
                        return 0;
                    }
                };
            default:
                result = null;
        }
        return result;
    }

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

                //Fetch the contacts from the entries of the current address book
                Contact contact = null;
                for(int i=0; i<mContacts.size(); ++i){
                    contact = mContacts.get(i);
                    contact.fetchIfNeeded();
                    if(mContactData.get(i) == null) {
                        mContactHeaders.add(contact.getFirstName() + " " + contact.getLastName());
                        mContactData.put(mContactHeaders.get(mContactData.size()), contact);
                    }
                }

                //Attempt to sort the contacts list by first name
                Collections.sort(mContactHeaders, new Comparator<String>() {

                    @Override
                    public int compare(String lhs, String rhs) {
                        return lhs.compareTo(rhs);
                    }
                });

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


    public void displayList(){
        mAdapter = new ContactExpandableAdapter(this, mContactHeaders, mContactData);
        mExpandableView.setAdapter(mAdapter);
    }



//    /**
//     * Fetch the AddressBook from the database with the specific name the user has selected
//     * on the previous screen. Uses a query with two where clauses, one for matching userID
//     * and one to match the name.
//     */
//    private class FetchBookTask extends AsyncTask<String, Void, Boolean> {
//
//        @Override
//        protected void onPreExecute(){
//            mContactSpinner.setVisibility(View.VISIBLE);
//        }
//
//        @Override
//        protected Boolean doInBackground(String... params){
//            ParseQuery<AddressBook> bookQuery = ParseQuery.getQuery(AddressBook.class);
//            bookQuery.whereEqualTo("objectId", params[0]);
//
//            try{
//                List<AddressBook> books = bookQuery.find();
//                mBook = books.get(0);
//                mContacts = mBook.getEntries();
//
//                //Fetch the contacts from the entries of the current address book
//                for(Contact contact: mContacts){
//                    contact.fetchIfNeeded();
//                }
//
//                //Attempt to sort the contacts list by first name
//                Collections.sort(mContacts, new Comparator<Contact>() {
//                    @Override
//                    public int compare(Contact lhs, Contact rhs) {
//                        return lhs.getFirstName().compareTo(rhs.getFirstName());
//                    }
//                });
//
//                return true;
//            }catch(ParseException e){
//                e.printStackTrace();
//                Log.e(DroidBook.TAG, "Error: " + e.getMessage());
//            }
//
//            return false;
//        }
//
//        @Override
//        protected void onPostExecute(Boolean result){
//            if(result){
//                if(mContacts == null){
//                    mEmptyView.setVisibility(View.VISIBLE);
//                }else{
//                    displayContacts();
//                }
//            }
//            mContactSpinner.setVisibility(View.GONE);
//        }
//    }

//    /**
//     * Add the adapter to our ListView and populate it with our Contacts
//     */
//    public void displayContacts(){
//        Log.e(DroidBook.TAG, "Starting to add..");
//        ContactAdapter adapter = new ContactAdapter(this,
//                                                    R.layout.contact_item_view,
//                                                    mContacts);
//
//        if(mContacts.size() == 0) mEmptyView.setVisibility(View.VISIBLE);
//        else{
//            mEmptyView.setVisibility(View.INVISIBLE);
//            mContactsView.setVisibility(View.VISIBLE);
//        }
//
//        //Attach the ContactAdapter to the Contacts ListView
//        mContactsView.setAdapter(adapter);
//        mContactsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//            }
//        });
//    }

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
        Log.e(DroidBook.TAG, "Resumed!");
        if(mBook != null && mContacts != null){
            new FetchBookTask().execute(mBookId);
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        Log.e(DroidBook.TAG, "Paused!");
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
