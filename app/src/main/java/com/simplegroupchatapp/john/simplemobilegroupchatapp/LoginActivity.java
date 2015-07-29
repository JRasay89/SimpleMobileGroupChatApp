package com.simplegroupchatapp.john.simplemobilegroupchatapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.simplegroupchatapp.john.simplemobilegroupchatapp.app.AppConfig;
import com.simplegroupchatapp.john.simplemobilegroupchatapp.app.AppController;
import com.simplegroupchatapp.john.simplemobilegroupchatapp.helper.SQLiteHandler;
import com.simplegroupchatapp.john.simplemobilegroupchatapp.helper.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by John on 4/15/2015.
 */
public class LoginActivity extends Activity {

    //Log tag
    private static final String TAG = LoginActivity.class.getSimpleName();

    //The Buttons
    private Button myLoginButton;
    private Button myRegisterLinkButton;

    //The Input Boxes
    private EditText myUsernameText;
    private EditText myPasswordText;

    //Helper classes
    private SQLiteHandler myHandler;
    private SessionManager session;

    //Progress Dialog
    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Initialize the helper classes
        myHandler = new SQLiteHandler(this);
        session = new SessionManager(this);

        if (session.isLoggedIn()) {
            //Open the main activity
            Intent intent = new Intent(LoginActivity.this, ChatRoomActivity.class);
            startActivity(intent);
            finish();
        }

        //Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        //Initialize widgets
        init();
    }

    /**
     * Initialize the widgets
     */
    private void init() {

        //Set the reference of the button widget
        myLoginButton = (Button) findViewById(R.id.login_myLoginButton);
        myRegisterLinkButton = (Button) findViewById(R.id.login_myRegisterLinkButton);
        //Set the reference of the text widget
        myUsernameText = (EditText) findViewById(R.id.login_myUsernameText);
        myPasswordText = (EditText) findViewById(R.id.login_myPasswordText);


        //Set the listener for the buttons
        myLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = myUsernameText.getText().toString();
                String password = myPasswordText.getText().toString();

                //If both username and password are empty
                if (username.trim().length() <= 0 && password.trim().length() <= 0) {
                    Toast.makeText(LoginActivity.this, "Please enter your username and password!", Toast.LENGTH_LONG).show();
                }
                //If username is empty
                else if (username.trim().length() <= 0 && password.trim().length() > 0) {
                    Toast.makeText(LoginActivity.this, "Please enter your username!", Toast.LENGTH_LONG).show();
                }
                //If password is empty
                else if (username.trim().length() > 0 && password.trim().length() <= 0) {
                    Toast.makeText(LoginActivity.this, "Please enter your password!", Toast.LENGTH_LONG).show();
                }
                //All input box is filled out
                else {
                    //Validate the username and password
                    checkLogin(username, password);
                }

            } //End of onClick method
        }); //End of myLoginButton onClickListener


        //Link to register screen
        myRegisterLinkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                finish();
            } //End of onClick method

        }); //End of myRegisterLinkButton onClickListener


    } //End of init method

    /**
     * Verify the username and password by checking the mysql db
     * @param username of the account
     * @param password of the account
     */
    private void checkLogin(final String username, final String password) {

        String tag_string_req = "req_login";

        //Set the progress message and show it
        pDialog.setMessage("Logging in...");
        showDialog();

        StringRequest strRequest = new StringRequest(Request.Method.POST, AppConfig.URL_LOGIN,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {

                        Log.d(TAG, "Response: " + response);
                        hideDialog();

                        try {

                            JSONObject jObj = new JSONObject(response);
                            boolean error = jObj.getBoolean("error");

                            if (!error) {

                                //Set the session
                                session.setLogin(true);

                                JSONObject user = jObj.getJSONObject("user");
                                String username = user.getString("username");
                                String password = user.getString("password");

                                myHandler.addUsers(username, password);

                                //Open the main activity
                                Intent intent = new Intent(LoginActivity.this, ChatRoomActivity.class);
                                startActivity(intent);
                                finish();
                            }
                            else {
                                String errorMsg = jObj.getString("error_msg");
                                Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(LoginActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                        hideDialog();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String,String> params = new HashMap<String,String>();
                params.put("tag", "login");
                params.put("username", username);
                params.put("password", password);
                return params;
            }
        };
        //Add to the request queue
        AppController.getInstance().addToRequestQueue(strRequest, tag_string_req);

    } //End of checkLogin

    /**
     * Show the progress dialog
     */
    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    } //End of showDialog

    /**
     * Hide the progress dialog
     */
    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    } //End of hideDialog
}
