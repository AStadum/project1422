package com.addressbook.thorrism.addressbook;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;


public class BookSelectionScreen extends Activity {
    private TextView mEmptyView;
    private ListView mBooksView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.book_selection_screen);

        //Acquire XML objects
        mEmptyView = (TextView) findViewById(R.id.emptyBookList);
        mBooksView = (ListView) findViewById(R.id.booksList);

        //Initialize Parse
        initParse();
        queryForBooks();
    }

    /**
     * Initialize Parse to be used. Requires the context, App Id, and Client Id. Also,
     * the classes, or ParseObjects used, are registered for use.
     */
    public void initParse(){
        ParseObject.registerSubclass(AddressBook.class);
        Parse.initialize(this, "kpVXSqTA4cCxBYcDlcz1gGJKPZvMeofiKlWKzcV3", "T4FqPFp0ufX4qs8rIUDL8EX8RSluB0wGX51ZpL12" );
    }

    /**
     * Queries for the AddressBook(s) that exist for the current applications user
     * that is logged in. Uses "userID" to join the two tables together in order to
     * access them.
     *
     * "addressBooks" contains the result if they exist, display empty otherwise.
     */
    public void queryForBooks(){
        ParseQuery<AddressBook> bookQuery = ParseQuery.getQuery(AddressBook.class);
        bookQuery.whereEqualTo("userID", DroidBook.getInstance().getUser().getObjectId());
        bookQuery.findInBackground(new FindCallback<AddressBook>() {

            @Override
            public void done(List<AddressBook> addressBooks, ParseException e) {
                if (e != null) {

                } else {
                    mEmptyView.setVisibility(View.VISIBLE);
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.book_selection_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch(id){
            case (R.id.action_options):
                return true;

            case (R.id.action_addBook):
                addBook();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
     * Add a new Address book to the database for the current User.
     */
    public void addBook(){

    }

    /**
     * A toast method for testing purposes. Easier than typing each time lol
     */
    public void createToast(String s){
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }

    /**
     * Deal with the Android lifecycle.
     */
    @Override
    public void onStart(){
        super.onStart();
        DroidBook.getInstance().bookSelectionActivity = this;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        DroidBook.getInstance().bookSelectionActivity = null;
    }

    /*Prevent the user from returning to the splash screen (it is done)*/
    @Override
    public void onBackPressed(){
        return;
    }

}
