package com.addressbook.thorrism.addressbook;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.parse.Parse;
import com.parse.ParseUser;


public class SplashScreen extends Activity {
    private boolean started;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);
        started = false;

        Parse.initialize(this, "kpVXSqTA4cCxBYcDlcz1gGJKPZvMeofiKlWKzcV3", "T4FqPFp0ufX4qs8rIUDL8EX8RSluB0wGX51ZpL12");
        new LoadTask().execute();
    }


    /**
     * Implement a splash screen using AsyncTask and Thread.sleep(). Loads
     * the home screen after 2.5 seconds have passed.
     */
    private class LoadTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params){
            try{
                started = true;
                Thread.sleep(2500); //Load database here instead of sleeping
            }catch(Exception e){
                Log.e(DroidBook.getInstance().TAG, "Failure to sleep!");
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result){

            //Start the home screen activity
            if(started) {
                //Check if a user is already logged in, if so go straight to
                //to the address book screen.
                ParseUser user = ParseUser.getCurrentUser();
                if(user == null)
                    startActivity(new Intent(getApplicationContext(), StartScreen.class));
                else {
                    startActivity(new Intent(getApplicationContext(), BookSelectionScreen.class));
                    DroidBook.getInstance().setUser(user);
                }
            }
        }
    }

    /**
     * Deal with the Android lifecycle
     */
    @Override
    public void onBackPressed(){
        started = false;
        DroidBook.getInstance().close();
    }

    @Override
    public void onStart(){
        super.onStart();
        DroidBook.getInstance().splashScreenActivity = this;
    }

    @Override
    public void onStop(){
        super.onStop();
        started = false;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        started = false;
        DroidBook.getInstance().close();
    }

}
