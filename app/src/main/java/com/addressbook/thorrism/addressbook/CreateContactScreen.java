package com.addressbook.thorrism.addressbook;

import android.app.ActionBar;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.List;


public class CreateContactScreen extends Activity {

    private ScrollView mScrollView;
    private ProgressBar mProgressBar;
    private EditText mFirstNameEdit;
    private EditText mLastNameEdit;
    private EditText mAddressEdit;
    private EditText mZipcodeEdit;
    private EditText mCityEdit;
    private EditText mStateEdit;
    private EditText mEmailEdit;
    private EditText mNumberEdit;
    private Button   mAddBtn;
    private Button   mCancelBtn;
    private EditText mCurrentEdit;
    private AddressBook mBook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_contact_screen);

        //Set the logo for the action bar, replaces the icon + label
        ActionBar actionBar = getActionBar();
        actionBar.setIcon(R.drawable.ic_logo);

        //Acquire XML objects
        mFirstNameEdit = (EditText) findViewById(R.id.firstNameEdit);
        mLastNameEdit  = (EditText) findViewById(R.id.lastNameEdit);
        mAddressEdit   = (EditText) findViewById(R.id.addressNameEdit);
        mCityEdit      = (EditText) findViewById(R.id.cityNameEdit);
        mStateEdit     = (EditText) findViewById(R.id.stateNameEdit);
        mZipcodeEdit   = (EditText) findViewById(R.id.zipcodeEdit);
        mEmailEdit     = (EditText) findViewById(R.id.emailEdit);
        mNumberEdit    = (EditText) findViewById(R.id.numberEdit);
        mScrollView    = (ScrollView) findViewById(R.id.newContactScroll);
        mAddBtn        = (Button) findViewById(R.id.addContactBtn);
        mCancelBtn     = (Button) findViewById(R.id.cancelContactBtn);
        mProgressBar   = (ProgressBar) findViewById(R.id.addContactsSpinner);

        //Add listeners to the EditText fields
        addFocusListener(mFirstNameEdit);
        addFocusListener(mLastNameEdit);
        addFocusListener(mAddressEdit);
        addFocusListener(mCityEdit);
        addFocusListener(mStateEdit);
        addFocusListener(mZipcodeEdit);
        addFocusListener(mEmailEdit);
        addFocusListener(mNumberEdit);

       //Add listeners to the buttons
        addButtonListeners();

       //Retrieve from the extras passed from last activity the book we are operating on
       fetchBook(getIntent().getExtras().getString("BookId"));
    }

    public void addFocusListener(EditText v){
        v.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                //Check if the Version is JELLY BEAN, if so use deprecated setBackground method.
                if(android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN){
                    if(hasFocus){
                        mCurrentEdit = (EditText) v;
                        v.setBackgroundDrawable(getResources().getDrawable(R.drawable.edit_text_form_selected));
                        mScrollView.scrollTo((int)v.getX(), (int)v.getY());
                    }
                    else v.setBackgroundDrawable(getResources().getDrawable(R.drawable.edit_text_form));
                }
                else{
                    if(hasFocus){
                        mCurrentEdit = (EditText) v;
                        v.setBackground(getResources().getDrawable(R.drawable.edit_text_form_selected));
                        mScrollView.scrollTo(0, (int) v.getY());
                    }
                    else v.setBackground(getResources().getDrawable(R.drawable.edit_text_form));
                }
            }
        });
    }

    /**
     * Add the click listeners for the buttons to cancel creating new contact, and adding
     * a new contact. Both buttons return you to the previous screen.
     *
     * TODO do input checking for the data fields (first name, lastname, zipcode, etc..)
     */
    public void addButtonListeners(){

        //Do input checking, and return the user to the previous screen with updated contacts.
        mAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO DO INPUT CHECKING HERE.
                Contact contact = createContact();
                if(contact == null) Log.e(DroidBook.TAG, "Null");
                else{
                    DroidBook.getInstance().hideKeyboard(mCurrentEdit, getApplicationContext());
                    mBook.addEntry(contact);
                    new SaveTask().execute();
                }

            }
        });

        //Return the user to the previous screen (Contacts Screen)
        mCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DroidBook.getInstance().hideKeyboard(mCurrentEdit, getApplicationContext());
                onBackPressed();
            }
        });
    }

    /**
     * Capitalize the first letter for the argument string, and return the string back
     * once done.
     *
     * @param s - input string to have first letter capitalized
     * @return the output from capitalizing the first letter
     */
    public String capitalizeFirstLetter(String s){
        String tmp  = s.substring(1, s.length());
        String tmp2 = Character.toString(s.charAt(0)).toUpperCase();
        return tmp2+tmp;
    }

    public Contact createContact(){
        Contact contact = new Contact();
        contact.setFirstName(capitalizeFirstLetter(mFirstNameEdit.getText().toString()));
        contact.setLastName(capitalizeFirstLetter(mLastNameEdit.getText().toString()));
        contact.setZipcode(Integer.parseInt((mZipcodeEdit.getText().toString())));
        contact.setState(capitalizeFirstLetter(mStateEdit.getText().toString()));
        contact.setCity(capitalizeFirstLetter(mCityEdit.getText().toString()));
        contact.setAddress(mAddressEdit.getText().toString());
        contact.setEmail(mEmailEdit.getText().toString());
        contact.setNumber(mNumberEdit.getText().toString());
        contact.saveInBackground();
      //  contact.pinInBackground();
        return contact;
    }


    /**
     * Fetch the AddressBook from the database with the specific name the user has selected
     * on the previous screen. Uses a query with two where clauses, one for matching userID
     * and one to match the name.
     * @param name - the name of the AddressBook we query from the database.
     */
    public void fetchBook(String name) {
        ParseQuery<AddressBook> bookQuery = ParseQuery.getQuery(AddressBook.class);
        bookQuery.whereEqualTo("objectId", name);

        bookQuery.findInBackground(new FindCallback<AddressBook>() {

            @Override
            public void done(List<AddressBook> addressBooks, ParseException e) {
                if(e == null){
                    mBook = addressBooks.get(0);
                    Log.e(DroidBook.TAG, "Book: " + mBook.getObjectId());
                }else{
                    e.printStackTrace();
                    Log.e(DroidBook.TAG, "Error: " + e.getMessage());
                }
            }
        });
    }

    private class SaveTask extends AsyncTask<Void, Void, Void> {

        @Override
        public void onPreExecute() {
            mProgressBar.setVisibility(View.VISIBLE);
            mScrollView.setVisibility(View.GONE);
        }

        @Override
        public Void doInBackground(Void... params) {
            try {
                mBook.save();
            } catch (ParseException e) {
                e.printStackTrace();
                Log.e(DroidBook.TAG, "Error: " + e.getMessage());
            }
            return null;
        }

        @Override
        public void onPostExecute(Void result) {
            mProgressBar.setVisibility(View.GONE);
            mScrollView.setVisibility(View.VISIBLE);
            onBackPressed();
        }
    }

    /**
     * Menu options for the action bar
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_create_book_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Create a toast method. Easier than typing this each time.
     */
    public void createToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
