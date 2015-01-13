package com.addressbook.thorrism.addressbook;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Toast;


public class CreateContactScreen extends Activity {

    private ScrollView mScrollView;
    private EditText mFirstNameEdit;
    private EditText mLastNameEdit;
    private EditText mAddressEdit;
    private EditText mZipcodeEdit;
    private EditText mCityEdit;
    private EditText mStateEdit;
    private Button   mAddBtn;
    private Button   mCancelBtn;

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
        mScrollView    = (ScrollView) findViewById(R.id.newContactScroll);
        mAddBtn        = (Button) findViewById(R.id.addContactBtn);
        mCancelBtn     = (Button) findViewById(R.id.cancelContactBtn);

        //Add listeners to the EditText fields
        addFocusListener(mFirstNameEdit);
        addFocusListener(mLastNameEdit);
        addFocusListener(mAddressEdit);
        addFocusListener(mCityEdit);
        addFocusListener(mStateEdit);
        addFocusListener(mZipcodeEdit);

       //Add listeners to the buttons
        addButtonListeners();
    }

    public void addFocusListener(EditText v){
        v.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                //Check if the Version is JELLY BEAN, if so use deprecated setBackground method.
                if(android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN){
                    if(hasFocus){
                        v.setBackgroundDrawable(getResources().getDrawable(R.drawable.edit_text_form_selected));
                        mScrollView.scrollTo((int)v.getX(), (int)v.getY());
                    }
                    else v.setBackgroundDrawable(getResources().getDrawable(R.drawable.edit_text_form));
                }
                else{
                    if(hasFocus){
                        v.setBackground(getResources().getDrawable(R.drawable.edit_text_form_selected));
                        mScrollView.scrollTo(0, (int) v.getY()-20);
                    }
                    else v.setBackground(getResources().getDrawable(R.drawable.edit_text_form));
                }
            }
        });
    }

    public void addButtonListeners(){
        mAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createToast("Add");
            }
        });

        mCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createToast("Cancel");
            }
        });
    }

    /**
     * Menu options for the action bar
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_create_book_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
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
