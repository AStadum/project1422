package com.addressbook.thorrism.addressbook;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class BookSelectionScreen extends Activity {
    private TextView mEmptyView;
    private ListView mBooksView;
    private List<AddressBook> mBooks;
    private ProgressBar mProgressBar;
    private Vibrator mVibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.book_selection_screen);

        //Acquire XML objects and Vibrator object
        mEmptyView   = (TextView) findViewById(R.id.emptyBookList);
        mBooksView   = (ListView) findViewById(R.id.booksList);
        mProgressBar = (ProgressBar) findViewById(R.id.querySpinner);
        mVibrator    = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        //Set the action b/ar's icon to be the logo.
        ActionBar actionBar = getActionBar();
        actionBar.setIcon(R.drawable.ic_logo);

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

    /**
     * This AsyncTask performs a single network access used to query for if an AddressBook
     * already exists with the desired name the user inputs. If it does exist, an appropriate
     * Toast message is used to tell the user it already exists. Otherwise, an AddressBook is
     * created with the new name and added to the database.
     */
    private class AddBookTask extends AsyncTask<String, Void, Pair<Integer, String>>{

        @Override
        protected void onPreExecute(){
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Pair<Integer, String> doInBackground(String... params){
            ParseQuery<AddressBook> nameQuery = ParseQuery.getQuery(AddressBook.class);
            nameQuery.whereEqualTo("bookName", params[0]);
            Pair<Integer, String> pair;

            try{
                pair = new Pair<Integer, String>(nameQuery.count(), params[0]);
                return pair;
            }catch(ParseException e){
                Log.e(DroidBook.getInstance().TAG, e.getMessage());
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Pair<Integer, String> result){
            if(result.getLeft() == 0){
                AddressBook newBook = new AddressBook();
                newBook.setBookName(result.getRight());
                newBook.setUserID(DroidBook.getInstance().getUser().getObjectId());
                newBook.initEntries(new ArrayList<Contact>());
                newBook.saveInBackground(); //Important to call this, in order to save data
                mBooks.add(newBook);
                displayBooks();
            }
            mProgressBar.setVisibility(View.GONE);
        }
    }

    /**
     * This AsyncTask performs TWO network actions. One to query for the amount of AddressBook(s)
     * a user has within the database, and one to retrieve the books if they exist. If the count
     * is 0, an appropriate empty message is displayed. Otherwise, the ListView for the user's
     * AddressBooks is populated with the result from the second query (List of AddressBook(s)).
     */
    private class QueryBooksTask extends AsyncTask<Void, Void, Integer> {

        @Override
        protected void onPreExecute(){
            mProgressBar.setVisibility(View.VISIBLE);
            mEmptyView.setVisibility(View.GONE);
        }

        @Override
        protected Integer doInBackground(Void... params){
            ParseQuery<AddressBook> bookQuery = ParseQuery.getQuery(AddressBook.class);
            bookQuery.whereEqualTo("userID", DroidBook.getInstance().getUser().getObjectId());

            try{
                return bookQuery.count(); //Query for the number of AddressBook(s) that match user's ID
            }catch(ParseException e){
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Integer result){
            if(result != 0 && result != null) mEmptyView.setVisibility(View.GONE);

            //Once count has been checked, we query for the books that DO exist, and populate
            //the ListView with them.
            if(result == null) Log.e("Null", "Null");
            else {
                ParseQuery<AddressBook> bookQuery = ParseQuery.getQuery(AddressBook.class);
                bookQuery.whereEqualTo("userID", DroidBook.getInstance().getUser().getObjectId());
                bookQuery.findInBackground(new FindCallback<AddressBook>() {

                    public void done(List<AddressBook> books, ParseException e) {
                        if (e == null) {
                            mBooks = books;
                            displayBooks();
                        } else {
                            e.printStackTrace();
                        }
                    }
                });
            }
            mProgressBar.setVisibility(View.GONE);
        }
    }

    /**
     * Using a custom adapter, we inflate the ListView with the Address Books List
     * and add a header. Also checks if the Address Books List is empty, and displays
     * an appropriate message if so, or removes the message if not.
     */
    public void displayBooks(){
        BookAdapter adapter = new BookAdapter(this, R.layout.book_item_view, mBooks);
        View header = (View) getLayoutInflater().inflate(R.layout.book_item_header, null);

        //Only had the header if one doesn't already exist
        if(mBooksView.getHeaderViewsCount() == 0) mBooksView.addHeaderView(header);

        //Check if the size of books List is 0. If so, show empty view, otherwise remove empty view
        if(mBooks.size() == 0) mEmptyView.setVisibility(View.VISIBLE);
        else mEmptyView.setVisibility(View.INVISIBLE);

        //Attach the adapter to the ListView
        mBooksView.setAdapter(adapter);
    }

    /**
     * Add a listener for when the user performs a "Long Click" to the desired item within
     * the ListView, and brings up an AlertDialog to allow the user to modify a book.
     *
     * Also, a short vibrate is used to let the user know the long click was performed.
     */
    public void addBooksViewListener(){
        mBooksView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0) return; //Don't perform action on the header item
                Intent intent = new Intent(getApplicationContext(), BookScreen.class);
                AddressBook book = mBooks.get(position-1);
                intent.putExtra("BookId", book.getObjectId());
                startActivity(intent);
            }
        });

        mBooksView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
             //   View row = mBooksView.getChildAt(position);
              //  ImageView exitIcon = (ImageView) row.findViewById(R.id.exitViewIcon);
             //   exitIcon.setVisibility(View.VISIBLE);
            //    addExitListener(exitIcon, position);
                mVibrator.vibrate(100);
                modifyBook(position-1);
                return true;
            }
        });
    }


    /**
     * Uses the argument position to map the book we want to remove from our
     * List of AddressBook(s). Remove the book from the List, then remove the book
     * from the database. Update the user with a Toast when finished, and with an
     * update to the ListView display.
     *
     * @param position - the book we selected from the ListView
     */
    public void removeBook(int position){
        AddressBook book = mBooks.get(position);
        mBooks.remove(position);
        book.deleteInBackground(new DeleteCallback() {

            @Override
            public void done(ParseException e) {
                if(e==null){
                    createToast("Deleted Address Book");
                }else{
                    e.printStackTrace();
                    createToast("Failed to delete.");
                }
            }
        });

        //Update the display to show book removed
        displayBooks();
    }

    /**
     * Not currently used. Intended purpose was for if a user performs a "Long Click" on an
     * item within a list view, the remove icon is added on the far right of the item. When
     * the remove action is clicked, this listener removes the item from the ListView and
     * database, and also updates the ListView display for the user.
     *
     * @param icon - Remove item icon that is now visible
     * @param position - Position of the item we want to remove from the database / ListView
     */
    public void addExitListener(ImageView icon, final int position){
        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeBook(position-1); //Offset it to the correct position
            }
        });
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
     * Add a new Address book to the database for the current User. Creates an AlertDialog
     * window that appears in the center of the screen. Lowers opacity of background elements.
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
     * Modify an existing book the user has long clicked within the ListView. Options are to
     * delete or modify the Address Book.
     */
    public void modifyBook(final int position){
        LayoutInflater inflater = LayoutInflater.from(this);

        final View modifyBookView       = inflater.inflate(R.layout.modify_book, null);
        final TextView modifyBookText   = (TextView) modifyBookView.findViewById(R.id.modifyTextView);
        final LinearLayout modifyLayout = (LinearLayout) modifyBookView.findViewById(R.id.editBookLayout);
        final EditText modifyBookEdit   = (EditText) modifyBookView.findViewById(R.id.bookNameEdit);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(modifyBookView);

        builder.setCancelable(false)
                .setNegativeButton("Edit", null)
                .setPositiveButton("Delete", null)
                .setTitle(mBooks.get(position).getBookName());


        builder.setIcon(R.drawable.ic_launcher);

        //Build the dialog and create custom listeners for buttons
        final AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface d) {
                final Button editBtn   = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                final Button deleteBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

                //Edit button listener, modifies the view to allow for editing the name or saves new name
                editBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        //To implement button "state" we check the status of the button text
                        if(editBtn.getText().toString().equals("Edit")) { //Not in edit state yet
                            modifyBookText.setText(R.string.book_edit);
                            modifyLayout.setVisibility(View.VISIBLE);
                            editBtn.setText("Save");
                            deleteBtn.setText("Cancel");
                        }
                        else{  //In edit state, modify the name and update the display
                            mBooks.get(position).setBookName(modifyBookEdit.getText().toString());
                            displayBooks();
                            dialog.dismiss();
                        }
                    }
                });

                //Delete button listener, just removes the book selected or closes the edit options
                deleteBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        //To implement button "state" we check the status of the button text
                        if(editBtn.getText().toString().equals("Edit")) { //Not in edit state
                            removeBook(position);
                            dialog.dismiss();

                        }else{  //Within edit state, cancel edit revert to previous state
                            DroidBook.getInstance().hideKeyboard(modifyBookEdit, getApplicationContext());
                            modifyLayout.setVisibility(View.GONE);
                            modifyBookText.setText(R.string.book_modify);
                            editBtn.setText("Edit");
                            deleteBtn.setText("Delete");
                        }

                    }
                });
            }
        });
        dialog.show();
    }

    /**
     * Check if a user has already created a book with the same name, if not
     * we create a new AddressBook object, set the userID to the current user's
     * and update the display of address books.
     */
    public void createBookAndDisplay(String name){
        new AddBookTask().execute(name);
    }

    /**
     * A toast method for testing purposes. Easier than typing each time lol
     */
    public void createToast(String s){
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.book_selection_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
