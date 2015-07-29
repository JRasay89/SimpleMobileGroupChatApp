package com.simplegroupchatapp.john.simplemobilegroupchatapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.simplegroupchatapp.john.simplemobilegroupchatapp.app.AppConfig;
import com.simplegroupchatapp.john.simplemobilegroupchatapp.helper.SQLiteHandler;
import com.simplegroupchatapp.john.simplemobilegroupchatapp.helper.SessionManager;
import com.simplegroupchatapp.john.simplemobilegroupchatapp.other.Message;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by John on 4/19/2015.
 */
public class ChatRoomActivity extends Activity {
    private static final String TAG = ChatRoomActivity.class.getSimpleName();

    //Button
    private Button mySendButton;

    //Message box
    private EditText myChatText;

    private TextView myClientCountText;

    private SQLiteHandler myHandler;
    private SessionManager session;

    private HashMap<String, String> myUser;

    //Chat messages
    private MessageAdapter messageAdapter;
    private ListView myMessageListView;
    private ArrayList<Message> messageList;

    //Connection to server
    private ClientConnectThread clientConnection;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatroom);

        mySendButton = (Button) findViewById(R.id.mySendButton);

        //Initialize helper objects
        myHandler = new SQLiteHandler(this);
        session = new SessionManager(this);

        //Initialize the message box
        myChatText = (EditText) findViewById(R.id.myChatText);

        //Initialize text view
        myClientCountText = (TextView) findViewById(R.id.myClientCountText);
        myClientCountText.setText("Current people in room: 0");

        //Get the local database data
        myUser = myHandler.getUser();

        //Chat messages
        messageList = new ArrayList<Message>();
        messageAdapter = new MessageAdapter(this, messageList);
        myMessageListView = (ListView) findViewById(R.id.myMessageListView);
        myMessageListView.setAdapter(messageAdapter);

        //Set listener for the send button
        mySendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String chatMessage = myChatText.getText().toString();
                clientConnection.getClientMessage(chatMessage);
                myChatText.setText("");

            }
        });

        //Initialize the server connection runnable class, and enter the name of the client
        clientConnection = new ClientConnectThread(myUser.get("name").toString());

        //Starts the connection thread
        Thread connectionThread = new Thread(clientConnection);
        connectionThread.start();

    } //End of onCreate method


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Take appropriate action for each action item click
        switch (item.getItemId()) {
            case R.id.menu_logout:
                logout();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Logout the user from the chat session
     */
    public void logout() {

        //Set the login to false
        session.setLogin(false);

        //Delete the user
        myHandler.deleteUsers();

        //Disconnect the connection
        clientConnection.disconnect();

        Intent intent = new Intent(ChatRoomActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }


    /**
     * A class that extends Runnable and is used to connect to the server
     * and handles responses that are sent and received to and from the server
     */
    private class ClientConnectThread implements Runnable {
        //Name of the client
        private String name;
        //The message to be sent
        private String messageToSend;

        //Sockets and the data streams
        Socket socket = null;
        DataOutputStream dataOut = null;
        DataInputStream dataIn = null;

        //Boolean to check if the user is connected
        private boolean isConnected;

        //Flags to determine the type of response
        //FLAG_NEW = A new client has connected
        //FLAG_MESSAGE = A new message has been sent
        //FLAG_EXIT = A client has logout
        private final String FLAG_NEW = "new";
        private final String FLAG_MESSAGE = "message";
        private final String FLAG_EXIT = "exit";

        public ClientConnectThread(String name) {
            this.name = name;
            this.isConnected = true;
            this.messageToSend = "";
        }

        @Override
        public void run() {
            try {
                Log.d(TAG, "Socket Connection");
                socket = new Socket(AppConfig.SERVERIPADDRESS, AppConfig.SERVERSOCKETPORT);

                //Initialize the output and input stream
                dataOut = new DataOutputStream(socket.getOutputStream());
                dataIn = new DataInputStream(socket.getInputStream());

                //Initial response to send to the server
                sendMessage(name, "", FLAG_NEW);

                while (isConnected) {
                    if (dataIn.available() > 0) {
                        String receivedMessage = dataIn.readUTF();

                        parseMessage(receivedMessage);
                    }

                    //If client has a message to send
                    if (!messageToSend.equals("")) {
                        sendMessage(name, messageToSend, FLAG_MESSAGE);
                        messageToSend = "";
                    }
                }

                //Sends a response to the server that the client will logout from the room
                sendMessage(name, "", FLAG_EXIT);

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                //If the data output stream is not null, close the stream
                if (dataOut != null) {
                    try {
                        dataOut.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                //If the data input stream is not null, close the stream
                if (dataIn != null) {
                    try {
                        dataIn.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                //If socket is not null, close the socket
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } //End of finally
        } //End of run

        /**
         * Get the message typed by the client
         * @param clientMessage is the message typed by the client
         */
        private void getClientMessage(String clientMessage) {
            this.messageToSend = clientMessage;
        } //End of getClientMessage

        /**
         * Send message to the server
         * @param ClientName is the name of the client
         * @param messageToSend is the message to from the client to be sent to the server
         * @param flag is the flag to determine the type of response
         */
        private void sendMessage(String ClientName, String messageToSend, String flag) {
            try {
                JSONObject jsonObj = new JSONObject();
                jsonObj.put("name", ClientName);
                jsonObj.put("message", messageToSend);
                jsonObj.put("flag", flag);

                //Sends the message to the server
                dataOut.writeUTF(jsonObj.toString());
                dataOut.flush();

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } //End of sendMessage



        /**
         * Parse the message receive from the server
         * @param receivedMessage received from the server
         */
        private void parseMessage(String receivedMessage) {
            try {
                JSONObject jsonObj = new JSONObject(receivedMessage);
                //String name = jsonObj.getString("name");
                //String message = jsonObj.getString("message");
                String flag = jsonObj.getString("flag");

                //If a new client has joined the room
                if (flag.equals(FLAG_NEW)) {
                    final String currentClientCountText = "Current people in room: " + jsonObj.getInt("count");
                    final String toastMessage = jsonObj.getString("name") + " has joined the room. " +
                            jsonObj.getInt("count") + " people in the room.";

                    updateCurrentClientCount(currentClientCountText);
                    //Show the message using toast
                    showToastMessage(toastMessage);
                }

                //If a client has sent a message
                else if (flag.equals(FLAG_MESSAGE)) {
                    Message m = new Message(jsonObj.getString("message"), jsonObj.getString("name"));
                    m.setIsSelf(false);

                    if (jsonObj.getString("name").equals(name)) {
                        m.setIsSelf(true);
                    }

                    appendMessage(m);
                }

                //If a client has exit the room
                else if (flag.equals(FLAG_EXIT)) {
                    final String currentClientCountText = "Current people in room: " + jsonObj.getInt("count");
                    final String toastMessage = jsonObj.getString("name") + " has left the room. " +
                            jsonObj.getInt("count") + " people in the room.";

                    //Update UI
                    updateCurrentClientCount(currentClientCountText);
                    showToastMessage(toastMessage);
                }



            } catch (JSONException e) {
                e.printStackTrace();
            }
        } //End of parseMessage

        /**
         * Add the message to the message list and
         * display the messages on the screen
         * @param m is the message to be added to the list and displayed to the screen
         */
        private void appendMessage(final Message m) {
            //Update the UI
            ChatRoomActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    messageList.add(m);
                    messageAdapter.notifyDataSetChanged();
                }
            });

        } //End of appendMessage


        /**
         * Display the message given as a toast message
         * @param toastMessage is the message to be displayed
         */
        private void showToastMessage(final String toastMessage) {
            //Update the UI
            ChatRoomActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(ChatRoomActivity.this, toastMessage, Toast.LENGTH_LONG).show();
                }
            });
        } //End of showToastMessage

        private void updateCurrentClientCount(final String clientCountText) {

            //Update the UI
            ChatRoomActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    myClientCountText.setText(clientCountText);
                }
            });
        }
        /**
         * Disconnect the client's connection to the server
         */
        private void disconnect() {
            this.isConnected = false;
        } //End of disconnect

    } //End of ClientConnectThread class definition
}
