package com.addressbook.thorrism.addressbook;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.parse.ParseUser;

/**
 * Created by Lucas Crawford on 1/3/2015.
 */
public class DroidBook {
    public static String TAG = "DroidBook";
    public static Activity splashScreenActivity;
    public static Activity contactScreenActivity;
    public static Activity loginScreenActivity;
    public static Activity signupScreenActivity;
    public static Activity startScreenActivity;
    public static Activity bookSelectionActivity;
    public static DroidBook instance;
    public static String username;
    public static ParseUser user;

    /**
     * If there isn't an instance already created for current app running,
     * create one and return it. Otherwise, returns static value already
     * created for the application.
     *
     * @return Instance of the DroidBook being used.
     */
    public static DroidBook getInstance(){
        if(instance == null)
            instance = new DroidBook();
        return instance;
    }

    /**
     * Closes all the non-null (active) activities running within the
     * current instance of DroidBook.
     *
     * @return returns nothing.
     */
    public static void close(){
        if(splashScreenActivity != null) splashScreenActivity.finish();
        if(contactScreenActivity != null) contactScreenActivity.finish();
        if(loginScreenActivity != null) loginScreenActivity.finish();
        if(startScreenActivity != null) startScreenActivity.finish();
        if(bookSelectionActivity != null) bookSelectionActivity.finish();
        if(signupScreenActivity != null) signupScreenActivity.finish();
    }

    /**
    * Get and set the current ParseUser for the application.
    */
    public static ParseUser getUser() {
        return user;
    }

    public static void setUser(ParseUser user) {
        DroidBook.user = user;
    }

    /**
     * Check the network's state on the current device.
     */
    public static boolean checkNetworkStatus(Context ctx){
        ConnectivityManager manager = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        if(info.isRoaming()) Log.e(TAG, "Roaming");
        if(info.isFailover()) Log.e(TAG, "Failover");
        if(info.isAvailable()) Log.e(TAG, "Available");
        if(info.isConnected()) Log.e(TAG, "Connected");
        return info != null && info.isConnected();
    }

    /**
    * Close the keyboard if we don't want it to show anymore
    */
    public static void hideKeyboard(EditText editText, Context ctx){
        InputMethodManager mgr = (InputMethodManager) ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    /**
     * Set a TextView's font to roboto font from assets folder
     */
    public static void setFontRoboto(TextView view, Context ctx){
        Typeface tf = Typeface.createFromAsset(ctx.getAssets(), "roboto.ttf");
        view.setTypeface(tf);
    }
}
