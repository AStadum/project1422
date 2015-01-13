package com.addressbook.thorrism.addressbook;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class BookScreen extends Activity {

    private SearchView mSearch;
    private ScrollView mContactsList;
    private String mBookName;
    private AddressBook mBook;
    private Bundle mExtras;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.book_screen);

        //Acquire the contacts list view
        mContactsList = (ScrollView) findViewById(R.id.contactsView);
        mExtras       = getIntent().getExtras();
        mBookName     = mExtras.getString("BookName");

        //Set the action bar's icon to be the logo.
        ActionBar actionBar = getActionBar();
        actionBar.setIcon(R.drawable.ic_logo);

        //Initialize the Parse instance.
        initParse();
        fetchBook(mBookName);
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
     * Fetch the AddressBook from the database with the specific name the user has selected
     * on the previous screen. Uses a query with two where clauses, one for matching userID
     * and one to match the name.
     * @param name - the name of the AddressBook we query from the database.
     */
    public void fetchBook(String name){
        ParseQuery<AddressBook> bookQuery = ParseQuery.getQuery(AddressBook.class);
        bookQuery.whereEqualTo("userID", DroidBook.getInstance().getUser().getObjectId());
        bookQuery.whereEqualTo("bookName", name);

        bookQuery.findInBackground(new FindCallback<AddressBook>() {
            @Override
            public void done(List<AddressBook> addressBooks, ParseException e) {
                if(e == null){
                    mBook = addressBooks.get(0);
                }else{
                    e.printStackTrace();
                    Log.e(DroidBook.TAG, "Error: " + e.getMessage());
                }
            }
        });
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
     * Menu tools control. Currently just listens for Add button to be pressed.
     * @param item - the item the user has selected
     * @return boolean to return if the selection was successful or not.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_addBook:
                startActivity(new Intent(getApplicationContext(), CreateContactScreen.class));
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
     * Deal with the Android lifecycle.
     */
    @Override
    public void onStart(){
        super.onStart();
        DroidBook.getInstance().homeScreenActivity = this;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        DroidBook.getInstance().homeScreenActivity = null;
    }

    /*Prevent the user from returning to the splash screen (it is done)*/
    @Override
    public void onBackPressed(){
        return;
    }
}
