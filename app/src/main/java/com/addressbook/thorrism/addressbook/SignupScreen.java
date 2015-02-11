package com.addressbook.thorrism.addressbook;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SignupScreen extends Activity {
    private TextView mUserView;
    private TextView mEmailView;
    private TextView mNumberView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_screen);
        Parse.initialize(this, "###", "###");

        // Set up the login form.
        mEmailView = (TextView) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.signinBtn);
        mEmailSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mNumberView = (EditText) findViewById(R.id.number);
        mUserView = (EditText) findViewById(R.id.username);
        mProgressView = findViewById(R.id.login_progress);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String name     = mUserView.getText().toString();
        String email    = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        String number   = mNumberView.getText().toString();
        boolean cancel  = false;
        View focusView  = null;

        //Check for valid username
        if (TextUtils.isEmpty(name)) {
            mUserView.setError(getString(R.string.error_field_required));
            focusView = mUserView;
            cancel    = true;
        } else if (!TextUtils.isEmpty(name) && !isNameValid(name)) {
            mUserView.setError(getString(R.string.error_invalid_username));
            focusView = mUserView;
            cancel    = true;
        }

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password)) {
            if(isPasswordValid(password) ==0) {
                mPasswordView.setError(getString(R.string.error_invalid_password1));
                focusView = mPasswordView;
                cancel = true;
            }
            if(isPasswordValid(password)==-1){
                mPasswordView.setError(getString(R.string.error_invalid_password2));
                focusView = mPasswordView;
                cancel = true;
            }
        } else if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel    = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel    = true;
        } else if (!TextUtils.isEmpty(email) && !isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel    = true;
        }

        //Check if the number is a valid one.
        if (!TextUtils.isEmpty(number) && !isNumberValid(number)) {
            mNumberView.setError(getString(R.string.error_invalid_number));
            focusView = mNumberView;
            cancel    = true;
        } else if (TextUtils.isEmpty(number)) {
            mNumberView.setError(getString(R.string.error_field_required));
            focusView = mNumberView;
            cancel    = true;
        }

        /*Check for success, or failure to continue*/
        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            final ParseUser user = new ParseUser();
            user.setUsername(name);
            user.setPassword(password);
            user.setEmail(email);
            user.put("phone", number);

            //Attempt to see if the user name exists
            user.signUpInBackground(new SignUpCallback() {
                @Override
                public void done(ParseException e) {
                    View focusView  = null;

                    //Signup was a success
                    if (e == null) {
                        showProgress(true, user);
                    }

                    //Something was wrong with the information submit
                    else {
                        String message = "";
                        //The email already exists
                        if (e.getMessage().contains("email")) {
                            message = "Email '" + mEmailView.getText().toString() + "' already exists!";
                            focusView = mEmailView;
                            mEmailView.setError(message);
                        }

                        //Username already exists
                        if (e.getMessage().contains("username")) {
                            message = "Username '" + mUserView.getText().toString() + "' already taken!";
                            focusView = mUserView;
                            mUserView.setError(message);
                        }
                        focusView.requestFocus();
                    }
                }
            });

        }
    }

    /*Validation checking on the user inputs*/
    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private int isPasswordValid(String password) {
        if (password.length() < 6) {
            return 0;
        }
        Pattern p = Pattern.compile("[^a-zA-Z0-9 ]");
        Matcher m = p.matcher(password);
        boolean b = m.find();

        if (!b) {
            return -1;
        }
        return 1;
    }

    private boolean isNumberValid(String number) {
        return number.length() >= 10;
    }

    private boolean isNameValid(String name) {
        return (name.length() >= 4);
    }

    /**
     * Shows the progress UI and hides the login form. Also starts the
     */
    public void showProgress(final boolean show, ParseUser user) {
        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("USER", MODE_PRIVATE);
        prefs.edit().putString("USER-ID", user.getObjectId()).apply();
        startActivity(new Intent(this, BookSelectionScreen.class));
    }

    /*Add some functionality to the android lifecycle. Deal with adding these screens to the app*/
    @Override
    public void onStart(){
        super.onStart();
        DroidBook.getInstance().signupScreenActivity = this;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        DroidBook.getInstance().signupScreenActivity = null;
    }
}
