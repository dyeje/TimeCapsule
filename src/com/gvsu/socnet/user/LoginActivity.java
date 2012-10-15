/** LoginActivity.java */
package com.gvsu.socnet.user;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
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
import soc.net.R;

import com.gvsu.socnet.data.Server;

/****************************************************************
 * com.gvsu.socnet.LoginActivity
 * @author Caleb Gomer
 * @version 1.0
 ***************************************************************/
public class LoginActivity extends Activity implements
    OnClickListener {
  // TODO SUMMER LIST
  // DONE put login in a scroll view
  // Fix logic behind 'following user'
  // prevent server from getting/returning garbage
  // TODO Scaling radius based on number of capsules
  // parseAnd draw will stop at N capsules
  // TODO Polish up the user interface
  // everyone can be on the look out for a designer that could
  // possibly pretty up the app
  // TODO look for a small group to beta test it in the fall
  // TODO add pictures to the game maybe add a profile picture
  // (gravatar?)

  public static final String PROFILE = "profile",
      PLAYER_ID = "player_id";
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

  /****************************************************************
   * @see android.view.View.OnClickListener#onClick(android.view.View)
   * @param v
   ***************************************************************/
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

      String id = Server.login(strUsername, strPassword);
      if (!id.equals("-1")) {
        getSharedPreferences(PROFILE, 0).edit()
            .putString(PLAYER_ID, id).commit();
        finish();
//        gotoProfile();
      } else {
        showDialog("Nope...",
            "Your username or password are incorrect", this);
      }

      break;
    case R.id.button_new_user:
      Intent i = new Intent(getApplicationContext(),
          NewUserActivity.class);
      startActivity(i);
      finish();
    }

  }

  /****************************************************************
  * @param info
  * @param title
  * void
  ***************************************************************/
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

  /****************************************************************
   * void
   ***************************************************************/
  public void gotoProfile() {

    Intent intent = new Intent(getBaseContext(),
        ProfileActivity.class);
    startActivity(intent);
    finish();
  }
}
