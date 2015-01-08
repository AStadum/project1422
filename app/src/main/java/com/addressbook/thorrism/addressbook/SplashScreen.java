package com.addressbook.thorrism.addressbook;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


public class SplashScreen extends Activity {
    private boolean started;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);
        started = false;

        //Run the splash screen, after 2.5 seconds loads home screen
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
                Thread.sleep(2500);
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
                startActivity(new Intent(getApplicationContext(), HomeScreen.class));
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
