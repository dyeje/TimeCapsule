/** LoginActivity.java */
package com.gvsu.socnet.user;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.gvsu.socnet.data.AsyncCallbackListener;
import com.gvsu.socnet.data.AsyncDownloader;
import soc.net.R;

import com.gvsu.socnet.data.Server;

import java.util.HashMap;

/**
 * *************************************************************
 * com.gvsu.socnet.LoginActivity
 *
 * @author Caleb Gomer
 * @version 1.0
 *          *************************************************************
 */
public class LoginActivity extends Activity implements
    OnClickListener, AsyncCallbackListener {

  public static final String PROFILE = "profile";
  public static final String PLAYER_ID = "player_id";
  public TextView loginResult, username, password, newUser;
  public Button loginButton = null;

  @Override
  public void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

    SharedPreferences prefs = getSharedPreferences(PROFILE, 0);

    Log.d("debug", "id: " + prefs.getString(PLAYER_ID, "-1"));
    if (!prefs.getString(PLAYER_ID, "-1").equals("-1")) {
      finish();
      gotoProfile();
    }

    setContentView(R.layout.login);
    loginButton = (Button) findViewById(R.id.button_start);
    loginButton.setOnClickListener(this);
    username = (EditText) findViewById(R.id.username);
    username.requestFocus();
    password = (EditText) findViewById(R.id.password);
    loginResult = (TextView) findViewById(R.id.loginresult);
    newUser = (TextView) findViewById(R.id.button_new_user);
    newUser.setOnClickListener(this);

    username.setHint("Username");
    password.setHint("Password");
  }

  public void onBackPressed() {
    return;
  }

  /**
   * *************************************************************
   *
   * @param v *************************************************************
   * @see android.view.View.OnClickListener#onClick(android.view.View)
   */
  @Override
  public void onClick(View v) {
    switch (v.getId()) {
      case R.id.button_start:

        String strUsername = username.getText().toString();
        String strPassword = password.getText().toString();

        if (strUsername.equals("") || strPassword.equals("")) {
          showDialog("Missing Information",
              "Please enter a username and password", this);
          return;
        }


        HashMap<String, String> requestParams = new HashMap<String, String>();
        requestParams.put(AsyncDownloader.USERNAME, strUsername);
        requestParams.put(AsyncDownloader.PASSWORD, strPassword);

        AsyncDownloader.Payload request = new AsyncDownloader.Payload(AsyncDownloader.LOGIN, this, requestParams);

        new AsyncDownloader().execute(request);


//        new AsyncDownloader().execute(
//            new AsyncDownloader.Payload(
//                AsyncDownloader.LOGIN, new Object[]{
//                LoginActivity.this, new Object[]{
//                strUsername,
//                strPassword
//            }
//            }
//            )
//        );

        break;
      case R.id.button_new_user:
        Intent i = new Intent(getApplicationContext(),
            NewEditUserActivity.class);
        startActivity(i);
        finish();
    }

  }

  /**
   * *************************************************************
   *
   * @param info
   * @param title void
   *              *************************************************************
   */
  public void showDialog(String title, String info, Context context) {

    AlertDialog.Builder builder;
    AlertDialog alertDialog;

    LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
    View layout = inflater.inflate(R.layout.text_dialog,
        (ViewGroup) findViewById(R.id.layout_root));

    TextView text = (TextView) layout
        .findViewById(R.id.text_level);
    text.setText(info);

    builder = new AlertDialog.Builder(context);
    builder.setView(layout);
    alertDialog = builder.create();
    alertDialog.setTitle(title);
    alertDialog.show();
  }

  /**
   * *************************************************************
   * void
   * *************************************************************
   */
  public void gotoProfile() {

    Intent intent = new Intent(getBaseContext(),
        ProfileActivity.class);
    startActivity(intent);
    finish();
  }

  public void asyncDone(AsyncDownloader.Payload payload) {
    if (payload.exception == null) {
            switch (payload.taskType) {
              case AsyncDownloader.LOGIN:
                if (payload.result.equals("-1")) {
                  showDialog("Sorry...",
                      "Your username or password is incorrect", this);
                }
                else {
                  getSharedPreferences(PROFILE, 0).edit()
                      .putString(PLAYER_ID, payload.result).commit();
                  finish();
                }
                break;
            }
    }
    else {
      new AlertDialog.Builder(this)
          .setTitle("Internet Error ["+payload.taskType+"](" + payload.exception.getMessage() + "){ID-10-T}")
          .setMessage("Sorry, we couldn't find the internet. Please try that again...")
          .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
          })
          .show();
    }
  }
}
