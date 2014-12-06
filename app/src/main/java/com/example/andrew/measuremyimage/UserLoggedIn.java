package com.example.andrew.measuremyimage;

import android.content.Context;
import android.util.Log;

import com.example.andrew.measuremyimage.DataBase.UserSchema;

/**
 * Created by Andrew on 12/4/2014.
 */
public class UserLoggedIn {

    private UserSchema user;

    // Log cat tag
    private static final String LOG = "UserLoggedIn";

    //Singleton
    private static UserLoggedIn mInstance = null;

    public static UserLoggedIn getInstance() {
        if (mInstance == null) {
            mInstance = new UserLoggedIn();
        }
        return mInstance;
    }
    // constructor
    private UserLoggedIn() {
    }


    public UserSchema getUser() {
        return user;
    }

    public void setUser(UserSchema user) {
        Log.e(LOG, "Entering: setUser");
        this.user = user;
    }
}
