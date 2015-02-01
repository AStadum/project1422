package com.addressbook.thorrism.addressbook;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;


public class ContactEditScreen extends Activity {
    private ScrollView mScrollView;
    private EditText mFirstNameEdit;
    private EditText mLastNameEdit;
    private EditText mAddressEdit;
    private EditText mZipcodeEdit;
    private EditText mCityEdit;
    private EditText mStateEdit;
    private EditText mEmailEdit;
    private EditText mNumberEdit;
    private Button mAddBtn;
    private Button   mCancelBtn;
    private EditText mCurrentEdit;
    private LinearLayout mExtrasLayout;
    private Activity mActivity;
    private List<String> mExtraList;
    private EditText mExtraEdit;
    private TextView mExtraTitle;
    private View mExtraLine;
    private TextView mExtraButtonText;
    private ImageView mExtraImage;
    private Contact mContact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_edit_screen);

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
        mExtrasLayout  = (LinearLayout) findViewById(R.id.extrasLayout);
        mExtraEdit     = (EditText) findViewById(R.id.extraDataEdit);
        mExtraTitle    = (TextView) findViewById(R.id.extraTitleView);
        mExtraLine     = (View) findViewById(R.id.extraLineView);
        mExtraImage    = (ImageView) findViewById(R.id.add_extra_icon);
        mExtraButtonText = (TextView) findViewById(R.id.add_extra_view);

        mExtraList     = new ArrayList<String>();
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
        addFocusListener(mExtraEdit);

        //Add listeners to the buttons
        addButtonListeners();
        addExtraListener();

        //Fetch the contact and fill in the data from the contact
        fetchContact(getIntent().getExtras().getString("ContactID"));
        setContactData();
    }

    public void addFocusListener(EditText v){

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
     */
    public void addButtonListeners(){

        //Do input checking, and return the user to the previous screen with updated contacts.
        mAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Contact result = saveContact();

                //If the contact is not null, save it with result contacts data
                if(result != null) {
                    mContact.setFirstName(result.getFirstName());
                    mContact.setLastName(result.getLastName());
                    mContact.setZipcode(result.getZipcode());
                    mContact.setEmail(result.getEmail());
                    mContact.setNumber(result.getNumber());
                    mContact.setAddress(result.getAddress());
                    mContact.setCity(result.getCity());
                    mContact.setState(result.getState());
                    mContact.setExtras(result.getExtras());
                    mContact.saveEventually();
                    SharedPreferences prefs = getApplicationContext().getSharedPreferences("ADD_STATE", MODE_PRIVATE);
                    prefs.edit().putString("STATE", "MODIFIED").apply();
                    prefs.edit().putString("CONTACT", mContact.getObjectId()).apply();
                    createToast("Saved contact changes.");
                    mActivity.finish();
                }
            }
        });

        //Return the user to the previous screen (Contacts Screen)
        mCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DroidBook.hideKeyboard(mCurrentEdit, getApplicationContext());
                cancelEditDialog();
            }
        });
    }

    /**
     * Add a listener to the extra fields layout to listen for a user click. Either opens
     * a dialog for a new extra field, or one to edit the previous data.
     */
    public void addExtraListener(){
        mExtrasLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                createExtraDialog();
            }
        });
    }

    /**
     * Opens a dialog box confirming if a user truly wants to cancel editing a contact
     * @param
     */
    public void cancelEditDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);

        final View cancelEditView = inflater.inflate(R.layout.contact_edit_cancel, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(cancelEditView);

        builder.setCancelable(true)
                .setNegativeButton("No", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface i, int id) {
                        i.dismiss();
                    }
                })
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface i, int id) {
                        mActivity.finish();
                    }
                })
                .setTitle("Cancel contact edit");

        //Set the icon for the dialog window to the app's icon
        builder.setIcon(R.drawable.ic_launcher);

        //Build the dialog and create custom listeners for buttons
        final AlertDialog dialog = builder.create();

        //Remove the icons for the contact edit / remove to be visible
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
            }
        });
        dialog.show();
    }

    /**
     * Opens a dialog box for the user to either edit, or create a new "extra field" for this current
     * contact.
     */
    public void createExtraDialog(){
        final LayoutInflater inflater = LayoutInflater.from(this);

        View createExtraDialog = inflater.inflate(R.layout.create_extra, null);
        final EditText extraTitle = (EditText) createExtraDialog.findViewById(R.id.extraNameEdit);
        final EditText extraData = (EditText) createExtraDialog.findViewById(R.id.extraValueEdit);

        if(mExtraList.size() != 0){   //If the list isn't empty, then edit the old "extra field" data
            extraTitle.setText(mExtraList.get(0));
            extraData.setText(mExtraEdit.getText().toString());
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(createExtraDialog);

        builder.setCancelable(true)
                .setNegativeButton("Add", null)
                .setPositiveButton("Cancel", null)
                .setTitle("Create new field");

        //Set the icon for the dialog window to the app's icon
        builder.setIcon(R.drawable.ic_launcher);

        //Build the dialog and create custom listeners for buttons
        final AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface d) {
                final Button saveBtn   = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                final Button cancelBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

                saveBtn.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        //If the list isn't empty, we are updating last "extra field" data
                        if(mExtraList.size() != 0) {
                            mExtraList.set(0, extraTitle.getText().toString());
                            mExtraList.set(1, extraData.getText().toString());
                        }else{
                            mExtraList.add(extraTitle.getText().toString());
                            mExtraList.add(extraData.getText().toString());
                        }
                        updateExtraState(true);
                        dialog.dismiss();
                    }
                });

                cancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
            }
        });
        dialog.show();
    }

    /**
     * Method to check if the user has an extra field, fill it with the data from the intent
     * extras bundle. Otherwise, we set the extra edit data section invisible and set the button
     * to say the user can create a new data field.
     * @param exists
     */
    public void updateExtraState(boolean exists){
        if(exists){ //If user has an existing extra field for this contact
            mExtraTitle.setText(capitalizeFirstLetter(mExtraList.get(0)));
            mExtraEdit.setText(capitalizeFirstLetter(mExtraList.get(1)));
            mExtraEdit.setVisibility(View.VISIBLE);
            mExtraLine.setVisibility(View.VISIBLE);
            mExtraTitle.setVisibility(View.VISIBLE);
            mExtraImage.setVisibility(View.GONE);
            mExtraButtonText.setText("Edit extra field");
        }else{   //If user has removed the extra field for this contact
            mExtraEdit.setVisibility(View.GONE);
            mExtraLine.setVisibility(View.GONE);
            mExtraTitle.setVisibility(View.GONE);
            mExtraImage.setVisibility(View.VISIBLE);
            mExtraList = new ArrayList<String>();
            mExtraButtonText.setText(this.getResources().getString(R.string.add_field));
        }
    }

    /**
     * Fetch the Contact from the database with the specific name the user has selected
     * on the previous screen.
     * @param name - the name of the AddressBook we query from the database.
     */
    public void fetchContact(String name) {
        ParseQuery<Contact> contactQuery = ParseQuery.getQuery(Contact.class).fromLocalDatastore();
        contactQuery.whereEqualTo("objectId", name);

        contactQuery.findInBackground(new FindCallback<Contact>() {

            @Override
            public void done(List<Contact> contacts, ParseException e) {
                if(e == null) {
                    ParseObject.pinAllInBackground(contacts);
                    mContact = contacts.get(0);
                }
                else{
                    Log.e(DroidBook.TAG, "Error: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Retrieve the data from the user passed through the intent extras bundle, and set the
     * EditText fields with it. If an extra field exists, add it as well.
     */
    public void setContactData(){
        mFirstNameEdit.setText(getIntent().getExtras().getString("FirstName"));
        mLastNameEdit.setText(getIntent().getExtras().getString("LastName"));
        mCityEdit.setText(getIntent().getExtras().getString("CityName"));
        mStateEdit.setText(getIntent().getExtras().getString("StateName"));
        mAddressEdit.setText(getIntent().getExtras().getString("Address"));
        mZipcodeEdit.setText(getIntent().getExtras().getString("ZipCode"));
        mNumberEdit.setText(getIntent().getExtras().getString("Number"));
        mEmailEdit.setText(getIntent().getExtras().getString("Email"));

        if(!getIntent().getExtras().getString("ExtrasTitle").equals("")) {
            mExtraList.add(getIntent().getExtras().getString("ExtrasTitle"));
            mExtraList.add(getIntent().getExtras().getString("ExtrasData"));
            updateExtraState(true);
        }
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

    /**
     * Check the EditText input fields to see if a user has input valid data. If so, save
     * the contact and return to the contact screen.
     */
    public Contact saveContact(){
        Contact contact = new Contact();

        //Add a new extra to the contact field and the object id
        contact.setExtras(mExtraList);

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
        if(!checkZipInput(zip) && !zip.equals("")){
            createToast("Please enter a valid zipcode!");
            return null;
        }
        else {
            if(zip.length() != 0)
                contact.setZipcode(zip);
            else
                contact.setZipcode("");
        }

        //Only set the values if there is input
        boolean addressExists = false;
        if(mAddressEdit.getText().toString().length() != 0) {
            addressExists = true;
            contact.setAddress(capitalizeFirstLetter(mAddressEdit.getText().toString()));
        }
        else
            contact.setAddress("");

        if(mStateEdit.getText().toString().length() != 0)
            contact.setState(capitalizeFirstLetter(mStateEdit.getText().toString()));
        else {
            contact.setState("");
            if(addressExists){
                createToast("Please enter a city and state for this address");
                return null;
            }
        }
        if(mCityEdit.getText().toString().length() != 0)
            contact.setCity(capitalizeFirstLetter(mCityEdit.getText().toString()));
        else {
            contact.setCity("");
            if(addressExists){
                createToast("Please enter a city and state for this address");
                return null;
            }
        }

        if(mEmailEdit.getText().toString().length() != 0)
            contact.setEmail(mEmailEdit.getText().toString());
        else
            contact.setEmail("");
        if(mNumberEdit.getText().toString().length() != 0)
            contact.setNumber(mNumberEdit.getText().toString());
        else
            contact.setNumber("");

        return contact;
    }

    /**
     * Ensure that the user has entered a valid zip code. Return false if they didn't, return
     * true if it is. Only valid zip codes are 5 integer zips or 5 integers a dash and 4 integers
     * after the dash.
     *
     * @param zip - string containing the zip the user has attempted to enter.
     * @return true if valid, false if not.
     */
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
     * Create a toast method. Easier than typing this each time.
     */
    public void createToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.contact_edit_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Deal with the Android Lifecycle
     */
    @Override
    public void onStart(){
        super.onStart();
        DroidBook.getInstance().contactEditActivity = this;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        DroidBook.getInstance().contactEditActivity = null;
    }
}
