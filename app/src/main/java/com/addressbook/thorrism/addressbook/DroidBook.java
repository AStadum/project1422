package com.addressbook.thorrism.addressbook;

import android.app.Activity;

/**
 * Created by Lucas Crawford on 1/3/2015.
 */
public class DroidBook {
    public static String TAG = "DroidBook";
    public static Activity splashScreenActivity;
    public static Activity homeScreenActivity;
    public static DroidBook instance;

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
        if(homeScreenActivity != null) homeScreenActivity.finish();
    }
}
