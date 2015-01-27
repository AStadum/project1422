package com.addressbook.thorrism.addressbook;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.ArrayList;
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
    private Activity mActivity;
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
        mActivity      = this;

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
;

        v.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                //Check if the Version is JELLY BEAN, if so use deprecated setBackground method.
                if(android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN){
                    if(hasFocus){
                        mCurrentEdit = (EditText) v;
                        mScrollView.scrollTo(0, (int) v.getY() - 200);
                        v.setBackgroundDrawable(getResources().getDrawable(R.drawable.edit_text_form_selected));
                    }
                    else v.setBackgroundDrawable(getResources().getDrawable(R.drawable.edit_text_form));
                }
                else{
                    if(hasFocus){
                        mCurrentEdit = (EditText) v;
                        mScrollView.scrollTo(0, (int) v.getY() - 200);
                        v.setBackground(getResources().getDrawable(R.drawable.edit_text_form_selected));
                    }
                    else
                        v.setBackground(getResources().getDrawable(R.drawable.edit_text_form));
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
                mActivity.finish();
            }
        });
    }

    /**
     * Capitalize the first letter for the argument string, and return the string back
     * once done. Also, it does this for every first letter within the string.
     *
     * @param s - input string to have first letter capitalized
     * @return the output from capitalizing the first letter
     */
    public String capitalizeFirstLetter(String s){
        List<String> words = new ArrayList<String>();
        String current = "";
        for(char c : s.toCharArray()){
            if(c == ' '){
                words.add(current);
                current = "";
            }
            else current += c;
        }
        words.add(current);

        String result = "";
        if(words.size() > 1) { //If # of words found exceed more than one
            for (String word : words) {
                String tmp = word.substring(1, word.length());
                String tmp2 = Character.toString(word.charAt(0)).toUpperCase();
                result += tmp2 + tmp + ' ';
            }
        }else {                //Otherwise, just return the string with modified first letter
            String tmp = s.substring(1, s.length());
            String tmp2 = Character.toString(s.charAt(0)).toUpperCase();
            result += tmp2 + tmp + ' ';
        }
        return result.substring(0, result.length()-1);
    }

    public Contact createContact(){
        Contact contact = new Contact();
        if(mFirstNameEdit.getText().toString().length() != 0)
            contact.setFirstName(capitalizeFirstLetter(mFirstNameEdit.getText().toString()));
        else {
            createToast("Please enter a first name!");
            return null;
        }

        if(mLastNameEdit.getText().toString().length() != 0)
            contact.setLastName(capitalizeFirstLetter(mLastNameEdit.getText().toString()));
        else
            contact.setLastName("");

        String zip = mZipcodeEdit.getText().toString();

        //Do input checking on the zip code. Make sure the user submits a valid one
        if(!checkZipInput(zip)){
            createToast("Please enter a valid zipcode!");
            return null;
        }
        else
            contact.setZipcode(zip);

        //Only set the values if there is input
        if(mStateEdit.getText().toString().length() != 0)
            contact.setState(capitalizeFirstLetter(mStateEdit.getText().toString()));
        else
            contact.setState("");
        if(mCityEdit.getText().toString().length() != 0)
            contact.setCity(capitalizeFirstLetter(mCityEdit.getText().toString()));
        else
            contact.setCity("");
        if(mAddressEdit.getText().toString().length() != 0)
            contact.setAddress(capitalizeFirstLetter(mAddressEdit.getText().toString()));
        else
            contact.setAddress("");
        if(mEmailEdit.getText().toString().length() != 0)
            contact.setEmail(mEmailEdit.getText().toString());
        else
            contact.setEmail("");
        if(mNumberEdit.getText().toString().length() != 0)
            contact.setNumber(mNumberEdit.getText().toString());
        else
            contact.setNumber("");
        contact.saveEventually();
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
        ParseQuery<AddressBook> bookQuery = ParseQuery.getQuery(AddressBook.class).fromLocalDatastore();
        bookQuery.whereEqualTo("objectId", name);

        bookQuery.findInBackground(new FindCallback<AddressBook>() {

            @Override
            public void done(List<AddressBook> addressBooks, ParseException e) {
                if(e == null){
                    mBook = addressBooks.get(0);
                    ParseObject.pinAllInBackground(addressBooks);
                }
                else{
                    Log.e(DroidBook.TAG, "Error: " + e.getMessage());
                    e.printStackTrace();
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
            mBook.saveEventually();
            return null;
        }

        @Override
        public void onPostExecute(Void result) {
            mProgressBar.setVisibility(View.GONE);
            mScrollView.setVisibility(View.VISIBLE);
            SharedPreferences prefs = getApplicationContext().getSharedPreferences("ADD_STATE", MODE_PRIVATE);
            prefs.edit().putString("STATE", "NEW").apply();
            onBackPressed();
        }
    }

    public boolean checkZipInput(String zip){
        boolean result = false;

        //If the input is supposed to a 5 integer input for zipcode
        if(zip.length() == 5) {
            for (char c : zip.toCharArray()) {
                if ((int) c >= 48 && (int) c <= 57) result = true;
                else return false;
            }
        }

        //If the input is supposed to a 5 integer - 4 integer for zipcode
        if(zip.length() == 10){
            if (zip.charAt(5) != '-') return false;
            else{
                for (char c : zip.toCharArray()){
                    if ((int) c >= 48 && (int) c <= 57) result = true;
                    else {
                        if (c != '-') return false;
                    }
                }
            }
        }
        return result;
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

    /**
     * Deal with the Android Lifecycle
     */
    @Override
    public void onStart(){
        super.onStart();
        DroidBook.getInstance().contactCreateActivity = this;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        DroidBook.getInstance().contactCreateActivity = null;
    }

    @Override
    public void onBackPressed(){
        //Clear the focus from the current edit text
        super.onBackPressed();
    }

}
