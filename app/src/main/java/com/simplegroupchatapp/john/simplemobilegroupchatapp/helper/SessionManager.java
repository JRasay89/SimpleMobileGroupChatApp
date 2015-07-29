package com.simplegroupchatapp.john.simplemobilegroupchatapp.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Created by John on 4/18/2015.
 */
public class SessionManager {

    private static final String TAG = SessionManager.class.getSimpleName();

    SharedPreferences pref;

    SharedPreferences.Editor editor;
    Context _context;
    int MODE_PRIVATE = 0;

    private static final String MY_PREF_FILENAME = "MyPrefFile";
    private static final String KEY_IS_LOGGEDIN = "is_logged_in";

    public SessionManager(Context context) {
        this._context = context;
        pref = this._context.getSharedPreferences(MY_PREF_FILENAME, MODE_PRIVATE);
        editor = pref.edit();
    }

    public void setLogin(boolean isLoggedIn) {
        editor.putBoolean(KEY_IS_LOGGEDIN, isLoggedIn);
        editor.commit();

        Log.d(TAG, "User logged in successfully");
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGEDIN, false);
    }

}
