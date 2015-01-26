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

import java.util.ArrayList;
import java.util.List;

public class BookSelectionScreen extends Activity {
    private TextView mEmptyView;
    private ListView mBooksView;
    private View mActiveBook;
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
        DroidBook.setFontRoboto(mEmptyView, this);

        //Set the action b/ar's icon to be the logo.
        ActionBar actionBar = getActionBar();
        actionBar.setIcon(R.drawable.ic_logo);

        //Add some listeners
        addBooksViewListener();

        //Initialize Parse and the ListView fpr the Address Books
        initParse();
        mBooks = new ArrayList<AddressBook>();
        displayBooks();
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
            Pair<Integer, String> pair;
            int count = 0;
            for(AddressBook book : mBooks){
                if(book.getBookName().equals(params[0])) count += 1;
            }

            pair = new Pair<Integer, String>(count, params[0]);
            return pair;
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
            else
                createToast("An Address Book with that name already exists!");
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

        //Only had the header if one doesn't already exist
        if(mBooksView.getHeaderViewsCount() == 0){
            View header = (View) getLayoutInflater().inflate(R.layout.book_item_header, null);
            mBooksView.addHeaderView(header);
            TextView headerTitle = (TextView) header.findViewById(R.id.bookItemHeader);
            DroidBook.setFontRoboto(headerTitle, this);
        }

        //Check if the size of books List is 0. If so, show empty view, otherwise remove empty view
        if(mBooks.size() == 0) mEmptyView.setVisibility(View.VISIBLE);
        else mEmptyView.setVisibility(View.INVISIBLE);

        //Attach the adapter to the ListView
        mBooksView.setAdapter(adapter);
    }

    /**
     * Clear the icons from the active contact a user has Long clicked. If the active book is
     * null, the function just returns.
     */
    public void clearActiveBook(){
        //Check if null, otherwise hide the ImageViews from the active contact selected
        if(mActiveBook == null) return;
        ImageView exitIcon = (ImageView) mActiveBook.findViewById(R.id.exitViewIcon);
        ImageView editIcon = (ImageView) mActiveBook.findViewById(R.id.editViewIcon);
        exitIcon.setVisibility(View.GONE);
        editIcon.setVisibility(View.GONE);
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
                clearActiveBook();
                mActiveBook = mBooksView.getChildAt(position);
                if(position == 0) return; //Don't perform action on the header item
                Intent intent = new Intent(getApplicationContext(), ContactsScreen.class);
                AddressBook book = mBooks.get(position-1);
                intent.putExtra("BookId", book.getObjectId());
                startActivity(intent);
            }
        });

        mBooksView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                clearActiveBook();
                mActiveBook = mBooksView.getChildAt(position);
                ImageView exitIcon = (ImageView) mActiveBook.findViewById(R.id.exitViewIcon);
                ImageView editIcon = (ImageView) mActiveBook.findViewById(R.id.editViewIcon);
                exitIcon.setVisibility(View.VISIBLE);
                editIcon.setVisibility(View.VISIBLE);

                addExitListener(exitIcon, position);
                addEditListener(editIcon, position);
                mVibrator.vibrate(100);
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
     * Purpose is for if a user performs a "Long Click" on an item within a list view, the remove
     * icon is added on the far right of the item. When the remove action is clicked, an AlertDialog
     * is used to confirm if the user truly wishes to remove the book or not.
     *
     * @param icon - Remove item icon that is now visible
     * @param position - Position of the item we want to remove from the database / ListView
     */
    public void addExitListener(ImageView icon, final int position){
        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeBookDialog(position-1);
            }
        });
    }

    /**
     * Purpose is for if a user performs a "Long Click" on an item within a list view, the edit
     * icon is added on the far right of the item. When the edit action is clicked, an AlertDialog
     * is used to allow the user to modify the Address Book's name
     *
     * @param icon - Edit item icon that is now visible
     * @param position - Position of the item we want to edit
     */
    public void addEditListener(ImageView icon, final int position){
        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                modifyBook(position-1);
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
     * AlertDialog box is created when the user clicks the exit icon. The purpose is
     * intended for asking the user if they are absolutely sure they want to remove the
     * book from the database or not.
     *
     * TODO really let the user know all contacts are deleted
     * TODO also, delete the contact objects associated with this address book when gone
     *
     * @param position
     */
    public void removeBookDialog(final int position) {
        LayoutInflater inflater = LayoutInflater.from(this);

        final View modifyBookView = inflater.inflate(R.layout.remove_book, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(modifyBookView);

        builder.setCancelable(false)
                .setNegativeButton("No", null)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface i, int id) {
                        removeBook(position);
                    }
                })
                .setTitle("Remove Address Book");

        //Set the icon for the dialog window to the app's icon
        builder.setIcon(R.drawable.ic_launcher);

        //Build the dialog and create custom listeners for buttons
        final AlertDialog dialog = builder.create();
        dialog.show();
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
                                String name = bookNameEdit.getText().toString();
                                if(name.length() == 0) createToast("Please enter a valid name.");
                                else createBookAndDisplay(name);
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
        final EditText modifyBookEdit   = (EditText) modifyBookView.findViewById(R.id.bookNameEdit);
        modifyBookEdit.setText(mBooks.get(position).getBookName());

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(modifyBookView);

        builder.setCancelable(false)
                .setNegativeButton("Save", null)
                .setPositiveButton("Cancel", null)
                .setTitle("Edit Book Name");

        //Set the icon for the dialog window to the app's icon
        builder.setIcon(R.drawable.ic_launcher);

        //Build the dialog and create custom listeners for buttons
        final AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface d) {
                final Button editBtn   = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                final Button cancelBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

                //Edit button listener, modifies the view to allow for editing the name or saves new name
                editBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String name = modifyBookEdit.getText().toString();
                        if(name.length() == 0) createToast("Please enter a valid name.");
                        else {
                            mBooks.get(position).setBookName(name);
                            mBooks.get(position).saveInBackground();
                            displayBooks();
                            dialog.dismiss();
                        }
                    }
                });

                //Delete button listener, just removes the book selected or closes the edit options
                cancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DroidBook.hideKeyboard(modifyBookEdit, getApplicationContext());
                        dialog.dismiss();
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
