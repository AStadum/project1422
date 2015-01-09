package com.addressbook.thorrism.addressbook;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.parse.Parse;
import com.parse.ParseUser;

import java.sql.SQLException;


public class BookScreen extends Activity {

    private SearchView search;
    private ListView mContactsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.book_screen);

        //Acquire the contacts list view
        mContactsList = (ListView) findViewById(R.id.contactsView);

        //Initialize the Parse instance.
        Parse.initialize(this, "kpVXSqTA4cCxBYcDlcz1gGJKPZvMeofiKlWKzcV3", "T4FqPFp0ufX4qs8rIUDL8EX8RSluB0wGX51ZpL12");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.home_screen, menu);

        //Acquire the search view and manager for the search view, and add a search listener.
        search  = (SearchView) menu.findItem(R.id.action_search).getActionView();
        addSearchListener();

        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Add a listener for the search view when a user queries by inputting a contact name.
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
     * A toast method for testing purposes. Easier than typing each time lol
     */
    public void createToast(String s){
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }

    /**
     * Menu tools control. Currently just listens for Add button to be pressed.
     * @param item- the item the user has selected
     * @return boolean to return if the selection was successful or not.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                createToast("Add pressed!");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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

//private DatabaseAdapter mDatabaseAdapter;
//    //  private SimpleCursorAdapter mCursorAdapter;
//    //  private Cursor mCursor;
//    /**
//     * Initialize the database, creates a new one if it doesn't exist.
//     */
//    public void initDatabase(){
//        mDatabaseAdapter = new DatabaseAdapter(this);
//        try{
//            mDatabaseAdapter.open();
//        }catch(SQLException e){
//            e.printStackTrace();
//        }
//    }
//Local saving options
//    /**
//     * Add the listeners for the items in the ListView (contacts)
//     */
//    public void addItemListeners(){
//        mContactsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                mCursor.moveToPosition(position);
//                String test = mCursor.getString(mCursor.getColumnIndexOrThrow(mDatabaseAdapter.KEY_EMAIL));
//                createToast(test);
//            }
//        });
//    }
//    /**
//     * Display the values that exist currently within the database.
//     */
//    public void displayDatabase(){
//        mCursor = mDatabaseAdapter.fetchAll();
//
//        //Columns for each contact
//        String[] columns = new String[]{
//                mDatabaseAdapter.KEY_NAME,
//        };
//
//        //Views that receive their values (items of the ListView)
//        int[] views = new int[]{
//                R.id.contactNameView
//        };
//
//        //Attach the cursor to it's adapted and use the item view
//        mCursorAdapter = new SimpleCursorAdapter(
//                this, R.layout.item_view,
//                mCursor,
//                columns,
//                views,
//                0);
//
//        //Set the adapter to the ListView to populate it with the results from our database
//        mContactsList.setAdapter(mCursorAdapter);
//        addItemListeners();
//    }
