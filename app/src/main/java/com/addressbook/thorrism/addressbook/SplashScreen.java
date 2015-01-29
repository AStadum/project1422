package com.addressbook.thorrism.addressbook;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.parse.Parse;
import com.parse.ParseUser;


public class SplashScreen extends Activity {
    private boolean started;
    private boolean ended;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);
        started = false;
        ended   = false;

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
                ended   = true;
            }catch(Exception e){
                Log.e(DroidBook.getInstance().TAG, "Failure to sleep!");
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result){
            SharedPreferences prefs = getApplicationContext().getSharedPreferences("USER", MODE_PRIVATE);

            //Start the home screen activity
            if(started) {

                //Check if a user is already logged in, if so go straight to book screen
                ParseUser user = ParseUser.getCurrentUser();
                if(user == null && prefs.getString("USER_ID", "").equals(""))
                    startActivity(new Intent(getApplicationContext(), StartScreen.class));
                else {
                    startActivity(new Intent(getApplicationContext(), BookSelectionScreen.class));
                    if(prefs.getString("USER_ID", "").equals(""))
                        prefs.edit().putString("USER-ID", user.getObjectId()).apply();
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
        if(!ended)
            DroidBook.getInstance().close();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        started = false;
        DroidBook.getInstance().splashScreenActivity = null;
        DroidBook.getInstance().close();
    }

}
