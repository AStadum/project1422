package com.addressbook.thorrism.addressbook;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.parse.ParseUser;


public class ForgotPasswordScreen extends Activity {
    private Button mRequestBtn;
    private EditText mEmailEdit;
    private ProgressBar mSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgot_password_screen);

        mRequestBtn = (Button) findViewById(R.id.requestBtn);
        mEmailEdit  = (EditText) findViewById(R.id.accountEmailEdit);
        mSpinner    = (ProgressBar) findViewById(R.id.requestProgressBar);

        //add the listener for the password reset request button
        addRequestListener();
    }

    /**
     * Send a request to the input email if the email exists in the Parse database.
     * If not, an appropriate message is displayed.
     */
    public void addRequestListener(){
        mRequestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DroidBook.hideKeyboard(mEmailEdit, getApplicationContext());
                new RequestResetTask().execute();
            }
        });
    }

    private class RequestResetTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute(){
            mRequestBtn.setVisibility(View.GONE);
            mSpinner.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(Void... params){

            try{
                ParseUser.requestPasswordReset(mEmailEdit.getText().toString());
            }catch(com.parse.ParseException e){
                if(e != null){
                    e.printStackTrace();
                    return false;
                }
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result){
            mRequestBtn.setVisibility(View.VISIBLE);
            mSpinner.setVisibility(View.GONE);

            if(!result){
                Toast.makeText(getApplicationContext(), "Email not found! Please try again.", Toast.LENGTH_LONG).show();
                mEmailEdit.setError("Invalid email");
            }
            else{
                Toast.makeText(getApplicationContext(), "Reset password request sent.", Toast.LENGTH_LONG).show();
                startActivity(new Intent(getApplicationContext(), LoginScreen.class));
            }

        }
    }

    /**
     * Deal with Android lifecycle
     */
    public void onStart(){
        super.onStart();
        DroidBook.getInstance().resetPasswordActivity = this;
    }

    public void onDestroy(){
        super.onDestroy();
        DroidBook.getInstance().resetPasswordActivity = null;
    }
}

