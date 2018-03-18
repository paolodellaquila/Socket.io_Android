package com.example.francescopaolodellaquila.socketchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.provider.SyncStateContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;


public class LoginActivity extends AppCompatActivity {

    private EditText mUsernameView;

    private EditText mPasswordView;

    private String mUsername;

    private Socket mSocket;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //initialize socket
        try {
            mSocket = IO.socket("https://socket-io-chat.now.sh/");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        //initialize progress dialog
        progressDialog = new ProgressDialog(this);

        // Setting Title
        progressDialog.setTitle("SocketChat");

        // Setting Message
        progressDialog.setMessage("Caricamento...");

        // Set up the login form.
        mUsernameView = (EditText) findViewById(R.id.username_input);
        mPasswordView = (EditText) findViewById(R.id.username_password);

        mUsernameView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    if(mPasswordView.getText().toString().equals("Fermi123")){
                        progressDialog.show();
                        
                        attemptLogin();
                        return true;
                    }else{
                        Toast.makeText(LoginActivity.this, "Password Errata", Toast.LENGTH_SHORT).show();
                    }
                }
                return false;
            }
        });

        Button signInButton = (Button) findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mPasswordView.getText().toString().equals("Fermi123")){
                    progressDialog.show();

                    attemptLogin();
                }else{
                    Toast.makeText(LoginActivity.this, "Password Errata", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mSocket.on("login", onLogin);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mSocket.off("login", onLogin);
    }

    /**
     * Attempts to sign in the account specified by the login form.
     * If there are form errors (invalid username, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {

        // Reset errors.
        mUsernameView.setError(null);

        // Store values at the time of the login attempt.
        String username = mUsernameView.getText().toString().trim();

        // Check for a valid username.
        if (TextUtils.isEmpty(username)) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            mUsernameView.setError(getString(R.string.error_field_required));
            mUsernameView.requestFocus();
            return;
        }

        mUsername = username;

        // perform the user login attempt.
        mSocket.emit("add user", username);

        progressDialog.dismiss();
    }

    private Emitter.Listener onLogin = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];

            int numUsers;
            try {
                numUsers = data.getInt("numUsers");
            } catch (JSONException e) {
                return;
            }

            Intent intent = new Intent();
            intent.putExtra("username", mUsername);
            intent.putExtra("numUsers", numUsers);
            setResult(RESULT_OK, intent);
            finish();
        }
    };
}
