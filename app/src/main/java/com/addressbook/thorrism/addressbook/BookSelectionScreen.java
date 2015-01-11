package com.addressbook.thorrism.addressbook;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class BookSelectionScreen extends Activity {
    private TextView mEmptyView;
    private ListView mBooksView;
    private EditText mResultView;
    private ProgressBar mProgressBar;
    private Map<Integer, AddressBook> mBookMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.book_selection_screen);

        //Acquire XML objects
        mEmptyView   = (TextView) findViewById(R.id.emptyBookList);
        mBooksView   = (ListView) findViewById(R.id.booksList);
        mResultView  = (EditText) findViewById(R.id.resultEditView);
        mProgressBar = (ProgressBar) findViewById(R.id.querySpinner);
        mBookMap     = new HashMap<Integer, AddressBook>();

        //Add some listeners
        addBooksViewListener();

        //Initialize Parse
        initParse();
        new QueryBooksTask().execute();
    }

    /**
     * Initialize Parse to be used. Requires the context, App Id, and Client Id. Also,
     * the classes, or ParseObjects used, are registered for use.
     */
    public void initParse(){
        ParseObject.registerSubclass(AddressBook.class);
        Parse.initialize(this, "kpVXSqTA4cCxBYcDlcz1gGJKPZvMeofiKlWKzcV3", "T4FqPFp0ufX4qs8rIUDL8EX8RSluB0wGX51ZpL12" );
    }


    private class AddBookTask extends AsyncTask<String, Void, Boolean>{


        @Override
        protected void onPreExecute(){
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(String... params){
            ParseQuery<AddressBook> nameQuery = ParseQuery.getQuery(AddressBook.class);
            nameQuery.whereExists(params[0]);

            //Attempt to query for books that id's that match the name the user is trying to use
            try{
                nameQuery.find();
                return false;
            }catch(ParseException e){
                Log.e(DroidBook.getInstance().TAG, e.getMessage());
                e.printStackTrace();
                return true;
            }
        }

        @Override
        protected void onPostExecute(Boolean result){
            mProgressBar.setVisibility(View.GONE);
        }
    }

    private class QueryBooksTask extends AsyncTask<Void, Void, Integer> {

        @Override
        protected void onPreExecute(){
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Integer doInBackground(Void... params){
            ParseQuery<AddressBook> bookQuery = ParseQuery.getQuery(AddressBook.class);
            bookQuery.whereEqualTo("userID", DroidBook.getInstance().getUser().getObjectId());

            //Attempt to query for books that id's that match the current user's id
            try{
                return bookQuery.count();
            }catch(ParseException e){
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Integer result){
            if(result == 0) mEmptyView.setVisibility(View.VISIBLE);
            else{
                ParseQuery<AddressBook> bookQuery = ParseQuery.getQuery(AddressBook.class);
                bookQuery.whereEqualTo("userID", DroidBook.getInstance().getUser().getObjectId());
                bookQuery.findInBackground(new FindCallback<AddressBook>(){

                    public void done(List<AddressBook> books, ParseException e) {
                        if (e == null) {
                            displayBooks(books);
                        } else {
                            e.printStackTrace();
                        }
                    }
                });
                mEmptyView.setVisibility(View.GONE);
            }
         //   else displayBooks(result);
            mProgressBar.setVisibility(View.GONE);
        }
    }

    /**
     * Using the List of AddressBooks from the argument, we map an Address Book
     * to an index, and create a List of Strings for the name of the address books.
     * We then populate the list view with the names of the addressbooks.
     *
     * @param books - list of existing address book
     */
    public void displayBooks(List<AddressBook> books){
        String[] bookNames = new String[books.size()];
        int index = 0;
        for(AddressBook book: books){
            bookNames[index] = book.getBookName();
            mBookMap.put(index, book);
            ++index;
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                R.layout.book_item_view,
                bookNames
        );

        mBooksView.setAdapter(adapter);
    }

    public void addBooksViewListener(){
        mBooksView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                createToast("Works");
                return false;
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
        LayoutInflater inflater = LayoutInflater.from(this);
        View addBookView        = inflater.inflate(R.layout.add_book, null);

        //Start the build for the AlertDialog and set the custom view, along with click listeners
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(addBookView);

        final EditText bookNameEdit = (EditText) addBookView.findViewById(R.id.newBookEdit);

        builder.setCancelable(false)
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface i, int id) {
                                i.cancel();
                            }
                        })
                .setPositiveButton("Add",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface i, int id) {
                                createBookAndDisplay(bookNameEdit.getText().toString());
                            }
                        })
                .setTitle("Create New Book");

        //Build and create the dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Check if a user has already created a book with the same name, if not
     * we create a new AddressBook object, set the userID to the current user's
     * and update the display of address books.
     */
    public void createBookAndDisplay(String name){
        AddressBook newBook = new AddressBook();
        newBook.setBookName(name);
        newBook.setUserID(DroidBook.getInstance().getUser().getObjectId());
        newBook.saveInBackground(); //Important to call this, in order to save data
        new QueryBooksTask().execute();
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
