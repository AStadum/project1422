package com.addressbook.thorrism.addressbook;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import java.sql.SQLException;


public class HomeScreen extends Activity {

    private SearchView search;
    private ImageView mAddBtn;
    private ListView mContactsList;
    private DatabaseAdapter mDatabaseAdapter;
    private SimpleCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_screen);

        //Acquire the contacts list view
        mContactsList = (ListView) findViewById(R.id.contactsView);

       //Instantiate the database, creates a new one if one doesn't already exist
        initDatabase();
        createToast("Test");
       // mDatabaseAdapter.truncateContacts();
        mDatabaseAdapter.testCreate();
        displayDatabase();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.home_screen, menu);

        //Acquire the search view and manager for the search view, and add a search listener.
        search  = (SearchView) menu.findItem(R.id.action_search).getActionView();
        addSearchListener();

        //Acquire the add contact button
        mAddBtn = (ImageView) menu.findItem(R.id.action_add).getActionView();
        mAddBtn.setImageResource(R.drawable.ic_add);

        addContactListener();
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Initialize the database, creates a new one if it doesn't exist.
     */
    public void initDatabase(){
        mDatabaseAdapter = new DatabaseAdapter(this);
        try{
            mDatabaseAdapter.open();
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    public void displayDatabase(){
        Cursor cursor = mDatabaseAdapter.fetchAll();

        //Columns for each contact. Just displays the name for now of each contact
        String[] columns = new String[]{
            mDatabaseAdapter.KEY_NAME,
        };

        //Views that receive their values.
        int[] views = new int[]{
                R.id.contactNameView
        };

        //Attach the cursor to it's adapted and use the item view
        mCursorAdapter = new SimpleCursorAdapter(
                this, R.layout.item_view,
                cursor,
                columns,
                views,
                0);


        mContactsList.setAdapter(mCursorAdapter);
    }

    /**
     * Add a listener for the search view when a user querys.
     */
    public void addSearchListener(){
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

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
     * Add a listener for the add contact button
     */
    public void addContactListener(){
        mAddBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                createToast("Works");
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                Toast.makeText(getApplicationContext(), "Click search", Toast.LENGTH_LONG).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * A toast method for testing purposes. Easier than typing each time
     */
    public void createToast(String s){
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }
}
