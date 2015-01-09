package com.addressbook.thorrism.addressbook;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;


public class StartScreen extends Activity {
    private Button mLoginBtn;
    private Button mSignupBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_screen);
        //Acquire buttons for signup / login, and add listeners for them.
        mSignupBtn = (Button) findViewById(R.id.signupBtn);
        mLoginBtn  = (Button) findViewById(R.id.loginBtn);
        addListeners();
    }

    public void addListeners(){
        mSignupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), SignupScreen.class));
            }
        });

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), LoginScreen.class));
            }
        });
    }

    /*Add some functionality to the android lifecycle. Deal with adding these screens to the app*/
    @Override
    public void onStart(){
        super.onStart();
        DroidBook.getInstance().startScreenActivity = this;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        DroidBook.getInstance().startScreenActivity = null;
    }

    /*Prevent the user from returning to the splash screen (it is done)*/
    @Override
    public void onBackPressed(){
        return;
    }


}
