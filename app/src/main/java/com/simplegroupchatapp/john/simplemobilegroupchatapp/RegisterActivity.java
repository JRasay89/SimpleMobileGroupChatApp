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
public class RegisterActivity extends Activity {

    //Log tag
    private static final String TAG = RegisterActivity.class.getSimpleName();

    //The Buttons
    private Button myRegisterButton;
    private Button myLoginLinkButton;
    //The input box
    private EditText myUsernameText;
    private EditText myPasswordText;

    //Helper classes
    private SQLiteHandler myHandler;
    private SessionManager session;

    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Initialize the helper classes
        myHandler = new SQLiteHandler(this);
        session = new SessionManager(this);

        //Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        init();
    }

    /**
     * Initialize the widgets
     */
    private void init() {

        //Set the reference of the button widget
        myRegisterButton = (Button) findViewById(R.id.register_myRegisterButton);
        myLoginLinkButton = (Button) findViewById(R.id.register_myLoginLinkButton);

        //Set the reference of the text widget
        myUsernameText = (EditText) findViewById(R.id.register_myUsernameText);
        myPasswordText = (EditText) findViewById(R.id.register_myPasswordText);


        myRegisterButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                String username = myUsernameText.getText().toString();
                String password = myPasswordText.getText().toString();

                /*
                // Check for empty data in the form
                if (username.trim().length() > 0 && password.trim().length() > 0) {
                    // login the user
                    //registerUser(username, password);
                    Toast.makeText(RegisterActivity.this, "Good Job!", Toast.LENGTH_LONG).show();
                } else {
                    // Prompt user to enter credentials
                    Toast.makeText(RegisterActivity.this, "Please enter the credentials!", Toast.LENGTH_LONG).show();
                }
                */
                //Check for empty data in the input box

                //If both username and password are empty
                if (username.trim().length() <= 0 && password.trim().length() <= 0) {
                    Toast.makeText(RegisterActivity.this, "Please enter your username and password!", Toast.LENGTH_LONG).show();
                }
                //If username is empty
                else if (username.trim().length() <= 0 && password.trim().length() > 0) {
                    Toast.makeText(RegisterActivity.this, "Please enter your username!", Toast.LENGTH_LONG).show();
                }
                //If password is empty
                else if (username.trim().length() > 0 && password.trim().length() <= 0) {
                    Toast.makeText(RegisterActivity.this, "Please enter your password!", Toast.LENGTH_LONG).show();
                }
                //All input box is filled out
                else {
                    //Toast.makeText(RegisterActivity.this, "Good Job!", Toast.LENGTH_LONG).show();
                    registerUser(username, password);
                }

            } //End of onClick method
        }); //End of myRegisterButton onClickListener

        //Link to login screen
        myLoginLinkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        }); //End of myLoginLinkButton onClickListener

    } //End of init method

    public void registerUser(final String username, final String password) {
        String tag_string_req = "req_register";

        //Show dialog
        pDialog.setMessage("Registering...");
        showDialog();

        StringRequest strRequest = new StringRequest(Request.Method.POST, AppConfig.URL_REGISTER,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "Register Response: " + response.toString());
                        hideDialog();
                        try {
                            JSONObject jObj = new JSONObject(response);
                            boolean error = jObj.getBoolean("error");

                            if(!error) {

                                session.setLogin(true);

                                //Get the username and password
                                JSONObject user = jObj.getJSONObject("user");
                                String username = user.getString("username");
                                String password = user.getString("password");

                                //Add the user in the local database
                                myHandler.addUsers(username, password);

                                //Open the main activity
                                Intent intent = new Intent(RegisterActivity.this, ChatRoomActivity.class);
                                startActivity(intent);
                                finish();
                            }
                            else {
                                String errorMsg = jObj.getString("error_msg");
                                Toast.makeText(RegisterActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } //End of onResponse
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(RegisterActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                        hideDialog();
                    } //End of onErrorResponse
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String,String> params = new HashMap<String,String>();
                params.put("tag", "register");
                params.put("username", username);
                params.put("password", password);
                return params;
            } //End of getParams
        };

        AppController.getInstance().addToRequestQueue(strRequest, tag_string_req);
    } //End of registerUser method


    /**
     * Show the progress dialog
     */
    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    /**
     * Hide the progress dialog
     */
    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}
