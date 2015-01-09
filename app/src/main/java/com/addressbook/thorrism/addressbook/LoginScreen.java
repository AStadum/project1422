package com.addressbook.thorrism.addressbook;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;


public class LoginScreen extends Activity {
    private EditText mUserView;
    private EditText mPasswordView;
    private TextView mErrorUsernameView;
    private TextView mErrorPasswordView;
    private Button   mLoginBtn;
    private ProgressBar mProgressBar;
    private boolean  displayBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_login_screen);

        mUserView          = (EditText) findViewById(R.id.login_username);
        mPasswordView      = (EditText) findViewById(R.id.login_password);
        mErrorUsernameView = (TextView) findViewById(R.id.error_username);
        mErrorPasswordView = (TextView) findViewById(R.id.error_password);
        mLoginBtn          = (Button) findViewById(R.id.login_button);
        mProgressBar       = (ProgressBar) findViewById(R.id.loginSpinner);
        displayBtn         = false;

        //Add listeners for the password input and login button
        addListeners();

        //Initialize the parse database
        Parse.initialize(this, "kpVXSqTA4cCxBYcDlcz1gGJKPZvMeofiKlWKzcV3", "T4FqPFp0ufX4qs8rIUDL8EX8RSluB0wGX51ZpL12");
    }

   /*Reset inputs if they have been altered by in either the username or password EditText*/
   public void resetInputs(){
       mUserView.setTypeface(null, Typeface.NORMAL);
       mUserView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
       mErrorUsernameView.setText("");
       mPasswordView.setTypeface(null, Typeface.NORMAL);
       mPasswordView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
       mErrorPasswordView.setText("");
   }

   /*Add various listeners to the UI for the user to interact with*/
   public void addListeners(){
       mUserView.addTextChangedListener(new TextWatcher(){

           public void afterTextChanged(Editable s) {
           }

           public void beforeTextChanged(CharSequence s, int start,
                                         int count, int after) {
           }

           public void onTextChanged(CharSequence s, int start, int before, int count) {

               //Check if the length is 0, if so we can show login button if password length is as well
               if(s.length() > 0) {
                   displayBtn = true;
                   resetInputs();
               }
               else{
                   displayBtn = false;
                   mUserView.setTypeface(null, Typeface.ITALIC);
               }

               //Remove the login button if nothing is entered
               if(!displayBtn && mPasswordView.getText().length() == 0)
                   mLoginBtn.setVisibility(View.INVISIBLE);

               else if(displayBtn && mPasswordView.getText().length() > 0)
                   mLoginBtn.setVisibility(View.VISIBLE);
           }
       });

       mPasswordView.addTextChangedListener(new TextWatcher() {

           public void afterTextChanged(Editable s) {
           }

           public void beforeTextChanged(CharSequence s, int start,
                                         int count, int after) {
           }

           //If username input is longer than 0, and so is password input, show the login button
           public void onTextChanged(CharSequence s, int start, int before, int count) {

               //If the length of the username input and password input > 0, show login button
               if (s.length() > 0 && displayBtn) {
                   mLoginBtn.setVisibility(View.VISIBLE);
               }

               //Remove the login button if nothing is entered
               else if (!displayBtn && s.length() == 0)
                   mLoginBtn.setVisibility(View.INVISIBLE);

               //Check if the view should have italic or not
               if (s.length() == 0) {
                   mPasswordView.setTypeface(null, Typeface.ITALIC);
               }
               else
                   resetInputs();

           }
       });

       //Listener for clicking the done button the soft input keyboard.
       mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
           public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
               if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                   new CheckUsername().execute(mUserView.getText().toString());
                   hideKeyboard(mPasswordView);
                   return true;
               }
               return false;
           }
       });

       mLoginBtn.setOnClickListener(new View.OnClickListener() {

           @Override
           public void onClick(View v) {
               new CheckUsername().execute(mUserView.getText().toString());
               hideKeyboard(mPasswordView);
           }
       });
    }

    /*Hide the keyboard based on the edit text the triggered this*/
    public void hideKeyboard(EditText editTex){
        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(editTex.getWindowToken(), 0);
    }


    /*Async thread to check if a username entered by the user exists in the Parse user database*/
    public class CheckUsername extends AsyncTask<String, Void, Boolean>{

        @Override
        protected void onPreExecute(){
            mLoginBtn.setText("");
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(String... params){
            ParseQuery<ParseUser> query = ParseUser.getQuery();
            query.whereEqualTo("username", params[0]);
            List<ParseUser> result = null;
            try{
                result = query.find();
            }catch(ParseException e){
                if(e == null){
                    //do something here I guess
                }
            }

            if(result.size() > 0)
                return true;
            else
                return false;
        }

        @Override
        protected void onPostExecute(Boolean result){
            //Found username entered by the user, attempt login with password now.
            if(result) {
                new LoginUser().execute();
            }

            //Username not found, let the user know the username was wrong.
            else {
                mErrorUsernameView.setText(getString(R.string.error_username_message));
                final Drawable xImage = getApplicationContext().getResources().getDrawable(R.drawable.ximage);
                mUserView.setCompoundDrawablesWithIntrinsicBounds(null, null, xImage, null);

                //Add listener to remove on touch the red x and message drawn for incorrect input
                mUserView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if(event.getAction() == MotionEvent.ACTION_DOWN){
                            if(event.getRawX() >= (mUserView.getRight() - 100)){
                                mUserView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                                mUserView.setText("");
                                mUserView.setHint(getString(R.string.prompt_username));
                                mUserView.setTypeface(null, Typeface.ITALIC);
                                mErrorUsernameView.setText("");
                                mUserView.requestFocus();
                                mUserView.setOnTouchListener(null);
                                return true;
                            }
                        }
                        return false;
                    }
                });
                mLoginBtn.setText(getString(R.string.login_user));
                mLoginBtn.setVisibility(View.INVISIBLE);
                mProgressBar.setVisibility(View.INVISIBLE);
            }

        }
    }

    /*Async thread to attempt a user login using Parse. This happens upon user entering a valid username*/
    public class LoginUser extends AsyncTask<Void, Void, Boolean>{

        @Override
        protected void onPreExecute(){

        }

        @Override
        protected Boolean doInBackground(Void... params){
            Boolean result = true;
            try{
                ParseUser.logIn(mUserView.getText().toString(), mPasswordView.getText().toString());
            }catch(ParseException e){

                //If an exception is raised, information the user provided was wrong (namely password)
                if(e != null) {
                    Log.e("FAILURE", e.getMessage());
                    result = false;
                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(Boolean result){

            //Login was successful, start the app
            if(result){
                DroidBook.username = mUserView.getText().toString();
                DroidBook.getInstance().setUser(ParseUser.getCurrentUser());
                startActivity(new Intent(getBaseContext(), BookSelectionScreen.class));
            }

            //Password was wrong, we already checked the username so tell the user so.
            else{
                //Display wrong password, and tell user to try again.
                mErrorPasswordView.setText(getString(R.string.error_password_message));
                final Drawable xImage = getApplicationContext().getResources().getDrawable(R.drawable.ximage);
                mPasswordView.setCompoundDrawablesWithIntrinsicBounds(null, null, xImage, null);

                //Add listener to remove on touch the red x and message drawn for incorrect input
                mPasswordView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if(event.getAction() == MotionEvent.ACTION_DOWN){
                            if(event.getRawX() >= (mPasswordView.getRight() - 100)){
                                mPasswordView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                                mPasswordView.setText("");
                                mPasswordView.setHint(getString(R.string.prompt_password));
                                mPasswordView.setTypeface(null, Typeface.ITALIC);
                                mErrorPasswordView.setText("");
                                mPasswordView.requestFocus();
                                mPasswordView.setOnTouchListener(null);
                                return true;
                            }
                        }
                        return false;
                    }
                });
                mLoginBtn.setText(getString(R.string.login_user));
                mLoginBtn.setVisibility(View.INVISIBLE);
                mProgressBar.setVisibility(View.INVISIBLE);
            }
        }
    }

    /*Add some functionality to the android lifecycle. Deal with adding these screens to the app*/
    @Override
    public void onStart(){
        super.onStart();
        DroidBook.getInstance().loginScreenActivity = this;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        DroidBook.getInstance().loginScreenActivity = null;
    }


}
